import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';
import 'exercise_detail_screen.dart';

// 【类名必须完全正确！首字母大写，驼峰正确，Dart大小写敏感】
class ExerciseListScreen extends StatefulWidget {
  const ExerciseListScreen({super.key});

  @override
  State<ExerciseListScreen> createState() => _ExerciseListScreenState();
}

class _ExerciseListScreenState extends State<ExerciseListScreen> {
  List<dynamic> _exercises = [];
  List<dynamic> _filteredExercises = [];
  bool _isLoading = true;
  String _errorMessage = '';
  final TextEditingController _searchController = TextEditingController();

  @override
  void initState() {
    super.initState();
    _loadExercises();
    _searchController.addListener(_filterExercises);
  }

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  Future<void> _loadExercises() async {
    try {
      final response = await http.get(
        Uri.parse('http://127.0.0.1:8080/api/exercises/standard'),
      );

      if (response.statusCode == 200) {
        setState(() {
          _exercises = jsonDecode(response.body);
          _filteredExercises = _exercises;
          _isLoading = false;
        });
      } else {
        setState(() {
          _errorMessage = '加载失败，状态码：${response.statusCode}';
          _isLoading = false;
        });
      }
    } catch (e) {
      setState(() {
        _errorMessage = '网络错误：$e';
        _isLoading = false;
      });
    }
  }

  void _filterExercises() {
    final query = _searchController.text.toLowerCase();
    setState(() {
      _filteredExercises = _exercises.where((exercise) {
        final name = (exercise['name'] ?? '').toLowerCase();
        final description = (exercise['description'] ?? '').toLowerCase();
        return name.contains(query) || description.contains(query);
      }).toList();
    });
  }

  Future<void> _addExerciseToPlan(dynamic exercise) async {
    // 这里可以实现添加动作到训练计划的逻辑
    // 暂时显示一个成功消息
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text('已添加 ${exercise['name']} 到训练计划')),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('动作库'),
        backgroundColor: Colors.black,
        foregroundColor: Colors.white,
        elevation: 0,
      ),
      body: Container(
        color: Colors.white,
        child: Column(
          children: [
            // 搜索栏
            Padding(
              padding: const EdgeInsets.all(16.0),
              child: Container(
                decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(color: Colors.black, width: 1),
                  color: Colors.white,
                ),
                child: TextField(
                  controller: _searchController,
                  style: const TextStyle(color: Colors.black),
                  decoration: InputDecoration(
                    hintText: '搜索动作...',
                    hintStyle: const TextStyle(color: Colors.grey),
                    prefixIcon: const Icon(Icons.search, color: Colors.black),
                    border: InputBorder.none,
                    contentPadding: const EdgeInsets.all(16),
                  ),
                ),
              ),
            ),
            // 动作列表
            Expanded(
              child: _isLoading
                  ? const Center(child: CircularProgressIndicator(color: Colors.black))
                  : _errorMessage.isNotEmpty
                  ? Center(child: Text(_errorMessage, style: const TextStyle(color: Colors.black)))
                  : _filteredExercises.isEmpty
                  ? const Center(child: Text('没有找到匹配的动作', style: TextStyle(color: Colors.black)))
                  : ListView.builder(
                      itemCount: _filteredExercises.length,
                      itemBuilder: (ctx, index) {
                        final exercise = _filteredExercises[index];
                        return Container(
                          margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                          decoration: BoxDecoration(
                            borderRadius: BorderRadius.circular(12),
                            border: Border.all(color: Colors.black, width: 1),
                            color: Colors.white,
                            boxShadow: [
                              BoxShadow(
                                color: Colors.black.withOpacity(0.05),
                                blurRadius: 8,
                                offset: const Offset(0, 2),
                              ),
                            ],
                          ),
                          child: ListTile(
                            title: Text(exercise['name'] ?? '未知动作', style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.black)),
                            subtitle: Text(exercise['description'] ?? '无描述', maxLines: 2, overflow: TextOverflow.ellipsis, style: const TextStyle(color: Colors.black)),
                            trailing: Row(
                              mainAxisSize: MainAxisSize.min,
                              children: [
                                IconButton(
                                  icon: const Icon(Icons.add, color: Colors.black),
                                  onPressed: () => _addExerciseToPlan(exercise),
                                ),
                                IconButton(
                                  icon: const Icon(Icons.arrow_forward, color: Colors.black),
                                  onPressed: () {
                                    Navigator.push(
                                      context,
                                      MaterialPageRoute(
                                        builder: (context) => ExerciseDetailScreen(exerciseId: exercise['id']),
                                      ),
                                    );
                                  },
                                ),
                              ],
                            ),
                          ),
                        );
                      },
                    ),
            ),
          ],
        ),
      ),
    );
  }
}