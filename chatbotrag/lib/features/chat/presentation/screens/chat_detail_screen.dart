import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:chatbotrag/shared/models/message_model.dart';
import 'package:chatbotrag/shared/services/api_service.dart';

class ChatDetailScreen extends ConsumerStatefulWidget {
  final int? conversationId;   // Peut être null au début

  const ChatDetailScreen({super.key, this.conversationId});

  @override
  ConsumerState<ChatDetailScreen> createState() => _ChatDetailScreenState();
}

class _ChatDetailScreenState extends ConsumerState<ChatDetailScreen> {
  List<MessageModel> messages = [];
  final _controller = TextEditingController();
  final _scrollController = ScrollController();
  bool isLoading = false;

  // Variable importante : on stocke l'ID de conversation une fois qu'elle est créée
  int? _currentConversationId;

  @override
  void initState() {
    super.initState();
    _currentConversationId = widget.conversationId;
    if (_currentConversationId != null) {
      _loadConversation();
    }
  }

  Future<void> _loadConversation() async {
    setState(() => isLoading = true);
    try {
      final api = ref.read(apiServiceProvider);
      final response = await api.get('/api/chat/conversation/$_currentConversationId');
      final data = response.data;
      messages = (data['messages'] as List)
          .map((m) => MessageModel(role: m['role'], content: m['content']))
          .toList();
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text("Erreur chargement : $e")),
      );
    } finally {
      setState(() => isLoading = false);
      _scrollToBottom();
    }
  }

  Future<void> _sendMessage() async {
    if (_controller.text.trim().isEmpty) return;

    final userMessage = _controller.text.trim();
    _controller.clear();

    setState(() {
      messages.add(MessageModel(role: "USER", content: userMessage));
      isLoading = true;
    });
    _scrollToBottom();

    try {
      final api = ref.read(apiServiceProvider);
      final response = await api.post('/api/chat/send', data: {
        "message": userMessage,
        "conversationId": _currentConversationId,   // On utilise la variable locale
      });

      final data = response.data;

      // Mise à jour de l'ID de conversation (très important !)
      if (_currentConversationId == null && data['conversationId'] != null) {
        _currentConversationId = data['conversationId'];
      }

      // Ajout de la réponse IA
      final assistantMessage = data['messages'].last;
      setState(() {
        messages.add(MessageModel(
          role: assistantMessage['role'],
          content: assistantMessage['content'],
        ));
      });
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text("Erreur : $e")),
      );
    } finally {
      setState(() => isLoading = false);
      _scrollToBottom();
    }
  }

  void _scrollToBottom() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (_scrollController.hasClients) {
        _scrollController.animateTo(
          _scrollController.position.maxScrollExtent,
          duration: const Duration(milliseconds: 400),
          curve: Curves.easeOut,
        );
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Assistant IA"),
      ),
      body: Column(
        children: [
          Expanded(
            child: ListView.builder(
              controller: _scrollController,
              padding: const EdgeInsets.all(16),
              itemCount: messages.length + (isLoading ? 1 : 0),
              itemBuilder: (context, index) {
                if (index == messages.length && isLoading) {
                  return const Align(
                    alignment: Alignment.centerLeft,
                    child: Padding(
                      padding: EdgeInsets.only(top: 8),
                      child: Row(
                        children: [
                          CircularProgressIndicator(strokeWidth: 2),
                          SizedBox(width: 12),
                          Text("L'IA réfléchit..."),
                        ],
                      ),
                    ),
                  );
                }

                final message = messages[index];
                final isUser = message.role == "USER";

                return Align(
                  alignment: isUser ? Alignment.centerRight : Alignment.centerLeft,
                  child: Container(
                    margin: const EdgeInsets.symmetric(vertical: 6),
                    padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                    constraints: BoxConstraints(
                        maxWidth: MediaQuery.of(context).size.width * 0.75),
                    decoration: BoxDecoration(
                      color: isUser ? Colors.indigo : Colors.grey.shade200,
                      borderRadius: BorderRadius.circular(20),
                    ),
                    child: Text(
                      message.content,
                      style: TextStyle(
                        color: isUser ? Colors.white : Colors.black87,
                        fontSize: 16,
                      ),
                    ),
                  ),
                );
              },
            ),
          ),
          Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: Theme.of(context).scaffoldBackgroundColor,
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withOpacity(0.05),
                  blurRadius: 10,
                  offset: const Offset(0, -2),
                )
              ],
            ),
            child: Row(
              children: [
                Expanded(
                  child: TextField(
                    controller: _controller,
                    decoration: InputDecoration(
                      hintText: "Posez votre question...",
                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(30)),
                      contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 14),
                    ),
                    maxLines: null,
                    onSubmitted: (_) => _sendMessage(),
                  ),
                ),
                const SizedBox(width: 8),
                FloatingActionButton(
                  mini: true,
                  onPressed: isLoading ? null : _sendMessage,
                  child: const Icon(Icons.send),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}