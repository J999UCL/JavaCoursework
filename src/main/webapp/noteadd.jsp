<%@ page import="uk.ac.ucl.model.Note" %>
<%
  Note note = (Note) request.getAttribute("note");
  String indexParam = request.getParameter("index");

  // Decide which "index" value to use
  String finalIndex = "";
  if (indexParam != null && !indexParam.isEmpty()) {
    // Otherwise, if an indexParam is present, use that
    finalIndex = indexParam;
  }




%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title><%= (note != null) ? "Edit Note" : "Add Note" %></title>
</head>
<body>
<h2><%= (note != null) ? "Edit Note" : "Add Note" %></h2>

<form action="modifyNote" method="post">
  <!-- Single hidden input for index -->
  <input type="hidden" name="index" value="<%= finalIndex %>">

  <label for="title">Title:</label>
  <input type="text" id="title" name="title"
         value="<%= (note != null) ? note.getTitle() : "" %>"
         required><br>

  <label for="content">Content:</label>
  <textarea id="content" name="content" required>
    <%= (note != null) ? note.getContent() : "" %>
  </textarea><br>

  <label for="category">Category:</label>
  <input type="text" id="category" name="category"
         value="<%= (note != null) ? note.getCategory() : "" %>"
         required><br>

  <label for="imageUrl">Image URL:</label>
  <input type="text" id="imageUrl" name="imageUrl"
         value="<%= (note != null) ? note.getImageUrl() : "" %>"><br>

  <button type="submit">
    <%= (note != null) ? "Update Note" : "Add Note" %>
  </button>
</form>
</body>
</html>
