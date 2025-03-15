package uk.ac.ucl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import uk.ac.ucl.model.FileStorageManager;
import uk.ac.ucl.model.Note;

public class CategoryIndex implements IndexEntry{
    private final String name;
    private List<Integer> noteIds = new ArrayList<>();
    private List<CategoryIndex> subCategories = new ArrayList<>();

    private final LocalDateTime createdAt;

    public CategoryIndex(String name) {
        this.name = name;
        this.createdAt = LocalDateTime.now();
    }

    @JsonCreator
    public CategoryIndex(@JsonProperty("name") String name,
                         @JsonProperty("noteIds") List<Integer> noteIds,
                         @JsonProperty("subCategories") List<CategoryIndex> subCategories,
                         @JsonProperty("createdAt") LocalDateTime createdAt) {
        this.name = name;
        this.noteIds = (noteIds != null) ? noteIds : new ArrayList<>();
        this.subCategories = (subCategories != null) ? subCategories : new ArrayList<>();
        this.createdAt = createdAt;
    }
    public String getName() {
        return name;
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
        List<IndexEntry> children = new ArrayList<IndexEntry>(subCategories);
        List<Note> allNotes = new ArrayList<>();

        try {
            allNotes = fileStorageManager.loadNotes();
        } catch (Exception ex) {
            throw new ServletException("Unable to load notes.", ex);
        }
        children.addAll(allNotes);
        return children;
    }

    public List<Integer> getNoteIds() {
        return noteIds;
    }

    public void setNoteIds(List<Integer> noteIds) {
        this.noteIds = noteIds;
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
    public void addNoteId(int noteId) {
        if (!noteIds.contains(noteId)) {
            noteIds.add(noteId);
        }
    }

    // Method to delete a note ID
    public void deleteNoteId(int noteId) {
        noteIds.remove(Integer.valueOf(noteId));
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
