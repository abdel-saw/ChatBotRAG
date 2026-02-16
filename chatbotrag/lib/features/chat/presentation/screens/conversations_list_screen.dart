import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:chatbotrag/shared/models/conversation_model.dart';
import 'package:chatbotrag/shared/services/api_service.dart';

class ConversationsListScreen extends ConsumerStatefulWidget {
  const ConversationsListScreen({super.key});

  @override
  ConsumerState<ConversationsListScreen> createState() => _ConversationsListScreenState();
}

class _ConversationsListScreenState extends ConsumerState<ConversationsListScreen> {
  List<ConversationModel> conversations = [];
  bool isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadConversations();
  }

  Future<void> _loadConversations() async {
    setState(() => isLoading = true);
    try {
      final api = ref.read(apiServiceProvider);
      final response = await api.get('/api/chat/conversations');
      final List<dynamic> data = response.data;
      conversations = data.map((e) => ConversationModel.fromJson(e)).toList();
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text("Erreur : $e")),
      );
    } finally {
      setState(() => isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Historique des conversations"),
      ),
      body: isLoading
          ? const Center(child: CircularProgressIndicator())
          : conversations.isEmpty
              ? const Center(
                  child: Text("Aucune conversation\nCommencez une nouvelle discussion"),
                )
              : ListView.builder(
                  itemCount: conversations.length,
                  itemBuilder: (context, index) {
                    final convo = conversations[index];
                    return Dismissible(
                      key: Key(convo.id.toString()),
                      direction: DismissDirection.endToStart,
                      background: Container(
                        color: Colors.red,
                        alignment: Alignment.centerRight,
                        padding: const EdgeInsets.only(right: 20),
                        child: const Icon(Icons.delete, color: Colors.white, size: 30),
                      ),
                      onDismissed: (direction) async {
                        try {
                          final api = ref.read(apiServiceProvider);
                          await api.delete('/api/chat/conversation/${convo.id}');

                          setState(() {
                            conversations.removeAt(index);
                          });

                          ScaffoldMessenger.of(context).showSnackBar(
                            const SnackBar(content: Text("Conversation supprimée")),
                          );
                        } catch (e) {
                          ScaffoldMessenger.of(context).showSnackBar(
                            SnackBar(content: Text("Erreur suppression : $e")),
                          );
                        }
                      },
                      child: ListTile(
                        leading: const Icon(Icons.chat_bubble_outline),
                        title: Text(convo.title),
                        subtitle: Text("${convo.messageCount} messages • ${convo.lastUpdatedAt.day}/${convo.lastUpdatedAt.month}"),
                        trailing: const Icon(Icons.arrow_forward_ios, size: 16),
                        onTap: () => context.push('/chat/${convo.id}'),
                      ),
                    );
                  },
                ),
      floatingActionButton: FloatingActionButton(
        child: const Icon(Icons.add),
        onPressed: () => context.push('/chat/new'),
      ),
    );
  }
}