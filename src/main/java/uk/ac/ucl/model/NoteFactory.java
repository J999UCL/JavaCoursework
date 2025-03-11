// NoteFactory.java
package uk.ac.ucl.model;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class NoteFactory {
    public static Note createNote(int index, String title, String content, String category, String imageUrl, String url) {
        LocalDateTime createdAt = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return new Note(index, title, content, category, imageUrl, url, createdAt.format(formatter));
    }

    public static List<Note> loadNotes(String filePath) throws IOException {
        return Note.loadNotesFromFile(filePath);
    }

    public static void saveNotes(List<Note> notes, String filePath) throws IOException {
        Note.saveNotesToFile(notes, filePath);
    }

    public static void addNoteToFile(Note note, String filePath) throws IOException {
        List<Note> notes = loadNotes(filePath);
        notes.add(note);
        saveNotes(notes, filePath);
    }
}