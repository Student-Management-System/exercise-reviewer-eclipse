package net.ssehub.teaching.exercise_reviewer.eclipse.background;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;


import net.ssehub.teaching.exercise_reviewer.eclipse.Activator;
import net.ssehub.teaching.exercise_reviewer.eclipse.dialog.AdvancedExceptionDialog;
import net.ssehub.teaching.exercise_reviewer.eclipse.dialog.DownloadAllResultDialog;
import net.ssehub.teaching.exercise_reviewer.eclipse.log.EclipseLog;
import net.ssehub.teaching.exercise_submitter.lib.ExerciseSubmitterManager;
import net.ssehub.teaching.exercise_submitter.lib.data.Assignment;
import net.ssehub.teaching.exercise_submitter.lib.replay.ReplayException;
import net.ssehub.teaching.exercise_submitter.lib.replay.Replayer;
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
    private Consumer<DownloadAllSubmissionsJob> callbackDownloadAllSubmissionsJob;
    private IWorkbenchWindow window;

    /**
     * This class handles the project.
     * @author lukas
     *
     */
    public class Project {
        private String groupName;
        private Optional<File> file = Optional.empty();
        private Optional<IProject> project = Optional.empty();
        private Optional<Exception> exception = Optional.empty();
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
            return this.exception.isEmpty();
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

    }

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

        try {
            ExerciseSubmitterManager manager = Activator.getDefault().getManager();
            IApiConnection api = manager.getStudentManagementConnection();
            List<String> allGroups = new ArrayList<>(api.getAllGroups(manager.getCourse(), this.assignment));

            SubMonitor submonitor = SubMonitor.convert(monitor, allGroups.size());
            List<Replayer> replayers = new LinkedList<>();

            for (String group : allGroups) {
                Project project = new Project(group);
                try {
                    Replayer replayer = manager.getReplayer(this.assignment, group);
                    replayers.add(replayer);
                    
                    File temporaryCheckout = replayer.replayLatest();
                    submonitor.split(1).done();
                    project.setFile(temporaryCheckout);
                    createIProject(project, group);
                } catch (ReplayException | CoreException e) {
                    project.setException(e);
                }
                this.projects.add(project);
                submonitor.split(1).done();
            }

            this.createWorkingSetAndAddProjects(workingsetmanager);

            copyDownloadedProjects();
                     
            
            for (Replayer replayer : replayers) {
                try {
                    replayer.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            
            Display.getDefault().syncExec(() -> {
                DownloadAllResultDialog dialog = new DownloadAllResultDialog(getShell().orElse(new Shell()), projects);
                dialog.open();
            });
           

        } catch (ApiException e) {
            Display.getDefault().syncExec(() -> {
                AdvancedExceptionDialog.showUnexpectedExceptionDialog(e, "Cant download all submissions");
            });
        }
    }
    /**
     * Copys the downloaded files to the projectfolders.
     */
    private void copyDownloadedProjects() {
        for (Project element: this.projects) {
            if (element.isSucceeded()) {
                try {
                    this.copyProject(element.getFile().get().toPath(), 
                            element.project.get().getLocation().toFile().toPath());
                    this.copyDefaultClasspath(element.project.get().getLocation().toFile().toPath());
                    element.project.get().refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
                } catch (IOException | CoreException e) {
                    //element.setException(e);
                }
            }
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

    /**
     * Creates the IProject.
     *
     * @param groupname
     * @param project
     * @return boolean , true if it worked.
     * @throws CoreException
     */
    private boolean createIProject(Project project, String groupname) throws CoreException {
        boolean isCreated = false;

        String projectName = "Submission from " + groupname + " for " + assignment.getName();
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot root = workspace.getRoot();
        IProject newProject = root.getProject(projectName);
        newProject.create(null);
        newProject.open(null);
        project.setIProject(newProject);
        isCreated = true;


        try {
            IProjectDescription description = newProject.getDescription();
            description.setNatureIds(new String[] {"org.eclipse.jdt.core.javanature"});
            newProject.setDescription(description, null);
        } catch (CoreException e) {
            EclipseLog.warning("Failed to set java nature for new project: " + e.getMessage());
        }

        Activator.getDefault().getProjectManager()
        .setConnection(project.getProject().get(), groupname, assignment.getManagementId());

        return isCreated;
    }

    /**
     * Copies the default <code>.classpath</code> file from the resources to the given target directory.
     *
     * @param targetDirectory The directory where to create the <code>.classpath</code> file in.
     *
     * @throws IOException If creating the file fails.
     */
    private void copyDefaultClasspath(Path targetDirectory) throws IOException {
        InputStream in = this.getClass().getResourceAsStream(".classpath");
        if (in != null) {
            Files.copy(in, targetDirectory.resolve(".classpath"));
        } else {
            throw new IOException(".classpath resource not found");
        }
    }

    /**
     * Copies the project from the temp folder to the project folder.
     *
     * @param source
     * @param target
     * @throws IOException
     */
    private void copyProject(Path source, Path target) throws IOException {
        try {
            Files.walk(source).forEach(sourceFile -> {
                Path targetFile = target.resolve(source.relativize(sourceFile));

                try {
                    if (Files.isDirectory(sourceFile)) {
                        if (!Files.exists(targetFile)) {
                            Files.createDirectory(targetFile);
                        }
                    } else {
                        Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }


}
