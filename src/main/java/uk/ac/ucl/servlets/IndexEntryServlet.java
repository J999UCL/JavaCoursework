package uk.ac.ucl.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import uk.ac.ucl.model.CategoryIndex;
import uk.ac.ucl.model.FileStorageManager;
import uk.ac.ucl.model.IndexEntry;
import uk.ac.ucl.model.Note;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@WebServlet("")
public class IndexEntryServlet extends HttpServlet {

    private FileStorageManager fileStorageManager;

    @Override
    public void init() throws ServletException {
        super.init();
        fileStorageManager = new FileStorageManager();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 1. Parse the URL path to determine the category path.
        String[] categoryPathParams = request.getParameterValues("categoryPath");
        List<String> categoryPath = categoryPathParams != null ? List.of(categoryPathParams) : new ArrayList<>();

        // 2. Load all notes and the category hierarchy.
        List<Note> allNotes;
        CategoryIndex rootCategory;
        try {
            allNotes = fileStorageManager.loadNotes();
            rootCategory = fileStorageManager.loadCategories();
        } catch (Exception e) {
            throw new ServletException("Failed to load data", e);
        }

        // 3. Find the current category based on the URL path.
        IndexEntry currentCategory = findCategoryByPath(rootCategory, categoryPath);
        if (currentCategory == null) {
            // If not found, you might show a message or default to the root.
            currentCategory = new CategoryIndex("No such category");
        }

        // 4. Convert the note IDs in the current category into actual Note objects.
        List<IndexEntry> entries = currentCategory.getChildren();

        // 5. Check for a "sortBy" parameter and sort the notes accordingly.
        String sortBy = request.getParameter("sortBy");
        if (sortBy != null && !sortBy.isEmpty()) {
            applySorting(entries, sortBy);
        }

        // 6. Place the data into request attributes for the JSP.

        request.setAttribute("categoryPath", categoryPath);
        request.setAttribute("Entries", entries);

        // 7. Forward to the JSP page (e.g., notesMain.jsp) for rendering.
        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }

    /**
     * Recursively finds the CategoryIndex corresponding to the given category path.
     */
    private IndexEntry findCategoryByPath(CategoryIndex root, List<String> categoryPath) throws ServletException {
        if (categoryPath.isEmpty()) {
            return root;
        }
        IndexEntry current = root;
        for (String catName : categoryPath) {
            boolean found = false;
            for (IndexEntry sub : current.getChildren()) {
                if (sub.getName().equalsIgnoreCase(catName)) {
                    current = sub;
                    found = true;
                    break;
                }
            }
            if (!found) {
                return null;
            }
        }
        return current;
    }

    /**
     * Converts a list of note IDs to the corresponding Note objects by looking them up in allNotes.
     */
    private List<Note> convertNoteIdsToNotes(List<Integer> noteIds, List<Note> allNotes) {
        List<Note> result = new ArrayList<>();
        if (noteIds != null) {
            for (Integer id : noteIds) {
                for (Note n : allNotes) {
                    if (n.getId() == id) {
                        result.add(n);
                        break;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Sorts the list of notes according to the sortBy parameter.
     * Supported values: "title", "createdAt".
     */
    private void applySorting(List<IndexEntry> entry, String sortBy) {
        switch (sortBy) {
            case "title":
                entry.sort(Comparator.comparing(IndexEntry::getName, String.CASE_INSENSITIVE_ORDER));
                break;
            case "createdAt":
                entry.sort(Comparator.comparing(IndexEntry::get_time));
                break;
            default:
                // No sorting or add additional sorting criteria as needed.
                break;
        }
    }
}