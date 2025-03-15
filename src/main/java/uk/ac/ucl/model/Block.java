package uk.ac.ucl.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Block {
    private int id;
    private String type; // "text" or "image"
    private String data; // the actual text content or image location

    public Block() {
    }

    /**
     * Constructs a new Block with the given type and data.
     * @param type the type of block ("text" or "image")
     * @param data the content of the block (text or the image path)
     */
    public Block(int id, String type, String data) {
        this.id = id;
        this.type = type;
        this.data = data;
    }

    // Getters and setters for id, type, and data
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Getter and setter for type
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    // Getter and setter for data
    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    /**
     * Converts this Block into a JSON object (an ObjectNode) using Jackson.
     *
     * @return an ObjectNode representing this Block.
     */
    public ObjectNode toJson() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonNode = mapper.createObjectNode();
        jsonNode.put("type", type);
        jsonNode.put("data", data);
        return jsonNode;
    }

    @Override
    public String toString() {
        return "Block{" +
                "type='" + type + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}
