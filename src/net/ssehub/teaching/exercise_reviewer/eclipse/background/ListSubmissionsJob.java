package net.ssehub.teaching.exercise_reviewer.eclipse.background;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ILock;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import net.ssehub.teaching.exercise_reviewer.eclipse.Activator;
import net.ssehub.teaching.exercise_reviewer.eclipse.dialog.AdvancedExceptionDialog;
import net.ssehub.teaching.exercise_reviewer.lib.Reviewer;
import net.ssehub.teaching.exercise_reviewer.lib.data.Assessment;
import net.ssehub.teaching.exercise_submitter.lib.data.Assignment;
import net.ssehub.teaching.exercise_submitter.lib.student_management_system.ApiException;

/**
 * sds.
 *
 * @author lukas
 *
 */
public class ListSubmissionsJob extends Job {

    private static ILock lock = Job.getJobManager().newLock();
    private Shell shell;
    private Consumer<ListSubmissionsJob> callbackCheckSubmission;
    private Optional<List<Assessment>> assessmentlist = Optional.empty();

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
        super("List Submission Job");
        this.shell = parent;
        this.callbackCheckSubmission = callbackListSubmission;
        this.currentAssignment = assignment;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {

        try {
            lock.acquire();
            // Submission submission = new Submission();
            this.retrieveAssessmentList();
            this.callbackCheckSubmission.accept(this);

        } finally {

            lock.release();
        }

        return Status.OK_STATUS;
    }

    /**
     * Retrieves the assessmentlist from the server.
     */
    private void retrieveAssessmentList() {

        Reviewer reviewer = Activator.getDefault().getReviewer();

        try {
            this.assessmentlist = Optional.ofNullable(reviewer.getAllSubmissionsFromAssignment(this.currentAssignment));
//            Display.getCurrent().asyncExec(() -> {
//                //TODO: get shell
//                MessageDialog.openInformation(new Shell(), getName(), getName());
//            });

        } catch (ApiException e) {
            Display.getCurrent().syncExec(
                () -> AdvancedExceptionDialog.showUnexpectedExceptionDialog(e, "Cant retrieve assessment list"));
        }
    }

    /**
     * Return the submissionlist retrieved from the server.
     *
     * @return List<String>
     */
    public Optional<List<Assessment>> getAssessmentList() {
        return this.assessmentlist;
    }

}
