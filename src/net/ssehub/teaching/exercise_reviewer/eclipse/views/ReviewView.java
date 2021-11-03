package net.ssehub.teaching.exercise_reviewer.eclipse.views;

import java.util.Optional;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import net.ssehub.teaching.exercise_reviewer.eclipse.dialog.AdvancedExceptionDialog;
import net.ssehub.teaching.exercise_reviewer.eclipse.listener.ProjectSelectionListener;
import net.ssehub.teaching.exercise_reviewer.lib.data.Assessment;
import net.ssehub.teaching.exercise_reviewer.lib.data.User;

/**
 * This class displays information and you can score and give additional
 * assessment.
 *
 * @author lukas
 *
 */
public class ReviewView extends ViewPart {
    private Label label;

    private Label labelUsers;
    private Label labelProject;

    private Text reviewinput;

    private Button reviewButton;

    private Optional<Comment> comment = Optional.empty();

    private ISelectionListener listener = new ProjectSelectionListener();

    /**
     * Creates an instance of the ReviewView class.
     */
    public ReviewView() {
        super();
    }

    @Override
    public void createPartControl(Composite parent) {
        this.label = new Label(parent, 0);
        this.label.setText("Hello World");

        this.createReviewInformation(parent);
        this.createSelectionListener();

    }

    @Override
    public void setFocus() {
        this.label.setFocus();

    }

    @Override
    public void dispose() {
        // important: We need do unregister our listener when the view is disposed
        this.getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(this.listener);
        super.dispose();
    }

    /**
     * Creates the reviewinfo screen.
     *
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

        this.labelProject = new Label(group, 0);
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gridData.horizontalSpan = 1;
        this.labelProject.setLayoutData(gridData);

        label = new Label(group, 0);
        label.setText("User(s):");
        gridData = new GridData(GridData.BEGINNING);
        gridData.horizontalSpan = 1;
        label.setLayoutData(gridData);

        this.labelUsers = new Label(group, 0);
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gridData.horizontalSpan = 1;
        this.labelUsers.setLayoutData(gridData);

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
//        reviewinput = new Text(group, SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
//        gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
//        gridData.heightHint = 180;
//        gridData.widthHint = 400;
//        gridData.horizontalSpan = 2;
//        reviewinput.setLayoutData(gridData);

        this.reviewButton = new Button(group, SWT.PUSH);
        this.reviewButton.setText("Open Comment");
        this.reviewButton.setLayoutData(gridData);
        this.clickopenReview();

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
     * Creates the selection listener.
     */
    private void createSelectionListener() {
        this.getSite().getWorkbenchWindow().getSelectionService().addPostSelectionListener(this.listener);
    }

    /**
     * Triggerd if the user click on review.
     */
    private void clickopenReview() {
        this.reviewButton.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent arg0) {
                if (ReviewView.this.comment.isPresent()) {
                    try {
                        ReviewView.this.comment.get().openEditor();
                    } catch (PartInitException e) {
                        AdvancedExceptionDialog.showUnexpectedExceptionDialog(e, "Cant open Editor");
                    }
                }

            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
                // TODO Auto-generated method stub

            }
        });
    }

    /**
     * Refreshes the review data with the current submission.
     *
     * @param assessment
     */
    public void refreshReviewInformation(Assessment assessment) {
        this.labelProject.setText(assessment.getAssignment().getName());

        this.labelUsers.setText(assessment.getUser()
                .orElse(new User("not available", "not available", "not available", "not available")).getDisplayname());

        this.comment = Optional.ofNullable(new Comment(assessment.getComment().orElse("not available")));

        this.labelProject.pack();
        this.labelUsers.pack();
    }

}
