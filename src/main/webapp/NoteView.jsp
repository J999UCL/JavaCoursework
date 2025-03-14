<%@ page import="uk.ac.ucl.model.Note" %><%--
  Created by IntelliJ IDEA.
  User: Jeet
  Date: 12/03/2025
  Time: 11:47 am
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>View Note</title>
</head>
<body>
<div class="container">
    <header>
        <h1>View Note</h1>
    </header>
    <div class="note-details">
        <h2><%= ((Note) request.getAttribute("note")).getTitle() %></h2>
        <p><%= ((Note) request.getAttribute("note")).getContent() %></p>
        <form action="noteadd.jsp" method="get">
            <input type="hidden" name="note" value="<%= ((Note) request.getAttribute("note")).getIndex() %>">
            <button type="submit">Edit Note</button>
        </form>
    </div>
</div>
</body>
</html>