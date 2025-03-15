package uk.ac.ucl.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import uk.ac.ucl.model.FileStorageManager;
import uk.ac.ucl.model.Note;

import java.io.IOException;
import java.util.List;

@WebServlet("/note/view/*")
public class NoteViewServlet extends HttpServlet {

    private FileStorageManager fileStorageManager;

    @Override
    public void init() throws ServletException {
        super.init();
        fileStorageManager = new FileStorageManager();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // URL pattern: /note/view/category1/category2/123
        // Extract the path info
        String pathInfo = request.getPathInfo(); // e.g., "/category1/category2/123"
        if (pathInfo == null || pathInfo.equals("/")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No note specified.");
            return;
        }

        // Split the path by "/" to get its segments.
        String[] parts = pathInfo.split("/");
        if (parts.length < 2) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid note path.");
            return;
        }

        // The last segment should be the note ID.
        String noteIdStr = parts[parts.length - 1];
        int noteId;
        try {
            noteId = Integer.parseInt(noteIdStr);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid note ID.");
            return;
        }

        // Load all notes from storage.
        List<Note> allNotes;
        try {
            allNotes = fileStorageManager.loadNotes();
        } catch (Exception ex) {
            throw new ServletException("Unable to load notes.", ex);
        }

        // Find the note with the matching ID.
        Note foundNote = null;
        for (Note note : allNotes) {
            if (note.getId() == noteId) {
                foundNote = note;
                break;
            }
        }

        if (foundNote == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Note not found.");
            return;
        }

        // Optionally, build a category path string for breadcrumb/back navigation.
        // All segments except the first empty string and the last note ID.
        StringBuilder categoryPath = new StringBuilder();
        for (int i = 1; i < parts.length - 1; i++) {
            categoryPath.append("/").append(parts[i]);
        }

        // Set attributes to be used in the JSP.
        request.setAttribute("note", foundNote);
        request.setAttribute("categoryPath", categoryPath.toString());

        // Forward to noteView.jsp for rendering the full note.
        request.getRequestDispatcher("/noteView.jsp").forward(request, response);
    }
}