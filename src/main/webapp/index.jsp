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
        <input type="hidden" name="action" value="search" />
        <%
            List<String> categoryPath = (List<String>) request.getAttribute("categoryPath");
            if (categoryPath != null) {
                for (String cat : categoryPath) {
        %>
        <input type="hidden" name="categoryPath" value="<%= cat %>" />
        <%
                }
            }
        %>
        <input type="text" name="query" placeholder="Search notes..." />
        <button type="submit">Search</button>
        <br/><br/>
        <button type="submit" name="sortBy" value="title">Sort by Title</button>
        <button type="submit" name="sortBy" value="createdAt">Sort by Date</button>
    </form>
    <div>
        <a href="${pageContext.request.contextPath}/note/create">+ Add Note</a>
    </div>
    <h3>Notes</h3>
    <%
        List<IndexEntry> IndexEntries = (List<IndexEntry>) request.getAttribute("Entries");
        if (IndexEntries != null && !IndexEntries.isEmpty()) {
    %>
    <ul>
        <% for (IndexEntry entry : IndexEntries) { %>
        <li>
            <a href="${pageContext.request.contextPath}/note/view/">
                <span><%= entry.getName() %></span><br/>
            </a>
        </li>
        <% } %>
    </ul>
    <% } else { %>
    <p>No notes found in this category.</p>
    <% } %>
</div>
</body>
</html>