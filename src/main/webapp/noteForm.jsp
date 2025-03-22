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
        /* Toolbar styling */
        #toolbar {
            background: #f9f9f9;
            border: 1px solid #ccc;
            margin-bottom: 1em;
            padding: 5px;
        }
        #toolbar button, #toolbar label {
            margin-right: 0.5em;
            cursor: pointer;
        }
        /* Editor styling */
        #editor {
            border: 1px solid #ccc;
            min-height: 250px;
            padding: 8px;
        }
        #editor img {
            max-width: 200px;
            margin: 0.5em 0;
            cursor: pointer;
        }
        /* Button group */
        .button-group button {
            margin-right: 0.5em;
        }
    </style>
</head>
<body>

<h2>
    <c:choose>
        <c:when test="${action eq 'edit'}">Edit Note</c:when>
        <c:otherwise>Create Note</c:otherwise>
    </c:choose>
</h2>

<!--
     enctype must be multipart/form-data so that file inputs are included
     in the POST request.
-->
<form action="${pageContext.request.contextPath}/note/${action}"
      method="post"
      enctype="multipart/form-data"
      onsubmit="syncEditorContent()">

    <input type="hidden" name="categoryPath" value="${categoryPath}" />

    <!-- If editing, include the note's ID -->
    <c:if test="${action eq 'edit' and not empty note}">
        <input type="hidden" name="noteId" value="${note.id}" />
    </c:if>

    <!-- Note title -->
    <label for="title">Title:</label>
    <input type="text" id="title" name="title"
           value="${not empty note ? note.title : ''}" />
    <br/><br/>

    <!-- Toolbar for formatting and image insertion -->
    <div id="toolbar">
        <button type="button" onclick="wrapSelection('strong')"><b>Bold</b></button>
        <button type="button" onclick="wrapSelection('em')"><i>Italic</i></button>
        <button type="button" onclick="applyColor()">Color</button>
        <button type="button" onclick="createLink()">Link</button>

        <!-- Button to insert image -->
        <button type="button" id="insertImageButton">Insert Image</button>

        <!-- A single visible file input for picking images (multiple times).
             Each selection will be cloned and placed into the form, allowing
             multiple picks that accumulate. -->
        <input type="file"
               id="imageInput"
               name="images[]"
               style="display:none;"
               accept="image/*"
               multiple>
    </div>

    <!-- WYSIWYG Editor -->
    <div id="editor" contenteditable="true">
        <c:if test="${action eq 'edit' and not empty note}">
            <c:if test="${not empty note.contentBlocks}">
                <c:forEach var="block" items="${note.contentBlocks}">
                    <c:choose>
                        <c:when test="${block.type eq 'text'}">
                            <p>
                                <c:out value="${block.data}" />
                            </p>
                        </c:when>
                        <c:when test="${block.type eq 'image'}">
                            <c:set var="imgData" value="${images[block.data]}" />
                            <c:choose>
                                <c:when test="${not empty imgData}">
                                    <img src="${imgData}"
                                         alt="Image Block"
                                         style="max-width:100%;"
                                         data-existing="true"
                                         data-ref="${block.data}" />
                                </c:when>
                                <c:otherwise>
                                    <p>[Image not available]</p>
                                </c:otherwise>
                            </c:choose>
                        </c:when>
                        <c:otherwise>
                            <p>
                                <c:out value="${block.data}" />
                            </p>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>
            </c:if>
        </c:if>
    </div>


    <textarea id="editorContent" name="editorContent" style="display:none;"></textarea>

    <div class="button-group" style="margin-top:1em;">
        <button type="submit">Submit Note</button>
    </div>
</form>


<script>
    /**
     * Whenever the form is submitted, copy the current editor HTML into
     * the hidden <textarea> so it will be sent with the form.
     */
    function syncEditorContent() {
        document.getElementById('editorContent').value = document.getElementById('editor').innerHTML;
    }

    /**
     * Simple utility to get or ensure a valid range in #editor.
     */
    function getCurrentRange() {
        const editor = document.getElementById('editor');
        const sel = window.getSelection();
        if (sel.rangeCount === 0) {
            const range = document.createRange();
            range.selectNodeContents(editor);
            range.collapse(false);
            sel.addRange(range);
            return range;
        }
        return sel.getRangeAt(0);
    }

    /**
     * Wrap the current selection with a tag (e.g., <strong> or <em>)
     */
    function wrapSelection(tagName) {
        const sel = window.getSelection();
        if (!sel.rangeCount) return;
        const range = sel.getRangeAt(0);
        if (range.collapsed) {
            alert("Please select some text first.");
            return;
        }
        const frag = range.extractContents();
        const el = document.createElement(tagName);
        el.appendChild(frag);
        range.insertNode(el);
        // Move caret after the new element
        range.setStartAfter(el);
        range.setEndAfter(el);
        sel.removeAllRanges();
        sel.addRange(range);
    }

    /**
     * Apply color by wrapping selection in a <span style="color:...">
     */
    function applyColor() {
        const color = prompt("Enter a color or hex code:", "#ff0000");
        if (!color) return;
        const sel = window.getSelection();
        if (!sel.rangeCount) return;
        const range = sel.getRangeAt(0);
        if (range.collapsed) {
            alert("Select text to color first.");
            return;
        }
        const frag = range.extractContents();
        const span = document.createElement('span');
        span.style.color = color;
        span.appendChild(frag);
        range.insertNode(span);
        range.setStartAfter(span);
        range.setEndAfter(span);
        sel.removeAllRanges();
        sel.addRange(range);
    }

    /**
     * Create a hyperlink by wrapping selection in an <a href="..." target="_blank">
     */
    function createLink() {
        const url = prompt("Enter a URL:", "https://");
        if (!url) return;
        const sel = window.getSelection();
        if (!sel.rangeCount) return;
        const range = sel.getRangeAt(0);
        if (range.collapsed) {
            alert("Select text to link first.");
            return;
        }
        const frag = range.extractContents();
        const a = document.createElement('a');
        a.href = url;
        a.target = "_blank";
        a.appendChild(frag);
        range.insertNode(a);
        range.setStartAfter(a);
        range.setEndAfter(a);
        sel.removeAllRanges();
        sel.addRange(range);
    }

    // Handle multiple image picks that accumulate
    const insertImageButton = document.getElementById('insertImageButton');
    const fileInput = document.getElementById('imageInput');
    const editorDiv = document.getElementById('editor');
    const form = document.querySelector('form');

    // Clicking "Insert Image" triggers file input
    insertImageButton.addEventListener('click', () => {
        fileInput.click();
    });

    fileInput.addEventListener('change', (event) => {
        const files = event.target.files;
        if (!files || files.length === 0) {
            return;
        }

        // Preview each selected image in the editor
        for (let i = 0; i < files.length; i++) {
            const file = files[i];
            const img = document.createElement('img');
            img.src = URL.createObjectURL(file);
            img.style.maxWidth = "200px";
            editorDiv.appendChild(img);
        }

        // Clone the original file input (with all selected files)
        const hiddenInput = fileInput.cloneNode(true);
        hiddenInput.style.display = 'none';
        // It's often best to remove the 'id' to avoid duplicates:
        hiddenInput.removeAttribute('id');

        // Append the clone into the form so these files get submitted
        form.appendChild(hiddenInput);

        // Reset the original input, so user can pick more images next time
        fileInput.value = "";
    });
</script>

</body>
</html>
