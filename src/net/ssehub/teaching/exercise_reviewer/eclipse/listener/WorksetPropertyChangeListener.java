package net.ssehub.teaching.exercise_reviewer.eclipse.listener;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkingSet;

/**
 * This class gets called when a workset is changed.
 * Reason for this Class is to catch the deletion of a workingset to delete all sub projects.
 * @author lukas
 *
 */
public class WorksetPropertyChangeListener implements IPropertyChangeListener {

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        System.out.println("");
        if (event.getProperty().equals("workingSetRemove")) {
            IWorkingSet workingset = (IWorkingSet) event.getOldValue();
            List<IProject> projects = new ArrayList<IProject>();
            for (IAdaptable adaptable : workingset.getElements()) {
                projects.add(((IAdaptable) adaptable).getAdapter(IProject.class));
            }
            //TODO: maybe to to in background
            for (IProject project : projects) {
                try {
                    project.delete(true, new NullProgressMonitor());
                } catch (CoreException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        
    }
    

}
