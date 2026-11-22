import 'package:chatbotrag/core/router/app_router.dart';
import 'package:chatbotrag/shared/services/auth_service.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  setUp(() => SharedPreferences.setMockInitialValues({}));

  Future<ProviderContainer> mount(WidgetTester tester) async {
    tester.view.physicalSize = const Size(390, 844);
    tester.view.devicePixelRatio = 1;
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);
    final container = ProviderContainer();
    addTearDown(container.dispose);
    await tester.pumpWidget(UncontrolledProviderScope(
      container: container,
      child: MaterialApp.router(routerConfig: container.read(goRouterProvider)),
    ));
    await tester.pumpAndSettle();
    return container;
  }

  testWidgets('signed-out users are redirected away from documents', (tester) async {
    final container = await mount(tester);
    container.read(goRouterProvider).go('/documents');
    await tester.pumpAndSettle();
    expect(find.text('Se connecter'), findsOneWidget);
    expect(container.read(goRouterProvider).routeInformationProvider.value.uri.path, '/login');
  });

  testWidgets('invalid login fields are rejected before an API request', (tester) async {
    await mount(tester);
    await tester.tap(find.text('Se connecter'));
    await tester.pumpAndSettle();
    expect(find.text('Email requis'), findsOneWidget);
    expect(find.text('Minimum 6 caractères'), findsOneWidget);
  });

  testWidgets('the registration form opens and returns to login', (tester) async {
    await mount(tester);
    await tester.tap(find.text("Pas encore de compte ? S'inscrire"));
    await tester.pumpAndSettle();
    expect(find.text('Inscription'), findsOneWidget);
    await tester.tap(find.text("J'ai déjà un compte ? Se connecter"));
    await tester.pumpAndSettle();
    expect(find.text('Se connecter'), findsOneWidget);
  });

  testWidgets('session changes redirect without replacing the router', (tester) async {
    final container = await mount(tester);
    final router = container.read(goRouterProvider);
    await container.read(authServiceProvider).login('fixture-token', 'fixture@example.test');
    await tester.pumpAndSettle();
    expect(container.read(goRouterProvider), same(router));
    expect(router.routeInformationProvider.value.uri.path, '/home');
    await container.read(authServiceProvider).logout();
    await tester.pumpAndSettle();
    expect(router.routeInformationProvider.value.uri.path, '/login');
    expect(find.text('Se connecter'), findsOneWidget);
  });
}
