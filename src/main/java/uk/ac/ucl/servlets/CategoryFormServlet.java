package uk.ac.ucl.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import uk.ac.ucl.model.CategoryIndex;
import uk.ac.ucl.model.FileStorageManager;

import java.io.IOException;

@WebServlet("/category/create")
public class CategoryFormServlet extends HttpServlet {

    private FileStorageManager fileStorageManager;

    @Override
    public void init() throws ServletException {
        super.init();
        fileStorageManager = new FileStorageManager();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/categoryForm.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Retrieve the category name from the form
        String categoryName = request.getParameter("categoryName");
        String categoryPathParam = request.getParameter("categoryPath");
        if (categoryPathParam == null){categoryPathParam = "0";}

        CategoryIndex newCategory = new CategoryIndex(categoryName);

        // Save the updated categories back to storage
        try {
            fileStorageManager.saveNewCategories(categoryPathParam, newCategory);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        response.sendRedirect(request.getContextPath() + "/?categoryPath=" + categoryPathParam);
    }
}