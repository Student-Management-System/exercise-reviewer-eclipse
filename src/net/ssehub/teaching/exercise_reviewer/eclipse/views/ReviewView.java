package net.ssehub.teaching.exercise_reviewer.eclipse.views;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.osgi.framework.Bundle;

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

    private Action uploadAction;

    private Table table;
    
    private Text textPoints;

    private Optional<Comment> comment = Optional.empty();
    private Optional<Assignment> assignment = Optional.empty();
    private Optional<String> groupname = Optional.empty();
    private Optional<Assessment> assessment = Optional.empty();
    private List<Problem> problems = new ArrayList<Problem>();

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
        this.table = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        this.table.setLinesVisible(true);
        this.table.setHeaderVisible(true);

        String[] columns = {"Description", "Path", "Line", "Column" };

        for (String column : columns) {
            TableColumn tc = new TableColumn(this.table, SWT.LEFT);
            tc.setText(column);
        }

        for (int i = 0; i < this.table.getColumnCount(); i++) {
            this.table.getColumn(i).pack();
        }
        
        this.table.addSelectionListener(new SelectionListener() {
            
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                if (!problems.isEmpty()) {
                    clickProblem(problems.get(table.getSelectionIndex()));
                }
                
            }
            
            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
                // TODO Auto-generated method stub
                
            }
        });

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

        this.reviewButton = new Button(group, SWT.PUSH);
        this.reviewButton.setText("Open Comment");
        this.reviewButton.setLayoutData(gridData);
        this.clickopenReview();
        
        Label labelPoints = new Label(group, 0);
        labelPoints.setText("Points:");
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gridData.horizontalSpan = 1;
        labelPoints.setLayoutData(gridData);
        
        this.textPoints = new Text(group, SWT.BORDER);
        this.textPoints.setTextLimit(5);
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
        gridData.horizontalSpan = 1;
        this.textPoints.setLayoutData(gridData);


        createToolbar();

    }
    /**
     * Creates the Toolbar for the View.
     */
    private void createToolbar() {

        this.uploadAction = new Action("Upload assessment") {
            /**
             * Creates the button press action.
             */
            @Override
            public void run() {
                clickUpload();
            }
        };
        this.uploadAction.setImageDescriptor(this.getImageDescriptor("icons/upload.png"));
        
        IToolBarManager mgr = this.getViewSite().getActionBars().getToolBarManager();
        mgr.add(this.uploadAction);

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
     * Called when the upload button is pressed.
     */
    private void clickUpload() {
        
        ExerciseSubmitterManager manager = Activator.getDefault().getManager();
        IRunnableStuMgmt<Boolean> func = new IRunnableStuMgmt<Boolean>() {
           

            @Override
            public Boolean run() {
                boolean success = true;
                if (assignment.isPresent() && groupname.isPresent() && assessment.isPresent()) {
                    try {
                        Assessment uploadAssessment = new Assessment();
                        uploadAssessment.setManagementId(assessment.get().getManagementId().orElse(""));
                        uploadAssessment.setComment(comment.orElse(new Comment("Not Available", page)).getComment());
                        Display.getDefault().syncExec(() -> uploadAssessment.setPoints(
                                Double.parseDouble(textPoints.getText())));
                        manager.getStudentManagementConnection().uploadAssessment(manager.getCourse(), assignment.get(),
                                groupname.get(), uploadAssessment);
                    } catch (ApiException e) {
                        success = false;
                    } catch (NumberFormatException e) {
                        success = false;
                        Display.getDefault().syncExec(() -> {
                            MessageDialog.openError(getSite().getShell(), "Upload Assessment", 
                                    "Cant upload because no Points selected");
                        });
                    }
                } else {
                    success = false;
                }
                return success;
            }

        };
        StuMgmtJob<Boolean> job = new StuMgmtJob<Boolean>("upload assessment", func, this::onFinishedUploadAssessment);
        job.setUser(true);
        job.schedule();
      
    }
    /**
     * Called when a problem from the list is clicked.
     * @param problem
     */
    private void clickProblem(Problem problem) {
        final IFile inputFile = ResourcesPlugin.getWorkspace().getRoot().getFile(
                Path.fromOSString("/Submission from " + groupname.get() + "/" 
                        + problem.getFile().orElse(new File("not available")).toString()));
   
        if (inputFile != null) {
            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            try {
                IFileStore store = EFS.getStore(inputFile.getLocationURI());
                IEditorInput input = new FileStoreEditorInput(store);
                IEditorPart openEditor = IDE.openEditor(page, input, "org.eclipse.jdt.ui.CompilationUnitEditor", true);

                if (openEditor instanceof ITextEditor) {
                    ITextEditor textEditor = (ITextEditor) openEditor;
                    IDocument document = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
                    textEditor.selectAndReveal(document.getLineOffset(problem.getLine().orElse(1) - 1), 
                            document.getLineLength(problem.getLine().orElse(1) - 1));
                }
                
            } catch (CoreException | BadLocationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    /**
     * Gets the imagedescriptor from the relative icon path.
     *
     * @param relativePath
     * @return ImageDescriptor
     */
    private ImageDescriptor getImageDescriptor(String relativePath) {
        Activator.getDefault();
        Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
        URL fullPathString = bundle.getEntry(relativePath);
        return ImageDescriptor.createFromURL(fullPathString);
    }

    /**
     * Refreshes the review data with the current submission.
     *
     * @param groupName
     * @param assignment
     */
    public void refreshReviewInformation(String groupName, Assignment assignment) {

        this.assignment = Optional.ofNullable(assignment);
        this.groupname = Optional.ofNullable(groupName);

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
        StuMgmtJob<Assessment> job = new StuMgmtJob<Assessment>("refreshInformation", func, this::onFinishedStumgmtJob);
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
            this.comment = Optional.ofNullable(new Comment(assessment.getComment().orElse("Not Available"), page));
            this.assessment = Optional.ofNullable(assessment);
            if (assessment.getPoints().isPresent()) {
                Display.getDefault().syncExec(() -> this.textPoints.setText(
                        Double.toString(assessment.getPoints().get())));
            }
            if (assessment.getProblemlist() != null) {
                problems = assessment.getProblemlist();
                Display.getDefault().syncExec(() -> {
                    for (Problem problem : problems) {
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
    /**
     * Called when the upload job is finished.
     * @param job
     */
    private void onFinishedUploadAssessment(StuMgmtJob<Boolean> job) {
        if (job.getOutput() != null && job.getOutput())  {
            Display.getDefault().syncExec(() -> {
                MessageDialog.openInformation(job.getShell().orElse(new Shell()), "Upload Assessment", 
                        "Assessment successfully uploadet");
            });
        } else {
            Display.getDefault().syncExec(() -> {
                MessageDialog.openError(job.getShell().orElse(new Shell()), "Upload Assessment", 
                        "Assessment uploading failed");
            });
        }
    }

}
