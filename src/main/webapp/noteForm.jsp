<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ page import="java.util.*, uk.ac.ucl.model.Note, uk.ac.ucl.model.Block" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>
        <%
            String action = (String) request.getAttribute("action");
            if ("edit".equals(action)) {
                out.print("Edit Note");
            } else {
                out.print("Create Note");
            }
        %>
    </title>
</head>
<body>
<h2>
    <%
        if ("edit".equals(request.getAttribute("action"))) {
            out.print("Edit Note");
        } else {
            out.print("Create Note");
        }
    %>
</h2>
<!-- The form action uses the servlet mapping; the category path is passed via a hidden field -->
<form action="<%= request.getContextPath() %><%= request.getAttribute("action") %>" method="post" enctype="multipart/form-data">
    <!-- Hidden field for the current category path -->
    <input type="hidden" name="categoryPath" value="<%= request.getAttribute("categoryPath") %>" />

    <!-- If editing, include the note's ID -->
    <%
        if ("edit".equals(request.getAttribute("action")) && request.getAttribute("note") != null) {
    %>
    <input type="hidden" name="noteId" value="<%= ((Note) request.getAttribute("note")).getId() %>" />
    <%
        }
    %>

    <!-- Note Title -->
    <label for="title">Title:</label>
    <input type="text" id="title" name="title"
           value="<%= (request.getAttribute("title") != null)
                        ? request.getAttribute("title")
                        : ((request.getAttribute("note") != null)
                            ? ((Note)request.getAttribute("note")).getTitle()
                            : "") %>" /><br/><br/>

    <%
        // Retrieve the list of blocks from a partial submission or from the existing note.
        List<Block> blocks = (List<Block>) request.getAttribute("blocks");
        if (blocks == null) {
            Note note = (Note) request.getAttribute("note");
            if (note != null && note.getContentBlocks() != null && !note.getContentBlocks().isEmpty()) {
                blocks = note.getContentBlocks();
            } else {
                blocks = new ArrayList<>();
                blocks.add(new Block(1, "text", ""));
            }
        }
        int blockCount = blocks.size();
    %>
    <!-- Hidden field for block count -->
    <input type="hidden" name="blockCount" value="<%= blockCount %>" />

    <!-- Render each block -->
    <%
        for (int i = 0; i < blockCount; i++) {
            Block block = blocks.get(i);
    %>
    <fieldset>
        <legend>Block <%= (i + 1) %> (<%= block.getType() %>)</legend>
        <!-- The block type is passed as hidden data -->
        <input type="hidden" name="blockType" value="<%= block.getType() %>" />
        <% if ("image".equalsIgnoreCase(block.getType())) { %>
        <label for="blockImage<%= i %>">Upload Image:</label>
        <input type="file" id="blockImage<%= i %>" name="blockImage<%= i %>" accept="image/*" /><br/>
        <% if (block.getData() != null && !block.getData().isEmpty()) { %>
        <p>Current Image: <img src="<%= request.getContextPath() + "/" + block.getData() %>" alt="Image" style="max-width:200px;"/></p>
        <% } %>
        <% } else { %>
        <label for="blockData<%= i %>">Text:</label>
        <textarea id="blockData<%= i %>" name="blockData" rows="4" cols="50"><%= block.getData() %></textarea>
        <% } %>
    </fieldset>
    <%
        }
    %>

    <div class="button-group">
        <!-- "Add Image" button triggers partial submission -->
        <input type="submit" name="actionType" value="Add Image" />
        <!-- "Submit Note" finalizes the note -->
        <input type="submit" name="actionType" value="Submit Note" />
    </div>
</form>
</body>
</html>
