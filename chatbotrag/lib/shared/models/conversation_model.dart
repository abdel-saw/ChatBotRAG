class ConversationModel {
  final int id;
  final String title;
  final DateTime lastUpdatedAt;
  final int messageCount;

  ConversationModel({
    required this.id,
    required this.title,
    required this.lastUpdatedAt,
    required this.messageCount,
  });

  factory ConversationModel.fromJson(Map<String, dynamic> json) {
    return ConversationModel(
      id: json['id'],
      title: json['title'],
      lastUpdatedAt: DateTime.parse(json['lastUpdatedAt']),
      messageCount: json['messageCount'] ?? 0,
    );
  }
}