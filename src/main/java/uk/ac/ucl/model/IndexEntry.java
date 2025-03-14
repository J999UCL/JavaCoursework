package uk.ac.ucl.model;

import java.util.List;
import java.util.Collections;

public interface IndexEntry {
    /**
     * Returns the display name of the entry (e.g., a note's title or a category name).
     *
     * @return the name of the index entry.
     */
    String getName();

    /**
     * Returns a list of child entries if this entry is composite (e.g., a category with subcategories or notes).
     * For entries that do not contain children (like a Note), this method can return an empty list.
     *
     * @return a list of child IndexEntry objects, or an empty list if none exist.
     */
    default List<IndexEntry> getChildren() {
        return Collections.emptyList();
    }
}
