package net.ssehub.teaching.exercise_reviewer.eclipse.listener;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import net.ssehub.teaching.exercise_reviewer.eclipse.Activator;
import net.ssehub.teaching.exercise_reviewer.eclipse.views.ReviewView;


/**
 * This class handles the project change selection event.
 *
 * @author lukas
 *
 */
public class ProjectSelectionListener implements ISelectionListener {

    @Override
    public void selectionChanged(IWorkbenchPart arg0, ISelection newSelection) {

        if (newSelection instanceof IStructuredSelection) {
            Object element = ((IStructuredSelection) newSelection).getFirstElement();
            if (element instanceof IAdaptable) {
                IResource resource = ((IAdaptable) element).getAdapter(IResource.class);
                if (resource != null && Activator.getDefault().isConnected()) {
                    final IProject project = resource.getProject();
    
                    try {                       
                         
                        ReviewView reviewview = (ReviewView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                              .getActivePage()
                              .showView("net.ssehub.teaching.exercise_reviewer.eclipse.views.reviewview");
                        
                        reviewview.refreshReviewInformation(Activator.getDefault()
                                .getProjectManager().getGroupName(project).orElse("not connected"),
                                Activator.getDefault()
                                .getProjectManager().getAssignmentId(project).orElse("not connected"));
                        
                    } catch (PartInitException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } 
                }

            }
        }

    }

}
