package net.ssehub.teaching.exercise_reviewer.eclipse.views;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import net.ssehub.teaching.exercise_reviewer.eclipse.dialog.AdvancedExceptionDialog;

/**
 * This class handles the comment for the assessment.
 * @author lukas
 *
 */
public class Comment {
    
    private static IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
            .getActivePage();
    
    private String comment;
    
    private File file;
    private Optional<IEditorPart> editor;
    
    /**
     * Creates an instance of comment.
     * @param comment
     */
    public Comment(String comment) {
        this.comment = comment;
        this.createTempFile();
    }
    /**
     * Creats an temporary file.
     */
    private void createTempFile() {
        try {
            this.file =  Files.createTempFile("comment", null).toFile();
            this.file.deleteOnExit();
            writeCommentInFile();
        } catch (IOException e) {
            AdvancedExceptionDialog.showUnexpectedExceptionDialog(e, "Cant load comment");
        }
    }
    /**
     * Writes the current comment in the temp file.
     * @throws IOException
     */
    private void writeCommentInFile() throws IOException {
        FileWriter fw = new FileWriter(this.file);
        fw.write(this.comment);
        fw.flush();
        fw.close();
    }
    /**
     * Opens the editor with the current comment.
     * @throws PartInitException
     */
    public void openEditor() throws PartInitException {
        IFileStore fileStore = EFS.getLocalFileSystem().getStore(this.file.toURI());
        editor = Optional.ofNullable(IDE.openEditorOnFileStore(page, fileStore));
        
    }
    /**
     * get the current comment loaded from the temp file.
     * @return String
     */
    public String getComment() {
        if (editor.isPresent()) {
            editor.get().doSave(new NullProgressMonitor());
        }
        String stringcomment = null;
        try {
            stringcomment = Files.readString(this.file.toPath());
        } catch (IOException e) {
            AdvancedExceptionDialog.showUnexpectedExceptionDialog(e, "Cant load comment");
        }
        
        return stringcomment;
        
    }
    /**
     * Gets the tempfile.
     * @return File
     */
    public File getPathToFile() {
        return this.file;
    }

}
