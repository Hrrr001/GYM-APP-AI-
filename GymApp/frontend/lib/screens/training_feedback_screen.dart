import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';

class TrainingFeedbackScreen extends StatefulWidget {
  final Map<String, dynamic>? currentPlan;

  const TrainingFeedbackScreen({super.key, this.currentPlan});

  @override
  _TrainingFeedbackScreenState createState() => _TrainingFeedbackScreenState();
}

class _TrainingFeedbackScreenState extends State<TrainingFeedbackScreen> {
  final _feelingCtl = TextEditingController();
  final _issuesCtl = TextEditingController();
  String _completionStatus = 'completed';
  bool _isLoading = false;
  Map<String, dynamic>? _result;
  Map<String, dynamic>? _adjustedPlan;

  Future<void> _submitFeedback() async {
    if (_feelingCtl.text.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('请填写训练感受')),
      );
      return;
    }

    setState(() { _isLoading = true; _result = null; _adjustedPlan = null; });

    try {
      final now = DateTime.now();
      final date = '${now.year}-${now.month.toString().padLeft(2, '0')}-${now.day.toString().padLeft(2, '0')}';

      final resp = await http.post(
        Uri.parse('http://localhost:8080/api/ai/feedback/submit'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({
          'user_id': 1,
          'date': date,
          'feeling': _feelingCtl.text,
          'issues': _issuesCtl.text.isNotEmpty ? _issuesCtl.text : null,
          'completion_status': _completionStatus,
        }),
      );

      if (resp.statusCode == 200) {
        final data = jsonDecode(resp.body);
        setState(() {
          _result = data;
          _isLoading = false;
        });

        final parsed = data['parsed'];
        if (parsed != null && (parsed['pain_areas'] as List?)?.isNotEmpty == true) {
          final painMsg = '检测到不适部位：${(parsed['pain_areas'] as List).join(', ')}。建议调整训练计划。';
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text(painMsg),
              action: SnackBarAction(label: '调整计划', onPressed: _adjustPlan),
            ),
          );
        }
      }
    } catch (e) {
      setState(() { _isLoading = false; });
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('提交失败: $e')),
      );
    }
  }

  Future<void> _adjustPlan() async {
    if (widget.currentPlan == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('无可用计划进行调整')),
      );
      return;
    }

    setState(() { _isLoading = true; });

    final feedbackText = '${_feelingCtl.text} ${_issuesCtl.text}';

    try {
      final resp = await http.post(
        Uri.parse('http://localhost:8080/api/ai/feedback/adjust-plan'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({
          'current_plan': widget.currentPlan,
          'feedback_text': feedbackText,
          'user_profile': {'fitness_level': 'beginner', 'training_years': 1},
          'date': DateTime.now().toIso8601String(),
        }),
      );

      if (resp.statusCode == 200) {
        final data = jsonDecode(resp.body);
        setState(() {
          _adjustedPlan = data;
          _isLoading = false;
        });
      }
    } catch (e) {
      setState(() { _isLoading = false; });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('训练反馈'), backgroundColor: Colors.black),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text('本次训练感受', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
            const SizedBox(height: 8),
            TextField(
              controller: _feelingCtl,
              maxLines: 3,
              decoration: const InputDecoration(
                hintText: '描述你的训练感受：强度是否合适？有没有不适？动作是否标准？',
                border: OutlineInputBorder(),
              ),
            ),

            const SizedBox(height: 16),
            const Text('遇到的问题 / 不适', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
            const SizedBox(height: 8),
            TextField(
              controller: _issuesCtl,
              maxLines: 2,
              decoration: const InputDecoration(
                hintText: '如：膝盖痛、腰部不适、某动作做不了...',
                border: OutlineInputBorder(),
              ),
            ),

            const SizedBox(height: 16),
            const Text('完成情况', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
            const SizedBox(height: 8),
            Row(
              children: ['completed', 'partial', 'missed'].map((v) {
                final labels = {'completed': '全部完成', 'partial': '部分完成', 'missed': '未完成'};
                final active = _completionStatus == v;
                return Padding(
                  padding: const EdgeInsets.only(right: 8),
                  child: ChoiceChip(
                    label: Text(labels[v]!),
                    selected: active,
                    selectedColor: Colors.black,
                    labelStyle: TextStyle(color: active ? Colors.white : Colors.black),
                    side: BorderSide(color: Colors.black, width: active ? 0 : 1),
                    onSelected: (_) => setState(() => _completionStatus = v),
                  ),
                );
              }).toList(),
            ),

            const SizedBox(height: 24),
            SizedBox(
              width: double.infinity,
              height: 48,
              child: ElevatedButton(
                onPressed: _isLoading ? null : _submitFeedback,
                child: _isLoading
                    ? const SizedBox(width: 20, height: 20, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
                    : const Text('提交反馈', style: TextStyle(fontSize: 16)),
              ),
            ),

            if (_result != null) ...[
              const SizedBox(height: 16),
              Container(
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: Colors.green[50],
                  borderRadius: BorderRadius.circular(8),
                  border: Border.all(color: Colors.green[200]!),
                ),
                child: const Row(
                  children: [
                    Icon(Icons.check_circle, color: Colors.green),
                    SizedBox(width: 8),
                    Text('反馈已收到！AI将根据你的反馈自动调整后续计划。'),
                  ],
                ),
              ),
              if (_result!['follow_up_suggestion'] != null)
                Padding(
                  padding: const EdgeInsets.only(top: 8),
                  child: Text(_result!['follow_up_suggestion']!, style: const TextStyle(fontSize: 13, color: Colors.grey)),
                ),
            ],

            if (_adjustedPlan != null) ...[
              const SizedBox(height: 24),
              const Text('AI 调整结果', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
              const SizedBox(height: 8),
              _buildAdjustments(),
            ],
          ],
        ),
      ),
    );
  }

  Widget _buildAdjustments() {
    final ruleAdj = (_adjustedPlan!['rule_based_adjustments'] as List<dynamic>?) ?? [];
    final aiAdj = _adjustedPlan!['ai_adjustment'];

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        if (ruleAdj.isNotEmpty) ...[
          const Text('规则调整:', style: TextStyle(fontWeight: FontWeight.bold)),
          ...ruleAdj.map((a) {
            final m = a as Map<String, dynamic>;
            return Card(
              child: ListTile(
                leading: Icon(
                  m['type'] == '动作替换' ? Icons.swap_horiz : Icons.tune,
                  color: Colors.orange,
                ),
                title: Text('${m['type']}: ${m['original']} -> ${m['adjusted']}'),
                subtitle: Text(m['reason'] ?? ''),
                dense: true,
              ),
            );
          }),
        ],
        if (aiAdj != null) ...[
          const SizedBox(height: 8),
          Text('AI建议: ${aiAdj['adjustment_summary'] ?? ''}',
              style: const TextStyle(fontSize: 14)),
        ],
      ],
    );
  }

  @override
  void dispose() {
    _feelingCtl.dispose();
    _issuesCtl.dispose();
    super.dispose();
  }
}
