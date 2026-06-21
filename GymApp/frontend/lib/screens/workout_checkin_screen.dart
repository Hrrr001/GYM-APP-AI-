import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';
import 'workout_record_detail_screen.dart';

class WorkoutCheckinScreen extends StatefulWidget {
  const WorkoutCheckinScreen({super.key});

  @override
  _WorkoutCheckinScreenState createState() => _WorkoutCheckinScreenState();
}

class _WorkoutCheckinScreenState extends State<WorkoutCheckinScreen> {
  final _durationController = TextEditingController();
  final _feelingController = TextEditingController();
  final _notesController = TextEditingController();
  DateTime _selectedDate = DateTime.now();
  List<dynamic> _plans = [];
  List<dynamic> _exercises = [];
  final List<dynamic> _selectedExercises = [];
  int? _selectedPlanId;
  bool _isLoading = false;
  bool _isLoadingPlans = true;
  bool _isLoadingExercises = true;
  String _errorMessage = '';

  @override
  void initState() {
    super.initState();
    _loadPlans();
    _loadExercises();
  }

  Future<void> _loadPlans() async {
    try {
      final response = await http.get(
        Uri.parse('http://localhost:8080/api/plans/user/1'), // 暂时硬编码用户ID
      );

      if (response.statusCode == 200) {
        setState(() {
          _plans = jsonDecode(response.body);
          _isLoadingPlans = false;
        });
      } else {
        setState(() {
          _isLoadingPlans = false;
        });
      }
    } catch (e) {
      setState(() {
        _isLoadingPlans = false;
      });
    }
  }

  Future<void> _loadExercises() async {
    try {
      final response = await http.get(
        Uri.parse('http://localhost:8080/api/exercises/standard'),
      );

      if (response.statusCode == 200) {
        setState(() {
          _exercises = jsonDecode(response.body);
          _isLoadingExercises = false;
        });
      } else {
        setState(() {
          _isLoadingExercises = false;
        });
      }
    } catch (e) {
      setState(() {
        _isLoadingExercises = false;
      });
    }
  }

  void _addExercise(dynamic exercise) {
    setState(() {
      _selectedExercises.add({
        'exercise': exercise,
        'sets': 3,
        'reps': 10,
        'weight': null,
        'completed': true,
      });
    });
  }

  void _removeExercise(int index) {
    setState(() {
      _selectedExercises.removeAt(index);
    });
  }

  Future<void> _submitCheckin() async {
    if (_durationController.text.isEmpty) {
      setState(() {
        _errorMessage = '请输入训练时长';
      });
      return;
    }

    setState(() {
      _isLoading = true;
      _errorMessage = '';
    });

    try {
      // 创建训练记录
      final recordResponse = await http.post(
        Uri.parse('http://localhost:8080/api/workouts/create'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({
          'userId': 1, // 暂时硬编码用户ID
          'planId': _selectedPlanId,
          'date': _selectedDate.toIso8601String().split('T')[0],
          'duration': int.parse(_durationController.text),
          'feeling': _feelingController.text,
          'notes': _notesController.text,
        }),
      );

      if (recordResponse.statusCode == 200) {
        final recordData = jsonDecode(recordResponse.body);
        int recordId = recordData['recordId'];

        // 添加训练详情
        for (var item in _selectedExercises) {
          await http.post(
            Uri.parse('http://localhost:8080/api/workouts/$recordId/details'),
            headers: {'Content-Type': 'application/json'},
            body: jsonEncode({
              'exerciseId': item['exercise']['id'],
              'sets': item['sets'],
              'reps': item['reps'],
              'weight': item['weight'],
              'completed': item['completed'],
            }),
          );
        }

        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('训练打卡成功！')),
        );

        Navigator.push(
          context,
          MaterialPageRoute(
            builder: (context) => WorkoutRecordDetailScreen(recordId: recordId),
          ),
        );
      } else {
        final errorData = jsonDecode(recordResponse.body);
        setState(() {
          _errorMessage = errorData['message'] ?? '打卡失败';
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
        title: const Text('训练打卡'),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            // 日期选择
            ListTile(
              title: const Text('训练日期'),
              subtitle: Text('${_selectedDate.year}-${_selectedDate.month}-${_selectedDate.day}'),
              trailing: const Icon(Icons.calendar_today),
              onTap: () async {
                final pickedDate = await showDatePicker(
                  context: context,
                  initialDate: _selectedDate,
                  firstDate: DateTime.now().subtract(const Duration(days: 30)),
                  lastDate: DateTime.now(),
                );
                if (pickedDate != null) {
                  setState(() {
                    _selectedDate = pickedDate;
                  });
                }
              },
            ),

            // 训练计划选择
            const SizedBox(height: 16),
            const Text('选择训练计划（可选）'),
            _isLoadingPlans
                ? const CircularProgressIndicator()
                : DropdownButton<int?>(
                    value: _selectedPlanId,
                    hint: const Text('选择计划'),
                    items: [
                      const DropdownMenuItem<int?>(
                        value: null,
                        child: Text('无计划'),
                      ),
                      ..._plans.map((plan) => DropdownMenuItem<int>(
                            value: plan['id'],
                            child: Text(plan['name']),
                          )),
                    ],
                    onChanged: (value) {
                      setState(() {
                        _selectedPlanId = value;
                      });
                    },
                  ),

            // 训练时长
            const SizedBox(height: 16),
            TextField(
              controller: _durationController,
              decoration: const InputDecoration(
                labelText: '训练时长（分钟）',
                border: OutlineInputBorder(),
              ),
              keyboardType: TextInputType.number,
            ),

            // 训练感受
            const SizedBox(height: 16),
            TextField(
              controller: _feelingController,
              decoration: const InputDecoration(
                labelText: '训练感受（可选）',
                border: OutlineInputBorder(),
              ),
              maxLines: 2,
            ),

            // 备注
            const SizedBox(height: 16),
            TextField(
              controller: _notesController,
              decoration: const InputDecoration(
                labelText: '备注（可选）',
                border: OutlineInputBorder(),
              ),
              maxLines: 2,
            ),

            // 训练内容
            const SizedBox(height: 24),
            const Text(
              '训练内容',
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 16),

            // 添加动作按钮
            ElevatedButton(
              onPressed: () {
                showDialog(
                  context: context,
                  builder: (context) => AlertDialog(
                    title: const Text('选择动作'),
                    content: _isLoadingExercises
                        ? const CircularProgressIndicator()
                        : SingleChildScrollView(
                            child: Column(
                              children: _exercises.map((exercise) {
                                return ListTile(
                                  title: Text(exercise['name']),
                                  subtitle: Text('${exercise['category']} · ${exercise['equipment']}'),
                                  onTap: () {
                                    _addExercise(exercise);
                                    Navigator.pop(context);
                                  },
                                );
                              }).toList(),
                            ),
                          ),
                    actions: [
                      TextButton(
                        onPressed: () => Navigator.pop(context),
                        child: const Text('取消'),
                      ),
                    ],
                  ),
                );
              },
              child: const Text('添加动作'),
            ),

            // 已选动作列表
            const SizedBox(height: 16),
            if (_selectedExercises.isEmpty)
              const Text('暂无动作')
            else
              Column(
                children: _selectedExercises.asMap().entries.map((entry) {
                  int index = entry.key;
                  var item = entry.value;
                  return Card(
                    margin: const EdgeInsets.symmetric(vertical: 8),
                    child: Padding(
                      padding: const EdgeInsets.all(16.0),
                      child: Column(
                        children: [
                          Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              Text(
                                item['exercise']['name'],
                                style: const TextStyle(fontWeight: FontWeight.bold),
                              ),
                              IconButton(
                                icon: const Icon(Icons.delete),
                                onPressed: () => _removeExercise(index),
                              ),
                            ],
                          ),
                          const SizedBox(height: 8),
                          Row(
                            children: [
                              Expanded(
                                child: TextField(
                                  keyboardType: TextInputType.number,
                                  decoration: const InputDecoration(labelText: '组数'),
                                  onChanged: (value) {
                                    if (value.isNotEmpty) {
                                      item['sets'] = int.parse(value);
                                    }
                                  },
                                  controller: TextEditingController(text: item['sets'].toString()),
                                ),
                              ),
                              const SizedBox(width: 16),
                              Expanded(
                                child: TextField(
                                  keyboardType: TextInputType.number,
                                  decoration: const InputDecoration(labelText: '次数'),
                                  onChanged: (value) {
                                    if (value.isNotEmpty) {
                                      item['reps'] = int.parse(value);
                                    }
                                  },
                                  controller: TextEditingController(text: item['reps'].toString()),
                                ),
                              ),
                              const SizedBox(width: 16),
                              Expanded(
                                child: TextField(
                                  keyboardType: TextInputType.number,
                                  decoration: const InputDecoration(labelText: '重量（kg）'),
                                  onChanged: (value) {
                                    if (value.isNotEmpty) {
                                      item['weight'] = double.parse(value);
                                    } else {
                                      item['weight'] = null;
                                    }
                                  },
                                  controller: TextEditingController(text: item['weight']?.toString() ?? ''),
                                ),
                              ),
                            ],
                          ),
                          Row(
                            children: [
                              const Text('完成状态:'),
                              Checkbox(
                                value: item['completed'],
                                onChanged: (value) {
                                  item['completed'] = value ?? true;
                                  setState(() {});
                                },
                              ),
                            ],
                          ),
                        ],
                      ),
                    ),
                  );
                }).toList(),
              ),

            // 错误信息
            if (_errorMessage.isNotEmpty)
              Text(_errorMessage, style: const TextStyle(color: Colors.red)),

            // 提交按钮
            const SizedBox(height: 24),
            _isLoading
                ? const CircularProgressIndicator()
                : ElevatedButton(
                    onPressed: _submitCheckin,
                    child: const Text('提交打卡'),
                  ),
          ],
        ),
      ),
    );
  }
}
