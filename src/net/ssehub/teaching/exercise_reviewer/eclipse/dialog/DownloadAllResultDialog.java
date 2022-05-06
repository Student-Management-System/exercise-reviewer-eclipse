package net.ssehub.teaching.exercise_reviewer.eclipse.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import net.ssehub.teaching.exercise_reviewer.eclipse.background.DownloadAllSubmissionsJob.Project;

/**
 * This class displays the Downloadall Result. The number of 
 * successes and failures and their exceptions.
 * 
 * @author lukas
 *
 */
public class DownloadAllResultDialog extends Dialog {
    private List<Project> successProjects;
    private List<Project> noSubmissionProjects;
    private List<Project> failedProjects;
    
    private Button openExceptionDialog;
    /**
     * Creates a new instance of {@link DownloadAllResultDialog}.
     * @param parentShell
     * @param projects
     */
    public DownloadAllResultDialog(Shell parentShell,  List<Project> projects) {
        super(parentShell);
        this.sortProjects(projects);
    }
    
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;

        container.setLayout(gridLayout);

        new Label(container, SWT.NULL).setText("Download succeded:");
        Label resultSucceded = new Label(container, SWT.RIGHT);
        GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        resultSucceded.setLayoutData(gridData);
        String resultSuccededString = Integer.toString(this.successProjects.size());
        resultSucceded.setText(resultSuccededString);

        new Label(container, SWT.NULL).setText("No submission:");
        Label noSubmission = new Label(container, SWT.RIGHT);
        noSubmission.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        String noSubmissionString = Integer.toString(this.noSubmissionProjects.size());
        noSubmission.setText(noSubmissionString);
        
        new Label(container, SWT.NULL).setText("Download failed:");
        Label resultFailed = new Label(container, SWT.RIGHT);
        resultFailed.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        String resultFailedString = Integer.toString(this.failedProjects.size());
        resultFailed.setText(resultFailedString);
        
        new Label(container, SWT.NULL).setText("Reasons for failing:");
        this.openExceptionDialog = new Button(container, SWT.PUSH);
        this.openExceptionDialog.setText("Open");
        this.openExceptionDialog.addSelectionListener(new SelectionListener() {
            
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                ExceptionTableDialog tableDialog = new ExceptionTableDialog(getParentShell(), failedProjects);
                tableDialog.open();
                
            }
            
            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
              
                
            }
        });
           
               
        return container;
    }
    /**
     * Sorts the project to the right list.
     * @param projects
     */
    private void sortProjects(List<Project> projects) {
        this.successProjects = new ArrayList<Project>();
        this.noSubmissionProjects = new ArrayList<Project>();
        this.failedProjects = new ArrayList<Project>();
        
        for (Project project : projects) {
            if (project.isSucceeded()) {
                this.successProjects.add(project);
            } else if (project.isNoSubmission()) {
                this.noSubmissionProjects.add(project);
            } else {
                this.failedProjects.add(project);
            }
        }
    }
    
    

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Download Submissions Result");
    }

    @Override
    protected boolean isResizable() {
        return true;
    }
    

}
