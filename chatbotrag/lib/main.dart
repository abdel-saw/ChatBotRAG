import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:chatbotrag/core/theme/app_theme.dart';
import 'package:chatbotrag/core/router/app_router.dart';

void main() {
  runApp(const ProviderScope(child: CareerForgeApp()));
}

class CareerForgeApp extends ConsumerWidget {
  const CareerForgeApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final router = ref.watch(goRouterProvider);

    return MaterialApp.router(
      title: 'ChatBotRAG',
      theme: AppTheme.lightTheme,
      darkTheme: AppTheme.darkTheme,
      themeMode: ThemeMode.system,
      debugShowCheckedModeBanner: false,
      routerConfig: router,
    );
  }
}