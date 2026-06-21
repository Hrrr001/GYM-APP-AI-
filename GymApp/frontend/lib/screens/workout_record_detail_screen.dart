import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';

class WorkoutRecordDetailScreen extends StatefulWidget {
  final int recordId;

  const WorkoutRecordDetailScreen({super.key, required this.recordId});

  @override
  _WorkoutRecordDetailScreenState createState() => _WorkoutRecordDetailScreenState();
}

class _WorkoutRecordDetailScreenState extends State<WorkoutRecordDetailScreen> {
  dynamic _record;
  List<dynamic> _details = [];
  String _aiFeedback = '';
  bool _isLoading = true;
  bool _isLoadingFeedback = false;
  String _errorMessage = '';

  @override
  void initState() {
    super.initState();
    _loadRecordDetail();
  }

  Future<void> _loadRecordDetail() async {
    try {
      // 加载训练记录基本信息
      final recordResponse = await http.get(
        Uri.parse('http://localhost:8080/api/workouts/${widget.recordId}'),
      );

      if (recordResponse.statusCode == 200) {
        setState(() {
          _record = jsonDecode(recordResponse.body);
        });

        // 加载训练记录详情
        final detailsResponse = await http.get(
          Uri.parse('http://localhost:8080/api/workouts/${widget.recordId}/details'),
        );

        if (detailsResponse.statusCode == 200) {
          setState(() {
            _details = jsonDecode(detailsResponse.body);
            _isLoading = false;
          });
        } else {
          setState(() {
            _errorMessage = '加载训练记录详情失败';
            _isLoading = false;
          });
        }
      } else {
        setState(() {
          _errorMessage = '加载训练记录失败';
          _isLoading = false;
        });
      }
    } catch (e) {
      setState(() {
        _errorMessage = '网络错误，请稍后重试';
        _isLoading = false;
      });
    }
  }

  Future<void> _loadAIFeedback() async {
    setState(() {
      _isLoadingFeedback = true;
    });

    try {
      final response = await http.get(
        Uri.parse('http://localhost:8080/api/workouts/${widget.recordId}/ai-feedback'),
      );

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        setState(() {
          _aiFeedback = data['feedback'];
          _isLoadingFeedback = false;
        });
      } else {
        setState(() {
          _isLoadingFeedback = false;
        });
      }
    } catch (e) {
      setState(() {
        _isLoadingFeedback = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('训练详情'),
      ),
      body: Container(
        width: double.infinity,
        child: _isLoading
            ? const Center(child: CircularProgressIndicator())
            : _errorMessage.isNotEmpty
                ? Center(child: Text(_errorMessage))
                : _record == null
                    ? const Center(child: Text('训练记录不存在'))
                    : SingleChildScrollView(
                        padding: const EdgeInsets.all(16.0),
                        child: Container(
                          width: double.infinity,
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              // 训练基本信息
                              Container(
                                width: double.infinity,
                                padding: const EdgeInsets.all(16.0),
                                decoration: BoxDecoration(
                                  border: Border.all(color: Colors.grey),
                                  borderRadius: BorderRadius.circular(8),
                                ),
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Text(
                                      '训练日期: ${_record['date']}',
                                      style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                                    ),
                                    const SizedBox(height: 8),
                                    Text('训练时长: ${_record['duration']}分钟'),
                                    if (_record['feeling'] != null && _record['feeling'].isNotEmpty)
                                      Text('训练感受: ${_record['feeling']}'),
                                    if (_record['notes'] != null && _record['notes'].isNotEmpty)
                                      Text('备注: ${_record['notes']}'),
                                  ],
                                ),
                              ),
                              const SizedBox(height: 24),

                              // 训练内容
                              const Text(
                                '训练内容',
                                style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                              ),
                              const SizedBox(height: 16),
                              if (_details.isEmpty)
                                const Text('暂无训练内容')
                              else
                                Column(
                                  children: _details.map((detail) {
                                    return Card(
                                      margin: const EdgeInsets.symmetric(vertical: 8),
                                      child: Padding(
                                        padding: const EdgeInsets.all(16.0),
                                        child: Column(
                                          crossAxisAlignment: CrossAxisAlignment.start,
                                          children: [
                                            Text(
                                              detail['exercise']['name'],
                                              style: const TextStyle(fontWeight: FontWeight.bold),
                                            ),
                                            const SizedBox(height: 8),
                                            Text('${detail['sets']}组 × ${detail['reps']}次'),
                                            if (detail['weight'] != null)
                                              Text('重量: ${detail['weight']}kg'),
                                            Text('完成状态: ${detail['completed'] ? '已完成' : '未完成'}'),
                                          ],
                                        ),
                                      ),
                                    );
                                  }).toList(),
                                ),
                              const SizedBox(height: 24),

                              // AI反馈
                              const Text(
                                'AI训练反馈',
                                style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                              ),
                              const SizedBox(height: 16),
                              _isLoadingFeedback
                                  ? const CircularProgressIndicator()
                                  : _aiFeedback.isEmpty
                                      ? ElevatedButton(
                                          onPressed: _loadAIFeedback,
                                          child: const Text('获取AI反馈'),
                                        )
                                      : Card(
                                          child: Padding(
                                            padding: const EdgeInsets.all(16.0),
                                            child: Text(_aiFeedback),
                                          ),
                                        ),
                            ],
                          ),
                        ),
                      ),
      ),
    );
  }
}
