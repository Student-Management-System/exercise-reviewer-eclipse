package net.ssehub.teaching.exercise_reviewer.eclipse.views;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

import net.ssehub.teaching.exercise_reviewer.eclipse.dialog.ExceptionDialog;

/**
 * This class handles the comment for the assessment.
 *
 * @author lukas
 *
 */
public class Comment {

    private IWorkbenchPage page;

    private String comment;

    private File file;
    private Optional<IEditorPart> editor;

    /**
     * Creates an instance of comment.
     *
     * @param page
     */
    public Comment(IWorkbenchPage page) {
        this.page = page;
        comment = "";
        this.createTempFile();
    }

    @Override
    public int hashCode() {
        return Objects.hash(comment);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Comment other = (Comment) obj;
        String one = comment;
        String two = other.comment;
        one.equals(two);
        return comment.equals(other.comment);
    }

    /**
     * Creates an temporary file.
     */
    private void createTempFile() {
        try {
            this.file = Files.createTempFile("comment", null).toFile();
            this.file.deleteOnExit();
        } catch (IOException e) {
            ExceptionDialog.showUnexpectedExceptionDialog(e, "Cant load comment");
        }
    }

    /**
     * Writes the current comment in the temp file.
     *
     * @throws IOException
     */
    private void writeCommentInFile() throws IOException {
        FileWriter fw = new FileWriter(this.file);
        fw.write(this.comment);
        fw.flush();
        fw.close();
    }

    /**
     * Sets the current comment for the assessment.
     * 
     * @param comment
     */
    public void setComment(String comment) {
        if (!this.comment.equals(comment)) {
            this.comment = comment;
            try {
                writeCommentInFile();
            } catch (IOException e) {
                ExceptionDialog.showUnexpectedExceptionDialog(e, "Cant save comment");
            }
        }
    }

    /**
     * Opens the editor with the current comment.
     *
     * @throws PartInitException
     */
    public void openEditor() throws PartInitException {
        IFileStore fileStore = EFS.getLocalFileSystem().getStore(this.file.toURI());
        this.editor = Optional.ofNullable(IDE.openEditorOnFileStore(this.page, fileStore));
    }

    /**
     * get the current comment loaded from the temp file.
     *
     * @return String
     */
    public String getComment() {
        Display.getDefault().syncExec(() -> {
            if (this.editor.isPresent()) {
                this.editor.get().doSave(new NullProgressMonitor());
            }
        });
        comment = readFileToString();
        
        return comment;

    }
    /**
     * Reads the file from the path where its located to string. Includes special characters-
     * 
     * @return String
     */
    private String readFileToString() {
        String fileString = "";
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String string;

            while ((string = br.readLine()) != null) {
                fileString += string;
                fileString += "\n";
            }
            br.close();
        } catch (IOException ex) {

            Display.getDefault().syncExec(() -> {
                ExceptionDialog.showUnexpectedExceptionDialog(ex, "Cant load comment");

            });
        }
        return fileString;

    }

    /**
     * Gets the tempfile.
     *
     * @return File
     */
    public File getPathToFile() {
        return this.file;
    }

}
