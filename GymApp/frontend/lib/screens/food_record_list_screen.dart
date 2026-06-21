import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';
import 'food_record_add_screen.dart';

class FoodRecordListScreen extends StatefulWidget {
  const FoodRecordListScreen({super.key});

  @override
  _FoodRecordListScreenState createState() => _FoodRecordListScreenState();
}

class _FoodRecordListScreenState extends State<FoodRecordListScreen> {
  List<dynamic> _records = [];
  DateTime _selectedDate = DateTime.now();
  bool _isLoading = true;
  String _errorMessage = '';

  @override
  void initState() {
    super.initState();
    _loadRecords();
  }

  Future<void> _loadRecords() async {
    try {
      String dateStr = _selectedDate.toIso8601String().split('T')[0];
      final response = await http.get(
        Uri.parse('http://localhost:8080/api/nutrition/food/user/1/date/$dateStr'), // 暂时硬编码用户ID
      );

      if (response.statusCode == 200) {
        setState(() {
          _records = jsonDecode(response.body);
          _isLoading = false;
        });
      } else {
        setState(() {
          _errorMessage = '加载饮食记录失败';
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

  Future<void> _deleteRecord(int recordId) async {
    try {
      final response = await http.delete(
        Uri.parse('http://localhost:8080/api/nutrition/food/$recordId'),
      );

      if (response.statusCode == 200) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('饮食记录删除成功！')),
        );
        _loadRecords();
      } else {
        final errorData = jsonDecode(response.body);
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('删除失败：${errorData['message'] ?? '未知错误'}')),
        );
      }
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('网络错误，请稍后重试')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('饮食记录'),
        actions: [
          IconButton(
            icon: const Icon(Icons.add),
            onPressed: () {
              Navigator.push(
                context,
                MaterialPageRoute(builder: (context) => const FoodRecordAddScreen()),
              );
            },
          ),
        ],
      ),
      body: Column(
        children: [
          // 日期选择器
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
                  _isLoading = true;
                });
                _loadRecords();
              }
            },
          ),

          // 饮食记录列表
          Expanded(
            child: _isLoading
                ? const Center(child: CircularProgressIndicator())
                : _errorMessage.isNotEmpty
                    ? Center(child: Text(_errorMessage))
                    : _records.isEmpty
                        ? const Center(child: Text('暂无饮食记录'))
                        : ListView.builder(
                            itemCount: _records.length,
                            itemBuilder: (context, index) {
                              final record = _records[index];
                              return Card(
                                margin: const EdgeInsets.symmetric(vertical: 8, horizontal: 16),
                                child: Padding(
                                  padding: const EdgeInsets.all(16.0),
                                  child: Column(
                                    crossAxisAlignment: CrossAxisAlignment.start,
                                    children: [
                                      Row(
                                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                        children: [
                                          Text(
                                            record['mealType'],
                                            style: const TextStyle(fontWeight: FontWeight.bold),
                                          ),
                                          IconButton(
                                            icon: const Icon(Icons.delete, color: Colors.red),
                                            onPressed: () {
                                              showDialog(
                                                context: context,
                                                builder: (context) => AlertDialog(
                                                  title: const Text('确认删除'),
                                                  content: const Text('确定要删除这个饮食记录吗？'),
                                                  actions: [
                                                    TextButton(
                                                      onPressed: () => Navigator.pop(context),
                                                      child: const Text('取消'),
                                                    ),
                                                    TextButton(
                                                      onPressed: () {
                                                        Navigator.pop(context);
                                                        _deleteRecord(record['id']);
                                                      },
                                                      child: const Text('删除'),
                                                    ),
                                                  ],
                                                ),
                                              );
                                            },
                                          ),
                                        ],
                                      ),
                                      const SizedBox(height: 8),
                                      Text(record['foodName']),
                                      const SizedBox(height: 8),
                                      Text('热量: ${record['calories']}卡路里'),
                                      if (record['protein'] != null)
                                        Text('蛋白质: ${record['protein']}g'),
                                      if (record['carbs'] != null)
                                        Text('碳水: ${record['carbs']}g'),
                                      if (record['fat'] != null)
                                        Text('脂肪: ${record['fat']}g'),
                                    ],
                                  ),
                                ),
                              );
                            },
                          ),
          ),
        ],
      ),
    );
  }
}
