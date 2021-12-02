package net.ssehub.teaching.exercise_reviewer.eclipse.listener;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import net.ssehub.teaching.exercise_reviewer.eclipse.Activator;
import net.ssehub.teaching.exercise_reviewer.eclipse.dialog.AdvancedExceptionDialog;
import net.ssehub.teaching.exercise_reviewer.eclipse.views.AllReviewableSubmissionsView;
import net.ssehub.teaching.exercise_reviewer.eclipse.views.ReviewView;
import net.ssehub.teaching.exercise_submitter.lib.data.Assignment;
import net.ssehub.teaching.exercise_submitter.lib.data.Assignment.State;

/**
 *This class gets called when a IPart is change.
 *For example when a editor focus is changed.
 *
 * @author lukas
 *
 */
public class EditorFocusChangeListener implements IPartListener {

    @Override
    public void partActivated(IWorkbenchPart arg0) {
       
    }

    @Override
    public void partBroughtToTop(IWorkbenchPart part) {
        // TODO Auto-generated method stub
        if (part instanceof IEditorPart) {
            IFile file = ((IEditorPart) part).getEditorInput().getAdapter(IFile.class);
            if (file != null) {
                IProject project = file.getProject();
                
                if (project != null)  {
                    String projectname = project.getName();
                    AllReviewableSubmissionsView listview = (AllReviewableSubmissionsView)  
                            PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                            .getActivePage().findView("net.ssehub.teaching.exercise_reviewer.eclipse.views."
                                    + "allreviewablesubmissionsview");
                            
                    
                    ReviewView reviewview;
                    try {
                        reviewview = (ReviewView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                              .getActivePage()
                              .showView("net.ssehub.teaching.exercise_reviewer.eclipse.views.reviewview");
                        reviewview.refreshReviewInformation(Activator.getDefault()
                                .getProjectManager().getGroupName(projectname).orElse("not connected"),
                                listview.getSelectedAssignment().orElse(
                                        new Assignment("Not available", "Not available", State.SUBMISSION, false)));
                        
                    } catch (PartInitException e) {
                        Display.getDefault().syncExec(() 
                               -> AdvancedExceptionDialog.showUnexpectedExceptionDialog(e,
                               "Cant open Review view"));
                    }
                    
                }
                
            }
          
        }
    }

    @Override
    public void partClosed(IWorkbenchPart arg0) {
      
    }

    @Override
    public void partDeactivated(IWorkbenchPart arg0) {
      
    }

    @Override
    public void partOpened(IWorkbenchPart arg0) {
       
        
    }

}
