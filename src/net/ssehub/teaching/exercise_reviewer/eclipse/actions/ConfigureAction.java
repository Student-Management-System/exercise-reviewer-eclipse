package net.ssehub.teaching.exercise_reviewer.eclipse.actions;


import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.dialogs.PreferencesUtil;



/**
 * This Class handles the ConfigureAction which is called from Confiogure in the
 *  menue Exercise Reviewer Settings.
 * 
 * @author lukas
 *
 */
public class ConfigureAction extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        String id = "net.ssehub.teaching.exercise_reviewer.eclipse.preferences.PreferencePage";
        PreferencesUtil.createPreferenceDialogOn(getShell(event), id, new String[] {id}, null).open();
        return null;
    }
   /**
    * Gets the shell of of an {@see #ExecutionEvent} or gets the active display shell.
    * @param event
    * @return the shell
    */
    private Shell getShell(ExecutionEvent event) {
        IWorkbenchWindow window = null;
        if (event.getApplicationContext() instanceof IEvaluationContext) {
            IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();

            window = (IWorkbenchWindow) context.getVariable(ISources.ACTIVE_WORKBENCH_WINDOW_NAME);
        }
        
        Shell shell;
        if (window != null) {
            shell = window.getShell();
        } else {
            shell = Display.getCurrent().getActiveShell();
        }
        
        return shell;
    }
}
