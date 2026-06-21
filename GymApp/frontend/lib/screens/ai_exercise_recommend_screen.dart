import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';
import 'exercise_detail_screen.dart';

class AIExerciseRecommendScreen extends StatefulWidget {
  const AIExerciseRecommendScreen({super.key});

  @override
  _AIExerciseRecommendScreenState createState() => _AIExerciseRecommendScreenState();
}

class _AIExerciseRecommendScreenState extends State<AIExerciseRecommendScreen> {
  final _controller = TextEditingController();
  List<dynamic> _recommendedExercises = [];
  bool _isLoading = false;
  String _errorMessage = '';

  Future<void> _recommendExercises() async {
    if (_controller.text.isEmpty) {
      setState(() {
        _errorMessage = '请输入您的需求';
      });
      return;
    }

    setState(() {
      _isLoading = true;
      _errorMessage = '';
    });

    try {
      final response = await http.post(
        Uri.parse('http://localhost:8080/api/exercises/ai/recommend'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({
          'userId': 1, // 暂时硬编码，实际应该从登录状态获取
          'query': _controller.text,
        }),
      );

      if (response.statusCode == 200) {
        setState(() {
          _recommendedExercises = jsonDecode(response.body);
          _isLoading = false;
        });
      } else {
        setState(() {
          _errorMessage = '获取推荐失败';
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

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('AI动作推荐'),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            TextField(
              controller: _controller,
              decoration: InputDecoration(
                labelText: '请描述您的需求',
                hintText: '例如：膝盖有积液不能深蹲，想练腿',
                border: const OutlineInputBorder(),
                suffixIcon: IconButton(
                  icon: const Icon(Icons.send),
                  onPressed: _recommendExercises,
                ),
              ),
              maxLines: 3,
            ),
            const SizedBox(height: 16),
            if (_isLoading)
              const CircularProgressIndicator()
            else if (_errorMessage.isNotEmpty)
              Text(_errorMessage, style: const TextStyle(color: Colors.red))
            else if (_recommendedExercises.isNotEmpty)
              Expanded(
                child: ListView.builder(
                  itemCount: _recommendedExercises.length,
                  itemBuilder: (context, index) {
                    final exercise = _recommendedExercises[index];
                    return ListTile(
                      title: Text(exercise['name']),
                      subtitle: Text('${exercise['category']} · ${exercise['equipment']} · ${exercise['difficulty']}'),
                      onTap: () {
                        Navigator.push(
                          context,
                          MaterialPageRoute(
                            builder: (context) => ExerciseDetailScreen(exerciseId: exercise['id']),
                          ),
                        );
                      },
                    );
                  },
                ),
              )
            else
              const Expanded(
                child: Center(
                  child: Text('请输入您的需求，AI将为您推荐合适的动作'),
                ),
              ),
          ],
        ),
      ),
    );
  }
}
