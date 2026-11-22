import 'package:chatbotrag/shared/services/auth_service.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  setUp(() => SharedPreferences.setMockInitialValues({}));

  test('login persists the session and logout removes it', () async {
    final auth = AuthService();
    addTearDown(auth.dispose);
    await auth.login('fixture-token', 'fixture@example.test');
    final prefs = await SharedPreferences.getInstance();
    expect(auth.isLoggedIn, isTrue);
    expect(prefs.getString('token'), 'fixture-token');
    expect(prefs.getString('email'), 'fixture@example.test');

    await auth.logout();

    expect(auth.isLoggedIn, isFalse);
    expect(auth.userEmail, isNull);
    expect(prefs.containsKey('token'), isFalse);
    expect(prefs.containsKey('email'), isFalse);
  });

  test('a new service restores a saved session', () async {
    SharedPreferences.setMockInitialValues({
      'token': 'fixture-saved-token', 'email': 'saved@example.test',
    });
    final auth = AuthService();
    addTearDown(auth.dispose);
    await auth.loadFromStorage();
    expect(auth.token, 'fixture-saved-token');
    expect(auth.userEmail, 'saved@example.test');
    expect(auth.isLoggedIn, isTrue);
  });

  test('empty storage keeps the user signed out', () async {
    final auth = AuthService();
    addTearDown(auth.dispose);
    await auth.loadFromStorage();
    expect(auth.isLoggedIn, isFalse);
    expect(auth.token, isNull);
  });
}
