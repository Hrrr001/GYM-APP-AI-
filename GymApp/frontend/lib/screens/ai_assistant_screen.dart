import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';
import 'package:uuid/uuid.dart';

class AIAssistantScreen extends StatefulWidget {
  const AIAssistantScreen({super.key});

  @override
  _AIAssistantScreenState createState() => _AIAssistantScreenState();
}

class _AIAssistantScreenState extends State<AIAssistantScreen> {
  final _messageController = TextEditingController();
  final List<Map<String, dynamic>> _messages = [];
  bool _isLoading = false;
  String _sessionId = '';
  final _uuid = const Uuid();

  @override
  void initState() {
    super.initState();
    _messages.add({
      'sender': 'ai',
      'text': '你好！我是你的AI健身助手，持有NSCA-CSCS和ACSM认证。\n\n我可以帮你：\n- 专业健身知识问答\n- 生成个性化训练计划\n- 分析训练反馈并调整方案\n\n请告诉我你的需求！',
      'references': [],
      'safety_warnings': [],
    });
  }

  Future<void> _ensureSession() async {
    if (_sessionId.isNotEmpty) return;
    try {
      final resp = await http.post(
        Uri.parse('http://localhost:8080/api/ai/qa/session/new'),
      );
      if (resp.statusCode == 200) {
        final data = jsonDecode(resp.body);
        _sessionId = data['session_id'] ?? _uuid.v4().substring(0, 12);
      } else {
        _sessionId = _uuid.v4().substring(0, 12);
      }
    } catch (e) {
      _sessionId = _uuid.v4().substring(0, 12);
    }
  }

  Future<void> _sendMessage() async {
    if (_messageController.text.trim().isEmpty) return;

    final message = _messageController.text.trim();
    setState(() {
      _messages.add({'sender': 'user', 'text': message});
      _isLoading = true;
    });
    _messageController.clear();

    await _ensureSession();

    try {
      final resp = await http.post(
        Uri.parse('http://localhost:8080/api/ai/qa/ask'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({
          'session_id': _sessionId,
          'question': message,
        }),
      );

      if (resp.statusCode == 200) {
        final data = jsonDecode(resp.body);
        setState(() {
          _messages.add({
            'sender': 'ai',
            'text': data['answer'] ?? '抱歉，无法处理该问题。',
            'references': data['references'] ?? [],
            'safety_warnings': data['safety_warnings'] ?? [],
            'confidence': data['confidence'] ?? '中',
            'disclaimer': data['disclaimer'],
          });
          _isLoading = false;
        });

        // Suggest follow-up questions
        final related = data['related_questions'] as List<dynamic>?;
        if (related != null && related.isNotEmpty) {
          setState(() {
            _messages.add({
              'sender': 'ai',
              'text': '你可能还想问：\n${related.map((q) => '  - $q').join('\n')}',
              'is_suggestion': true,
            });
          });
        }
      } else {
        setState(() {
          _messages.add({'sender': 'ai', 'text': 'AI服务暂时不可用，请稍后重试。'});
          _isLoading = false;
        });
      }
    } catch (e) {
      setState(() {
        _messages.add({'sender': 'ai', 'text': '网络错误，请检查连接后重试。'});
        _isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('AI健身助手'),
        backgroundColor: Colors.black,
        foregroundColor: Colors.white,
        elevation: 0,
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () {
              setState(() {
                _messages.clear();
                _sessionId = '';
              });
              initState();
            },
            tooltip: '新会话',
          ),
        ],
      ),
      body: Container(
        color: Colors.white,
        child: Column(
          children: [
            Expanded(
              child: ListView.builder(
                padding: const EdgeInsets.all(16),
                itemCount: _messages.length,
                itemBuilder: (context, index) => _buildMessageBubble(_messages[index]),
              ),
            ),
            _buildInputBar(),
          ],
        ),
      ),
    );
  }

  Widget _buildMessageBubble(Map<String, dynamic> msg) {
    final isUser = msg['sender'] == 'user';
    final isSuggestion = msg['is_suggestion'] == true;
    final refs = msg['references'] as List<dynamic>? ?? [];
    final warnings = msg['safety_warnings'] as List<dynamic>? ?? [];
    final confidence = msg['confidence'] as String?;

    return Container(
      margin: const EdgeInsets.symmetric(vertical: 6),
      alignment: isUser ? Alignment.centerRight : Alignment.centerLeft,
      child: ConstrainedBox(
        constraints: BoxConstraints(maxWidth: MediaQuery.of(context).size.width * 0.78),
        child: Container(
          padding: const EdgeInsets.all(14),
          decoration: BoxDecoration(
            color: isUser ? Colors.black : (isSuggestion ? Colors.grey[100] : Colors.white),
            borderRadius: BorderRadius.circular(12),
            border: isUser ? null : Border.all(color: isSuggestion ? Colors.grey : Colors.black, width: 1),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withOpacity(0.04),
                blurRadius: 6,
                offset: const Offset(0, 2),
              ),
            ],
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                msg['text'] ?? '',
                style: TextStyle(
                  color: isUser ? Colors.white : Colors.black,
                  fontSize: 15,
                  height: 1.4,
                ),
              ),
              if (confidence != null && !isSuggestion) ...[
                const SizedBox(height: 6),
                Row(
                  children: [
                    Icon(Icons.science, size: 12, color: Colors.grey[600]),
                    const SizedBox(width: 4),
                    Text('置信度: $confidence', style: TextStyle(fontSize: 11, color: Colors.grey[600])),
                  ],
                ),
              ],
              if (refs.isNotEmpty) ...[
                const SizedBox(height: 4),
                Text('参考: ${refs.take(3).join(', ')}',
                    style: TextStyle(fontSize: 10, color: Colors.grey[500])),
              ],
              if (warnings.isNotEmpty) ...[
                const SizedBox(height: 6),
                ...warnings.map((w) => Container(
                      margin: const EdgeInsets.only(top: 2),
                      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                      decoration: BoxDecoration(
                        color: Colors.red[50],
                        borderRadius: BorderRadius.circular(4),
                        border: Border.all(color: Colors.red[200]!),
                      ),
                      child: Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Icon(Icons.warning_amber, size: 12, color: Colors.red[700]),
                          const SizedBox(width: 4),
                          Flexible(child: Text(w, style: TextStyle(fontSize: 11, color: Colors.red[700]))),
                        ],
                      ),
                    )),
              ],
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildInputBar() {
    return Padding(
      padding: const EdgeInsets.all(12.0),
      child: Container(
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(12),
          border: Border.all(color: Colors.black, width: 1),
        ),
        child: Row(
          children: [
            Expanded(
              child: TextField(
                controller: _messageController,
                style: const TextStyle(color: Colors.black),
                decoration: const InputDecoration(
                  hintText: '输入你的健身问题...',
                  hintStyle: TextStyle(color: Colors.grey),
                  border: InputBorder.none,
                  contentPadding: EdgeInsets.all(14),
                ),
                onSubmitted: (_) => _sendMessage(),
              ),
            ),
            _isLoading
                ? const Padding(
                    padding: EdgeInsets.symmetric(horizontal: 14),
                    child: SizedBox(
                      width: 20, height: 20,
                      child: CircularProgressIndicator(strokeWidth: 2, color: Colors.black),
                    ),
                  )
                : IconButton(
                    icon: const Icon(Icons.send, color: Colors.black),
                    onPressed: _sendMessage,
                  ),
          ],
        ),
      ),
    );
  }

  @override
  void dispose() {
    _messageController.dispose();
    super.dispose();
  }
}
