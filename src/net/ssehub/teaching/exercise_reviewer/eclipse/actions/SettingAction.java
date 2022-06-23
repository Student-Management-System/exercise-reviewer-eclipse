package net.ssehub.teaching.exercise_reviewer.eclipse.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchWindow;

import net.ssehub.teaching.exercise_reviewer.eclipse.Activator;
import net.ssehub.teaching.exercise_reviewer.eclipse.background.IRunnableStuMgmt;
import net.ssehub.teaching.exercise_reviewer.eclipse.background.StuMgmtJob;
import net.ssehub.teaching.exercise_reviewer.eclipse.dialog.CourseSelectionDialog;
import net.ssehub.teaching.exercise_reviewer.eclipse.dialog.ExceptionDialog;
import net.ssehub.teaching.exercise_reviewer.eclipse.preferences.PreferencePage;
import net.ssehub.teaching.exercise_submitter.lib.data.Course;
import net.ssehub.teaching.exercise_submitter.lib.student_management_system.ApiConnection;
import net.ssehub.teaching.exercise_submitter.lib.student_management_system.ApiException;

/**
 * Sample action.
 * @author lukas
 *
 */
public class SettingAction extends AbstractHandler {
    private IWorkbenchWindow window;

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        if (event.getApplicationContext() instanceof IEvaluationContext) {
            IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();

            window = (IWorkbenchWindow) context.getVariable(ISources.ACTIVE_WORKBENCH_WINDOW_NAME);
        
            Properties prop = new Properties();
            String username = "";
            String password = "";
            try {
                username =  PreferencePage.SECURE_PREFERENCES.get(PreferencePage.KEY_USERNAME, "");   
                password =  PreferencePage.SECURE_PREFERENCES.get(PreferencePage.KEY_PASSWORD, "");
                prop.load(Activator.class.getResourceAsStream("config.properties"));
            } catch (StorageException e1) {
                ExceptionDialog.showPreferenceReadError();
            } catch (IOException e) {
                ExceptionDialog.showConfigFileReadError();
            } 
            
            final String finalUsername = username;
            final String finalPassword = password;
            
            IRunnableStuMgmt<List<Course>> func = new IRunnableStuMgmt<List<Course>>() {
    
                @Override
                public List<Course> run() {
                    Set<Course> courses = null;
                    try {
                        ApiConnection connection = new ApiConnection(prop.getProperty("authurl"), 
                                prop.getProperty("mgmturl"));
                        connection.login(finalUsername, finalPassword);
                        courses = connection.getAllCourses();
                        
                    } catch (ApiException e) {
                        ExceptionDialog.showUnexpectedExceptionDialog(e, "Courses cant be downloaded");
                    }
                    List<Course> asList = new ArrayList<>(courses);
                    return asList;
                }
            };
        
            StuMgmtJob<List<Course>> job = new StuMgmtJob<>("List all Courses", func, this::onFinishedGetCourses);
            job.setUser(true);
            job.schedule();
        
        }

        return null;
    }
    /**
     * Called when the List all Course Job is finished.
     * 
     * @param job
     */
    private void onFinishedGetCourses(StuMgmtJob<List<Course>> job) {
        if (job.getOutput() != null) {
            Display.getDefault().syncExec(() -> {
                
                CourseSelectionDialog dialog = new CourseSelectionDialog(window.getShell()
                        , job.getOutput());
                dialog.open();
                
                if (dialog.getSelectedCourse().isPresent()) {
                    
                    try {
                        PreferencePage.SECURE_PREFERENCES.put(PreferencePage.KEY_COURSEID,
                                dialog.getSelectedCourse().get()
                                .getId(), true);
                        Activator.getDefault().initManagerWithExceptionHandling();
                    } catch (StorageException e) {
                        ExceptionDialog
                           .showUnexpectedExceptionDialog(e, "Cant save courseid");
                    } 
                    
                } else {
                    MessageDialog.openWarning(Display.getDefault().getActiveShell(),
                            "Course Selection", "No Course Selected");
                }
            });
   
        } 
    }
}
