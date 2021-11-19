package net.ssehub.teaching.exercise_reviewer.eclipse.background;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
import net.ssehub.teaching.exercise_reviewer.eclipse.log.EclipseLog;
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
    private List<Project> projects = new ArrayList<Project>();
    private Consumer<DownloadAllSubmissionsJob> callbackDownloadAllSubmissionsJob;
    private IWorkbenchWindow window;

    /**
     * This class handles the project.
     * @author lukas
     *
     */
    private class Project {
        private File file;
        private Optional<IProject> project;
        private Optional<Exception> exception;
        /**
         * Creates a new instance of Project.
         * @param file
         */
        public Project(File file) {
            this.file = file;
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
        public File getFile() {
            return this.file;
        }
        /**
         * Gets the IProject.
         * @return Optional<IProject>
         */
        public Optional<IProject> getProject() {
            return this.project;
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
            Set<String> set = Activator.getDefault().getManager().getStudentManagementConnection()
                    .getAllGroups(Activator.getDefault().getManager().getCourse(), this.assignment);
            List<String> listNames = new ArrayList<>(set);

            SubMonitor submonitor = SubMonitor.convert(monitor, listNames.size());

            for (String string : listNames) {

                Replayer replayer = null;
                File file = null;
                try {
                    replayer = Activator.getDefault().getManager().getReplayer(this.assignment);
                    file = replayer.replayLatest(string);
                    submonitor.split(1).done();
                    Project project = new Project(file);

                    this.createIProject(project, string);
                } catch (ReplayException | GroupNotFoundException e) {
                    System.out.println(e);
                }

                submonitor.split(1).done();

            }

            this.createWorkingSetAndAddProjects(workingsetmanager);

            for (Project element: this.projects) {
                try {
                    this.copyProject(element.getFile().toPath(), element.project.get().getLocation().toFile().toPath());
                    this.copyDefaultClasspath(element.project.get().getLocation().toFile().toPath());
                } catch (IOException e) {
                    element.setException(e);
                }

            }



        } catch (ApiException e) {
            Display.getDefault().syncExec(() -> {
                AdvancedExceptionDialog.showUnexpectedExceptionDialog(e, "Cant download all submissions");
            });
        } catch (CoreException e) {
            Display.getDefault().syncExec(() -> {
                AdvancedExceptionDialog.showUnexpectedExceptionDialog(e, "Cant create projects");
            });
        }
    }
    /**
     * Creates the workingset and adds the projects.
     * @param workingsetmanager
     */
    private void createWorkingSetAndAddProjects(IWorkingSetManager workingsetmanager) {
        IProject[] projectsArray = new IProject[this.projects.size()];
        for (int i = 0; i < this.projects.size(); i++) {
            projectsArray[i] = this.projects.get(i).getProject().get();
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

        String projectName = "Submission from " + groupname;
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot root = workspace.getRoot();
        IProject newProject = root.getProject(projectName);
        newProject.create(null);
        newProject.open(null);
        project.setIProject(newProject);
        this.projects.add(project);
        isCreated = true;


        try {
            IProjectDescription description = newProject.getDescription();
            description.setNatureIds(new String[] {"org.eclipse.jdt.core.javanature"});
            newProject.setDescription(description, null);
        } catch (CoreException e) {
            EclipseLog.warning("Failed to set java nature for new project: " + e.getMessage());
        }
        newProject.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());

        Activator.getDefault().getProjectManager().setConnection(projectName, groupname);

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
