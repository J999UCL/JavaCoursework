package uk.ac.ucl.model;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {

    // Parses the comma‑separated category path string into a list of integers.
    public static List<Long> parseCategoryPath(String categoryPathParam) {
        if (categoryPathParam != null && !categoryPathParam.isEmpty()) {
            return Arrays.stream(categoryPathParam.split(","))
                    .map(String::trim)
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    // Java
    public static List<CategoryIndex> getCategoryHierarchy(FileStorageManager fileStorageManager, String categoryPathParam)
            throws ServletException {
        List<Long> categoryPath = parseCategoryPath(categoryPathParam);
        CategoryIndex rootCategory;
        try {
            rootCategory = fileStorageManager.loadCategories();
        } catch (Exception e) {
            throw new ServletException("Failed to load categories.", e);
        }
        List<CategoryIndex> hierarchy = new ArrayList<>();
        hierarchy.add(rootCategory);
        CategoryIndex current = rootCategory;
//        Logic problem here
        for (int i = 1; i < categoryPath.size(); i++) {
            long targetId = categoryPath.get(i);
            boolean found = false;
            for (CategoryIndex child : current.getSubCategories()) {
                if (child.getId() == targetId) {
                    hierarchy.add(child);
                    current = child;
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new ServletException("Invalid category path: no category with id " + targetId);
            }
        }
        return hierarchy;
    }

    public static Map<String, String> getEncodedImages(Note note, HttpServletRequest request) {
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




//    // Loads the full category hierarchy and then returns the current category defined by the path.
//    public static IndexEntry getCategoryHierarchy(FileStorageManager fileStorageManager, String categoryPathParam)
//            throws ServletException {
//        List<Integer> categoryPath = parseCategoryPath(categoryPathParam);
//        // Load the full category hierarchy.
//        CategoryIndex rootCategory;
//        try {
//            rootCategory = fileStorageManager.loadCategories();
//        } catch (Exception e) {
//            throw new ServletException("Failed to load data", e);
//        }
//        // Find the current category using the parsed path.
//        IndexEntry currentCategory = findCategoryByPath(rootCategory, categoryPath);
//        if (currentCategory == null) {
//            currentCategory = new CategoryIndex("No such category");
//        }
//        return currentCategory;
//    }
//
//    // Recursively finds the category based on a list of category IDs.
//    public static IndexEntry findCategoryByPath(CategoryIndex root, List<Integer> categoryPath) throws ServletException {
//        if (categoryPath == null || categoryPath.isEmpty()) {
//            return root;
//        }
//        IndexEntry current = root;
//        for (Integer targetId : categoryPath) {
//            boolean found = false;
//            for (IndexEntry child : current.getChildren()) {
//                if (child.getId() == targetId) {
//                    current = child;
//                    found = true;
//                    break;
//                }
//            }
//            if (!found) {
//                return null;
//            }
//        }
//        return current;
//    }
}