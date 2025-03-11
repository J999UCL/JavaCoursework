<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Search Results</title>
</head>
<body>
<h2>Search Results</h2>
<ul>
  <c:forEach var="note" items="${searchResults}">
    <li>${note.index}: ${note.title} - ${note.content}</li>
  </c:forEach>
</ul>
</body>
</html>