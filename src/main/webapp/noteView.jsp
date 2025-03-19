<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ page import="uk.ac.ucl.model.Note" %>
<%@ page import="uk.ac.ucl.model.Block" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>View Note</title>
</head>
<body>
<%
    Note note = (Note) request.getAttribute("note");
    String categoryPath = (String) request.getAttribute("categoryPath");
%>
<div>
    <% if (note != null) { %>
    <h1><%= note.getTitle() %></h1>
    <p>Created: <%= note.getCreatedAt() %></p>
    <hr/>
    <div>
        <% if (note.getContentBlocks() != null && !note.getContentBlocks().isEmpty()) {
            for (Block block : note.getContentBlocks()) {
                if ("text".equalsIgnoreCase(block.getType())) { %>
        <p><%= block.getData() %></p>
        <% } else if ("image".equalsIgnoreCase(block.getType())) { %>
        <img src="<%= block.getData() %>" alt="Image Block" style="max-width:100%;"/>
        <% } else { %>
        <p><%= block.getData() %></p>
        <% }
        }
        } else { %>
        <p>No content available for this note.</p>
        <% } %>
    </div>
    <% } else { %>
    <p>Note not found.</p>
    <% } %>

    <!-- Buttons -->
    <div>
        <form action="<%= request.getContextPath() %>/note/edit" method="get" style="display:inline;">
            <input type="hidden" name="noteId" value="<%= note != null ? note.getId() : "" %>"/>
            <button type="submit">Edit</button>
        </form>
        <form action="<%= request.getContextPath() %>/" method="get" style="display:inline;">
            <input type="hidden" name="categoryPath" value="<%= categoryPath %>"/>
            <button type="submit">Back</button>
        </form>
    </div>
</div>
</body>
</html>
