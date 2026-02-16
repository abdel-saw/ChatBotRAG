import 'package:chatbotrag/features/chat/presentation/screens/chat_detail_screen.dart';
import 'package:chatbotrag/features/chat/presentation/screens/conversations_list_screen.dart';
import 'package:chatbotrag/features/document/presentation/screens/upload_screen.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:chatbotrag/features/auth/presentation/screens/login_screen.dart';
import 'package:chatbotrag/features/auth/presentation/screens/register_screen.dart';
import 'package:chatbotrag/features/home/presentation/screens/home_screen.dart';
import 'package:chatbotrag/features/document/presentation/screens/documents_screen.dart';
import 'package:chatbotrag/shared/services/auth_service.dart';

final goRouterProvider = Provider<GoRouter>((ref) {
  final authService = ref.watch(authServiceProvider);

  return GoRouter(
    initialLocation: '/login',
    refreshListenable: authService,
    redirect: (context, state) {
      final isLoggedIn = authService.isLoggedIn;
      final isAuthRoute =
          state.matchedLocation == '/login' || state.matchedLocation == '/register';

      if (!isLoggedIn && !isAuthRoute) return '/login';
      if (isLoggedIn && isAuthRoute) return '/home';

      return null;
    },
    routes: [
      GoRoute(
        path: '/login',
        builder: (context, state) => const LoginScreen(),
      ),
      GoRoute(
        path: '/register',
        builder: (context, state) => const RegisterScreen(),
      ),
      GoRoute(
        path: '/home',
        builder: (context, state) => const HomeScreen(),
      ),

      GoRoute(
        path: '/documents',
        builder: (context, state) => const DocumentsScreen(),
      ),

      GoRoute(
        path: '/chat/conversations',
        builder: (context, state) => const ConversationsListScreen(),
      ),
      GoRoute(
        path: '/chat/:id',
        builder: (context, state) {
          final param = state.pathParameters['id'];
          final convId = (param != null && param != 'new') 
              ? int.tryParse(param) 
              : null;
          return ChatDetailScreen(conversationId: convId);
        },
      ),
      GoRoute(
        path: '/chat/new',  // Pour démarrer une nouvelle conversation
        builder: (context, state) => const ChatDetailScreen(conversationId: null),
     ),

      GoRoute(
        path: '/upload',
        builder: (context, state) => const UploadScreen(),
      ),
      // Autres routes (documents, chat) seront ajoutées plus tard
    ],
  );
});