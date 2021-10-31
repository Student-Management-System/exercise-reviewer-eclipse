package net.ssehub.teaching.exercise_reviewer.eclipse.log;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import net.ssehub.teaching.exercise_reviewer.eclipse.Activator;

/**
 * Utility methods for logging to the eclipse log.
 * 
 * @author Adam
 * @author Lukas
 */
public class EclipseLog {
    private static final ILog LOG = Activator.getDefault().getLog();

    /**
     * No instances.
     */
    private EclipseLog() {
    }
    
    /**
     * Logs an error message.
     * 
     * @param message The error text.
     */
    public static void error(String message) {
        LOG.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, message));
    }
    
    /**
     * Logs a warning message.
     * 
     * @param message The warning text.
     */
    public static void warning(String message) {
        LOG.log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, message));
    }
    
    /**
     * Logs an info message.
     * 
     * @param message The info text.
     */
    public static void info(String message) {
        LOG.log(new Status(IStatus.INFO, Activator.PLUGIN_ID, message));
    }


}
