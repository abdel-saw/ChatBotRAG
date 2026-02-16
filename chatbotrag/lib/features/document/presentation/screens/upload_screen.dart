import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:file_picker/file_picker.dart';
import 'package:dio/dio.dart';
import 'package:chatbotrag/shared/services/api_service.dart';

class UploadScreen extends ConsumerStatefulWidget {
  const UploadScreen({super.key});

  @override
  ConsumerState<UploadScreen> createState() => _UploadScreenState();
}

class _UploadScreenState extends ConsumerState<UploadScreen> {
  FilePickerResult? result;
  final TextEditingController titleController = TextEditingController();
  bool isLoading = false;

  Future<void> _pickFile() async {
    final picked = await FilePicker.platform.pickFiles(
      type: FileType.custom,
      allowedExtensions: ['pdf', 'txt', 'docx'],  // ← Ajout TXT + DOCX
    );

    if (picked != null) {
      setState(() {
        result = picked;
        if (titleController.text.isEmpty) {
          titleController.text = picked.files.first.name;
        }
      });
    }
  }

  Future<void> _upload() async {
    if (result == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text("Sélectionnez un fichier")),
      );
      return;
    }

    setState(() => isLoading = true);

    try {
      final file = result!.files.first;
      final formData = FormData.fromMap({
        'file': await MultipartFile.fromFile(file.path!, filename: file.name),
        'title': titleController.text.isEmpty ? file.name : titleController.text,
      });

      final api = ref.read(apiServiceProvider);
      await api.post('/api/documents/upload', data: formData);

      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text("Document uploadé avec succès !")),
      );

      if (mounted) context.pop(); // Retour
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
      appBar: AppBar(title: const Text("Uploader un document")),
      body: Padding(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          children: [
            Card(
              child: ListTile(
                leading: const Icon(Icons.upload_file),
                title: Text(result == null ? "Aucun fichier sélectionné" : result!.files.first.name),
                subtitle: const Text("PDF, TXT ou DOCX acceptés"),
                trailing: ElevatedButton(
                  onPressed: _pickFile,
                  child: const Text("Choisir"),
                ),
              ),
            ),
            const SizedBox(height: 24),
            TextField(
              controller: titleController,
              decoration: const InputDecoration(
                labelText: "Titre (optionnel)",
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 40),
            SizedBox(
              width: double.infinity,
              height: 56,
              child: ElevatedButton(
                onPressed: isLoading ? null : _upload,
                child: isLoading
                    ? const CircularProgressIndicator(color: Colors.white)
                    : const Text("Uploader", style: TextStyle(fontSize: 18)),
              ),
            ),
          ],
        ),
      ),
    );
  }
}