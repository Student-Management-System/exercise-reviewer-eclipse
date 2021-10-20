package net.ssehub.teaching.exercise_reviewer.eclipse.views;

import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.Bundle;

import net.ssehub.teaching.exercise_reviewer.eclipse.Activator;
/**
 * This class creates the {@link AllReviewableSubmissionsView} which contains a list with all reviewable
 * submissions and a download all button.
 * @author lukas
 *
 */
public class AllReviewableSubmissionsView extends ViewPart {
    private Label label;
    private Action getAllSubmissionAction;

    /**
     * Creates an instance of the ReviewView class.
     */
    public AllReviewableSubmissionsView() {
        super();
    }

    @Override
    public void createPartControl(Composite parent) {
        label = new Label(parent, 0);
        label.setText("Hello World");

        createActions();
        createToolbar();
    }

    @Override
    public void setFocus() {
        label.setFocus();

    }
    /**
     * Creates the action.
     */
    public void createActions() {
        getAllSubmissionAction = new Action("Add...") {
            /**
             * Creates the button press action.
             */
            public void run() {
                System.out.println("");
            }
        };
        getAllSubmissionAction.setImageDescriptor(getImageDescriptor("icons/download.png"));

    }
    /**
     * Creates the tooolbar for the view.
     */
    private void createToolbar() {

        IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
        mgr.add(getAllSubmissionAction);

    }
    /**
     * Gets the imagedescriptor from the relative icon path.
     * @param relativePath
     * @return ImageDescriptor
     */
    private ImageDescriptor getImageDescriptor(String relativePath) {
        Activator.getDefault();
        Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
        URL fullPathString = bundle.getEntry(relativePath);
        return ImageDescriptor.createFromURL(fullPathString);
    }

}
