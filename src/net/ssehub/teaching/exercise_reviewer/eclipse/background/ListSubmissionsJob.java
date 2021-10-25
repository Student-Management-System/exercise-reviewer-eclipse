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

import net.ssehub.teaching.exercise_reviewer.lib.data.Submission;

/**
 * sds.
 * 
 * @author lukas
 *
 */
public class ListSubmissionsJob extends Job {

    private static ILock lock = Job.getJobManager().newLock();
    private Consumer<ListSubmissionsJob> callbackCheckSubmission;

    /**
     * this.
     * 
     * @param callbackListSubmission
     */
    public ListSubmissionsJob(Consumer<ListSubmissionsJob> callbackListSubmission) {
        super("List Submission Job");
        this.callbackCheckSubmission = callbackListSubmission;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {

        try {
            lock.acquire();
            // Submission submission = new Submission();

            this.callbackCheckSubmission.accept(this);
            throw new IOException();

        } catch (IOException ex) {

        } finally {

            lock.release();
        }

        return Status.OK_STATUS;
    }

    /**
     * Return the submissionlist retrieved from the server.
     * 
     * @return List<String>
     */
    public List<Submission> getSubmissionList() {
        List<Submission> list = new ArrayList<Submission>();
        

        for (int i = 0; i < 100; i++) {
            Submission submission = new Submission("assignmentid:" + Integer.toString(i),
                    "userid:" + Integer.toString(i), "displayname:" + Integer.toString(i), 
                    "date:" + Integer.toString(i));
            list.add(submission);
        }

        return list;

    }

}
