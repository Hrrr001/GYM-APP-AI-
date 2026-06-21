import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';

class BodyMeasurementAddScreen extends StatefulWidget {
  const BodyMeasurementAddScreen({super.key});

  @override
  _BodyMeasurementAddScreenState createState() => _BodyMeasurementAddScreenState();
}

class _BodyMeasurementAddScreenState extends State<BodyMeasurementAddScreen> {
  final _weightController = TextEditingController();
  final _bodyFatController = TextEditingController();
  final _muscleMassController = TextEditingController();
  final _waistController = TextEditingController();
  final _hipController = TextEditingController();
  DateTime _selectedDate = DateTime.now();
  bool _isLoading = false;
  String _errorMessage = '';

  Future<void> _submitMeasurement() async {
    if (_weightController.text.isEmpty) {
      setState(() {
        _errorMessage = '请输入体重';
      });
      return;
    }

    setState(() {
      _isLoading = true;
      _errorMessage = '';
    });

    try {
      final response = await http.post(
        Uri.parse('http://localhost:8080/api/nutrition/body/create'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({
          'userId': 1, // 暂时硬编码用户ID
          'date': _selectedDate.toIso8601String().split('T')[0],
          'weight': double.parse(_weightController.text),
          'bodyFat': _bodyFatController.text.isEmpty ? null : double.parse(_bodyFatController.text),
          'muscleMass': _muscleMassController.text.isEmpty ? null : double.parse(_muscleMassController.text),
          'waist': _waistController.text.isEmpty ? null : double.parse(_waistController.text),
          'hip': _hipController.text.isEmpty ? null : double.parse(_hipController.text),
        }),
      );

      if (response.statusCode == 200) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('身体数据记录创建成功！')),
        );
        Navigator.pop(context);
      } else {
        final errorData = jsonDecode(response.body);
        setState(() {
          _errorMessage = errorData['message'] ?? '创建失败';
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
        title: const Text('添加身体数据记录'),
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

            // 身体数据输入
            const SizedBox(height: 16),
            TextField(
              controller: _weightController,
              decoration: const InputDecoration(
                labelText: '体重（kg）',
                border: OutlineInputBorder(),
              ),
              keyboardType: TextInputType.number,
            ),
            const SizedBox(height: 16),
            TextField(
              controller: _bodyFatController,
              decoration: const InputDecoration(
                labelText: '体脂率（%）',
                border: OutlineInputBorder(),
              ),
              keyboardType: TextInputType.number,
            ),
            const SizedBox(height: 16),
            TextField(
              controller: _muscleMassController,
              decoration: const InputDecoration(
                labelText: '肌肉量（kg）',
                border: OutlineInputBorder(),
              ),
              keyboardType: TextInputType.number,
            ),
            const SizedBox(height: 16),
            TextField(
              controller: _waistController,
              decoration: const InputDecoration(
                labelText: '腰围（cm）',
                border: OutlineInputBorder(),
              ),
              keyboardType: TextInputType.number,
            ),
            const SizedBox(height: 16),
            TextField(
              controller: _hipController,
              decoration: const InputDecoration(
                labelText: '臀围（cm）',
                border: OutlineInputBorder(),
              ),
              keyboardType: TextInputType.number,
            ),

            // 错误信息
            if (_errorMessage.isNotEmpty)
              Text(_errorMessage, style: const TextStyle(color: Colors.red)),

            // 提交按钮
            const SizedBox(height: 24),
            _isLoading
                ? const CircularProgressIndicator()
                : ElevatedButton(
                    onPressed: _submitMeasurement,
                    child: const Text('提交'),
                  ),
          ],
        ),
      ),
    );
  }
}