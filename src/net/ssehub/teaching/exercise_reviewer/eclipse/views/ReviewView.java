package net.ssehub.teaching.exercise_reviewer.eclipse.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

import net.ssehub.teaching.exercise_reviewer.lib.data.Submission;

/**
 * This class displays information and you can score and give additional assessment.
 * 
 * @author lukas
 *
 */
public class ReviewView extends ViewPart {
    private Label label;
    
    private Label labelUsers;
    private Label labelProject;
    /**
     * Creates an instance of the ReviewView class.
     */
    public ReviewView() {
        super();
    }

    @Override
    public void createPartControl(Composite parent) {
        label = new Label(parent, 0);
        label.setText("Hello World");
        
        createReviewInformation(parent);
        
    }

    @Override
    public void setFocus() {
        label.setFocus();
        
    }
    /**
     * Creates the reviewinfo screen.
     * @param parent
     */
    private void createReviewInformation(Composite parent) {
        Group group = new Group(parent, SWT.H_SCROLL | SWT.V_SCROLL);
        group.setText("Review Data");
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        group.setLayout(gridLayout);

        Label label = new Label(group, 0);
        label.setText("Exercise:");
        GridData gridData = new GridData(GridData.BEGINNING);
        gridData.horizontalSpan = 1;
        label.setLayoutData(gridData);

        labelProject = new Label(group, 0);
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gridData.horizontalSpan = 1;
        labelProject.setLayoutData(gridData);
        
        label = new Label(group, 0);
        label.setText("User(s):");
        gridData = new GridData(GridData.BEGINNING);
        gridData.horizontalSpan = 1;
        label.setLayoutData(gridData);

        labelUsers = new Label(group, 0);
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gridData.horizontalSpan = 1;
        labelUsers.setLayoutData(gridData);

        Label labelReview = new Label(group, 0);
        labelReview.setText("Review:");
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gridData.horizontalSpan = 1;
        labelReview.setLayoutData(gridData);

//        ButtonSelectionListener buttonListener = new ButtonSelectionListener();
//        gatherButton = new Button(group, SWT.PUSH);
//        gatherButton.setText("Gather from Markers");
//        gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
//        gridData.horizontalSpan = 1;
//        gatherButton.setLayoutData(gridData);
//        gatherButton.addSelectionListener(buttonListener);
//        review = new Text(group, SWT.WRAP | SWT.BORDER);
//        gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
//        gridData.heightHint = 180;
//        gridData.widthHint = 400;
//        gridData.horizontalSpan = 2;
//        review.setLayoutData(gridData);

//        labelCredits = new Label(group, 0);
//        gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
//        gridData.horizontalSpan = 1;
//        labelCredits.setLayoutData(gridData);
//        labelCredits.setText(CREDITS_LABEL_TEXT_SIMPLE);
//
//        credits = new Text(group, SWT.BORDER);
//        credits.setTextLimit(5);
//        gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
//        gridData.horizontalSpan = 1;
//        credits.setLayoutData(gridData);
    }
    /**
     * Refreshes the review data with the current submission.
     * @param submission
     */
    public void refreshReviewInformation(Submission submission) {
        this.labelProject.setText("");
        this.labelUsers.setText(submission.getUserDisplayName());
        
        this.labelUsers.pack();
    }

}
