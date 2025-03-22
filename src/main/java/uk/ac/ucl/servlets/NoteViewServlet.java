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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
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
                    String key = block.getData(); // image file name
                    String absolutePath = request.getServletContext().getRealPath("/") +
                            "\\data\\images\\" + key;
                    File imageFile = new File(absolutePath);
                    if (imageFile.exists()) {
                        try {
                            // Read the original image
                            BufferedImage originalImage = ImageIO.read(imageFile);

                            // Set the target width (e.g., 300 pixels) and compute the height to maintain aspect ratio
                            int targetWidth = 300;
                            int targetHeight = originalImage.getHeight() * targetWidth / originalImage.getWidth();

                            // Create a resized image
                            BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
                            Graphics2D g2d = resizedImage.createGraphics();
                            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                            g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
                            g2d.dispose();

                            // Convert the resized image to a byte array
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            ImageIO.write(resizedImage, "jpg", baos);
                            byte[] fileContent = baos.toByteArray();

                            // Encode the resized image in Base64
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