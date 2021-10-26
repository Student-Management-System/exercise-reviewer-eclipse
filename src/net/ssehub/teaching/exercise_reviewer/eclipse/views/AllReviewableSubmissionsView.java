package net.ssehub.teaching.exercise_reviewer.eclipse.views;

import java.net.URL;
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
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.Bundle;

import net.ssehub.teaching.exercise_reviewer.eclipse.Activator;
import net.ssehub.teaching.exercise_reviewer.eclipse.background.ListSubmissionsJob;
import net.ssehub.teaching.exercise_reviewer.lib.data.Submission;

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
    
    private Optional<java.util.List<Submission>> submissionlist = Optional.empty();
    
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
                    
                    if (submissionlist.isPresent()) {
                        int selected = swtList.getSelectionIndex();
                        if (!(selected < 0 || selected >= swtList.getItemCount())) {
                         
                            reviewview.refreshReviewInformation(submissionlist.get().get(selected));
                        }
                    }
                } catch (PartInitException e) {
                    //TODO exception
                }
                
            }
            
            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
                // TODO Auto-generated method stub
                
            }
        });
        
        this.combo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.SIMPLE);
        
        createRightclickMenu();
        

//        Submission
        this.combo.add("test");
        this.combo.pack();

    }
    /**
     * Creates a menu that is activated if a selected list item is rightclicked.
     */
    private void createRightclickMenu() {
        final Menu menu = new Menu(swtList);
        this.swtList.setMenu(menu);
        menu.addMenuListener(new MenuAdapter()
        {
            @Override
            public void menuShown(MenuEvent event) {
                int selected = swtList.getSelectionIndex();

                if (selected < 0 || selected >= swtList.getItemCount()) {
                    return;
                }

                MenuItem[] items = menu.getItems();
                for (int i = 0; i < items.length; i++) {
                    items[i].dispose();
                }
                MenuItem newItem = new MenuItem(menu, SWT.NONE);
                newItem.setText("Download \"" + swtList.getItem(swtList.getSelectionIndex()) + "\"");
                
                newItem.addSelectionListener(new SelectionListener() {
                    
                    @Override
                    public void widgetSelected(SelectionEvent arg0) {
                        MessageDialog.openInformation(new Shell(), "test", swtList.getItem(selected));
                        
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
        submissionlist = Optional.ofNullable(job.getSubmissionList());
        Display.getDefault().syncExec(() -> {
            for (Submission element : submissionlist.get()) {
                this.swtList.add(element.getUserDisplayName());
            }

        });
    }

}
