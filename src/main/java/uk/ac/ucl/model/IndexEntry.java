package uk.ac.ucl.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.ServletException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public interface IndexEntry {
    /**
     * Returns the display name of the entry (e.g., a note's title or a category name).
     *
     * @return the name of the index entry.
     */
    int getId();
    String getName();
    LocalDateTime get_time();

    /**
     * Returns a list of child entries if this entry is composite (e.g., a category with subcategories or notes).
     * For entries that do not contain children (like a Note), this method can return an empty list.
     *
     * @return a list of child IndexEntry objects, or an empty list if none exist.
     */
    default List<IndexEntry> getChildren() throws ServletException {
        return new ArrayList<>();
    }


}
