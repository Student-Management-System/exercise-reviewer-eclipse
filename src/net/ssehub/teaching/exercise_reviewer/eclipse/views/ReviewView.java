package net.ssehub.teaching.exercise_reviewer.eclipse.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

/**
 * 
 * @author lukas
 *
 */
public class ReviewView extends ViewPart {
    private Label label;
    public ReviewView() {
        super();
    }

    @Override
    public void createPartControl(Composite parent) {
        label = new Label(parent, 0);
        label.setText("Hello World");
        
    }

    @Override
    public void setFocus() {
       label.setFocus();
        
    }

}
