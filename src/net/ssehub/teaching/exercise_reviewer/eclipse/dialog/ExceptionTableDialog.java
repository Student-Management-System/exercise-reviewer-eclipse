package net.ssehub.teaching.exercise_reviewer.eclipse.dialog;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import net.ssehub.teaching.exercise_reviewer.eclipse.background.DownloadAllSubmissionsJob.Project;

/**
 * This Dialog display all exceptions thrown from downloading the submissions.
 * 
 * @author lukas
 *
 */
public class ExceptionTableDialog extends Dialog {
    
    private Table table;
    private List<Project> failedProjects;
    
    /**
     * Creates an instance of {@link ExceptionTableDialog}.
     * @param parentShell
     * @param failedProjects
     */
    public ExceptionTableDialog(Shell parentShell, List<Project> failedProjects) {
        super(parentShell);
        this.failedProjects = failedProjects;
    }
    
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        
        createTable(container);
        
        return container;
    }
    /**
     * Creates the table.
     * @param parent
     */
    private void createTable(Composite parent) {
        
        this.table = new Table(parent, SWT.FULL_SELECTION | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        this.table.setLinesVisible(true);
        this.table.setHeaderVisible(true);
        FillLayout fillLayout = new FillLayout();
        this.table.setLayout(fillLayout);
        parent.setLayout(fillLayout);

        String[] columns = {"Groupname", "Exception message"};

        for (String column : columns) {
            TableColumn tc = new TableColumn(this.table, SWT.LEFT);
            tc.setText(column);
        }
        
        for (Project project : failedProjects) {
            TableItem item = new TableItem(this.table, SWT.NONE);
            item.setText(0, project.getGroupName());
            item.setText(1, project.getException().get().getMessage());
        }

        for (int i = 0; i < this.table.getColumnCount(); i++) {
            this.table.getColumn(i).pack();
        }
        
        
    }
    
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Failing reasons");
    }
    
    @Override
    protected boolean isResizable() {
        return true;
    }
    
    
    

}
