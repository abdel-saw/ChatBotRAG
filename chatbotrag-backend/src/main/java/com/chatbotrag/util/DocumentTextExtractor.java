package com.chatbotrag.util;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class DocumentTextExtractor {

    /**
     * Extrait le texte de n'importe quel document supporté par Tika (PDF, DOCX, etc.)
     * @param inputStream le flux du fichier
     * @return le texte extrait
     * @throws Exception si erreur d'extraction
     */
    public String extractText(InputStream inputStream) throws Exception {
        // -1 = pas de limite de caractères (important pour documents longs)
        BodyContentHandler handler = new BodyContentHandler(-1);
        AutoDetectParser parser = new AutoDetectParser();
        Metadata metadata = new Metadata();
        ParseContext context = new ParseContext();

        parser.parse(inputStream, handler, metadata, context);

        return handler.toString().trim();
    }
}