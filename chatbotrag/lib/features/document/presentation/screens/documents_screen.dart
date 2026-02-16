import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:chatbotrag/shared/models/document_model.dart';
import 'package:chatbotrag/shared/services/api_service.dart';
import 'package:file_picker/file_picker.dart';
import 'package:dio/dio.dart';

class DocumentsScreen extends ConsumerStatefulWidget {
  const DocumentsScreen({super.key});

  @override
  ConsumerState<DocumentsScreen> createState() => _DocumentsScreenState();
}

class _DocumentsScreenState extends ConsumerState<DocumentsScreen> {
  List<DocumentModel> documents = [];
  bool isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadDocuments();
  }

  Future<void> _loadDocuments() async {
    setState(() => isLoading = true);
    try {
      final api = ref.read(apiServiceProvider);
      final response = await api.get('/api/documents');
      final List<dynamic> data = response.data;
      documents = data.map((e) => DocumentModel.fromJson(e)).toList();
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text("Erreur : $e")),
      );
    } finally {
      setState(() => isLoading = false);
    }
  }

  Future<void> _uploadDocument() async {
    final result = await FilePicker.platform.pickFiles(
      allowedExtensions: ['pdf', 'txt', 'docx'],
    );

    if (result == null) return;

    setState(() => isLoading = true);

    try {
      final file = result.files.first;
      final formData = FormData.fromMap({
        'file': await MultipartFile.fromFile(file.path!, filename: file.name),
        'title': file.name,
      });

      final api = ref.read(apiServiceProvider);
      await api.post('/api/documents/upload', data: formData);

      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text("Document uploadé avec succès !")),
      );

      _loadDocuments(); // Refresh list
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text("Erreur d'upload : $e")),
      );
    } finally {
      setState(() => isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Mes Documents"),
        actions: [
          IconButton(
            icon: const Icon(Icons.add),
            onPressed: _uploadDocument,
          ),
        ],
      ),
      body: isLoading
          ? const Center(child: CircularProgressIndicator())
          : documents.isEmpty
              ? const Center(
                  child: Text(
                    "Aucun document\nAppuyez sur + pour commencer",
                    textAlign: TextAlign.center,
                  ),
                )
              : ListView.builder(
                  padding: const EdgeInsets.all(16),
                  itemCount: documents.length,
                  itemBuilder: (context, index) {
                    final doc = documents[index];
                    return Dismissible(
                      key: Key(doc.id.toString()),
                      direction: DismissDirection.endToStart,
                      background: Container(
                        color: Colors.red,
                        alignment: Alignment.centerRight,
                        padding: const EdgeInsets.only(right: 20),
                        child: const Icon(Icons.delete, color: Colors.white, size: 30),
                      ),
                      onDismissed: (direction) async {
                        try {
                          final api = ref.read(apiServiceProvider);
                          await api.delete('/api/documents/${doc.id}');

                          setState(() {
                            documents.removeAt(index);
                          });

                          ScaffoldMessenger.of(context).showSnackBar(
                            const SnackBar(content: Text("Document supprimé")),
                          );
                        } catch (e) {
                          ScaffoldMessenger.of(context).showSnackBar(
                            SnackBar(content: Text("Erreur suppression : $e")),
                          );
                        }
                      },
                      child: Card(
                        margin: const EdgeInsets.only(bottom: 12),
                        child: ListTile(
                          leading: const Icon(Icons.description, size: 40, color: Colors.indigo),
                          title: Text(doc.title, style: const TextStyle(fontWeight: FontWeight.bold)),
                          trailing: Text(
                            "${doc.uploadedAt.day}/${doc.uploadedAt.month}/${doc.uploadedAt.year}",
                            style: const TextStyle(fontSize: 12),
                          ),
                        ),
                      ),
                    );
                  },
                ),
    );
  }
}