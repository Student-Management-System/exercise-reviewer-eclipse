package net.ssehub.teaching.exercise_reviewer.eclipse.preferences;

import java.util.Optional;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.Preferences;

import net.ssehub.teaching.exercise_reviewer.eclipse.Activator;

/**
 * Saves the assessmentId for the projects dowbloaded.
 * @author lukas
 *
 */
public class ProjectManager {

    private static Preferences preferences = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
    /**
     * Creates a new instance of Projectmanager.
     */
    public ProjectManager() {

    }

    /**
     * Saves the assessmentid.
     * @param projectname
     * @param groupname
     */
    public void setConnection(String projectname, String groupname) {
        preferences.put(projectname, groupname);
    }
    /**
     * Gets the assessmentid.
     * @param projectname
     * @return String , the assessmentid
     * @throws ProjectException
     */
    public Optional<String> getGroupNamer(String projectname) {

        Optional<String> groupname = Optional.ofNullable(preferences.get(projectname, null));

        return groupname;
    }





}
