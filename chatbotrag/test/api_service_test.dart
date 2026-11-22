import 'dart:typed_data';
import 'package:chatbotrag/shared/services/api_service.dart';
import 'package:chatbotrag/shared/services/auth_service.dart';
import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

class FixtureAdapter implements HttpClientAdapter {
  final int status;
  RequestOptions? request;
  FixtureAdapter(this.status);

  @override
  Future<ResponseBody> fetch(RequestOptions options, Stream<Uint8List>? stream,
      Future<void>? cancelFuture) async {
    request = options;
    return ResponseBody.fromString('{"ok":true}', status,
        headers: {Headers.contentTypeHeader: [Headers.jsonContentType]});
  }

  @override
  void close({bool force = false}) {}
}

void main() {
  late AuthService auth;
  late ApiService api;
  setUp(() {
    SharedPreferences.setMockInitialValues({});
    auth = AuthService();
    api = ApiService(auth);
  });
  tearDown(() { api.dio.close(force: true); auth.dispose(); });

  test('uses compiled API_BASE_URL or the Android emulator default', () {
    const expected = String.fromEnvironment('API_BASE_URL',
        defaultValue: 'http://10.0.2.2:8080');
    expect(api.dio.options.baseUrl, expected);
  });

  test('requests without a session contain no bearer token', () async {
    final adapter = FixtureAdapter(200);
    api.dio.httpClientAdapter = adapter;
    await api.get('/fixture');
    expect(adapter.request!.headers.containsKey('Authorization'), isFalse);
  });

  test('authenticated requests carry the current bearer token', () async {
    await auth.login('fixture-token', 'fixture@example.test');
    final adapter = FixtureAdapter(200);
    api.dio.httpClientAdapter = adapter;
    await api.get('/fixture');
    expect(adapter.request!.headers['Authorization'], 'Bearer fixture-token');
  });

  test('HTTP 401 clears the persisted session and propagates the error', () async {
    await auth.login('fixture-token', 'fixture@example.test');
    api.dio.httpClientAdapter = FixtureAdapter(401);
    await expectLater(api.get('/fixture'), throwsA(isA<DioException>()));
    expect(auth.isLoggedIn, isFalse);
    final prefs = await SharedPreferences.getInstance();
    expect(prefs.containsKey('token'), isFalse);
  });
}
