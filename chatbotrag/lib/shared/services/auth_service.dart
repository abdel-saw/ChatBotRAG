import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';

class AuthService extends ChangeNotifier {
  String? _token;
  String? _userEmail;

  String? get token => _token;
  String? get userEmail => _userEmail;
  bool get isLoggedIn => _token != null;

  final Ref _ref;

  AuthService(this._ref);

  Future<void> login(String token, String email) async {
    _token = token;
    _userEmail = email;

    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('token', token);
    await prefs.setString('email', email);

    notifyListeners();
  }

  Future<void> logout() async {
    _token = null;
    _userEmail = null;

    final prefs = await SharedPreferences.getInstance();
    await prefs.remove('token');
    await prefs.remove('email');

    notifyListeners();
  }

  Future<void> loadFromStorage() async {
    final prefs = await SharedPreferences.getInstance();
    _token = prefs.getString('token');
    _userEmail = prefs.getString('email');

    if (_token != null) notifyListeners();
  }
}

final authServiceProvider = ChangeNotifierProvider<AuthService>((ref) {
  final authService = AuthService(ref);
  authService.loadFromStorage();
  return authService;
});