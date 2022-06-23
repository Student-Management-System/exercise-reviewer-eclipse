package net.ssehub.teaching.exercise_reviewer.eclipse.background;

import java.time.LocalDateTime;
import java.util.Optional;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Shell;

import net.ssehub.teaching.exercise_reviewer.eclipse.Activator;

/**
 * This class checks if a connection with the Server can be established.
 * 
 * @author lukas
 *
 */
public class WaitForInternetConnection extends ReviewerJobs {

    private int timeBetweenPingInSec = 1;
    private LocalDateTime lastPingTime;

    /**
     * Creates a new instance of {@link WaitForInternetConnection}.
     * 
     * @param name
     * @param shell
     * @param timeBetweenPingInSec
     */
    public WaitForInternetConnection(String name, Optional<Shell> shell, int timeBetweenPingInSec) {
        super(name, shell);
        this.timeBetweenPingInSec = timeBetweenPingInSec;
        this.lastPingTime = LocalDateTime.now();
    }

    @Override
    protected void runAsync(IProgressMonitor monitor) {

        while (!Activator.getDefault().isConnected()) {
            if (!lastPingTime.plusSeconds(timeBetweenPingInSec).isAfter(LocalDateTime.now())) {
                lastPingTime = LocalDateTime.now();
                System.out.println("tic");
                if (Activator.getDefault().reConnect()) {
                    break;
                }
            }

        }

    }

}
