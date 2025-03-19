package uk.ac.ucl.model;

import jakarta.servlet.ServletException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {

    // Parses the comma‑separated category path string into a list of integers.
    public static List<Integer> parseCategoryPath(String categoryPathParam) {
        if (categoryPathParam != null && !categoryPathParam.isEmpty()) {
            return Arrays.stream(categoryPathParam.split(","))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    // Java
    public static List<CategoryIndex> getCategoryHierarchy(FileStorageManager fileStorageManager, String categoryPathParam)
            throws ServletException {
        List<Integer> categoryPath = parseCategoryPath(categoryPathParam);
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
            Integer targetId = categoryPath.get(i);
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