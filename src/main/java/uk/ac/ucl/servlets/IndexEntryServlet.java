package uk.ac.ucl.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import uk.ac.ucl.model.CategoryIndex;
import uk.ac.ucl.model.FileStorageManager;
import uk.ac.ucl.model.IndexEntry;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import static uk.ac.ucl.model.Utils.getCategoryHierarchy;

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
        String categoryPathParam = request.getParameter("categoryPath");
        if (categoryPathParam == null){categoryPathParam = "0";}

        List<CategoryIndex> categoryHierarchy = getCategoryHierarchy(fileStorageManager, categoryPathParam);
        List<IndexEntry> entries = categoryHierarchy.getLast().getChildren();

        // 5. Check for a "sortBy" parameter and sort the notes accordingly.
        String sortBy = request.getParameter("sortBy");
        if (sortBy != null && !sortBy.isEmpty()) {
            applySorting(entries, sortBy);
        }

        // 6. Place the data into request attributes for the JSP.
        request.setAttribute("categoryPath", categoryPathParam);
        request.setAttribute("categoryHierarchy", categoryHierarchy);
        request.setAttribute("Entries", entries);

        // 7. Forward to the JSP page (e.g., notesMain.jsp) for rendering.
        request.getRequestDispatcher("/index.jsp").forward(request, response);
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