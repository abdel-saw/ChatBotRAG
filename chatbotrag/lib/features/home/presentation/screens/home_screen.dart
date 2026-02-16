import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:chatbotrag/shared/services/auth_service.dart';

class HomeScreen extends ConsumerWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final authService = ref.watch(authServiceProvider);
    final userName = authService.userEmail?.split('@')[0] ?? "Utilisateur";

    return Scaffold(
      appBar: AppBar(
        title: const Text("ChatBot RAG"),
        actions: [
          IconButton(
            icon: const Icon(Icons.logout),
            onPressed: () async {
              await ref.read(authServiceProvider).logout();
              if (context.mounted) context.go('/login');
            },
          ),
        ],
      ),
      body: Padding(
        padding: const EdgeInsets.all(20.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              "Bonjour, $userName 👋",
              style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
            ),
            const SizedBox(height: 8),
            const Text(
              "Que souhaitez-vous faire aujourd'hui ?",
              style: TextStyle(fontSize: 16, color: Colors.grey),
            ),
            const SizedBox(height: 40),

            // Cartes d'actions
            GridView.count(
              shrinkWrap: true,
              crossAxisCount: 2,
              mainAxisSpacing: 16,
              crossAxisSpacing: 16,
              childAspectRatio: 1.1,
              children: [
                _buildActionCard(
                  context,
                  icon: Icons.upload_file,
                  title: "Upload document",
                  subtitle: "Ajouter un document",
                  color: Colors.indigo,
                  onTap: () => context.push('/upload'), // à créer plus tard
                ),
                _buildActionCard(
                  context,
                  icon: Icons.chat_bubble_outline,
                  title: "Discuter avec l'IA",
                  subtitle: "Nouvelle conversation",
                  color: Colors.deepPurple,
                  onTap: () => context.push('/chat/new'),
                ),
                _buildActionCard(
                  context,
                  icon: Icons.description_outlined,
                  title: "Mes documents",
                  subtitle: "Voir et gérer",
                  color: Colors.teal,
                  onTap: () => context.push('/documents'),
                ),
                _buildActionCard(
                  context,
                  icon: Icons.history,
                  title: "Mes conversations",
                  subtitle: "Historique",
                  color: Colors.orange,
                  onTap: () => context.push('/chat/conversations'),
                ),
              ],
            ),

            const Spacer(),

            Center(
              child: Text(
                "Backend connecté • Ollama (local)",
                style: TextStyle(color: Colors.green.shade600, fontSize: 12),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildActionCard(
    BuildContext context, {
    required IconData icon,
    required String title,
    required String subtitle,
    required Color color,
    required VoidCallback onTap,
  }) {
    return Card(
      elevation: 3,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(16),
        child: Container(
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(16),
          ),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(icon, size: 40, color: color),
              const SizedBox(height: 12),
              Text(
                title,
                style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 4),
              Text(
                subtitle,
                style: TextStyle(fontSize: 12, color: Colors.grey.shade600),
                textAlign: TextAlign.center,
              ),
            ],
          ),
        ),
      ),
    );
  }
}