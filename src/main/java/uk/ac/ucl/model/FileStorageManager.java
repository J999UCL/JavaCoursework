package uk.ac.ucl.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Part;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import static uk.ac.ucl.model.Utils.getCategoryHierarchy;

public class FileStorageManager {
    private static final String NOTES_FILE = "C:\\Users\\jeetu\\Desktop\\Java\\JavaCoursework\\src\\main\\webapp\\data\\notes.json";
    private static final String CATEGORIES_FILE = "C:\\Users\\jeetu\\Desktop\\Java\\JavaCoursework\\src\\main\\webapp\\data\\categories.json";
    private static final String IMAGES_DIR = "C:\\Users\\jeetu\\Desktop\\Java\\JavaCoursework\\src\\main\\webapp\\data\\images";

    private final ObjectMapper objectMapper;

    public FileStorageManager() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        // Enable pretty printing for easier readability of the output files
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Saves the list of Note objects to the NOTES_FILE.
     *
     * @param notes the list of notes to save.
     * @throws Exception if an I/O error occurs.
     */
    public void saveNotes(List<Note> notes) throws Exception {
        objectMapper.writeValue(new File(NOTES_FILE), notes);
    }

    /**
     * Loads the list of Note objects from the NOTES_FILE.
     *
     * @return a List of Note objects; returns an empty list if the file does not exist.
     * @throws Exception if an I/O error occurs.
     */
    public List<Note> loadNotes() throws Exception {
        File file = new File(NOTES_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, Note.class);
        List<Note> notes = objectMapper.readValue(file, listType);
        return notes != null ? notes : new ArrayList<>();
    }

    /**
     * Saves the CategoryIndex (hierarchical category structure) to the CATEGORIES_FILE.
     *
     * @param newCategory the category index to save.
     * @throws Exception if an I/O error occurs.
     */
    public void saveNewCategories(String categoryPathParam, CategoryIndex newCategory) throws Exception {

        List<CategoryIndex> hierarchy = getCategoryHierarchy(this, categoryPathParam);
        CategoryIndex root = hierarchy.getFirst();
        CategoryIndex parentReference = hierarchy.getLast();
        parentReference.addSubCategory(newCategory);
        objectMapper.writeValue(new File(CATEGORIES_FILE), root);
    }

    public void SaveNewNote(String categoryPathParam, long id) throws Exception {
        List<CategoryIndex> hierarchy = getCategoryHierarchy(this, categoryPathParam);
        CategoryIndex root = hierarchy.getFirst();
        CategoryIndex parentReference = hierarchy.getLast();
        parentReference.addNoteId(id);
        objectMapper.writeValue(new File(CATEGORIES_FILE), root);
    }

    public String saveImage(Part imagePart) throws ServletException, IOException {
        // Retrieve the original file name
        String originalFileName = imagePart.getSubmittedFileName();
        if (originalFileName == null || originalFileName.isEmpty()) {
            throw new ServletException("No file selected for upload.");
        }

        // Prepend a timestamp to avoid collisions
        String savedFileName = System.currentTimeMillis() + "_" + originalFileName;

        // Ensure the upload directory exists
        Path uploadPath = Path.of(IMAGES_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Construct the full path to save the file
        Path filePath = uploadPath.resolve(savedFileName);

        // Copy the uploaded file to the target location
        Files.copy(imagePart.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return savedFileName;
    }

    // Java
    public String saveImageFile(byte[] imageBytes) throws IOException {
        // Generate a default file name with a timestamp.
        String defaultFileName = System.currentTimeMillis() + "_image.jpg";

        // Ensure the upload directory exists.
        Path uploadPath = Path.of(IMAGES_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Construct the full path to save the file.
        Path filePath = uploadPath.resolve(defaultFileName);

        // Copy the byte array to the target location via a ByteArrayInputStream.
        try (InputStream in = new ByteArrayInputStream(imageBytes)) {
            Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        return defaultFileName;
    }

    /**
     * Loads the CategoryIndex from the CATEGORIES_FILE.
     *
     * @return the loaded CategoryIndex; if the file does not exist, returns a new CategoryIndex with a default name.
     * @throws Exception if an I/O error occurs.
     */
    public CategoryIndex loadCategories() throws Exception {
        File file = new File(CATEGORIES_FILE);
        if (!file.exists()) {
            // Return a default empty category index (e.g., with name "Root")
            CategoryIndex defaultCategory = new CategoryIndex("Root");
            objectMapper.writeValue(new File(CATEGORIES_FILE), defaultCategory);
            return new CategoryIndex("Root");
        }
        return objectMapper.readValue(file, CategoryIndex.class);
    }
}
