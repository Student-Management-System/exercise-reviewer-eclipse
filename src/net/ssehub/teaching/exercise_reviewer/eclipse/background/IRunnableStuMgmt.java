package net.ssehub.teaching.exercise_reviewer.eclipse.background;

/**
 * Interface for the StuMgmtjob.
 *
 * @author lukas
 *
 * @param <Output>
 */
public interface IRunnableStuMgmt<Output> {
    /**
     * Method tho give the StuMgtmJob.
     *
     * @return Output
     */
    public Output run();
}
