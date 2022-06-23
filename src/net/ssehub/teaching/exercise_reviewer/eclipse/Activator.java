package net.ssehub.teaching.exercise_reviewer.eclipse;

import java.io.IOException;
import java.util.Properties;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import net.ssehub.teaching.exercise_reviewer.eclipse.background.WaitForInternetConnection;
import net.ssehub.teaching.exercise_reviewer.eclipse.dialog.ExceptionDialog;
import net.ssehub.teaching.exercise_reviewer.eclipse.exception.ManagerNotConnected;
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

    private WaitForInternetConnection connectionJob = null;

    private boolean isConnected = false;
    private boolean isInit = false;

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
     * Initializes the {@link ExerciseSubmitterManager} with the username and
     * password from the preference store.
     * <p>
     * May be called multiple times, if the username or password in the preference
     * store change.
     * 
     * @throws IOException
     * @throws StorageException
     * @throws ApiException
     * @throws AuthenticationException
     * @throws NetworkException
     * @throws UserNotInCourseException
     */
    private synchronized void initManager() throws IOException, StorageException, UserNotInCourseException,
            NetworkException, AuthenticationException, ApiException {

        Properties prop = new Properties();
        prop.load(Activator.class.getResourceAsStream("config.properties"));

        String username = PreferencePage.SECURE_PREFERENCES.get(PreferencePage.KEY_USERNAME, "");
        String password = PreferencePage.SECURE_PREFERENCES.get(PreferencePage.KEY_PASSWORD, "");
        String courseid = PreferencePage.SECURE_PREFERENCES.get(PreferencePage.KEY_COURSEID, "");

        if (courseid.equals("")) {
            courseid = null;
            MessageDialog.openError(Display.getDefault().getActiveShell(), "No Course Selected",
                    "You need to select a course: Reviewer Settings -> select course");
        }

        EclipseLog.info("Creating manager with username " + username);
        ExerciseSubmitterFactory factory = new ExerciseSubmitterFactory();
        factory.withUsername(username).withPassword(password).withCourse(courseid)
                .withAuthUrl(prop.getProperty("authurl")).withMgmtUrl(prop.getProperty("mgmturl"))
                .withExerciseSubmitterServerUrl(prop.getProperty("exerciseSubmitterUrl"));
        this.manager = factory.build();

        boolean tutorrights = this.manager.getStudentManagementConnection()
                .hasTutorRights(new Course(courseid, courseid));
        if (!tutorrights) {
            ExceptionDialog.showNoTutorrights();
        }

        isConnected = true;

    }
    /**
     * Initialize the manager and handles the exceptions.
     * 
     */
    public synchronized void initManagerWithExceptionHandling() {
        try {
            initManager();
        } catch (NetworkException e) {
            ExceptionDialog.showConnectionCantBeEstabilished();
            isConnected = false;
        } catch (UserNotInCourseException e) {
            ExceptionDialog.showNotEnrolledInCourse();
            isConnected = false;
        } catch (AuthenticationException e) {
            ExceptionDialog.showLoginFailed();
            isConnected = false;
        } catch (ApiException e) {
            ExceptionDialog.showUnexpectedExceptionDialog(e, "Generic API exception");
            isConnected = false;
        } catch (IOException e) {
            ExceptionDialog.showConfigFileReadError();
            isConnected = false;
        } catch (StorageException e) {
            ExceptionDialog.showPreferenceReadError();
            isConnected = false;
        }

    }

    /**
     * Returns the {@link ExerciseSubmitterManager}. Manager is lazily initialized.
     *
     * @return The {@link ExerciseSubmitterManager}.
     * @throws ManagerNotConnected
     */
    public synchronized ExerciseSubmitterManager getManager() throws ManagerNotConnected {
        if (!this.isConnected && !this.isInit) {
            initManagerWithExceptionHandling();
            this.isInit = true;
        } else {
            if (connectionJob == null) {
                connectionJob = new WaitForInternetConnection(PLUGIN_ID, null, 10);
                connectionJob.addJobChangeListener(new IJobChangeListener() {

                    @Override
                    public void sleeping(IJobChangeEvent arg0) {
                    }

                    @Override
                    public void scheduled(IJobChangeEvent arg0) {
                    }

                    @Override
                    public void running(IJobChangeEvent arg0) {
                    }

                    @Override
                    public void done(IJobChangeEvent arg0) {
                        initManagerWithExceptionHandling();
                        isConnected = true;
                    }

                    @Override
                    public void awake(IJobChangeEvent arg0) {
                    }

                    @Override
                    public void aboutToRun(IJobChangeEvent arg0) {
                    }
                });
                connectionJob.schedule();
            } else if (connectionJob.getState() != Job.RUNNING) {
                connectionJob.schedule();
            }
            throw new ManagerNotConnected(ManagerNotConnected.NOINTERNETCONNECTION);
        }

        if (manager == null) {
            throw new ManagerNotConnected(ManagerNotConnected.NOINTERNETCONNECTION);
        }
        // TODO: this returns null if init failed and thus causes NullPointerExceptions
        // all over the place
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
     * Clears the current ExerciseManager.
     */
    public synchronized void clearManager() {
        this.manager = null;
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
    /**
     * Try`s to reconnect the reviewer with the server.
     * 
     * @return true if it was successful.
     */
    public synchronized boolean reConnect() {
        boolean worked = false;
        try {
            initManager();
            worked = true;
        } catch (IOException | StorageException | ApiException e) {
            worked = false;
        }
        return worked;
    }

}
