package net.ssehub.teaching.exercise_reviewer.eclipse.background;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;

import net.ssehub.teaching.exercise_reviewer.eclipse.Activator;
import net.ssehub.teaching.exercise_reviewer.eclipse.dialog.AdvancedExceptionDialog;
import net.ssehub.teaching.exercise_submitter.lib.data.Assignment;
import net.ssehub.teaching.exercise_submitter.lib.replay.ReplayException;
import net.ssehub.teaching.exercise_submitter.lib.replay.Replayer;
import net.ssehub.teaching.exercise_submitter.lib.student_management_system.ApiException;
import net.ssehub.teaching.exercise_submitter.lib.student_management_system.GroupNotFoundException;

/**
 * This class is a job to handle the download process for the submissions.
 *
 * @author lukas
 *
 */
public class DownloadAllSubmissionsJob extends ReviewerJobs {

    private Assignment assignment;
    private List<IProject> projects = new ArrayList<IProject>();
    private Consumer<DownloadAllSubmissionsJob> callbackDownloadAllSubmissionsJob;
    private IWorkbenchWindow window;

    /**
     * This creates an instance of {@link DownloadAllSubmissionsJob}.
     *
     * @param shell
     * @param assignment
     * @param callbackDownloadAllSubmissionsJob
     * @param window
     */
    public DownloadAllSubmissionsJob(Shell shell, Assignment assignment,
            Consumer<DownloadAllSubmissionsJob> callbackDownloadAllSubmissionsJob, IWorkbenchWindow window) {
        super("Download all Submissions", Optional.ofNullable(shell));
        this.assignment = assignment;
        this.callbackDownloadAllSubmissionsJob = callbackDownloadAllSubmissionsJob;
        this.window = window;
    }

    @Override
    protected void runAsync(IProgressMonitor monitor) {
        monitor.beginTask("Download submissions", 100);
        IWorkingSetManager workingsetmanager = PlatformUI.getWorkbench().getWorkingSetManager();
        IWorkbenchPage page = this.window.getActivePage();

        try {
            Set<String> set = Activator.getDefault().getManager().getStudentManagementConnection()
                    .getAllGroups(Activator.getDefault().getManager().getCourse(), this.assignment);
            List<String> listNames = new ArrayList<>(set);

            SubMonitor submonitor = SubMonitor.convert(monitor, listNames.size());

            for (String string : listNames) {

                Replayer replayer = null;
                try {
                    replayer = Activator.getDefault().getManager().getReplayer(this.assignment);
                    replayer.replay(replayer.getVersions().get(0));
                } catch (ReplayException | GroupNotFoundException e) {
                    // TODO think about exception list.
                }

                submonitor.split(1).done();

                this.createIProject(string);

            }

            IWorkingSet[] workingsets = workingsetmanager.getWorkingSets();

            IWorkingSet newSet = workingsetmanager.createWorkingSet(this.assignment.getName(),
                    (IAdaptable[]) this.projects.toArray());

            List<IWorkingSet> listworkingset = Arrays.asList(workingsets);
            listworkingset.add(newSet);
            workingsets = (IWorkingSet[]) listworkingset.toArray();

            page.setWorkingSets(workingsets);

        } catch (ApiException e) {
            Display.getDefault().syncExec(() -> {
                AdvancedExceptionDialog.showUnexpectedExceptionDialog(e, "Cant download all submissions");
            });
        }
    }

    /**
     * Creates the IProject.
     *
     * @param groupname
     * @return boolean , true if it worked.
     */
    private boolean createIProject(String groupname) {
        boolean isCreated = false;

        String projectName = "Submission from: " + groupname;
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot root = workspace.getRoot();
        IProject newProject = root.getProject(projectName);
        try {
            newProject.create(null);
            newProject.open(null);
            this.projects.add(newProject);
            isCreated = true;
        } catch (CoreException e) {

        }
//        this.location = Optional.ofNullable(newProject.getLocation().toFile());
//
//        try {
//            IProjectDescription description = newProject.getDescription();
//            description.setNatureIds(new String[] {"org.eclipse.jdt.core.javanature"});
//            newProject.setDescription(description, null);
//        } catch (CoreException e) {
//            EclipseLog.warning("Failed to set java nature for new project: " + e.getMessage());
//        }

        return isCreated;
    }

}
