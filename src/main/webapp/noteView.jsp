<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ page import="uk.ac.ucl.model.Note" %>
<%@ page import="uk.ac.ucl.model.Block" %>
<%@ page import="java.util.Map" %>
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
    Map<String, String> imageMap = (Map<String, String>) request.getAttribute("images");
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
        <%   } else if ("image".equalsIgnoreCase(block.getType())) {
            String imgData = imageMap != null ? imageMap.get(block.getData()) : null;
            if (imgData != null) { %>
        <img src="<%= imgData %>" alt="Image Block" style="max-width:100%;"/>
        <%      } else { %>
        <p>[Image not available]</p>
        <%      }
        } else { %>
        <p><%= block.getData() %></p>
        <%  }
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
            <input type="hidden" name="noteId" value="<%= note != null ? String.valueOf(note.getId()) : "" %>"/>
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