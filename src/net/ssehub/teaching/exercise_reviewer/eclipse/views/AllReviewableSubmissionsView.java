package net.ssehub.teaching.exercise_reviewer.eclipse.views;

import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.Bundle;

import net.ssehub.teaching.exercise_reviewer.eclipse.Activator;
import net.ssehub.teaching.exercise_reviewer.eclipse.background.DownloadAllSubmissionsJob;
import net.ssehub.teaching.exercise_reviewer.eclipse.background.IRunnableStuMgmt;
import net.ssehub.teaching.exercise_reviewer.eclipse.background.ListSubmissionsJob;
import net.ssehub.teaching.exercise_reviewer.eclipse.background.StuMgmtJob;
import net.ssehub.teaching.exercise_reviewer.eclipse.dialog.AdvancedExceptionDialog;
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
    private Label label;
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
        FillLayout fillLayout = new FillLayout(SWT.VERTICAL);
        parent.setLayout(fillLayout);
        this.swtList = new List(parent, SWT.BORDER | SWT.V_SCROLL | SWT.SINGLE);

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
                                    AllReviewableSubmissionsView.this.selectedAssignment.get());
                        }
                    }
                } catch (PartInitException e) {
                    // TODO exception
                }

            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
                // TODO Auto-generated method stub

            }
        });

        this.createRightclickMenu();
        this.createCombo(parent);

        //        Submission

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
                        MessageDialog.openInformation(new Shell(), "test",
                                AllReviewableSubmissionsView.this.swtList.getItem(selected));

                    }

                    @Override
                    public void widgetDefaultSelected(SelectionEvent arg0) {
                        // TODO Auto-generated method stub

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
                    this.selectedAssignment.get(), this::onDownloadAllSubmissionsFinished,
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow());
            job.setUser(true);
            job.schedule();
        }
    }

    /**
     * Creats the combo widget.
     *
     * @param parent
     */
    private void createCombo(Composite parent) {
        this.combo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.SIMPLE);
        this.combo.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
                // TODO Auto-generated method stub

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
        // TODO maybe as a background job too

        IRunnableStuMgmt<java.util.List<Assignment>> func = new IRunnableStuMgmt<java.util.List<Assignment>>() {

            @Override
            public java.util.List<Assignment> run() {
                java.util.List<Assignment> assignments = null;
                try {
                    assignments = Activator.getDefault().getManager().getAllAssignments();
                } catch (ApiException e) {
                    Display.getDefault().syncExec(() -> {
                        AdvancedExceptionDialog.showUnexpectedExceptionDialog(e, "Cant load assignments");
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
                this.combo.removeAll();
                for (Assignment assignment : this.assignments.get()) {
                    this.combo.add(assignment.getName());
                }
                this.combo.pack();
                if (this.assignments.get().size() > 0) {
                    this.selectedAssignment = Optional.ofNullable(this.assignments.get().get(0));
                    this.combo.select(0);
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
     * When download all job is finished.
     *
     * @param job
     */
    private void onDownloadAllSubmissionsFinished(DownloadAllSubmissionsJob job) {

    }
    /**
     * Gets the selected Assignment from the combobox.
     * @return Optional<Assignment>
     */
    public Optional<Assignment> getSelectedAssignment() {
        return this.selectedAssignment;
    }

}
