import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';

class ExerciseDetailScreen extends StatefulWidget {
  final int exerciseId;

  const ExerciseDetailScreen({super.key, required this.exerciseId});

  @override
  _ExerciseDetailScreenState createState() => _ExerciseDetailScreenState();
}

class _ExerciseDetailScreenState extends State<ExerciseDetailScreen> {
  dynamic _exercise;
  bool _isLoading = true;
  String _errorMessage = '';

  @override
  void initState() {
    super.initState();
    _loadExerciseDetails();
  }

  Future<void> _loadExerciseDetails() async {
    try {
      final response = await http.get(
        Uri.parse('http://127.0.0.1:8080/api/exercises/${widget.exerciseId}'),
      );

      if (response.statusCode == 200) {
        setState(() {
          _exercise = jsonDecode(response.body);
          _isLoading = false;
        });
      } else {
        setState(() {
          _errorMessage = '加载动作详情失败';
          _isLoading = false;
        });
      }
    } catch (e) {
      setState(() {
        _errorMessage = '网络错误，请检查网络连接';
        _isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('动作详情'),
        backgroundColor: Colors.white,
        foregroundColor: Colors.black,
        elevation: 1,
      ),
      body: Container(
        width: double.infinity,
        color: Colors.white,
        child: _isLoading
            ? const Center(child: CircularProgressIndicator(color: Colors.black))
            : _errorMessage.isNotEmpty
                ? Center(child: Text(_errorMessage, style: const TextStyle(color: Colors.black)))
                : _exercise == null
                    ? const Center(child: Text('动作不存在', style: const TextStyle(color: Colors.black)))
                    : Padding(
                        padding: const EdgeInsets.all(16.0),
                        child: Container(
                          width: double.infinity,
                          decoration: BoxDecoration(
                            borderRadius: BorderRadius.circular(16),
                            color: Colors.grey[50],
                            boxShadow: [
                              BoxShadow(
                                color: Colors.black.withOpacity(0.1),
                                blurRadius: 10,
                                offset: const Offset(0, 2),
                              ),
                            ],
                          ),
                          padding: const EdgeInsets.all(24),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                _exercise['name'] ?? '未知动作',
                                style: const TextStyle(
                                  fontSize: 24,
                                  fontWeight: FontWeight.bold,
                                  color: Colors.black,
                                ),
                              ),
                              const SizedBox(height: 16),
                              Text(
                                '分类: ${_exercise['category'] ?? '未分类'}',
                                style: TextStyle(fontSize: 16, color: Colors.grey[700]),
                              ),
                              const SizedBox(height: 24),
                              Text(
                                '描述:',
                                style: const TextStyle(
                                  fontSize: 18,
                                  fontWeight: FontWeight.bold,
                                  color: Colors.black,
                                ),
                              ),
                              const SizedBox(height: 8),
                              Text(
                                _exercise['description'] ?? '无描述',
                                style: const TextStyle(fontSize: 16, color: Colors.black87),
                              ),
                              const SizedBox(height: 24),
                              Text(
                                '注意事项:',
                                style: const TextStyle(
                                  fontSize: 18,
                                  fontWeight: FontWeight.bold,
                                  color: Colors.black,
                                ),
                              ),
                              const SizedBox(height: 8),
                              Text(
                                _exercise['notes'] ?? '无注意事项',
                                style: const TextStyle(fontSize: 16, color: Colors.black87),
                              ),
                              const SizedBox(height: 24),
                              ElevatedButton(
                                onPressed: () {
                                  // 实现添加到训练计划的逻辑
                                  ScaffoldMessenger.of(context).showSnackBar(
                                    SnackBar(content: Text('已添加 ${_exercise['name']} 到训练计划')),
                                  );
                                },
                                style: ElevatedButton.styleFrom(
                                  backgroundColor: Colors.black,
                                  foregroundColor: Colors.white,
                                  shape: RoundedRectangleBorder(
                                    borderRadius: BorderRadius.circular(30),
                                  ),
                                  padding: const EdgeInsets.symmetric(horizontal: 32, vertical: 12),
                                ),
                                child: const Text('添加到训练计划'),
                              ),
                            ],
                          ),
                        ),
                      ),
      ),
    );
  }
}