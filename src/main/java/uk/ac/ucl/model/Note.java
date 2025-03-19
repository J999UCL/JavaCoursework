// Note.java
package uk.ac.ucl.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.CollectionType;
import jakarta.servlet.ServletException;

public class Note implements IndexEntry {

    private final int id;
    private String title;
    private List<Block> contentBlocks;

    // Configure the date format for JSON output
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public Note(String title, List<Block> contentBlocks){
        this.id = PersistentIdGenerator.getNextId();
        this.title = title;
        this.contentBlocks = contentBlocks;
        this.createdAt = LocalDateTime.now();

    }

    // Parameterized constructor
    @JsonCreator
    public Note(@JsonProperty("id") int id,
                @JsonProperty("title") String title,
                @JsonProperty("contentBlocks") List<Block> contentBlocks,
                @JsonProperty("createdAt") LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.contentBlocks = contentBlocks;
        this.createdAt = createdAt;
    }

    // Getters and setters

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Block> getContentBlocks() {
        return contentBlocks;
    }

    public void setContentBlocks(List<Block> contentBlocks) {
        this.contentBlocks = contentBlocks;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Function to add a text content block
    public void addTextContentBlock(String text) {
        int blockId = contentBlocks.size() + 1;
        Block textBlock = new Block(blockId, "text", text);
        contentBlocks.add(textBlock);
    }

    public void addImageContentBlock(String imageUrl) {
        int blockId = contentBlocks.size() + 1;
        Block imageBlock = new Block(blockId, "image", imageUrl);
        contentBlocks.add(imageBlock);
    }

    // Function to delete a content block
    public void deleteContentBlockById(int blockId) {
        contentBlocks.removeIf(block -> block.getId() == blockId);
    }


    @Override
    @JsonIgnore
    public String getName() {
        return title;
    }

    @Override
    @JsonIgnore
    public LocalDateTime get_time() {
        return createdAt;
    }

    @Override
    public List<IndexEntry> getChildren() throws ServletException {
        return IndexEntry.super.getChildren();
    }

    /**
     * Converts this Note into a JSON object (an ObjectNode) using Jackson.
     * The resulting JSON includes fields for type, id, title, createdAt, and contentBlocks.
     *
     * @return an ObjectNode representing this Note.
     */
    public ObjectNode toJson() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode noteJson = mapper.createObjectNode();

        noteJson.put("type", "note");              // Helps during deserialization.
        noteJson.put("id", id);
        noteJson.put("title", title);
        noteJson.put("createdAt", createdAt.toString());

        ArrayNode blocksArray = mapper.createArrayNode();
        if (contentBlocks != null) {
            for (Block block : contentBlocks) {
                blocksArray.add(block.toJson());
            }
        }
        noteJson.set("contentBlocks", blocksArray);

        return noteJson;
    }

    // Note typically doesn't have children, so the default getChildren() returning an empty list is sufficient.

    @Override
    public String toString() {
        return "Note{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", contentBlocks=" + contentBlocks +
                ", createdAt=" + createdAt.toString()+
                '}';
    }
}