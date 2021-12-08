package net.ssehub.teaching.exercise_reviewer.eclipse;

import java.io.IOException;
import java.util.Properties;

import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import net.ssehub.teaching.exercise_reviewer.eclipse.dialog.AdvancedExceptionDialog;
import net.ssehub.teaching.exercise_reviewer.eclipse.log.EclipseLog;
import net.ssehub.teaching.exercise_reviewer.eclipse.preferences.PreferencePage;
import net.ssehub.teaching.exercise_reviewer.eclipse.preferences.ProjectManager;
import net.ssehub.teaching.exercise_submitter.lib.ExerciseSubmitterFactory;
import net.ssehub.teaching.exercise_submitter.lib.ExerciseSubmitterManager;
import net.ssehub.teaching.exercise_submitter.lib.data.Course;
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

    private ExerciseSubmitterManager manager;

    private ProjectManager projectmanager;
    
    private boolean isConnected = false;

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
     * Initializes the {@link ExerciseSubmitterManager} with the username and password from the preference store.
     * <p>
     * May be called multiple times, if the username or password in the preference store change.
     */
    public synchronized void initManager() {
        try {

            Properties prop = new Properties();
            prop.load(Activator.class.getResourceAsStream("config.properties"));

            String username =  PreferencePage.SECURE_PREFERENCES.get(PreferencePage.KEY_USERNAME, ""); 
            String password =  PreferencePage.SECURE_PREFERENCES.get(PreferencePage.KEY_PASSWORD, "");
            

            EclipseLog.info("Creating manager with username " + username);
            ExerciseSubmitterFactory factory = new ExerciseSubmitterFactory();
            factory
            .withUsername(username)
            .withPassword(password)
            .withCourse(prop.getProperty("courseid"))
            .withAuthUrl(prop.getProperty("authurl"))
            .withMgmtUrl(prop.getProperty("mgmturl"))
                .withExerciseSubmitterServerUrl(prop.getProperty("exerciseSubmitterUrl"));
            this.manager = factory.build();
            
            boolean tutorrights = this.manager.getStudentManagementConnection()
                    .hasTutorRights(new Course(prop.getProperty("courseid")
                    , prop.getProperty("courseid")));
            if (!tutorrights) {
                Display.getDefault().syncExec(() -> MessageDialog.openError(
                        Display.getDefault().getActiveShell(), "Login failed - Eclipse Reviewer",
                        "In Course " + this.manager.getCourse().getId() + " not registered as a Tutor"));
            } 
            
            isConnected = true;

        } catch (NetworkException e) {
            AdvancedExceptionDialog.showUnexpectedExceptionDialog(e, "Failed to connect to student management system \n"
                    + "Check you internet connection");
            isConnected = false;
            // TODO: more user-friendly dialog?
        } catch (UserNotInCourseException e) {
            AdvancedExceptionDialog.showUnexpectedExceptionDialog(e,
                    "User not enrolled in course or course does not exist");
            isConnected = false;
            // TODO: more user-friendly dialog?
        } catch (AuthenticationException e) {
            Display.getDefault().syncExec(() -> MessageDialog.openError(
                    Display.getCurrent().getActiveShell(), 
                    "Auth Error", "Cant login. Please check your username and password"));
            isConnected = false;
            // TODO: more user-friendly dialog
        } catch (ApiException e) {
            AdvancedExceptionDialog.showUnexpectedExceptionDialog(e, "Generic API exception");
            isConnected = false;
        } catch (IOException e) {
            AdvancedExceptionDialog.showUnexpectedExceptionDialog(e, "Cant read config file");
            isConnected = false;
        } catch (StorageException e) {
            AdvancedExceptionDialog.showUnexpectedExceptionDialog(e, "Failed to load login data from preferences");
            isConnected = false;
        }
        
        
    }

    /**
     * Returns the {@link ExerciseSubmitterManager}. Manager is lazily initialized.
     *
     * @return The {@link ExerciseSubmitterManager}.
     */
    public synchronized ExerciseSubmitterManager getManager() {
        if (this.manager == null || !this.isConnected) {
            initManager();
        }
        // TODO: this returns null if init failed and thus causes NullPointerExceptions all over the place
        return this.manager;
    }
    /**
     * Returns the {@link #projectmanager}. Manager is lazily initialized.
     *
     * @return The {@link #projectmanager}.
     */
    public synchronized ProjectManager getProjectManager() {
        if (this.projectmanager == null) {
            this.projectmanager = new ProjectManager();
        }

        return this.projectmanager;
    }

    /**
     * Checks whether the {@link ExerciseSubmitterManager} is initialized.
     *
     * @return Whether the manager is intialized.
     */
    public synchronized boolean isManagerInitialized() {
        return this.manager != null;
    }


    /**
     * Returns the shared instance.
     *
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

    /**
     * Returns the if the manager is connected successfully.
     * 
     * @return is connected.
     */
    public synchronized boolean isConnected() {
        return isConnected;
    }

}
