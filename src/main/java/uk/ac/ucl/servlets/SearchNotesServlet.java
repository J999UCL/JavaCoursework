// SearchNotesServlet.java
package uk.ac.ucl.servlets;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import uk.ac.ucl.model.Note;
import uk.ac.ucl.model.NoteFactory;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/searchNotes")
public class SearchNotesServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String query = request.getParameter("query");

        String filePath = getServletContext().getRealPath("/data/notes.json");
        List<Note> notes = NoteFactory.loadNotes(filePath);
        List<Note> searchResults = notes.stream()
                .filter(note -> note.getTitle().contains(query) || note.getContent().contains(query))
                .collect(Collectors.toList());

        request.setAttribute("searchResults", searchResults);

        ServletContext context = getServletContext();
        RequestDispatcher dispatch = context.getRequestDispatcher("/searchResults.jsp");
        dispatch.forward(request, response);
    }
}