package net.ssehub.teaching.exercise_reviewer.eclipse.actions;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchWindow;

import net.ssehub.teaching.exercise_reviewer.eclipse.Activator;
import net.ssehub.teaching.exercise_reviewer.eclipse.background.IRunnableStuMgmt;
import net.ssehub.teaching.exercise_reviewer.eclipse.background.StuMgmtJob;
import net.ssehub.teaching.exercise_reviewer.eclipse.dialog.CourseSelectionDialog;
import net.ssehub.teaching.exercise_reviewer.eclipse.dialog.ExceptionDialog;
import net.ssehub.teaching.exercise_reviewer.eclipse.preferences.PreferencePage;
import net.ssehub.teaching.exercise_submitter.lib.data.Course;
import net.ssehub.teaching.exercise_submitter.lib.student_management_system.ApiException;

import org.eclipse.swt.widgets.Display;

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
        
            IRunnableStuMgmt<List<Course>> func = new IRunnableStuMgmt<List<Course>>() {
    
                @Override
                public List<Course> run() {
                    List<Course> courses = null;
                    try {
                        courses = Activator.getDefault().getManager().getStudentManagementConnection().getAllCourses();
                        
                    } catch (ApiException e) {
                        ExceptionDialog.showUnexpectedExceptionDialog(e, "Courses cant be downloaded");
                    }
                    // TODO Auto-generated method stub
                    return courses;
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
                        Activator.getDefault().getManager().setCourse(dialog.getSelectedCourse().get());
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
