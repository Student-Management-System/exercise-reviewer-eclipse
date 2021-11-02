package net.ssehub.teaching.exercise_reviewer.eclipse;

import java.io.IOException;
import java.util.Properties;

import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import net.ssehub.teaching.exercise_reviewer.eclipse.dialog.AdvancedExceptionDialog;
import net.ssehub.teaching.exercise_reviewer.lib.Reviewer;
import net.ssehub.teaching.exercise_submitter.lib.ExerciseSubmitterFactory;
import net.ssehub.teaching.exercise_submitter.lib.ExerciseSubmitterManager;
import net.ssehub.teaching.exercise_submitter.lib.student_management_system.ApiException;
import net.ssehub.teaching.exercise_submitter.lib.student_management_system.AuthenticationException;
import net.ssehub.teaching.exercise_submitter.lib.student_management_system.NetworkException;
import net.ssehub.teaching.exercise_submitter.lib.student_management_system.UserNotInCourseException;

/**
 * The activator class controls the plug-in life cycle.
 */
public class Activator extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "exercise-reviewer-eclipse"; //$NON-NLS-1$

    // The shared instance
    private static Activator plugin;
    
    private Reviewer reviewer;

    /**
     * Creates an instance of the Activator.
     */
    public Activator() {
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }
    /**
     * Inits the reviewer.
     */
    public synchronized void initReviewer() {
        try {
            
            Properties prop = new Properties();
            prop.load(Activator.class.getClassLoader().getResourceAsStream("/config/config.properties"));
            //TODO remove hardcoded login
            Reviewer reviewer = new Reviewer("adam", "123456", "java-wise2021", prop.getProperty("mgmturl"),
                    prop.getProperty("authurl"));
            reviewer
                .withExerciseSubmitterServerUrl(prop.getProperty("exerciseSubmitterUrl"));
           
            this.reviewer = reviewer;
            
        } catch (NetworkException e) {
            AdvancedExceptionDialog.showUnexpectedExceptionDialog(e,
                "Failed to connect to student management system");
            // TODO: more user-friendly dialog?
        } catch (UserNotInCourseException e) {
            AdvancedExceptionDialog.showUnexpectedExceptionDialog(e,
                    "User not enrolled in course or course does not exist");
            // TODO: more user-friendly dialog?
        } catch (AuthenticationException e) {
            AdvancedExceptionDialog.showUnexpectedExceptionDialog(e, "Failed to log into student management system");
            // TODO: more user-friendly dialog
        } catch (ApiException e) {
            AdvancedExceptionDialog.showUnexpectedExceptionDialog(e, "Generic API exception");
        } catch (IOException e) {
            AdvancedExceptionDialog.showUnexpectedExceptionDialog(e, "Cant read config file");
        }
    }
    
    /**
     * Gets the reviewer or creates if not existing.
     * @return Reviewer
     */
    public synchronized Reviewer getReviewer() {
        if (reviewer == null) {
            initReviewer();
        }
        // TODO: this returns null if init failed and thus causes NullPointerExceptions all over the place
        return reviewer;
    }

    /**
     * Returns the shared instance.
     *
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

}
