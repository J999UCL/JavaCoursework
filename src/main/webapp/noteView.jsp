<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ page import="uk.ac.ucl.model.Note" %>
<%@ page import="uk.ac.ucl.model.Block" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>View Note</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            padding: 20px;
            background: #fdfdfd;
        }
        .header {
            margin-bottom: 20px;
        }
        .back-button {
            text-decoration: none;
            color: #007aff;
            font-size: 1em;
        }
        .note-title {
            font-size: 1.8em;
            font-weight: bold;
            margin: 10px 0;
        }
        .note-created {
            color: #888;
            font-size: 0.9em;
        }
        .note-content {
            margin-top: 20px;
            line-height: 1.5;
        }
        .note-content p {
            margin: 10px 0;
        }
        .edit-button {
            margin-top: 30px;
            display: inline-block;
            background: #007aff;
            color: #fff;
            padding: 10px 15px;
            text-decoration: none;
            border-radius: 5px;
        }
    </style>
</head>
<body>
<div class="header">
    <!-- Back button: categoryPath is passed from the servlet (e.g. "/category1/category2") -->
    <a class="back-button" href="<%= request.getContextPath() %>/notes<%= request.getAttribute("categoryPath") %>">&larr; Back</a>

    <!-- Note Title and Creation Date -->
    <%
        Note note = (Note) request.getAttribute("note");
        if (note != null) {
    %>
    <div class="note-title"><%= note.getTitle() %></div>
    <div class="note-created">Created: <%= note.getCreatedAt() %></div>
    <%
    } else {
    %>
    <p>Note not found.</p>
    <%
        }
    %>
</div>

<div class="note-content">
    <%
        if (note != null && note.getContentBlocks() != null && !note.getContentBlocks().isEmpty()) {
            for (Block block : note.getContentBlocks()) {
                if ("text".equalsIgnoreCase(block.getType())) {
    %>
    <p><%= block.getData() %></p>
    <%
    } else if ("image".equalsIgnoreCase(block.getType())) {
    %>
    <img src="<%= block.getData() %>" alt="Image Block" style="max-width:100%;"/>
    <%
    } else {
    %>
    <p><%= block.getData() %></p>
    <%
            }
        }
    } else {
    %>
    <p>No content available for this note.</p>
    <%
        }
    %>
</div>

<!-- Edit button: Redirects to the note edit page with the current category path and note ID -->
<div>
    <a class="edit-button" href="<%= request.getContextPath() %>/note/edit<%= request.getAttribute("categoryPath") %>/<%= note.getId() %>">Edit Note</a>
</div>
</body>
</html>
