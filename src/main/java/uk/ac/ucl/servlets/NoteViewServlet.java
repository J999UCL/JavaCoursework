package uk.ac.ucl.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import uk.ac.ucl.model.FileStorageManager;
import uk.ac.ucl.model.IndexEntry;
import uk.ac.ucl.model.Note;

import java.io.IOException;
import java.util.List;

import static uk.ac.ucl.model.Utils.getCategoryHierarchy;

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

        String categoryPathParam = request.getParameter("categoryPath");
        if (categoryPathParam == null){categoryPathParam = "0";}

        int noteId = 0;
        try {
            noteId = Integer.parseInt(request.getParameter("Id"));
        } catch (NumberFormatException e) {
            throw new RuntimeException("noteId parsing gone wrong",e);
        }

        IndexEntry currentCategory = getCategoryHierarchy(fileStorageManager, categoryPathParam).getLast();
        List<IndexEntry> children = currentCategory.getChildren();

        int finalNoteId = noteId;
        Note note = (Note) children.stream()
                .filter(entry -> entry.getId() == finalNoteId)
                .findAny()
                .orElseThrow(() -> new ServletException("Unable to find note."));

        request.setAttribute("note", note);
        request.setAttribute("categoryPath", categoryPathParam);
        request.getRequestDispatcher("/noteView.jsp").forward(request, response);


    }
}