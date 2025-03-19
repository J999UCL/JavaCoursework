package uk.ac.ucl.servlets;

import uk.ac.ucl.model.*;
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

import static uk.ac.ucl.model.Utils.getCategoryHierarchy;

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
        // Get the category path from a parameter (or default to empty)
        String categoryPath = request.getParameter("categoryPath");
        if (categoryPath == null) {
            categoryPath = "0";
        }
        request.setAttribute("categoryPath", categoryPath);

        // Determine mode by servlet mapping: create or edit.
        String servletPath = request.getServletPath();
        if (servletPath.contains("edit")) {
            handleEditGet(request, response);
        } else {
            handleCreateGet(request, response);
        }
    }

    private void handleCreateGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("action", "create");
        // For creation, no existing note is set.
        request.getRequestDispatcher("/noteForm.jsp").forward(request, response);
    }

    private void handleEditGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("action", "edit");
        String noteIdStr = request.getParameter("noteId");
        if (noteIdStr == null || noteIdStr.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No note specified for editing.");
            return;
        }
        int noteId;
        try {
            noteId = Integer.parseInt(noteIdStr);
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

    private Note loadNoteById(int noteId) throws ServletException {
        List<Note> notes;
        try {
            notes = fileStorageManager.loadNotes();
        } catch (Exception e) {
            throw new ServletException("Error loading notes.", e);
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
        String actionType = request.getParameter("actionType"); // "Add Image" or "Submit Note"
        String servletPath = request.getServletPath();
        boolean isEdit = servletPath.contains("edit");

        if ("Add Image".equalsIgnoreCase(actionType)) {
            handleAddImage(request, response, isEdit);
        } else if ("Submit Note".equalsIgnoreCase(actionType)) {
            handleSubmitNote(request, response, isEdit);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action type.");
        }
    }

    private void handleAddImage(HttpServletRequest request, HttpServletResponse response, boolean isEdit)
            throws ServletException, IOException {
        // Process current blocks from the form submission.
        List<Block> blocks = getBlocksFromRequest(request);

        // Update block count.
        int blockCount = blocks.size();

        // Append an image block and then a new empty text block.
        blocks.add(new Block(blockCount,"image", ""));
        blocks.add(new Block(blockCount + 1,"text", ""));


        // Preserve the current form state.
        request.setAttribute("blockCount", blockCount);
        request.setAttribute("blocks", blocks);
        request.setAttribute("title", request.getParameter("title"));
        request.setAttribute("categoryPath", request.getParameter("categoryPath"));
        request.setAttribute("action", isEdit ? "edit" : "create");
        if (isEdit) {
            request.setAttribute("noteId", request.getParameter("noteId"));
        }
        // Forward back to the form JSP.
        request.getRequestDispatcher("/noteForm.jsp").forward(request, response);
    }

    private void handleSubmitNote(HttpServletRequest request, HttpServletResponse response, boolean isEdit)
            throws ServletException, IOException {
        List<Note> notes;
        try {
            notes = fileStorageManager.loadNotes();
        } catch (Exception e) {
            throw new ServletException("Error loading notes.", e);
        }
        List<Block> blocks = getBlocksFromRequest(request);
        String title = request.getParameter("title");
        String categoryPathParam = request.getParameter("categoryPath");
        if (categoryPathParam == null){categoryPathParam = "0";}
        CategoryIndex currentCategory = getCategoryHierarchy(fileStorageManager, categoryPathParam).getLast();

        if (isEdit) {
            String noteIdStr = request.getParameter("noteId");
            int noteId;
            try {
                noteId = Integer.parseInt(noteIdStr);
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid note ID.");
                return;
            }
            Note noteToEdit = null;
            for (Note n : notes) {
                if (n.getId() == noteId) {
                    noteToEdit = n;
                    break;
                }
            }
            if (noteToEdit == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Note not found for editing.");
                return;
            }
            noteToEdit.setTitle(title);
            noteToEdit.setContentBlocks(blocks);
        } else {
            Note newNote = new Note(title, blocks);
            notes.add(newNote);
            currentCategory.addNoteId(newNote.getId());

            try {
                fileStorageManager.SaveNewNote(categoryPathParam, newNote.getId());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }

        try {
            fileStorageManager.saveNotes(notes);
        } catch (Exception e) {
            throw new ServletException("Error saving note.", e);
        }



        // Redirect back to the category view.
        response.sendRedirect(request.getContextPath() + "/?categoryPath=" + categoryPathParam);
    }

    private List<Block> getBlocksFromRequest(HttpServletRequest request) throws ServletException, IOException {
        // Java code snippet in getBlocksFromRequest method
        String[] blockTypes = request.getParameterValues("blockType");
        String[] blockDataArr = request.getParameterValues("blockData");
        List<Block> blocks = new ArrayList<>();
        if (blockTypes != null) {
            for (int i = 0; i < blockTypes.length; i++) {
                String type = blockTypes[i];
                String data = ("text".equalsIgnoreCase(type) && blockDataArr != null)
                        ? blockDataArr[i] : "";
                Block block = new Block(i + 1, type, data);
                if ("image".equalsIgnoreCase(type)) {
                    Part imagePart = request.getPart("blockImage" + i);
                    if (imagePart != null && imagePart.getSubmittedFileName() != null
                            && !imagePart.getSubmittedFileName().isEmpty()) {
                        String uploadsDir = request.getServletContext().getRealPath("/") + "uploads";
                        File uploadsDirFile = new File(uploadsDir);
                        if (!uploadsDirFile.exists()) {
                            uploadsDirFile.mkdirs();
                        }
                        String fileName = System.currentTimeMillis() + "_"
                                + imagePart.getSubmittedFileName();
                        String filePath = uploadsDir + File.separator + fileName;
                        imagePart.write(filePath);
                        block.setData("uploads/" + fileName);
                    }
                }
                blocks.add(block);
            }
        }
        return blocks;
    }

}