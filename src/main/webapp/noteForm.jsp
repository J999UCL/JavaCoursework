<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>
        <c:choose>
            <c:when test="${action eq 'edit'}">Edit Note</c:when>
            <c:otherwise>Create Note</c:otherwise>
        </c:choose>
    </title>
    <style>
        fieldset { margin-bottom: 1em; }
        .button-group input { margin-right: 1em; }
    </style>
</head>
<body>
<h2>
    <c:choose>
        <c:when test="${action eq 'edit'}">Edit Note</c:when>
        <c:otherwise>Create Note</c:otherwise>
    </c:choose>
</h2>

<form action="${pageContext.request.contextPath}/note/${action}" method="post" enctype="multipart/form-data">
    <input type="hidden" name="categoryPath" value="${categoryPath}" />

    <c:if test="${action eq 'edit' and not empty note}">
        <input type="hidden" name="noteId" value="${note.id}" />
    </c:if>

    <label for="title">Title:</label>
    <input type="text" id="title" name="title"
           value="${not empty title ? title : (not empty note ? note.title : '')}" /><br/><br/>

    <c:set var="currentBlocks" value="${blocks}" />
    <c:if test="${empty currentBlocks}">
        <c:set var="currentBlocks" value="${note.contentBlocks}" />
    </c:if>

    <c:choose>
        <c:when test="${empty currentBlocks}">
            <fieldset>
                <legend>Block 1 (text)</legend>
                <input type="hidden" name="blockType" value="text" />
                <label for="blockData0">Text:</label>
                <textarea id="blockData0" name="blockData" rows="4" cols="50"></textarea>
            </fieldset>
            <input type="hidden" name="blockCount" value="1" />
        </c:when>
        <c:otherwise>
            <input type="hidden" name="blockCount" value="${fn:length(currentBlocks)}" />
            <c:forEach var="block" items="${currentBlocks}" varStatus="status">
                <fieldset>
                    <legend>Block ${status.index + 1} (${block.type})</legend>
                    <input type="hidden" name="blockType" value="${block.type}" />
                    <c:choose>
                        <c:when test="${fn:toLowerCase(block.type) eq 'image'}">
                            <c:choose>
                                <c:when test="${empty block.data}">
                                    <label for="blockImage${status.index}">Upload Image:</label>
                                    <input type="file" id="blockImage${status.index}" name="blockImage${status.index}" accept="image/*" /><br/>
                                </c:when>
                                <c:otherwise>
                                    <p>Current Image: <img src="${pageContext.request.contextPath}/${block.data}" alt="Image" style="max-width:100px;"/></p>
                                    <!-- Preserve the image path so it is submitted back and not lost -->
                                    <input type="hidden" name="blockData" value="${block.data}" />
                                </c:otherwise>
                            </c:choose>
                        </c:when>
                        <c:otherwise>
                            <label for="blockData${status.index}">Text:</label>
                            <textarea id="blockData${status.index}" name="blockData" rows="4" cols="50">${block.data}</textarea>
                        </c:otherwise>
                    </c:choose>
                </fieldset>
            </c:forEach>
        </c:otherwise>
    </c:choose>

    <div class="button-group">
        <input type="submit" name="actionType" value="Add Image" />
        <input type="submit" name="actionType" value="Submit Note" />
    </div>
</form>
</body>
</html>