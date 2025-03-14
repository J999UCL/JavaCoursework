package uk.ac.ucl.servlets;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import uk.ac.ucl.model.Note;
import uk.ac.ucl.model.NoteFactory;

import java.io.IOException;
import java.util.List;

@WebServlet("/viewNote")
public class ViewNoteServlet extends HttpServlet {
    public void doGet (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String filePath = getServletContext().getRealPath("/data/notes.json");
        List<Note> notes = NoteFactory.loadNotes(filePath);

        String indexParam = request.getParameter("index");
        int index = Integer.parseInt(indexParam);

        Note selectedNote = notes.get(index-1);

        request.setAttribute("note", selectedNote);

        ServletContext context = getServletContext();
        RequestDispatcher dispatch = context.getRequestDispatcher("/NoteView.jsp");
        dispatch.forward(request, response);
    }
}
