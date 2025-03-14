<%@ page import="uk.ac.ucl.model.Note" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="header.jsp" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Notes App</title>
    <style>
        .note-card { border: 1px solid #ddd; padding: 10px; margin: 10px 0; cursor: pointer; }
        .search-bar { margin-bottom: 10px; }
        .sort-options { margin-bottom: 20px; }
    </style>
</head>
<body>
<div class="container">
    <header>
        <h1>Notes App</h1>
    </header>

    <!-- Search Bar -->
    <div class="search-bar">
        <form method="get" action="viewNotes">
            <input type="text" name="search" placeholder="Search notes..." value="<%= request.getParameter("search") != null ? request.getParameter("search") : "" %>">
            <button type="submit">Search</button>
        </form>
    </div>

    <!-- Sorting Options -->
    <div class="sort-options">
        <form method="get" action="viewNotes">
            <select name="sortBy" onchange="this.form.submit()">
                <option value="">Sort by</option>
                <option value="title" <%= request.getParameter("sortBy") != null && request.getParameter("sortBy").equals("title") ? "selected" : "" %>>Title</option>
                <option value="date" <%= request.getParameter("sortBy") != null && request.getParameter("sortBy").equals("date") ? "selected" : "" %>>Date</option>
            </select>
        </form>
    </div>

    <div class="header-container">
        <h2>Notes</h2>
        <div class="new-note-btn">
            <form action="noteadd.jsp" method="get">
                <button type="submit" class="btn">New Note</button>
            </form>
        </div>
    </div>

    <!-- Notes List -->
    <div id="notesList">
        <%-- Form to trigger <form method="get" action="">the GET request to the ViewNotesServlet --%>

            <ul>
                <%
                    // Retrieve the 'notes' attribute from the request
                    List<uk.ac.ucl.model.Note> notes = (List<uk.ac.ucl.model.Note>) request.getAttribute("notes");

                    // Check if the 'notes' list is available and not empty
                    if (notes != null && !notes.isEmpty()) {
                        // Loop through the list and display each note
                        for (uk.ac.ucl.model.Note note : notes) {
                %>
                <li>
                    <form method="get" action="viewNote">
                        <button type="submit" name="index" value="<%= note.getIndex() %>">
                            <%= note.getIndex() %>: <%= note.getTitle() %> - <%= note.getContent() %>
                        </button>
                    </form>
                </li>
                <%
                    }
                } else {
                %>
                <li>No notes available</li>
                <%
                    }
                %>
            </ul>

    </div>


</div>
</body>
</html>
