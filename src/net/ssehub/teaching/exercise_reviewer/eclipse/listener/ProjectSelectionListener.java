package net.ssehub.teaching.exercise_reviewer.eclipse.listener;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

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
                final IProject project = resource.getProject();

//                try {
//                    //PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                // .getActivePage().getActiveEditor().setFocus();
//                    //TODO need to change
//                    ReviewView reviewview = (ReviewView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
//                          .getActivePage().showView("net.ssehub.teaching.exercise_reviewer.eclipse.views.reviewview");
//                    reviewview.refreshReviewInformation(project.getName(), null /* TODO: get current assignment */);
//                } catch (PartInitException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }

            }
        }

    }

}
