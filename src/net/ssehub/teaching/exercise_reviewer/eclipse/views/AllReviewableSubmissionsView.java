package net.ssehub.teaching.exercise_reviewer.eclipse.views;

import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.Bundle;

import net.ssehub.teaching.exercise_reviewer.eclipse.Activator;
import net.ssehub.teaching.exercise_reviewer.eclipse.background.ListSubmissionsJob;

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
    private Combo combo;

    /**
     * Creates an instance of the ReviewView class.
     */
    public AllReviewableSubmissionsView() {
        super();
    }

    @Override
    public void createPartControl(Composite parent) {

        createActions();
        createToolbar();
        createComponent(parent);
    }

    @Override
    public void setFocus() {

    }

    /**
     * Creates the action.
     */
    public void createActions() {
        getAllSubmissionAction = new Action("Get list of submissions") {
            /**
             * Creates the button press action.
             */
            public void run() {
                clickRefresh();
            }
        };
        getAllSubmissionAction.setImageDescriptor(getImageDescriptor("icons/refresh.png"));

        downloadAllSubmissionAction = new Action("Download all submissions") {
            /**
             * Creates the button press action.
             */
            public void run() {
                System.out.println("");
            }
        };
        downloadAllSubmissionAction.setImageDescriptor(getImageDescriptor("icons/download.png"));

    }

    /**
     * Creates the tooolbar for the view.
     */
    private void createToolbar() {

        IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
        mgr.add(downloadAllSubmissionAction);
        mgr.add(getAllSubmissionAction);

    }

    /**
     * Craetes the widget that are on the view.
     * 
     * @param parent
     */
    private void createComponent(Composite parent) {
        this.swtList = new List(parent, SWT.BORDER | SWT.V_SCROLL | SWT.SINGLE);

        this.combo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.SIMPLE);

        this.combo.add("test");
        this.combo.pack();

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
     * Called when the refresh button i clicked.
     */
    private void clickRefresh() {
        ListSubmissionsJob job = new ListSubmissionsJob(this::onListSubmissionFinished);
        job.setUser(true);
        job.schedule();

    }

    /**
     * Called when retriving the submissionlist is done.
     * 
     * @param job
     */
    private void onListSubmissionFinished(ListSubmissionsJob job) {
        java.util.List<String> submissionlist = job.getSubmissionList();
        Display.getDefault().syncExec(() -> {
            for (String element : submissionlist) {
                this.swtList.add(element);
            }

        });
    }

}
