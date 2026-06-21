import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';
import 'body_measurement_add_screen.dart';

class BodyMeasurementListScreen extends StatefulWidget {
  const BodyMeasurementListScreen({super.key});

  @override
  _BodyMeasurementListScreenState createState() => _BodyMeasurementListScreenState();
}

class _BodyMeasurementListScreenState extends State<BodyMeasurementListScreen> {
  List<dynamic> _measurements = [];
  bool _isLoading = true;
  String _errorMessage = '';

  @override
  void initState() {
    super.initState();
    _loadMeasurements();
  }

  Future<void> _loadMeasurements() async {
    try {
      final response = await http.get(
        Uri.parse('http://localhost:8080/api/nutrition/body/user/1'), // 暂时硬编码用户ID
      );

      if (response.statusCode == 200) {
        setState(() {
          _measurements = jsonDecode(response.body);
          _isLoading = false;
        });
      } else {
        setState(() {
          _errorMessage = '加载身体数据失败';
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
        title: const Text('身体数据记录'),
        actions: [
          IconButton(
            icon: const Icon(Icons.add),
            onPressed: () {
              Navigator.push(
                context,
                MaterialPageRoute(builder: (context) => const BodyMeasurementAddScreen()),
              );
            },
          ),
        ],
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : _errorMessage.isNotEmpty
              ? Center(child: Text(_errorMessage))
              : _measurements.isEmpty
                  ? const Center(child: Text('暂无身体数据记录'))
                  : ListView.builder(
                      itemCount: _measurements.length,
                      itemBuilder: (context, index) {
                        final measurement = _measurements[index];
                        return Card(
                          margin: const EdgeInsets.symmetric(vertical: 8, horizontal: 16),
                          child: Padding(
                            padding: const EdgeInsets.all(16.0),
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children:
                                [
                                Text(
                                  measurement['date'],
                                  style: const TextStyle(fontWeight: FontWeight.bold),
                                ),
                                const SizedBox(height: 8),
                                Text('体重: ${measurement['weight']}kg'),
                                if (measurement['bodyFat'] != null)
                                  Text('体脂率: ${measurement['bodyFat']}%'),
                                if (measurement['muscleMass'] != null)
                                  Text('肌肉量: ${measurement['muscleMass']}kg'),
                                if (measurement['waist'] != null)
                                  Text('腰围: ${measurement['waist']}cm'),
                                if (measurement['hip'] != null)
                                  Text('臀围: ${measurement['hip']}cm'),
                              ],
                            ),
                          ),
                        );
                      },
                    ),
    );
  }
}