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

@WebServlet("/modifyNote")
public class ModifyNoteServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

//        RequestDispatcher dispatcher = request.getRequestDispatcher("/noteadd.jsp");
//        dispatcher.forward(request, response);

        String indexParam = request.getParameter("index");
        boolean isEdit = (indexParam != null && !indexParam.isEmpty());
        int index = isEdit ? Integer.parseInt(indexParam) : -1;

        System.out.println("Raw index parameter: " + request.getParameter("index"));
        System.out.println("Full Query String: " + request.getQueryString());

        String title = request.getParameter("title");
        String content = request.getParameter("content");
        String category = request.getParameter("category");
        String imageUrl = request.getParameter("imageUrl");
        String Url = request.getParameter("Url");

        String filePath = getServletContext().getRealPath("/data/notes.json");
        List<Note> notes = NoteFactory.loadNotes(filePath);

        if (isEdit) {
            editNote(notes, index, title, content, category, imageUrl);
        } else {
            addNote(notes, title, content, category, imageUrl, Url);
        }


        NoteFactory.saveNotes(notes, filePath);
        response.sendRedirect(request.getContextPath() + "/");
    }

    private void addNote(List<Note> notes, String title, String content, String category, String imageUrl, String url) throws IOException {
        int index = notes.size() + 1;
        Note newNote = NoteFactory.createNote(index, title, content, category, imageUrl, url);
        notes.add(newNote);
//        String filePath = getServletContext().getRealPath("/data/notes.json");
//        NoteFactory.addNoteToFile(newNote, filePath);

    }

    private void editNote(List<Note> notes, int index, String title, String content, String category, String imageUrl) {
        Note note = notes.get(index-1);
        note.setTitle(title);
        note.setContent(content);
        note.setCategory(category);
        note.setImageUrl(imageUrl);
    }
}