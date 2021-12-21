package net.ssehub.teaching.exercise_reviewer.eclipse.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Version;


import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;

/**
 * Gives the user statusinformation.
 * @author lukas
 *
 */
public class HelpDialog extends Dialog {
    
    private Label version;
    private Label tutor;
    private Label connected;
    
    private boolean bconnected;
    private boolean btutor;

    /**
     * Create the dialog.
     * @param parentShell
     * @param connected
     * @param tutor
     */
    public HelpDialog(Shell parentShell, boolean connected, boolean tutor) {
        super(parentShell);
        this.bconnected = connected;
        this.btutor = tutor;
    }

    /**
     * Create contents of the dialog.
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new GridLayout(3, false));
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        
        new Label(container, SWT.NONE).setText("Version: ");
        
        version = new Label(container, SWT.NONE);
      
        
        new Label(container, SWT.NONE);
        
        new Label(container, SWT.NONE).setText("Tutor Rights:");
        
        tutor = new Label(container, SWT.NONE);
     
        
        new Label(container, SWT.NONE);
        
        new Label(container, SWT.NONE).setText("Connected: ");
        
        connected = new Label(container, SWT.NONE);
      
        
   
        Version pluginversion = FrameworkUtil.getBundle(this.getClass()).getVersion();
        connected.setText(bconnected ? "True" : "False");
        version.setText(pluginversion.toString());
        tutor.setText(btutor ? "True" : "False");

        return container;
    }

    /**
     * Create contents of the button bar.
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }
    
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Help Dialog");
    }
    
    /**
     * Return the initial size of the dialog.
     */
    @Override
    protected Point getInitialSize() {
        return new Point(207, 195);
    }
    
}

    
