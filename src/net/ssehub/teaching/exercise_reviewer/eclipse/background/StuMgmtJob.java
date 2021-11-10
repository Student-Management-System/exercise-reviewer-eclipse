package net.ssehub.teaching.exercise_reviewer.eclipse.background;

import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This class handles the StuMgmt comnnection johbs for eclipse.
 *
 * @author lukas
 *
 * @param <Output>
 */
public class StuMgmtJob<Output> extends ReviewerJobs {
    private IRunnableStuMgmt<Output> runnable;
    private Output output;
    private Consumer<StuMgmtJob<Output>> callback;

    /**
     * Creates a instance of {@link StuMgmtJob}.
     *
     * @param name
     * @param runnable
     * @param callback
     */
    public StuMgmtJob(String name, IRunnableStuMgmt<Output> runnable, Consumer<StuMgmtJob<Output>> callback) {
        super(name, Optional.empty());
        this.runnable = runnable;
        this.callback = callback;
    }

    @Override
    protected void runAsync(IProgressMonitor monitor) {
        this.output = this.runnable.run();
        this.callback.accept(this);
    }

    /**
     * Gets the output.
     *
     * @return Output
     */
    public Output getOutput() {
        return this.output;
    }

}
