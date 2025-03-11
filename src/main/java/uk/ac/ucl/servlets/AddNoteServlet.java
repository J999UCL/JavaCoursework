// AddNoteServlet.java
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

@WebServlet("/addNote")
public class AddNoteServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String title = request.getParameter("title");
        String content = request.getParameter("content");
        String category = request.getParameter("category");
        String imageUrl = request.getParameter("imageUrl");
        String url = request.getParameter("url");

        String filePath = getServletContext().getRealPath("/data/notes.json");
        List<Note> notes = NoteFactory.loadNotes(filePath);
        int index = notes.size() + 1;

        Note newNote = NoteFactory.createNote(index, title, content, category, imageUrl, url);
        NoteFactory.addNoteToFile(newNote, filePath);

        response.sendRedirect("viewNotes");
    }
}