package net.ssehub.teaching.exercise_reviewer.eclipse.dialog;

import java.util.List;
import java.util.Optional;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import net.ssehub.teaching.exercise_submitter.lib.data.Course;

/**
 * This Class handles the Dialog for the Course Selection.
 * 
 * @author lukas
 *
 */
public class CourseSelectionDialog extends Dialog {
    private List<Course> courseList; 
    private Combo combo;
    private Optional<Course> selectedCourse = Optional.empty();
    
    /**
     * Creates an instance of CourseSelectionDialog.
     * @param parentShell
     * @param courselist
     */
    public CourseSelectionDialog(Shell parentShell, List<Course> courselist) {
        super(parentShell);
        this.courseList = courselist;
    }


    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new GridLayout(4, false));
        
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        
        Label selectLabel = new Label(container, SWT.NONE);
        selectLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        selectLabel.setText("Select your Course:");
        
        new Label(container, SWT.NONE);
        
        Combo combo = new Combo(container, SWT.NONE);
        combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        
        this.combo.select(0);
        createComboSelectionListener();
        this.combo.pack();
           
        return container;
    }
    /**
     * Creates the selectionlistenr for the combobox.
     */
    private void createComboSelectionListener() {
        this.combo.addSelectionListener(new SelectionListener() {
            
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                selectedCourse = Optional.ofNullable(courseList.get(combo.getSelectionIndex()));               
            }
            
            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
            
                
            }
        });
    }
   

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Course Selection Dialog");
    }
    
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    @Override
    protected Point getInitialSize() {
        return new Point(240, 120);
    }

    /**
     * Gets the selected Course.
     * @return Optional<Course>
     */
    public Optional<Course> getSelectedCourse() {
        return selectedCourse;
    }

}
