package uk.ac.ucl.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import uk.ac.ucl.model.Block;
import uk.ac.ucl.model.FileStorageManager;
import uk.ac.ucl.model.IndexEntry;
import uk.ac.ucl.model.Note;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

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

        long noteId = 0;
        try {
            noteId = Long.parseLong(request.getParameter("Id"));
        } catch (NumberFormatException e) {
            throw new RuntimeException("noteId parsing gone wrong",e);
        }

        IndexEntry currentCategory = getCategoryHierarchy(fileStorageManager, categoryPathParam).getLast();
        List<IndexEntry> children = currentCategory.getChildren();

        long finalNoteId = noteId;
        Note note = (Note) children.stream()
                .filter(entry -> entry.getId() == finalNoteId)
                .findAny()
                .orElseThrow(() -> new ServletException("Unable to find note."));

        request.setAttribute("note", note);
        request.setAttribute("images", getEncodedImages(note, request));
        request.setAttribute("categoryPath", categoryPathParam);
        request.getRequestDispatcher("/noteView.jsp").forward(request, response);


    }

    public static List<String> getImagePaths(Note note) {
        List<String> imagePaths = new ArrayList<>();
        if (note.getContentBlocks() != null) {
            for (Block block : note.getContentBlocks()) {
                if ("image".equalsIgnoreCase(block.getType())) {
                    imagePaths.add(block.getData());
                }
            }
        }
        return imagePaths;
    }


    // Java
    private Map<String, String> getEncodedImages(Note note, HttpServletRequest request) {
        Map<String, String> imageDataMap = new HashMap<>();
        if (note.getContentBlocks() != null) {
            for (Block block : note.getContentBlocks()) {
                if ("image".equalsIgnoreCase(block.getType())) {
                    String key = block.getData(); // Use the image path as key
                    String absolutePath = request.getServletContext().getRealPath("/") + "\\data\\" + key;
                    File imageFile = new File(absolutePath);
                    if (imageFile.exists()) {
                        try {
                            byte[] fileContent = Files.readAllBytes(imageFile.toPath());
                            String base64Image = Base64.getEncoder().encodeToString(fileContent);
                            String mimeType = request.getServletContext().getMimeType(imageFile.getName());
                            if (mimeType == null) {
                                mimeType = "image/jpeg";
                            }
                            String dataUri = "data:" + mimeType + ";base64," + base64Image;
                            imageDataMap.put(key, dataUri);
                        } catch (IOException e) {
                            // Skip image if reading fails
                        }
                    }
                }
            }
        }
        return imageDataMap;
    }

}