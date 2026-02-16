import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:chatbotrag/shared/services/auth_service.dart';

class ApiService {
  late final Dio dio;
  final AuthService authService;

  ApiService(this.authService) {
    dio = Dio(BaseOptions(
      baseUrl: 'http://192.168.11.110:8080',     // Pour émulateur Android
      // baseUrl: 'http://localhost:8080',       // Pour Web / iOS Simulator
      connectTimeout: const Duration(seconds: 30),
      receiveTimeout: const Duration(seconds: 360),
      headers: {'Content-Type': 'application/json'},
    ));

    // === INTERCEPTOR JWT AUTOMATIQUE ===
    dio.interceptors.add(InterceptorsWrapper(
      onRequest: (options, handler) {
        final token = authService.token;
        if (token != null) {
          options.headers['Authorization'] = 'Bearer $token';
        }
        return handler.next(options);
      },
      onError: (DioException e, handler) async {
        // Si token expiré ou invalide → déconnexion automatique
        if (e.response?.statusCode == 401) {
          await authService.logout();
        }
        return handler.next(e);
      },
    ));
  }

  // Méthodes HTTP utiles
  Future<Response> get(String path) => dio.get(path);
  Future<Response> post(String path, {dynamic data}) => dio.post(path, data: data);
  Future<Response> put(String path, {dynamic data}) => dio.put(path, data: data);
  Future<Response> delete(String path) => dio.delete(path);
}

// Provider
final apiServiceProvider = Provider<ApiService>((ref) {
  final authService = ref.watch(authServiceProvider);
  return ApiService(authService);
});