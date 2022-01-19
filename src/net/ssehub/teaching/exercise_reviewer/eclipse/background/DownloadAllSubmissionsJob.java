package net.ssehub.teaching.exercise_reviewer.eclipse.background;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;

import net.ssehub.teaching.exercise_reviewer.eclipse.Activator;
import net.ssehub.teaching.exercise_reviewer.eclipse.dialog.ExceptionDialog;
import net.ssehub.teaching.exercise_reviewer.eclipse.log.EclipseLog;
import net.ssehub.teaching.exercise_reviewer.eclipse.submissions.DownloadSubmission;
import net.ssehub.teaching.exercise_submitter.lib.ExerciseSubmitterManager;
import net.ssehub.teaching.exercise_submitter.lib.data.Assignment;
import net.ssehub.teaching.exercise_submitter.lib.student_management_system.ApiException;
import net.ssehub.teaching.exercise_submitter.lib.student_management_system.IApiConnection;

/**
 * This class is a job to handle the download process for the submissions.
 *
 * @author lukas
 *
 */
public class DownloadAllSubmissionsJob extends ReviewerJobs {

    private Assignment assignment;
    private List<Project> projects = new ArrayList<Project>();
    private IWorkbenchWindow window;

    /**
     * This class handles the project.
     * @author lukas
     *
     */
    public static class Project {
        private String groupName;
        private Optional<File> file = Optional.empty();
        private Optional<IProject> project = Optional.empty();
        private Optional<Exception> exception = Optional.empty();
        private boolean noSubmission;
        /**
         * Creates a new instance of Project.
         * @param groupname
         */
        public Project(String groupname) {
            this.groupName = groupname;
        }
        /**
         * Sets the file.
         * @param file
         */
        public void setFile(File file) {
            this.file = Optional.ofNullable(file);
        }
        /**
         * Sets the IProject.
         * @param project
         */
        public void setIProject(IProject project) {
            this.project = Optional.ofNullable(project);
        }
        /**
         * Sets the exception.
         * @param exception
         */
        public void setException(Exception exception) {
            this.exception = Optional.ofNullable(exception);
        }
        /**
         * Gets the exception as a optional.
         * @return Optional<Exception>
         */
        public Optional<Exception> getException() {
            return this.exception;
        }
        /**
         * Returns of creation is succeded.
         * @return boolean
         */
        public boolean isSucceeded() {
            return this.exception.isEmpty() && !this.noSubmission;
        }
        /**
         * Gets the File.
         * @return File
         */
        public Optional<File> getFile() {
            return this.file;
        }
        /**
         * Gets the IProject.
         * @return Optional<IProject>
         */
        public Optional<IProject> getProject() {
            return this.project;
        }
        /**
         * Gets the groupname.
         * @return String
         */
        public String getGroupName() {
            return this.groupName;
        }
        /**
         * Indicates that there was no submission to this assignment by this group.
         */
        public void setNoSubmission() {
            this.noSubmission = true;
        }
        /**
         * Returns whether there was no assignment by this group to this assignment.
         * @return Whether no submission has been made.
         */
        public boolean isNoSubmission() {
            return noSubmission;
        }
    }

    /**
     * This creates an instance of {@link DownloadAllSubmissionsJob}.
     *
     * @param shell
     * @param assignment
     * @param window
     */
    public DownloadAllSubmissionsJob(Shell shell, Assignment assignment, IWorkbenchWindow window) {
        super("Download all Submissions", Optional.ofNullable(shell));
        this.assignment = assignment;
        this.window = window;
    }

    @Override
    protected void runAsync(IProgressMonitor monitor) {
        monitor.beginTask("Download submissions", 100);
        IWorkingSetManager workingsetmanager = PlatformUI.getWorkbench().getWorkingSetManager();
        EclipseLog.info("Starting download Submissions");
        try {
            ExerciseSubmitterManager manager = Activator.getDefault().getManager();
            IApiConnection api = manager.getStudentManagementConnection();
            List<String> allGroups = new ArrayList<>(api.getAllGroups(manager.getCourse(), this.assignment));

            SubMonitor submonitor = SubMonitor.convert(monitor, allGroups.size());
        
            replayProjects(manager, allGroups, submonitor);

            this.createWorkingSetAndAddProjects(workingsetmanager);

            Display.getDefault().syncExec(() -> {
                DownloadSubmission.createResultDialog(this.getShell().orElse(new Shell()), this.projects);
            });
           

        } catch (ApiException e) {
            EclipseLog.error(e.getMessage());
            Display.getDefault().syncExec(() -> {
                ExceptionDialog.showUnexpectedExceptionDialog(e, "Cant download all submissions");
            });
        }
    }
    /**
     * Replay the submission from the groups.
     * @param manager
     * @param allGroups
     * @param submonitor
     * @throws CourseNotSelectedException
     */
    private void replayProjects(ExerciseSubmitterManager manager,
            List<String> allGroups, SubMonitor submonitor) {
        
        for (String group : allGroups) {
            Project project = new Project(group);
       
            DownloadSubmission submissions = new DownloadSubmission(group, project,
                   assignment, manager);
            submissions.start();
       
            this.projects.add(project);
            submonitor.split(1).done();
        }
    }
   
    /**
     * Creates the workingset and adds the projects.
     * @param workingsetmanager
     */
    private void createWorkingSetAndAddProjects(IWorkingSetManager workingsetmanager) {
        IProject[] projectsArray = new IProject[this.projects.size()];
        for (int i = 0; i < this.projects.size(); i++) {
            if (this.projects.get(i).isSucceeded()) {
                projectsArray[i] = this.projects.get(i).getProject().get();
            }
        }
    
        IWorkingSet[] sets = workingsetmanager.getAllWorkingSets();
    
        boolean alreadyExisting = false;
    
        for (IWorkingSet element : sets) {
            if (element.getName().equals(this.assignment.getName())) {
                element.setElements(projectsArray);
                alreadyExisting = true;
                break;
            }
        }
    
        if (!alreadyExisting) {
            IWorkingSet newSet = workingsetmanager.createWorkingSet(this.assignment.getName(), projectsArray);          
            workingsetmanager.addWorkingSet(newSet);
            
            //TODO: need to refresh gui or something
        }
        
    }
    
//    private WorkingSetModel getWorkingSetModel() {
//        WorkingSetModel result = null;
//        P
//        IWorkbenchPage page = JavaPlugin.getActivePage();
//
//        if (page != null) {
//            IWorkbenchPart activePart = page.getActivePart();
//
//            if (activePart instanceof PackageExplorerPart) {
//                result = ((PackageExplorerPart) activePart).getWorkingSetModel();
//            }
//        }
//
//        return result;
//    }

   
}
