<%@ page import="uk.ac.ucl.model.Note" %>
<%@ page import="java.util.List" %><%--
  Created by IntelliJ IDEA.
  User: Jeet
  Date: 10/03/2025
  Time: 6:57 pm
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>View Notes</title>
</head>
<body>
<h2>Notes</h2>
<ul>
  <%
    // Retrieve the 'notes' attribute from the request
    List<uk.ac.ucl.model.Note> notes = (List<uk.ac.ucl.model.Note>) request.getAttribute("notes");

    // Check if the 'notes' list is available and not empty
    if (notes != null && !notes.isEmpty()) {
      // Loop through the list and display each note
      for (uk.ac.ucl.model.Note note : notes) {
  %>
  <li><%= note.getIndex() %>: <%= note.getTitle() %> - <%= note.getContent() %></li>
  <%
    }
  } else {
  %>
  <li>No notes available.</li>
  <%
    }
  %>
</ul>
</body>
</html>
