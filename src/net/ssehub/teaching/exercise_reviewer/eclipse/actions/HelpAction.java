package net.ssehub.teaching.exercise_reviewer.eclipse.actions;


import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchWindow;

import net.ssehub.teaching.exercise_reviewer.eclipse.Activator;
import net.ssehub.teaching.exercise_reviewer.eclipse.background.IRunnableStuMgmt;
import net.ssehub.teaching.exercise_reviewer.eclipse.background.StuMgmtJob;
import net.ssehub.teaching.exercise_reviewer.eclipse.dialog.ExceptionDialog;
import net.ssehub.teaching.exercise_reviewer.eclipse.dialog.HelpDialog;
import net.ssehub.teaching.exercise_reviewer.eclipse.exception.ManagerNotConnected;
import net.ssehub.teaching.exercise_submitter.lib.ExerciseSubmitterManager;
import net.ssehub.teaching.exercise_submitter.lib.student_management_system.ApiException;

/**
 * This Class handles the HelpAction which is called from help in the
 *  menue Exercise Reviewer Settings.
 * 
 * @author lukas
 *
 */
public class HelpAction extends AbstractHandler {
    private IWorkbenchWindow window;

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        if (event.getApplicationContext() instanceof IEvaluationContext) {
            IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();

            window = (IWorkbenchWindow) context.getVariable(ISources.ACTIVE_WORKBENCH_WINDOW_NAME);
            
            createStuJob();
        
        
        }

        return null;
    }
    /**
     * Creates a stumgmtjob to check if the user has tutorrights for a specific course.
     */
    private void createStuJob() {
        ExerciseSubmitterManager manager;
        try {
            manager = Activator.getDefault().getManager();
        } catch (ManagerNotConnected e1) {
            ExceptionDialog.showConnectionCantBeEstabilished();
            return;
        }
        
        IRunnableStuMgmt<Boolean> func = new IRunnableStuMgmt<Boolean>() {
            
            @Override
            public Boolean run() {
                boolean result = false;
                try {
                    result = manager
                            .getStudentManagementConnection()
                            .hasTutorRights(manager.getCourse());
                } catch (ApiException | NullPointerException e) {
                    result = false;
                }
                return result;
            }
        };
        
        StuMgmtJob<Boolean> job = new StuMgmtJob<>("Get tutorights", func, this::onStuJobFinished);
        job.setUser(true);
        job.schedule();
        
        
    }
    /**
     * Called when the {@link #createStuJob()} is finished.
     * @param job
     */
    private void onStuJobFinished(StuMgmtJob<Boolean> job) {
       
        HelpDialog dialog = new HelpDialog(window.getShell(), Activator.getDefault().isConnected(), job.getOutput());
        Display.getDefault().syncExec(() -> dialog.open());
        
        
    }
}
