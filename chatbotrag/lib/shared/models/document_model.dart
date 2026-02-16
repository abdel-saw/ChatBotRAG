class DocumentModel {
  final int id;
  final String title;
  final DateTime uploadedAt;

  DocumentModel({
    required this.id,
    required this.title,
    required this.uploadedAt,
  });

  factory DocumentModel.fromJson(Map<String, dynamic> json) {
    return DocumentModel(
      id: json['id'],
      title: json['title'],
      uploadedAt: DateTime.parse(json['uploadedAt']),
    );
  }
}