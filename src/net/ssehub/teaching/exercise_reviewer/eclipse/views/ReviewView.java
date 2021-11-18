package net.ssehub.teaching.exercise_reviewer.eclipse.views;

import java.io.File;
import java.util.Optional;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import net.ssehub.teaching.exercise_reviewer.eclipse.Activator;
import net.ssehub.teaching.exercise_reviewer.eclipse.background.IRunnableStuMgmt;
import net.ssehub.teaching.exercise_reviewer.eclipse.background.StuMgmtJob;
import net.ssehub.teaching.exercise_reviewer.eclipse.dialog.AdvancedExceptionDialog;
import net.ssehub.teaching.exercise_reviewer.eclipse.listener.ProjectSelectionListener;
import net.ssehub.teaching.exercise_submitter.lib.ExerciseSubmitterManager;
import net.ssehub.teaching.exercise_submitter.lib.data.Assessment;
import net.ssehub.teaching.exercise_submitter.lib.data.Assignment;
import net.ssehub.teaching.exercise_submitter.lib.student_management_system.ApiException;
import net.ssehub.teaching.exercise_submitter.lib.submission.Problem;

/**
 * This class displays information and you can score and give additional
 * assessment.
 *
 * @author lukas
 *
 */
public class ReviewView extends ViewPart {

    private static IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

    private Label labelUsers;
    private Label labelProject;

    private Button reviewButton;

    private Table table;

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

        this.createProblemTable(parent);

        this.createReviewInformation(parent);
        this.createSelectionListener();

    }

    /**
     * Creates the problem table.
     *
     * @param parent
     */
    private void createProblemTable(Composite parent) {
        this.table = new Table(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        this.table.setLinesVisible(true);
        this.table.setHeaderVisible(true);

        String[] columns = {"Description", "Path", "Line", "Column"};

        for (String column : columns) {
            TableColumn tc = new TableColumn(this.table, SWT.LEFT);
            tc.setText(column);
        }

        for (int i = 0; i < this.table.getColumnCount(); i++) {
            this.table.getColumn(i).pack();
        }

    }

    @Override
    public void setFocus() {

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
     * @param groupName
     * @param assignment
     */
    public void refreshReviewInformation(String groupName, Assignment assignment) {
        this.labelProject.setText(assignment.getName());

        this.labelUsers.setText(groupName);

        ExerciseSubmitterManager manager = Activator.getDefault().getManager();
        IRunnableStuMgmt<Assessment> func = new IRunnableStuMgmt<Assessment>() {

            @Override
            public Assessment run() {
                Assessment assessment = null;
                try {
                    assessment = manager.getStudentManagementConnection()
                            .getAssessment(manager.getCourse(), assignment, groupName).orElse(new Assessment());
                } catch (ApiException e) {
                    System.out.println(e);
                }
                return assessment;
            }

        };
        StuMgmtJob<Assessment> job =
                new StuMgmtJob<Assessment>("refreshInformation", func, this::onFinishedStumgmtJob);
        job.setUser(true);
        job.schedule();

        this.labelProject.pack();
        this.labelUsers.pack();
    }

    /**
     * When the job is finished.
     *
     * @param job
     */
    private void onFinishedStumgmtJob(StuMgmtJob<Assessment> job) {
        Assessment assessment = job.getOutput();
        if (assessment != null) {
            this.comment = Optional.ofNullable(
                    new Comment(assessment.getComment().orElse("Not Available"), page));
            if (assessment.getProblemlist() != null) {
                Display.getDefault().syncExec(() -> {
                    for (Problem problem : assessment.getProblemlist()) {
                        TableItem item = new TableItem(this.table, SWT.NONE);
                        item.setText(0, problem.getMessage());
                        item.setText(1, problem.getFile().orElse(new File("Not loading")).toString());
                        item.setText(2, problem.getLine().get().toString());
                        item.setText(3, problem.getColumn().get().toString());

                    }

                    for (int i = 0; i < this.table.getColumnCount(); i++) {
                        this.table.getColumn(i).pack();
                    }

                });
            }
        } else {
            Display.getDefault().syncExec(() -> {
                this.table.clearAll();

            });
        }

    }

}
