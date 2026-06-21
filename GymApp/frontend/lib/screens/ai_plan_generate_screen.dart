import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';

class AIPlanGenerateScreen extends StatefulWidget {
  const AIPlanGenerateScreen({super.key});

  @override
  _AIPlanGenerateScreenState createState() => _AIPlanGenerateScreenState();
}

class _AIPlanGenerateScreenState extends State<AIPlanGenerateScreen> {
  // User profile
  final _genderCtl = TextEditingController(text: '男');
  final _ageCtl = TextEditingController(text: '25');
  final _heightCtl = TextEditingController(text: '175');
  final _weightCtl = TextEditingController(text: '70');
  final _bodyFatCtl = TextEditingController();
  final _trainingYearsCtl = TextEditingController(text: '1');
  final _injuriesCtl = TextEditingController();
  final _equipmentCtl = TextEditingController(text: '健身房');
  final _requirementsCtl = TextEditingController();

  String _fitnessLevel = 'beginner';
  String _goal = '增肌';
  int _duration = 4;
  int _daysPerWeek = 3;
  int _sessionMinutes = 60;

  Map<String, dynamic>? _generatedPlan;
  bool _isLoading = false;
  String _error = '';

  Future<void> _generatePlan() async {
    setState(() { _isLoading = true; _error = ''; _generatedPlan = null; });

    final body = {
      'gender': _genderCtl.text,
      'age': int.tryParse(_ageCtl.text) ?? 25,
      'height': double.tryParse(_heightCtl.text) ?? 170,
      'weight': double.tryParse(_weightCtl.text) ?? 65,
      'body_fat': double.tryParse(_bodyFatCtl.text),
      'training_years': double.tryParse(_trainingYearsCtl.text) ?? 0,
      'fitness_level': _fitnessLevel,
      'injuries': _injuriesCtl.text.isNotEmpty ? _injuriesCtl.text : null,
      'equipment': _equipmentCtl.text.isNotEmpty ? _equipmentCtl.text : null,
      'goal': _goal,
      'duration': _duration,
      'days_per_week': _daysPerWeek,
      'session_minutes': _sessionMinutes,
      'additional_requirements': _requirementsCtl.text.isNotEmpty ? _requirementsCtl.text : null,
    };

    try {
      final resp = await http.post(
        Uri.parse('http://localhost:8080/api/ai/plan/generate'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode(body),
      );

      if (resp.statusCode == 200) {
        final data = jsonDecode(resp.body);
        setState(() {
          _generatedPlan = data['plan'];
          _isLoading = false;
        });
      } else {
        final err = jsonDecode(resp.body);
        setState(() {
          _error = err['detail'] ?? err['message'] ?? '生成失败';
          _isLoading = false;
        });
      }
    } catch (e) {
      setState(() {
        _error = '网络错误: $e';
        _isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('AI 训练计划生成'), backgroundColor: Colors.black),
      body: Container(
        color: Colors.white,
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _sectionTitle('基本信息'),
              _rowFields([
                _field('性别', _genderCtl, hint: '男/女', flex: 1),
                const SizedBox(width: 12),
                _field('年龄', _ageCtl, hint: '岁', flex: 1, type: TextInputType.number),
              ]),
              const SizedBox(height: 10),
              _rowFields([
                _field('身高(cm)', _heightCtl, type: TextInputType.number, flex: 1),
                const SizedBox(width: 12),
                _field('体重(kg)', _weightCtl, type: TextInputType.number, flex: 1),
              ]),
              const SizedBox(height: 10),
              _rowFields([
                _field('体脂率(%)', _bodyFatCtl, type: TextInputType.number, flex: 1, hint: '可选'),
                const SizedBox(width: 12),
                _field('训练年限', _trainingYearsCtl, type: TextInputType.number, flex: 1, hint: '年'),
              ]),

              const SizedBox(height: 16),
              _sectionTitle('健身水平'),
              _chipSelector(['beginner', 'intermediate', 'advanced'], ['新手', '中级', '高级'], _fitnessLevel,
                  (v) => setState(() => _fitnessLevel = v)),
              const SizedBox(height: 16),
              _sectionTitle('训练目标'),
              _chipSelector(
                  ['增肌', '减脂', '塑形', '力量提升', '耐力增强'],
                  ['增肌', '减脂', '塑形', '力量', '耐力'],
                  _goal,
                  (v) => setState(() => _goal = v)),

              const SizedBox(height: 16),
              _sectionTitle('计划参数'),
              _rowFields([
                _intStepper('周期(周)', _duration, (v) => setState(() => _duration = v)),
                const SizedBox(width: 12),
                _intStepper('天/周', _daysPerWeek, (v) => setState(() => _daysPerWeek = v)),
                const SizedBox(width: 12),
                _intStepper('分钟/次', _sessionMinutes, (v) => setState(() => _sessionMinutes = v)),
              ]),

              const SizedBox(height: 16),
              _field('伤病/不适', _injuriesCtl, hint: '如：膝盖积液、腰部不适等', maxLines: 2),
              const SizedBox(height: 10),
              _field('可用器械', _equipmentCtl, hint: '健身房 / 居家哑铃 / 自重'),
              const SizedBox(height: 10),
              _field('额外要求', _requirementsCtl, hint: '如：偏好自由重量、需要居家方案', maxLines: 2),

              if (_error.isNotEmpty)
                Padding(
                  padding: const EdgeInsets.only(top: 12),
                  child: Text(_error, style: const TextStyle(color: Colors.red)),
                ),

              const SizedBox(height: 20),
              SizedBox(
                width: double.infinity,
                height: 48,
                child: ElevatedButton(
                  onPressed: _isLoading ? null : _generatePlan,
                  child: _isLoading
                      ? const SizedBox(width: 20, height: 20, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
                      : const Text('生成训练计划', style: TextStyle(fontSize: 16)),
                ),
              ),

              if (_generatedPlan != null) ...[
                const SizedBox(height: 24),
                _buildPlanView(),
              ],
            ],
          ),
        ),
      ),
    );
  }

  Widget _sectionTitle(String title) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Text(title, style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
    );
  }

  Widget _field(String label, TextEditingController ctl,
      {String? hint, int maxLines = 1, TextInputType type = TextInputType.text, int flex = 1}) {
    return Expanded(
      flex: flex,
      child: TextField(
        controller: ctl,
        keyboardType: type,
        maxLines: maxLines,
        decoration: InputDecoration(
          labelText: label,
          hintText: hint,
          border: const OutlineInputBorder(),
          contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
        ),
      ),
    );
  }

  Widget _rowFields(List<Widget> children) => Row(children: children);

  Widget _chipSelector(List<String> values, List<String> labels, String selected, Function(String) onSelect) {
    return Wrap(
      spacing: 8,
      runSpacing: 4,
      children: List.generate(values.length, (i) {
        final active = values[i] == selected;
        return ChoiceChip(
          label: Text(labels[i]),
          selected: active,
          selectedColor: Colors.black,
          labelStyle: TextStyle(color: active ? Colors.white : Colors.black),
          side: BorderSide(color: Colors.black, width: active ? 0 : 1),
          onSelected: (_) => onSelect(values[i]),
        );
      }),
    );
  }

  Widget _intStepper(String label, int value, Function(int) onChange) {
    return Expanded(
      child: Column(
        children: [
          Text(label, style: const TextStyle(fontSize: 12, color: Colors.grey)),
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              IconButton(
                icon: const Icon(Icons.remove_circle_outline, size: 20),
                onPressed: value > 1 ? () => onChange(value - 1) : null,
              ),
              Text('$value', style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
              IconButton(
                icon: const Icon(Icons.add_circle_outline, size: 20),
                onPressed: () => onChange(value + 1),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildPlanView() {
    final plan = _generatedPlan!;
    final name = plan['plan_name'] ?? '训练计划';
    final overview = plan['plan_overview'] ?? '';
    final safety = plan['safety_notes'] as List<dynamic>? ?? [];
    final weeks = plan['weekly_plans'] as List<dynamic>? ?? [];

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(name, style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold)),
        const SizedBox(height: 8),
        Text(overview, style: const TextStyle(fontSize: 14, color: Colors.grey)),
        if (safety.isNotEmpty) ...[
          const SizedBox(height: 12),
          const Text('安全提示', style: TextStyle(fontWeight: FontWeight.bold, color: Colors.red)),
          ...safety.map((s) => Padding(
                padding: const EdgeInsets.only(left: 8, top: 4),
                child: Row(crossAxisAlignment: CrossAxisAlignment.start, children: [
                  const Text('  ', style: TextStyle(color: Colors.red)),
                  Expanded(child: Text(s.toString(), style: const TextStyle(fontSize: 13))),
                ]),
              )),
        ],
        const SizedBox(height: 12),
        ...weeks.map((w) => _buildWeek(w)),
      ],
    );
  }

  Widget _buildWeek(Map<String, dynamic> week) {
    final theme = week['theme'] ?? '';
    final days = week['days'] as List<dynamic>? ?? [];
    return Card(
      margin: const EdgeInsets.symmetric(vertical: 6),
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('第${week['week']}周 $theme', style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 15)),
            const SizedBox(height: 6),
            ...days.map((d) {
              final exs = d['exercises'] as List<dynamic>? ?? [];
              return Padding(
                padding: const EdgeInsets.only(top: 4),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text('Day ${d['day']}: ${d['focus']}', style: const TextStyle(fontWeight: FontWeight.w500)),
                    ...exs.map((e) => Padding(
                          padding: const EdgeInsets.only(left: 12, top: 2),
                          child: Text('${e['name']}  ${e['sets']}组  ${e['reps']}次', style: const TextStyle(fontSize: 13)),
                        )),
                  ],
                ),
              );
            }),
          ],
        ),
      ),
    );
  }

  @override
  void dispose() {
    _genderCtl.dispose();
    _ageCtl.dispose();
    _heightCtl.dispose();
    _weightCtl.dispose();
    _bodyFatCtl.dispose();
    _trainingYearsCtl.dispose();
    _injuriesCtl.dispose();
    _equipmentCtl.dispose();
    _requirementsCtl.dispose();
    super.dispose();
  }
}
