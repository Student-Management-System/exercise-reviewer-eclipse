package net.ssehub.teaching.exercise_reviewer.eclipse.dialog;

import java.util.List;
import java.util.Optional;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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
        FillLayout fillLayout = new FillLayout();
        container.setLayout(fillLayout);
        
        this.combo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.SIMPLE);
        
        for (Course course : courseList) {
            this.combo.add(course.getName());
        }
        this.combo.setLayout(fillLayout);
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
        newShell.setText("Download Submissions Result");
    }

    @Override
    protected Point getInitialSize() {
        return new Point(220, 170);
    }

    /**
     * Gets the selected Course.
     * @return Optional<Course>
     */
    public Optional<Course> getSelectedCourse() {
        return selectedCourse;
    }

}
