<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ page import="java.util.*, uk.ac.ucl.model.*, uk.ac.ucl.model.CategoryIndex" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>My Notes</title>
</head>
<body>
<div>
    <!-- Search form with hidden fields for each category string -->
    <form action="" method="get">
        <input type="hidden" name="action" value="sort" />
        <%
            String categoryPath = (String) request.getAttribute("categoryPath");
        %>
        <input type="hidden" name="categoryPath" value="<%= categoryPath %>" />
        <%

        %>
        <input type="text" name="query" placeholder="Search notes..." />
        <button type="submit">Search</button>
        <br/><br/>
        <button type="submit" name="sortBy" value="title">Sort by Title</button>
        <button type="submit" name="sortBy" value="createdAt">Sort by Date</button>
    </form>
    <div>
        <form action="<%= request.getContextPath() %>/note/create" method="get" style="display:inline;">
            <input type="hidden" name="categoryPath" value="<%=categoryPath%>"/>
            <button type="submit">Add Note</button>
        </form>
    </div>

    <!-- Inline Category Creation Form -->
    <div>
        <form method="post" action="${pageContext.request.contextPath}/category/create">
            <label for="categoryName">New Category:</label>
            <input type="hidden" name="categoryPath" value="<%= categoryPath %>" />
            <input type="text" id="categoryName" name="categoryName" placeholder="Enter category name" required/>
            <button type="submit">Add Category</button>
        </form>
    </div>
    <%
        // Retrieve the category hierarchy as a list
        List<CategoryIndex> categoryHierarchy = (List<CategoryIndex>) request.getAttribute("categoryHierarchy");
        if (categoryHierarchy != null && !categoryHierarchy.isEmpty()) {
            // Get the last element (current category)
            CategoryIndex currentCategory = categoryHierarchy.getLast();
            int lastCommaIndex = categoryPath.lastIndexOf(",");
            String backCategoryPath = (lastCommaIndex != -1) ? categoryPath.substring(0, lastCommaIndex) : "";
            if (currentCategory != null && currentCategory.getId() != 0) {
    %>
    <h2>Category: <%= currentCategory.getName() %></h2>
    <form action="" method="get">
        <input type="hidden" name="categoryPath" value="<%= backCategoryPath %>" />
        <button type="submit">Back</button>
    </form>
    <%
            }
        }
    %>

    <h3>Notes</h3>
    <%
        List<IndexEntry> IndexEntries = (List<IndexEntry>) request.getAttribute("Entries");
        if (IndexEntries != null && !IndexEntries.isEmpty()) {
    %>
    <ul>
        <% for (IndexEntry entry : IndexEntries) {
            String categoryPathValue = categoryPath + ","+entry.getId();
            String actionUrl = "";
            if (entry instanceof Note) {
                actionUrl = "/note/view/";
                categoryPathValue = categoryPath;
            }
        %>
        <li>
            <form method="get" action="<%=actionUrl %>">
                <input type="hidden" name="Id" value="<%= entry.getId() %>" />
                <input type="hidden" name="categoryPath" value="<%= categoryPathValue%>" />
                <button type="submit"><%= entry.getName() %></button>
            </form>
        </li>
        <% } %>
    </ul>
    <% } else { %>
    <p>No notes found in this category.</p>
    <% } %>
</div>
</body>
</html>