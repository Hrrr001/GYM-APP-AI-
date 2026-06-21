import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';

class FoodRecordAddScreen extends StatefulWidget {
  const FoodRecordAddScreen({super.key});

  @override
  _FoodRecordAddScreenState createState() => _FoodRecordAddScreenState();
}

class _FoodRecordAddScreenState extends State<FoodRecordAddScreen> {
  final _foodNameController = TextEditingController();
  final _caloriesController = TextEditingController();
  final _proteinController = TextEditingController();
  final _carbsController = TextEditingController();
  final _fatController = TextEditingController();
  final _aiDescriptionController = TextEditingController();
  DateTime _selectedDate = DateTime.now();
  String _selectedMealType = 'breakfast';
  bool _useAI = false;
  bool _isLoading = false;
  String _errorMessage = '';

  final List<String> _mealTypes = ['breakfast', 'lunch', 'dinner', 'snack'];

  Future<void> _submitRecord() async {
    if (_useAI) {
      if (_aiDescriptionController.text.isEmpty) {
        setState(() {
          _errorMessage = '请输入饮食描述';
        });
        return;
      }
    } else {
      if (_foodNameController.text.isEmpty || _caloriesController.text.isEmpty) {
        setState(() {
          _errorMessage = '请输入食物名称和热量';
        });
        return;
      }
    }

    setState(() {
      _isLoading = true;
      _errorMessage = '';
    });

    try {
      if (_useAI) {
        // AI辅助创建饮食记录
        final response = await http.post(
          Uri.parse('http://localhost:8080/api/nutrition/food/ai/create'),
          headers: {'Content-Type': 'application/json'},
          body: jsonEncode({
            'userId': 1, // 暂时硬编码用户ID
            'date': _selectedDate.toIso8601String().split('T')[0],
            'mealType': _selectedMealType,
            'description': _aiDescriptionController.text,
          }),
        );

        if (response.statusCode == 200) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('饮食记录创建成功！')),
          );
          Navigator.pop(context);
        } else {
          final errorData = jsonDecode(response.body);
          setState(() {
            _errorMessage = errorData['message'] ?? '创建失败';
            _isLoading = false;
          });
        }
      } else {
        // 手动创建饮食记录
        final response = await http.post(
          Uri.parse('http://localhost:8080/api/nutrition/food/create'),
          headers: {'Content-Type': 'application/json'},
          body: jsonEncode({
            'userId': 1, // 暂时硬编码用户ID
            'date': _selectedDate.toIso8601String().split('T')[0],
            'mealType': _selectedMealType,
            'foodName': _foodNameController.text,
            'calories': double.parse(_caloriesController.text),
            'protein': _proteinController.text.isEmpty ? null : double.parse(_proteinController.text),
            'carbs': _carbsController.text.isEmpty ? null : double.parse(_carbsController.text),
            'fat': _fatController.text.isEmpty ? null : double.parse(_fatController.text),
          }),
        );

        if (response.statusCode == 200) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('饮食记录创建成功！')),
          );
          Navigator.pop(context);
        } else {
          final errorData = jsonDecode(response.body);
          setState(() {
            _errorMessage = errorData['message'] ?? '创建失败';
            _isLoading = false;
          });
        }
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
        title: const Text('添加饮食记录'),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            // 日期选择
            ListTile(
              title: const Text('选择日期'),
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

            // 餐次选择
            const SizedBox(height: 16),
            const Text('选择餐次'),
            DropdownButton<String>(
              value: _selectedMealType,
              items: _mealTypes.map((type) => DropdownMenuItem<String>(
                    value: type,
                    child: Text(type),
                  )).toList(),
              onChanged: (value) {
                setState(() {
                  _selectedMealType = value!;
                });
              },
            ),

            // AI辅助开关
            SwitchListTile(
              title: const Text('使用AI辅助'),
              value: _useAI,
              onChanged: (value) {
                setState(() {
                  _useAI = value;
                });
              },
            ),

            // 手动输入表单
            if (!_useAI)
              Column(
                children: [
                  const SizedBox(height: 16),
                  TextField(
                    controller: _foodNameController,
                    decoration: const InputDecoration(
                      labelText: '食物名称',
                      border: OutlineInputBorder(),
                    ),
                  ),
                  const SizedBox(height: 16),
                  TextField(
                    controller: _caloriesController,
                    decoration: const InputDecoration(
                      labelText: '热量（卡路里）',
                      border: OutlineInputBorder(),
                    ),
                    keyboardType: TextInputType.number,
                  ),
                  const SizedBox(height: 16),
                  TextField(
                    controller: _proteinController,
                    decoration: const InputDecoration(
                      labelText: '蛋白质（g）',
                      border: OutlineInputBorder(),
                    ),
                    keyboardType: TextInputType.number,
                  ),
                  const SizedBox(height: 16),
                  TextField(
                    controller: _carbsController,
                    decoration: const InputDecoration(
                      labelText: '碳水化合物（g）',
                      border: OutlineInputBorder(),
                    ),
                    keyboardType: TextInputType.number,
                  ),
                  const SizedBox(height: 16),
                  TextField(
                    controller: _fatController,
                    decoration: const InputDecoration(
                      labelText: '脂肪（g）',
                      border: OutlineInputBorder(),
                    ),
                    keyboardType: TextInputType.number,
                  ),
                ],
              ),

            // AI辅助输入
            if (_useAI)
              Column(
                children: [
                  const SizedBox(height: 16),
                  TextField(
                    controller: _aiDescriptionController,
                    decoration: const InputDecoration(
                      labelText: '描述您吃了什么',
                      hintText: '例如：今天中午吃了一碗牛肉面加一个煎蛋',
                      border: OutlineInputBorder(),
                    ),
                    maxLines: 3,
                  ),
                ],
              ),

            // 错误信息
            if (_errorMessage.isNotEmpty)
              Text(_errorMessage, style: const TextStyle(color: Colors.red)),

            // 提交按钮
            const SizedBox(height: 24),
            _isLoading
                ? const CircularProgressIndicator()
                : ElevatedButton(
                    onPressed: _submitRecord,
                    child: const Text('提交'),
                  ),
          ],
        ),
      ),
    );
  }
}
