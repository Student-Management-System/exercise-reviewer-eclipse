package net.ssehub.teaching.exercise_reviewer.eclipse.listener;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkingSet;


import net.ssehub.teaching.exercise_reviewer.eclipse.background.IRunnableStuMgmt;
import net.ssehub.teaching.exercise_reviewer.eclipse.background.StuMgmtJob;

/**
 * This class gets called when a workingset is changed.
 * Reason for this Class is to catch the deletion of a workingset to delete all sub projects.
 * 
 * @author lukas
 *
 */
public class WorksetPropertyChangeListener implements IPropertyChangeListener {

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getProperty().equals("workingSetRemove")) {
            IWorkingSet workingset = (IWorkingSet) event.getOldValue();
            List<IProject> projects = new ArrayList<IProject>();
            for (IAdaptable adaptable : workingset.getElements()) {
                projects.add(adaptable.getAdapter(IProject.class));
            }
            //TODO: maybe to to in background
            IRunnableStuMgmt<Boolean> func = new IRunnableStuMgmt<Boolean>() {
                
                @Override
                public Boolean run() {
                    for (IProject project : projects) {
                        try {
                            project.delete(true, new NullProgressMonitor());
                        } catch (CoreException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                    return true;
                }
            };
          
            boolean dialogResult = MessageDialog.openQuestion(new Shell(), 
                    "Workingset", "Should the SubProjects be deleted as well");
           
            if (dialogResult) {
                StuMgmtJob<Boolean> job = new StuMgmtJob<Boolean>("Delete Projects", func, this::onDeletingFinished);
                job.setUser(true);
                job.schedule();
            }
            
        }
        
    }
    /**
     * Gets called when the deleting job is done.
     * @param job
     */
    private void onDeletingFinished(StuMgmtJob<Boolean> job) {
        
    }
    

}
