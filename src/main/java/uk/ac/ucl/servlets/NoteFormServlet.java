package uk.ac.ucl.servlets;

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
import java.util.ArrayList;
import java.util.List;

@WebServlet({"/note/create", "/note/edit"})
@MultipartConfig
public class NoteFormServlet extends HttpServlet {

    private FileStorageManager fileStorageManager;

    // Define a constant for the directory where uploaded images will be stored.
    // Change this to your desired absolute path.
    private static final String UPLOAD_DIR = "C:\\Users\\jeetu\\Desktop\\Java\\JavaCoursework\\src\\main\\webapp\\data\\";

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
        String actionType = request.getParameter("actionType");
        String servletPath = request.getServletPath();
        boolean isEdit = servletPath.contains("edit");

        if ("Add Image".equalsIgnoreCase(actionType)) {
            processAddImage(request, response, isEdit);
        } else if ("Submit Note".equalsIgnoreCase(actionType)) {
            processSubmitNote(request, response, isEdit);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action type.");
        }
    }

    /**
     * Processes the "Add Image" action: it retrieves the current blocks,
     * appends a new image block and an empty text block, and then forwards
     * back to the note form JSP with the updated state.
     */
    private void processAddImage(HttpServletRequest request, HttpServletResponse response, boolean isEdit)
            throws ServletException, IOException {
        List<Block> blocks = extractBlocksFromRequest(request);
        int currentBlockCount = blocks.size();

        // Append new blocks: an image block, then an empty text block.
        blocks.add(new Block(currentBlockCount + 1, "image", ""));
        blocks.add(new Block(currentBlockCount + 2, "text", ""));

        // Preserve current form state.
        request.setAttribute("blocks", blocks);
        request.setAttribute("blockCount", blocks.size());
        request.setAttribute("title", request.getParameter("title"));
        request.setAttribute("categoryPath", request.getParameter("categoryPath"));
        request.setAttribute("action", isEdit ? "edit" : "create");
        if (isEdit) {
            request.setAttribute("noteId", request.getParameter("noteId"));
        }
        request.getRequestDispatcher("/noteForm.jsp").forward(request, response);
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
            int noteId;
            try {
                noteId = Integer.parseInt(noteIdParam);
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

        // Persist the updated list of notes.
        try {
            fileStorageManager.saveNotes(notes);
        } catch (Exception e) {
            throw new ServletException("Error saving notes", e);
        }

        // Redirect to the category view.
        response.sendRedirect(request.getContextPath() + "/?categoryPath=" + categoryPath);
    }

    /**
     * Helper method to locate a note by its ID in the list.
     */
    private Note findNoteById(List<Note> notes, int noteId) {
        for (Note note : notes) {
            if (note.getId() == noteId) {
                return note;
            }
        }
        return null;
    }

    /**
     * Extracts blocks from the request parameters and file parts.
     * For each block type, it processes text data or, if the block is an image,
     * handles the file upload and sets the image path.
     */
    private List<Block> extractBlocksFromRequest(HttpServletRequest request) throws ServletException, IOException {
        List<Block> blocks = new ArrayList<>();
        int text_block_num = 0;
        String[] blockTypes = request.getParameterValues("blockType");
        String[] blockDataArr = request.getParameterValues("blockData");

        if (blockTypes != null) {
            for (int i = 0; i < blockTypes.length; i++) {
                String type = blockTypes[i];
                String data = "";
                if ("text".equalsIgnoreCase(type)) {
                    if( blockDataArr != null && blockDataArr.length > text_block_num) {
                        data = blockDataArr[text_block_num];
                    }
                    text_block_num ++;
                }
                Block block = new Block(i + 1, type, data);
                if ("image".equalsIgnoreCase(type)) {
                    Part imagePart = request.getPart("blockImage" + i);
                    if (imagePart != null && imagePart.getSubmittedFileName() != null &&
                            !imagePart.getSubmittedFileName().isEmpty()) {
                        // Use the defined UPLOAD_DIR instead of the webapp directory.
                        File uploadDirFile = new File(UPLOAD_DIR);
                        if (!uploadDirFile.exists()) {
                            uploadDirFile.mkdirs();
                        }
                        String fileName = System.currentTimeMillis() + "_" + imagePart.getSubmittedFileName();
                        String filePath = UPLOAD_DIR + File.separator + fileName;
                        imagePart.write(filePath);
                        // Store the relative path or the location as needed.
                        block.setData(fileName);
                    }
                }
                blocks.add(block);
            }
        }
        return blocks;
    }
}
