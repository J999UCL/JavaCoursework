package uk.ac.ucl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.servlet.ServletException;
import uk.ac.ucl.model.FileStorageManager;
import uk.ac.ucl.model.Note;

public class CategoryIndex implements IndexEntry{
    private final long id;
    private String name;
    private List<Long> noteIds;
    private List<CategoryIndex> subCategories;

    private final LocalDateTime createdAt;

    @JsonCreator
    public CategoryIndex(@JsonProperty("name") String name,
                         @JsonProperty("noteIds") List<Long> noteIds,
                         @JsonProperty("subCategories") List<CategoryIndex> subCategories,
                         @JsonProperty("createdAt") LocalDateTime createdAt,
                         @JsonProperty("id") long id) {
        this.name = name;
        this.noteIds = (noteIds != null) ? noteIds : new ArrayList<>();
        this.subCategories = (subCategories != null) ? subCategories : new ArrayList<>();
        this.createdAt = createdAt;
        this.id = id;
    }

    public CategoryIndex(String name) {
        this.id = Instant.now().toEpochMilli();
        this.name = name;
        this.noteIds =  new ArrayList<>();
        this.subCategories = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
    }


    @Override
    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String setName(String name) {
        return this.name = name;
    }

    @Override
    @JsonIgnore
    public LocalDateTime get_time() {
        return createdAt;
    }

    @Override
    @JsonIgnore
    public List<IndexEntry> getChildren() throws ServletException {
        FileStorageManager fileStorageManager = new FileStorageManager();
        List<IndexEntry> children = new ArrayList<>(subCategories);
        List<Note> allNotes = new ArrayList<>();

        try {
            List<Note> filteredNotes = fileStorageManager.loadNotes().stream()
                    .filter(note -> noteIds.contains(note.getId())).toList();
            children.addAll(filteredNotes);
        } catch (Exception ex) {
            throw new ServletException("Unable to load notes.", ex);
        }
        return children;
    }

    public List<CategoryIndex> getSubCategories() {
        return subCategories;
    }

    public void setSubCategories(List<CategoryIndex> subCategories) {
        this.subCategories = subCategories;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Method to add a note ID
    public void addNoteId(long noteId) {
        if (!noteIds.contains(noteId)) {
            noteIds.add(noteId);
        }
    }

    // Method to delete a note ID
    public void deleteNoteId(long noteId) {
        noteIds.remove(noteId);
    }

    public List<Long> getNoteIds() {
        return noteIds;
    }

    public void setNoteIds(List<Long> noteIds) {
        this.noteIds = noteIds;
    }

    // Method to add a subcategory
    public void addSubCategory(CategoryIndex subCategory) {
        if (!subCategories.contains(subCategory)) {
            subCategories.add(subCategory);
        }
    }

    // Method to delete a subcategory
    public void deleteSubCategory(String CategoryName) {
        subCategories.removeIf(subCategory -> subCategory.getName().equals(CategoryName));
    }

}
