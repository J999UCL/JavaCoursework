// Note.java
package uk.ac.ucl.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import uk.ac.ucl.model.IndexEntry;

public class Note  {
    private int index;
    private String title;
    private String content;
    private String category;
    private String imageUrl; // Optional
    private final String createdAt;

    public Note(int index, String title, String content, String category, String imageUrl, String url, String createdAt) {
        this.index = index;
        this.title = title;
        this.content = content;
        this.category = category;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
    }

    public Note() {
        LocalDateTime createdAt = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        this.createdAt = createdAt.format(formatter);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    // Note.java
    public static List<Note> loadNotesFromFile(String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(filePath);
        if (file.exists() && file.length() > 0) {
            CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, Note.class);
            return objectMapper.readValue(file, listType);
        } else {
            return new ArrayList<>();
        }
    }

    public static void saveNotesToFile(List<Note> notes, String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        if (notes == null || notes.isEmpty()) {
            objectMapper.writeValue(new File(filePath), new ArrayList<Note>());
        } else {
            objectMapper.writeValue(new File(filePath), notes);
        }
    }
}