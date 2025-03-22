<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
        /* Highlight editor on dragover */
        #editor.dragover {
            outline: 2px dashed #00f;
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

<form action="${pageContext.request.contextPath}/note/${action}" method="post" enctype="multipart/form-data">
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


    <!-- Hidden field for blockContainer -->
    <div id="blockContainer"></div>


    <!-- Toolbar for formatting and image insertion -->
    <div id="toolbar">
        <button type="button" onclick="wrapSelection('strong')"><b>Bold</b></button>
        <button type="button" onclick="wrapSelection('em')"><i>Italic</i></button>
        <button type="button" onclick="applyColor()">Color</button>
        <button type="button" onclick="createLink()">Link</button>

        <!-- Button to insert image -->
        <button type="button" id="insertImageButton">Insert Image</button>
        <!-- Hidden file input for images -->
        <input type="file" id="imageInput" style="display:none;" accept="image/*">


    </div>

    <!-- WYSIWYG Editor -->
    <div id="editor" contenteditable="true">
    </div>

    <div class="button-group" style="margin-top:1em;">
        <button type="submit" onclick="prepareBlocks()">Submit Note</button>
    </div>
</form>

<script>
    // Utility: Get current range in the editor.
    // If no selection exists, place caret at end of editor.
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
        // Move caret after the new element.
        range.setStartAfter(el);
        range.setEndAfter(el);
        sel.removeAllRanges();
        sel.addRange(range);
    }

    /**
     * Apply color by wrapping selection in a span with inline color style.
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
     * Create a hyperlink by wrapping selection in an <a> tag.
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

    /**
     * Insert an image as base64 into the editor using the provided logic.
     * This function uses the logic from your provided snippet.
     */
    document.getElementById('insertImageButton').addEventListener('click', function(){
        document.getElementById('imageInput').click();
    });
    const uploadedFiles = [];
    // When a file is selected, read it as base64 and insert it at the current caret position.
    document.getElementById('imageInput').addEventListener('change', function(event) {
        const file = event.target.files[0];
        if (!file) return;

        const reader = new FileReader();
        reader.onload = function(e) {
            const base64String = e.target.result; // The base64 string of the image.
            const img = document.createElement('img');
            img.src = base64String;              // Set the image source to the base64 string.
            img.style.maxWidth = "200px";         // Limit the image width for preview.
            img.setAttribute('data-base64', base64String); // Save the base64 data for later use.
            const editor = document.getElementById('editor');
            editor.focus();
            const range = getCurrentRange();
            range.insertNode(img);
            range.setStartAfter(img);
            range.collapse(true);
        };
        reader.readAsDataURL(file);
    });


    /**
     * On submission, parse the editor's top-level child nodes into blocks.
     * Each <img> becomes {type:"image", data: base64}, and everything else is a text block.
     */
    function prepareBlocks() {
        const container = document.getElementById('blockContainer');
        container.innerHTML = "";
        const editor = document.getElementById("editor");
        editor.childNodes.forEach(node => {
            let html = "";
            if (node.nodeType === Node.ELEMENT_NODE) {
                if (node.tagName === "IMG") {
                    const base64 = node.getAttribute('data-base64');
                    if (base64) {
                        html += '<input type="hidden" name="blockType[]" value="image" />';
                        // Send the base64 string from the data attribute.
                        html += '<input type="hidden" name="blockImage[]" value="' + encodeURIComponent(base64) + '" /><br/>';
                    }
                } else {
                    html += '<input type="hidden" name="blockType[]" value="text" />';
                    html += '<input type="hidden" name="blockData[]" value="' + encodeURIComponent(node.innerHTML) + '" />';
                }
            } else if (node.nodeType === Node.TEXT_NODE) {
                const txt = node.textContent.trim();
                if (txt.length > 0) {
                    html += '<input type="hidden" name="blockType[]" value="text" />';
                    html += '<input type="hidden" name="blockData[]" value="' + encodeURIComponent(txt) + '" />';
                }
            }
            if (html !== "") {
                container.innerHTML += html;
            }
        });

    }
</script>

</body>
</html>
