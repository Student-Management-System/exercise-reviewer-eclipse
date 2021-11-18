package net.ssehub.teaching.exercise_reviewer.eclipse.background;

import java.util.Optional;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ILock;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Shell;

/**
 * Abstract class for Eclipse Jobs.
 * @author lukas
 *
 */
public abstract class ReviewerJobs extends Job {

    private static ILock lock = Job.getJobManager().newLock();
    private Optional<Shell> shell;

    /**
     * Creates a new instance of Reviewerjobs.
     * @param name
     * @param shell
     */
    public ReviewerJobs(String name, Optional<Shell> shell) {
        super(name);
        this.shell = shell;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        try {
            lock.acquire();

            this.runAsync(monitor);

        } finally {

            lock.release();
        }

        return Status.OK_STATUS;
    }

    /**
     * Override this class and put your work in.
     * @param monitor
     */
    protected abstract void runAsync(IProgressMonitor monitor);
    /**
     * Gets the current shell.
     * @return Optional<Shell>
     */
    public Optional<Shell> getShell() {
        return this.shell;
    }



}
