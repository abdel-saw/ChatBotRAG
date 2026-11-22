# Flutter client verification

The client workflow uses Flutter 3.41.0 (Dart 3.11) and Java 17 for Android.
From `chatbotrag`, run:

```bash
flutter pub get
flutter analyze --no-fatal-infos
flutter test
flutter test --dart-define=API_BASE_URL=https://api.example.test test/api_service_test.dart
flutter build web --release --dart-define=API_BASE_URL=https://api.example.test
flutter build apk --debug --dart-define=API_BASE_URL=http://10.0.2.2:8080
```

The test addresses are fixtures, not live services. API tests use a local Dio
adapter; session tests use mocked SharedPreferences. No backend or model calls
are made by these tests.

The suite covers session persistence/restoration, bearer headers, HTTP 401
logout, compiled API configuration, signed-out route protection, login
validation, registration navigation and router stability during session changes.
Widget tests use the real router and forms with a standard Material theme to
avoid downloading Google Fonts.

Static-analysis errors and warnings fail CI. Existing informational style and
deprecation notices remain visible but are not fatal; they are not suppressed.

The web and debug APK builds verify compilation. They do not establish working
file uploads on the web, device permissions, production signing, or a complete
document conversation against Ollama. Android remains the primary local client;
use the Docker setup in the root README to test it on an emulator. This change
does not publish or host the web build.
