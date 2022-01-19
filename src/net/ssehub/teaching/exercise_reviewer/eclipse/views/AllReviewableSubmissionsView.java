package net.ssehub.teaching.exercise_reviewer.eclipse.views;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.Bundle;

import net.ssehub.teaching.exercise_reviewer.eclipse.Activator;
import net.ssehub.teaching.exercise_reviewer.eclipse.background.DownloadAllSubmissionsJob;
import net.ssehub.teaching.exercise_reviewer.eclipse.background.DownloadAllSubmissionsJob.Project;
import net.ssehub.teaching.exercise_reviewer.eclipse.background.IRunnableStuMgmt;
import net.ssehub.teaching.exercise_reviewer.eclipse.background.ListSubmissionsJob;
import net.ssehub.teaching.exercise_reviewer.eclipse.background.StuMgmtJob;
import net.ssehub.teaching.exercise_reviewer.eclipse.dialog.ExceptionDialog;
import net.ssehub.teaching.exercise_reviewer.eclipse.submissions.DownloadSubmission;
import net.ssehub.teaching.exercise_submitter.lib.ExerciseSubmitterManager;
import net.ssehub.teaching.exercise_submitter.lib.data.Assignment;
import net.ssehub.teaching.exercise_submitter.lib.student_management_system.ApiException;

/**
 * This class creates the {@link AllReviewableSubmissionsView} which contains a
 * list with all reviewable submissions and a download all button.
 *
 * @author lukas
 *
 */
public class AllReviewableSubmissionsView extends ViewPart {
    private Action getAllSubmissionAction;
    private Action downloadAllSubmissionAction;
    private List swtList;

    private Optional<java.util.List<String>> groupNames = Optional.empty();
    private Optional<java.util.List<Assignment>> assignments = Optional.empty();
    private Optional<Assignment> selectedAssignment = Optional.empty();

    private Combo combo;

    /**
     * Creates an instance of the ReviewView class.
     */
    public AllReviewableSubmissionsView() {
        super();
    }

    @Override
    public void createPartControl(Composite parent) {

        this.createActions();
        this.createToolbar();
        this.createComponent(parent);
    }

    @Override
    public void setFocus() {

    }

    /**
     * Creates the action.
     */
    public void createActions() {
        this.getAllSubmissionAction = new Action("Get list of submissions") {
            /**
             * Creates the button press action.
             */
            @Override
            public void run() {
                AllReviewableSubmissionsView.this.clickRefresh();
            }
        };
        this.getAllSubmissionAction.setImageDescriptor(this.getImageDescriptor("icons/refresh.png"));

        this.downloadAllSubmissionAction = new Action("Download all submissions") {
            /**
             * Creates the button press action.
             */
            @Override
            public void run() {
                AllReviewableSubmissionsView.this.clickDownloadAll();
            }
        };
        this.downloadAllSubmissionAction.setImageDescriptor(this.getImageDescriptor("icons/download.png"));

    }

    /**
     * Creates the tooolbar for the view.
     */
    private void createToolbar() {

        IToolBarManager mgr = this.getViewSite().getActionBars().getToolBarManager();
        mgr.add(this.downloadAllSubmissionAction);
        mgr.add(this.getAllSubmissionAction);

    }

    /**
     * Crestes the widget that are on the view.
     *
     * @param parent
     */
    private void createComponent(Composite parent) {
        parent.setLayout(new GridLayout(2, false));
        new Label(parent, SWT.NONE);
        
        Label lblSelectAssignment = new Label(parent, SWT.NONE);
        lblSelectAssignment.setText("Select Assignment:");
        new Label(parent, SWT.NONE);
        
        combo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
        combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        new Label(parent, SWT.NONE);
        
        swtList = new List(parent, SWT.BORDER);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1);
        gd.heightHint = 392;
        gd.widthHint = 201;
        swtList.setLayoutData(gd);
             
        createListSelectionListener();

        this.createRightclickMenu();
        this.createComboSelectionListener();

    
    }
    /**
     * Creates the selection listener for the list.
     */
    private void createListSelectionListener() {
        this.swtList.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent arg0) {
                try {
                    ReviewView reviewview = (ReviewView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                            .getActivePage().showView("net.ssehub.teaching.exercise_reviewer.eclipse.views.reviewview");

                    if (AllReviewableSubmissionsView.this.groupNames.isPresent()) {
                        int selected = AllReviewableSubmissionsView.this.swtList.getSelectionIndex();
                        if (!(selected < 0 || selected >= AllReviewableSubmissionsView.this.swtList.getItemCount())) {

                            reviewview.refreshReviewInformation(
                                    AllReviewableSubmissionsView.this.groupNames.get().get(selected),
                                    AllReviewableSubmissionsView.this.selectedAssignment.get().getManagementId());
                        }
                    }
                } catch (PartInitException e) {
                    // TODO exception
                }

            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
               

            }
        });
    }

    /**
     * Creates a menu that is activated if a selected list item is rightclicked.
     */
    private void createRightclickMenu() {
        final Menu menu = new Menu(this.swtList);
        this.swtList.setMenu(menu);
        menu.addMenuListener(new MenuAdapter() {
            @Override
            public void menuShown(MenuEvent event) {
                int selected = AllReviewableSubmissionsView.this.swtList.getSelectionIndex();

                if (selected < 0 || selected >= AllReviewableSubmissionsView.this.swtList.getItemCount()) {
                    return;
                }

                MenuItem[] items = menu.getItems();
                for (MenuItem item : items) {
                    item.dispose();
                }
                MenuItem newItem = new MenuItem(menu, SWT.NONE);
                newItem.setText("Download \"" + AllReviewableSubmissionsView.this.swtList
                        .getItem(AllReviewableSubmissionsView.this.swtList.getSelectionIndex()) + "\"");

                newItem.addSelectionListener(new SelectionListener() {

                    @Override
                    public void widgetSelected(SelectionEvent arg0) {
                        clickRightMenue(AllReviewableSubmissionsView.this.swtList
                               .getItem(AllReviewableSubmissionsView.this.swtList.getSelectionIndex()));
                            
                    }

                    @Override
                    public void widgetDefaultSelected(SelectionEvent arg0) {
                      

                    }
                });
            }
        });

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
     * Called when the refresh button is clicked.
     */
    private void clickRefresh() {
        retrieveAssignments();
        this.swtList.removeAll();
        if (this.selectedAssignment.isPresent()) {
            ListSubmissionsJob job = new ListSubmissionsJob(this.getSite().getShell(), this::onListSubmissionFinished,
                    this.selectedAssignment.get());
            job.setUser(true);
            job.schedule();
        }

    }

    /**
     * Starts the download all submission job.
     */
    private void clickDownloadAll() {
        if (this.selectedAssignment.isPresent()) {
            DownloadAllSubmissionsJob job = new DownloadAllSubmissionsJob(this.getSite().getShell(),
                    this.selectedAssignment.get(), PlatformUI.getWorkbench().getActiveWorkbenchWindow());
            job.setUser(true);
            job.schedule();
        }
    }
    /**
     * Called when the right click menue button is activated.
     * @param groupname
     */
    private void clickRightMenue(String groupname) {
        if (this.selectedAssignment.isPresent()) {
            IRunnableStuMgmt<java.util.List<Project>> func = 
                    new IRunnableStuMgmt<java.util.List<Project>>() {
                    @Override
                    public java.util.List<Project> run() {
                        java.util.List<Project> list = new java.util.ArrayList<Project>();
                        Project project = new Project(groupname);
                        list.add(project);
                        ExerciseSubmitterManager manager = Activator.getDefault().getManager();
                        DownloadSubmission submission = new DownloadSubmission(groupname,
                                project, selectedAssignment.get(), manager);
                        
                        submission.start();
                        
                        return list;
                                
                    }
                };
            StuMgmtJob<java.util.List<Project>> job = new StuMgmtJob<java.util.List<Project>>(groupname, func, 
                    DownloadSubmission::onFinishedStuMgmtJob);
            job.setUser(true);
            job.schedule();
        
        }
    }
    /**
     * Creates the combo widget.
     *
     * 
     */
    private void createComboSelectionListener() {
        this.combo.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
              

            }

            @Override
            public void widgetSelected(SelectionEvent event) {
                if (AllReviewableSubmissionsView.this.assignments.isPresent()) {
                    AllReviewableSubmissionsView.this.selectedAssignment = Optional
                            .ofNullable(AllReviewableSubmissionsView.this.assignments.get()
                                    .get(AllReviewableSubmissionsView.this.combo.getSelectionIndex()));
                    AllReviewableSubmissionsView.this.clickRefresh();
                }

            }
        });
        this.retrieveAssignments();
    }

    /**
     * Retrieves the assignments from the server.
     */
    private void retrieveAssignments() {
     

        IRunnableStuMgmt<java.util.List<Assignment>> func = new IRunnableStuMgmt<java.util.List<Assignment>>() {

            @Override
            public java.util.List<Assignment> run() {
                java.util.List<Assignment> assignments = null;
                try {
                    ExerciseSubmitterManager manager = Activator.getDefault().getManager();
                    if (manager != null) {
                        
                        assignments = Activator.getDefault().getManager().getAllAssignments();
                        
                        Collections.sort(assignments, Comparator
                                .comparing(Assignment::getState, Comparator.comparingInt((Assignment.State s) -> {
                                    int order = 5;
                                    switch (s) {
                                    case IN_REVIEW: order = 0; break;
                                    case REVIEWED: order = 1; break;
                                    case CLOSED: order = 2; break;
                                    case INVISIBLE: order = 3; break;
                                    case SUBMISSION: order = 4; break;
                                    default: order = 5; break;
                                    }
                                    return order;
                                }))
                                .thenComparing(Assignment::getName));
                    }
                } catch (ApiException e) {
                    Display.getDefault().syncExec(() -> {
                        ExceptionDialog.showUnexpectedExceptionDialog(e, "Cant load assignments");
                    });
                }
                return assignments;
            }

        };

        StuMgmtJob<java.util.List<Assignment>> job = new StuMgmtJob<>("Load assignments", func,
                this::onListAssignments);
        job.setUser(true);
        job.schedule();
    }
    /**
     * Gets called when retrive assignments job is done.
     * @param job
     */
    private void onListAssignments(StuMgmtJob<java.util.List<Assignment>> job) {

        this.assignments = Optional.ofNullable(job.getOutput());
        Display.getDefault().syncExec(() -> {
            if (this.assignments.isPresent()) {
                String previouslySelectedAssignmentName = selectedAssignment.map(Assignment::getName).orElse(null);
                this.combo.removeAll();
                int indexToSelect = 0;
                int index = 0;
                for (Assignment assignment : this.assignments.get()) {
                    this.combo.add(assignment.getName() + " (" + assignment.getState() + ")");
                    if (assignment.getName().equals(previouslySelectedAssignmentName)) {
                        indexToSelect = index;
                    }
                    index++;
                }
                this.combo.pack();
                if (this.assignments.get().size() > indexToSelect) {
                    this.selectedAssignment = Optional.ofNullable(this.assignments.get().get(indexToSelect));
                    this.combo.select(indexToSelect);
                }
            }
        });
    }

    /**
     * Called when retriving the submissionlist is done.
     *
     * @param job
     */
    private void onListSubmissionFinished(ListSubmissionsJob job) {
        this.groupNames = job.getGroupNames().map(ArrayList::new);
        if (this.groupNames.isPresent()) {
            Display.getDefault().syncExec(() -> {
                for (String groupName : this.groupNames.get()) {
                    this.swtList.add(groupName);
                }

            });
        }
    }
    

    /**
     * Gets the selected Assignment from the combobox.
     * @return Optional<Assignment>
     */
    public Optional<Assignment> getSelectedAssignment() {
        return this.selectedAssignment;
    }

}
