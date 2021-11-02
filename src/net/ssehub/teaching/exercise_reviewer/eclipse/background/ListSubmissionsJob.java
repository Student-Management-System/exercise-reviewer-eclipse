package net.ssehub.teaching.exercise_reviewer.eclipse.background;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.SubmissionPublisher;
import java.util.function.Consumer;


import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ILock;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

import net.ssehub.teaching.exercise_reviewer.eclipse.Activator;
import net.ssehub.teaching.exercise_reviewer.eclipse.dialog.AdvancedExceptionDialog;
import net.ssehub.teaching.exercise_reviewer.lib.Reviewer;
import net.ssehub.teaching.exercise_reviewer.lib.data.Assessment;
import net.ssehub.teaching.exercise_reviewer.lib.student_management_system.ApiConnection;
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
    private Consumer<ListSubmissionsJob> callbackCheckSubmission;
    private Optional<List<Assessment>> assessmentlist = Optional.empty();
    
    private Assignment currentAssignment;

    /**
     * this.
     * 
     * @param callbackListSubmission
     * @param assignment
     */
    public ListSubmissionsJob(Consumer<ListSubmissionsJob> callbackListSubmission, Assignment assignment) {
        super("List Submission Job");
        this.callbackCheckSubmission = callbackListSubmission;
        this.currentAssignment = assignment;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {

        try {
            lock.acquire();
            // Submission submission = new Submission();
            retrieveAssessmentList();
            this.callbackCheckSubmission.accept(this);
            throw new IOException();

        } catch (IOException ex) {

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
            assessmentlist = Optional.ofNullable(reviewer.getAllSubmissionsFromAssignment(this.currentAssignment));
        } catch (ApiException e) {
            Display.getCurrent().syncExec(() -> 
                AdvancedExceptionDialog.showUnexpectedExceptionDialog(e, "Cant retrieve assessment list"));
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
