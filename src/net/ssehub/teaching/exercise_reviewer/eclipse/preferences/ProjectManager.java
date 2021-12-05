package net.ssehub.teaching.exercise_reviewer.eclipse.preferences;

import java.util.Optional;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.swt.widgets.Display;


import net.ssehub.teaching.exercise_reviewer.eclipse.Activator;



/**
 * Saves the assessmentId for the projects dowbloaded.
 * @author lukas
 *
 */
public class ProjectManager {

   
    private static final QualifiedName CONNECTED_ASSIGNMENT_NAME = new QualifiedName(Activator.PLUGIN_ID, "assignment");
    private static final QualifiedName CONNECTED_GROUP_NAME = new QualifiedName(Activator.PLUGIN_ID, "groupname");
    /**
     * Creates a new instance of Projectmanager.
     */
    public ProjectManager() {

    }

    /**
     * Saves the assessmentid.
     * @param project
     * @param projectname
     * @param assignmentid
     * 
     */
    public void setConnection(IProject project, String projectname , String assignmentid) {
        try {
            project.setPersistentProperty(CONNECTED_GROUP_NAME, projectname);
            project.setPersistentProperty(CONNECTED_ASSIGNMENT_NAME, assignmentid);
        } catch (CoreException e) {
            Display.getDefault().syncExec(() -> {
                
            });
        }
    }
    /**
     * Gets the assessmentid.
     * @param project
     * @return String , the assessmentid
     * @throws ProjectException
     */
    public Optional<String> getGroupName(IProject project) {

        Optional<String> groupName = Optional.empty();
        if (project.isOpen()) {
            try {
                groupName = Optional.ofNullable(project.getPersistentProperty(CONNECTED_GROUP_NAME));
            } catch (CoreException e) {
                Display.getDefault().syncExec(() -> {
                   
                });
            }
        }
        return groupName;
    }
    /**
     * Return an optional with the saved assignmentid.
     * 
     * @param project
     * @return Optional<String>
     */
    public Optional<String> getAssignmentId(IProject project) {
        Optional<String> assignmentId = Optional.empty();
        if (project.isOpen()) {
            try {
                assignmentId = Optional.ofNullable(project.getPersistentProperty(CONNECTED_ASSIGNMENT_NAME));
            } catch (CoreException e) {
                Display.getDefault().syncExec(() -> {
                   
                });
            }
        }
        return assignmentId;
    }

  




}
