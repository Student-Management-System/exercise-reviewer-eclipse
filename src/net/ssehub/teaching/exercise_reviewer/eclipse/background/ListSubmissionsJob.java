package net.ssehub.teaching.exercise_reviewer.eclipse.background;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import net.ssehub.teaching.exercise_reviewer.eclipse.Activator;
import net.ssehub.teaching.exercise_reviewer.eclipse.dialog.ExceptionDialog;
import net.ssehub.teaching.exercise_reviewer.eclipse.exception.ManagerNotConnected;
import net.ssehub.teaching.exercise_reviewer.eclipse.log.EclipseLog;
import net.ssehub.teaching.exercise_submitter.lib.ExerciseSubmitterManager;
import net.ssehub.teaching.exercise_submitter.lib.data.Assignment;
import net.ssehub.teaching.exercise_submitter.lib.student_management_system.ApiException;

/**
 * This class retrieves the list of regsitered groups for an assignment.
 *
 * @author lukas
 *
 */
public class ListSubmissionsJob extends ReviewerJobs {

    private Consumer<ListSubmissionsJob> callbackCheckSubmission;
    private Optional<Set<String>> groupNames = Optional.empty();

    private Assignment currentAssignment;

    /**
     * this.
     *
     * @param parent
     * @param callbackListSubmission
     * @param assignment
     */
    public ListSubmissionsJob(Shell parent, Consumer<ListSubmissionsJob> callbackListSubmission,
            Assignment assignment) {
        super("List Submission Job", Optional.ofNullable(parent));

        this.callbackCheckSubmission = callbackListSubmission;
        this.currentAssignment = assignment;
    }

    @Override
    protected void runAsync(IProgressMonitor monitor) {
        EclipseLog.info("Start list submission job");
        this.retrieveAssessmentList();
        this.callbackCheckSubmission.accept(this);

    }
    
    /**
     * Retrieves the assessmentlist from the server.
     */
    private void retrieveAssessmentList() {

        ExerciseSubmitterManager manager;
        try {
            manager = Activator.getDefault().getManager();
        } catch (ManagerNotConnected e1) {
            this.groupNames = Optional.empty();
            return;
        }

        try {
            this.groupNames = Optional.of(
                    manager.getStudentManagementConnection().getAllGroups(manager.getCourse(), this.currentAssignment));

            Display.getDefault().asyncExec(() -> {
                MessageDialog.openInformation(this.getShell().get(), this.getName(), "Retrieved ");
            });

        } catch (ApiException e) {
            EclipseLog.error(e.getLocalizedMessage());
            Display.getDefault().syncExec(
                () -> ExceptionDialog.showUnexpectedExceptionDialog(e, 
                        "Cant retrieve group list \n Check Internet connection"));
        }
    }

    /**
     * Return the group list retrieved from the server.
     *
     * @return List<String>
     */
    public Optional<Set<String>> getGroupNames() {
        return this.groupNames;
    }

}
