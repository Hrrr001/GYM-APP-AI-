import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';
import 'workout_record_detail_screen.dart';
import 'workout_checkin_screen.dart';

class WorkoutRecordListScreen extends StatefulWidget {
  const WorkoutRecordListScreen({super.key});

  @override
  _WorkoutRecordListScreenState createState() => _WorkoutRecordListScreenState();
}

class _WorkoutRecordListScreenState extends State<WorkoutRecordListScreen> {
  List<dynamic> _records = [];
  bool _isLoading = true;
  String _errorMessage = '';

  @override
  void initState() {
    super.initState();
    _loadRecords();
  }

  Future<void> _loadRecords() async {
    try {
      final response = await http.get(
        Uri.parse('http://localhost:8080/api/workouts/user/1'), // 暂时硬编码用户ID
      );

      if (response.statusCode == 200) {
        setState(() {
          _records = jsonDecode(response.body);
          _isLoading = false;
        });
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

  Future<void> _deleteRecord(int recordId) async {
    try {
      final response = await http.delete(
        Uri.parse('http://localhost:8080/api/workouts/$recordId'),
      );

      if (response.statusCode == 200) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('训练记录删除成功！')),
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
        title: const Text('训练记录'),
        actions: [
          IconButton(
            icon: const Icon(Icons.add),
            onPressed: () {
              Navigator.push(
                context,
                MaterialPageRoute(builder: (context) => const WorkoutCheckinScreen()),
              );
            },
          ),
        ],
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : _errorMessage.isNotEmpty
              ? Center(child: Text(_errorMessage))
              : _records.isEmpty
                  ? const Center(child: Text('暂无训练记录'))
                  : ListView.builder(
                      itemCount: _records.length,
                      itemBuilder: (context, index) {
                        final record = _records[index];
                        return ListTile(
                          title: Text('训练记录 - ${record['date']}'),
                          subtitle: Text('时长: ${record['duration']}分钟'),
                          onTap: () {
                            Navigator.push(
                              context,
                              MaterialPageRoute(
                                builder: (context) => WorkoutRecordDetailScreen(recordId: record['id']),
                              ),
                            );
                          },
                          trailing: IconButton(
                            icon: const Icon(Icons.delete, color: Colors.red),
                            onPressed: () {
                              showDialog(
                                context: context,
                                builder: (context) => AlertDialog(
                                  title: const Text('确认删除'),
                                  content: const Text('确定要删除这个训练记录吗？'),
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
                        );
                      },
                    ),
    );
  }
}
