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

import net.ssehub.teaching.exercise_reviewer.eclipse.views.ReviewView;
import net.ssehub.teaching.exercise_reviewer.lib.data.Submission;

public class ProjectSelectionListener implements ISelectionListener {

    @Override
    public void selectionChanged(IWorkbenchPart arg0, ISelection newSelection) {
        // TODO Auto-generated method stub
        if (newSelection instanceof IStructuredSelection) {
            Object element = ((IStructuredSelection) newSelection).getFirstElement();
            if (element instanceof IAdaptable) {
                IResource resource = (IResource) ((IAdaptable) element).getAdapter(IResource.class);
                final IProject project = resource.getProject();

                try {
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor().setFocus();
                    //TODO need to change
                    ReviewView reviewview = (ReviewView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                            .getActivePage().showView("net.ssehub.teaching.exercise_reviewer.eclipse.views.reviewview");
                    reviewview.refreshReviewInformation(new Submission(project.getName(), "", project.getName(), ""));
                } catch (PartInitException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }

    }

}
