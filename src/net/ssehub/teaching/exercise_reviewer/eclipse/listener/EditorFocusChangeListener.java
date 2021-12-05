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
import net.ssehub.teaching.exercise_reviewer.eclipse.views.ReviewView;


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
                  
                    ReviewView reviewview;
                    try {
                        reviewview = (ReviewView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                              .getActivePage()
                              .showView("net.ssehub.teaching.exercise_reviewer.eclipse.views.reviewview");
                        reviewview.refreshReviewInformation(Activator.getDefault()
                                .getProjectManager().getGroupName(project).orElse("not connected"),
                                Activator.getDefault().getProjectManager().getAssignmentId(project)
                                .orElse("not available"));
                        
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
