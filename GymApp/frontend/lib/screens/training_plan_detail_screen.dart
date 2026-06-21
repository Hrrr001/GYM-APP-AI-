import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';

class TrainingPlanDetailScreen extends StatefulWidget {
  final int planId;

  const TrainingPlanDetailScreen({super.key, required this.planId});

  @override
  _TrainingPlanDetailScreenState createState() => _TrainingPlanDetailScreenState();
}

class _TrainingPlanDetailScreenState extends State<TrainingPlanDetailScreen> {
  dynamic _plan;
  List<dynamic> _details = [];
  bool _isLoading = true;
  String _errorMessage = '';
  int _selectedWeek = 1;

  @override
  void initState() {
    super.initState();
    _loadPlanDetail();
  }

  Future<void> _loadPlanDetail() async {
    try {
      // 加载计划基本信息
      final planResponse = await http.get(
        Uri.parse('http://localhost:8080/api/plans/${widget.planId}'),
      );

      if (planResponse.statusCode == 200) {
        setState(() {
          _plan = jsonDecode(planResponse.body);
        });

        // 加载计划详情
        final detailsResponse = await http.get(
          Uri.parse('http://localhost:8080/api/plans/${widget.planId}/details'),
        );

        if (detailsResponse.statusCode == 200) {
          setState(() {
            _details = jsonDecode(detailsResponse.body);
            _isLoading = false;
          });
        } else {
          setState(() {
            _errorMessage = '加载计划详情失败';
            _isLoading = false;
          });
        }
      } else {
        setState(() {
          _errorMessage = '加载计划失败';
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

  Future<void> _deletePlan() async {
    try {
      final response = await http.delete(
        Uri.parse('http://localhost:8080/api/plans/${widget.planId}'),
      );

      if (response.statusCode == 200) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('计划删除成功！')),
        );
        Navigator.pop(context);
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
        title: const Text('计划详情'),
        actions: [
          IconButton(
            icon: const Icon(Icons.delete),
            onPressed: () {
              showDialog(
                context: context,
                builder: (context) => AlertDialog(
                  title: const Text('确认删除'),
                  content: const Text('确定要删除这个训练计划吗？'),
                  actions: [
                    TextButton(
                      onPressed: () => Navigator.pop(context),
                      child: const Text('取消'),
                    ),
                    TextButton(
                      onPressed: () {
                        Navigator.pop(context);
                        _deletePlan();
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
      body: Container(
        width: double.infinity,
        child: _isLoading
            ? const Center(child: CircularProgressIndicator())
            : _errorMessage.isNotEmpty
                ? Center(child: Text(_errorMessage))
                : _plan == null
                    ? const Center(child: Text('计划不存在'))
                    : Column(
                        children: [
                          // 计划基本信息
                          Container(
                            width: double.infinity,
                            padding: const EdgeInsets.all(16.0),
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  _plan['name'],
                                  style: const TextStyle(
                                    fontSize: 24,
                                    fontWeight: FontWeight.bold,
                                  ),
                                ),
                                const SizedBox(height: 8),
                                Text('目标: ${_plan['goal']}'),
                                Text('周期: ${_plan['duration']}周'),
                              ],
                            ),
                          ),

                          // 周数选择器
                          if (_plan['duration'] != null && _plan['duration'] > 0)
                            SizedBox(
                              height: 60,
                              child: ListView.builder(
                                scrollDirection: Axis.horizontal,
                                itemCount: _plan['duration'],
                                itemBuilder: (context, index) {
                                  int week = index + 1;
                                  return GestureDetector(
                                    onTap: () {
                                      setState(() {
                                        _selectedWeek = week;
                                      });
                                    },
                                    child: Container(
                                      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 10),
                                      margin: const EdgeInsets.symmetric(horizontal: 5),
                                      decoration: BoxDecoration(
                                        color: _selectedWeek == week ? Colors.black : Colors.grey[200],
                                        borderRadius: BorderRadius.circular(20),
                                      ),
                                      child: Text('第$week周', style: TextStyle(color: _selectedWeek == week ? Colors.white : Colors.black)),
                                    ),
                                  );
                                },
                              ),
                            ),

                          // 每日训练内容
                          Expanded(
                            child: ListView.builder(
                              itemCount: 7, // 一周7天
                              itemBuilder: (context, index) {
                                int day = index + 1;
                                // 过滤出当天的训练内容
                                final dayDetails = _details.where((detail) => 
                                  detail['week'] == _selectedWeek && detail['day'] == day
                                ).toList();

                                return Card(
                                  margin: const EdgeInsets.all(8.0),
                                  child: Padding(
                                    padding: const EdgeInsets.all(16.0),
                                    child: Column(
                                      crossAxisAlignment: CrossAxisAlignment.start,
                                      children: [
                                        Text(
                                          '第$day天',
                                          style: const TextStyle(
                                            fontSize: 18,
                                            fontWeight: FontWeight.bold,
                                          ),
                                        ),
                                        const SizedBox(height: 8),
                                        if (dayDetails.isEmpty)
                                          const Text('今日无训练')
                                        else
                                          Column(
                                            children: dayDetails.map((detail) {
                                              return ListTile(
                                                title: Text(detail['exercise']['name']),
                                                subtitle: Text('${detail['sets']}组 × ${detail['reps']}次'),
                                              );
                                            }).toList(),
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
