package net.ssehub.teaching.exercise_reviewer.eclipse.actions;

import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;

import net.ssehub.teaching.exercise_reviewer.eclipse.listener.WorksetPropertyChangeListener;

/**
 * This class gets called on startup.
 *
 * @author lukas
 *
 */
public class StartUp implements IStartup {

    @Override
    public void earlyStartup() {
        PlatformUI.getWorkbench().getWorkingSetManager().addPropertyChangeListener(new WorksetPropertyChangeListener());
    }

}
