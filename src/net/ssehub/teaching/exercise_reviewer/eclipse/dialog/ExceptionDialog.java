package net.ssehub.teaching.exercise_reviewer.eclipse.dialog;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import net.ssehub.teaching.exercise_reviewer.eclipse.Activator;

/**
 * Utility class for showing error dialogs for exceptions.
 * 
 * @author Adam
 * @author Lukas
 */
public class ExceptionDialog {
    /**
     * No instances.
     */
    private ExceptionDialog() {
    }
    
    
    /**
     * Runs the given runnable in the GUI thread.
     * 
     * @param runnable The runnable to run.
     */
    private static void runInGuiThread(Runnable runnable) {
        if (Display.getDefault().getThread() != Thread.currentThread()) {
            Display.getDefault().asyncExec(runnable);
        } else {
            runnable.run();
        }
    }
    
    /**
     * Creates a dialog for an unexpected exception.
     * 
     * @param exc The unexpected exception.
     * @param reason A short description of the reason that caused the exception. Alternatively, a description of the
     *      operation that failed. E.g.: <code>"Failed to load preferences"</code>
     */
    public static void showUnexpectedExceptionDialog(Throwable exc, String reason) {
        StringWriter stacktrace = new StringWriter();
        exc.printStackTrace(new PrintWriter(stacktrace));
        
       
        IStatus inner = new Status(IStatus.ERROR, Activator.PLUGIN_ID, stacktrace.toString());
        MultiStatus status = new MultiStatus(Activator.PLUGIN_ID, IStatus.ERROR, new IStatus[] {inner}, reason, null);
        // TODO: the stack trace cannot be easily copied in this dialog... maybe use a different method?
        
        runInGuiThread(() -> ErrorDialog.openError(Display.getDefault().getActiveShell(), "Unexpected Error",
                "An unexpected error occured.", status));
    }
    /**
     * Creates the dialog for loginfailed.
     */
    public static void showLoginFailed() {
        runInGuiThread(() ->MessageDialog.openError(Display.getDefault().getActiveShell(), 
                "Exercise reviewer: Login failed",
                "Check username or password"));

    }
    /**
     * Creates the dialog if no Course gets selected.
     */
    public static void showCourseSelectionFailed() {
        runInGuiThread(() ->MessageDialog.openError(Display.getDefault().getActiveShell(), 
                 "Exercise reviewer: Course selection failed",
               "No Course selected. GOTO: Preferences -> Exercise Reviewer and select your Course"));

    }
    
    
    
}
