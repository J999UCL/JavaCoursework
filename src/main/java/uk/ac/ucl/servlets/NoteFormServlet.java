package uk.ac.ucl.servlets;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.ac.ucl.model.Block;
import uk.ac.ucl.model.CategoryIndex;
import uk.ac.ucl.model.FileStorageManager;
import uk.ac.ucl.model.Note;
import uk.ac.ucl.model.Utils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static uk.ac.ucl.model.Utils.getEncodedImages;

@WebServlet({"/note/create", "/note/edit"})
@MultipartConfig
public class NoteFormServlet extends HttpServlet {

    private FileStorageManager fileStorageManager;

    @Override
    public void init() throws ServletException {
        super.init();
        fileStorageManager = new FileStorageManager();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Set the category path attribute, defaulting to "0" if missing.
        String categoryPath = request.getParameter("categoryPath");
        if (categoryPath == null || categoryPath.isEmpty()) {
            categoryPath = "0";
        }
        request.setAttribute("categoryPath", categoryPath);

        // Choose the right mode based on the servlet path.
        String servletPath = request.getServletPath();
        if (servletPath.contains("edit")) {
            processEditGet(request, response);
        } else {
            processCreateGet(request, response);
        }
    }

    private void processCreateGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("action", "create");
        // No existing note—simply forward to the note form JSP.
        request.getRequestDispatcher("/noteForm.jsp").forward(request, response);
    }

    private void processEditGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("action", "edit");
        String noteIdParam = request.getParameter("noteId");
        if (noteIdParam == null || noteIdParam.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing note ID for editing.");
            return;
        }
        long noteId;
        try {
            noteId = Long.parseLong(noteIdParam);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid note ID.");
            return;
        }
        Note note = loadNoteById(noteId);
        if (note == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Note not found.");
            return;
        }
        request.setAttribute("note", note);
        request.setAttribute("images", getEncodedImages(note, request));

        request.getRequestDispatcher("/noteForm.jsp").forward(request, response);
    }

    private Note loadNoteById(long noteId) throws ServletException {
        List<Note> notes;
        try {
            notes = fileStorageManager.loadNotes();
        } catch (Exception e) {
            throw new ServletException("Error loading notes", e);
        }
        for (Note note : notes) {
            if (note.getId() == noteId) {
                return note;
            }
        }
        return null;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String servletPath = request.getServletPath();
        boolean isEdit = servletPath.contains("edit");

        processSubmitNote(request, response, isEdit);


    }

    /**
     * Processes the "Submit Note" action: it either creates a new note or updates
     * an existing one based on the form input and then persists the changes.
     */
    private void processSubmitNote(HttpServletRequest request, HttpServletResponse response, boolean isEdit)
            throws ServletException, IOException {
        List<Note> notes;
        try {
            notes = fileStorageManager.loadNotes();
        } catch (Exception e) {
            throw new ServletException("Error loading notes", e);
        }

        List<Block> blocks = extractBlocksFromRequest(request);
        String title = request.getParameter("title");
        String categoryPath = request.getParameter("categoryPath");
        if (categoryPath == null || categoryPath.isEmpty()) {
            categoryPath = "0";
        }
        // Get the current category hierarchy and select the last one.
        CategoryIndex currentCategory = Utils.getCategoryHierarchy(fileStorageManager, categoryPath).getLast();

        if (isEdit) {
            // Update an existing note.
            String noteIdParam = request.getParameter("noteId");
            long noteId;
            try {
                noteId = Long.parseLong(noteIdParam);
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid note ID.");
                return;
            }
            Note noteToEdit = findNoteById(notes, noteId);
            if (noteToEdit == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Note not found for editing.");
                return;
            }
            noteToEdit.setTitle(title);
            noteToEdit.setContentBlocks(blocks);

        } else {
            // Create a new note.
            Note newNote = new Note(title, blocks);
            notes.add(newNote);
            currentCategory.addNoteId(newNote.getId());

            try {
                fileStorageManager.SaveNewNote(categoryPath, newNote.getId());
            } catch (Exception e) {
                throw new ServletException("Error saving new note ID", e);
            }

        }

        try {fileStorageManager.saveNotes(notes);
        } catch (Exception e) {
            throw new ServletException("Error saving notes", e);
        }

        // Redirect to the category view.
        response.sendRedirect(request.getContextPath() + "/?categoryPath=" + categoryPath);
    }

    /**
     * Helper method to locate a note by its ID in the list.
     */
    private Note findNoteById(List<Note> notes, long noteId) {
        for (Note note : notes) {
            if (note.getId() == noteId) {
                return note;
            }
        }
        return null;
    }

    /**
     * Extracts blocks from the request parameters and file parts.
     * For each block:
     *   - We read the "blockData" hidden field to retrieve the old data (text or image path).
     *   - If it's a text block, we just set that data.
     *   - If it's an image block:
     *       If a new file was uploaded, we store the new file path.
     *       Otherwise, we preserve the old data (existing image path).
     */
    private List<Block> extractBlocksFromRequest(HttpServletRequest request) throws ServletException, IOException {
        List<Block> blocks = new ArrayList<>();

        // Retrieve the editor content from the hidden field
        String editorContent = request.getParameter("editorContent");
        if (editorContent == null) {
            editorContent = "";
        }

        // Prepare regex to match all <img ...> tags and capture the src attribute.
        Pattern imgPattern = Pattern.compile("<img[^>]*src=\"([^\"]*)\"[^>]*>");
        Matcher matcher = imgPattern.matcher(editorContent);

        // Retrieve all image parts from the request using the new attribute name "images[]"
        List<Part> imageParts = new ArrayList<>();
        for (Part part : request.getParts()) {
            if ("images[]".equals(part.getName()) && part.getSize() > 0) {
                imageParts.add(part);
            }
        }

        int id = 0;

        int imageIndex = 0;

        int lastIndex = 0;
        // Iterate over all image tag matches
        while (matcher.find()) {
            int start = matcher.start();
            // Get the text block preceding the image tag
            String textBlock = editorContent.substring(lastIndex, start);
            if (!textBlock.trim().isEmpty()) {
                blocks.add(new Block(id++, "text", textBlock));
            }

            // Process the corresponding image part if available
            if (imageIndex < imageParts.size()) {
                String savedFileName = fileStorageManager.saveImage(imageParts.get(imageIndex));
                blocks.add(new Block(id++,"image", savedFileName));
                imageIndex++;
            }
            lastIndex = matcher.end();
        }

        // Process any remaining text after the last image tag
        if (lastIndex < editorContent.length()) {
            String textBlock = editorContent.substring(lastIndex);
            if (!textBlock.trim().isEmpty()) {
                blocks.add(new Block(id++, "text", textBlock));
            }
        }

        return blocks;
    }
}
