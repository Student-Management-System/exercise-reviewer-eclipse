package net.ssehub.teaching.exercise_reviewer.eclipse.submissions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import net.ssehub.teaching.exercise_reviewer.eclipse.Activator;
import net.ssehub.teaching.exercise_reviewer.eclipse.background.DownloadAllSubmissionsJob.Project;
import net.ssehub.teaching.exercise_reviewer.eclipse.dialog.DownloadAllResultDialog;
import net.ssehub.teaching.exercise_reviewer.eclipse.background.StuMgmtJob;
import net.ssehub.teaching.exercise_reviewer.eclipse.log.EclipseLog;
import net.ssehub.teaching.exercise_submitter.lib.ExerciseSubmitterManager;
import net.ssehub.teaching.exercise_submitter.lib.data.Assignment;
import net.ssehub.teaching.exercise_submitter.lib.replay.ReplayException;
import net.ssehub.teaching.exercise_submitter.lib.replay.Replayer;

/**
 * Handles the download from a user submission.
 * 
 * @author lukas
 *
 */
public class DownloadSubmission {
    
    private String groupname;
    private Project project;
    private Assignment assignment;
    private ExerciseSubmitterManager manager;
    
    /**
     * Creates instance of {@link #DownloadSubmission}.
     * @param groupname
     * @param project
     * @param assignment
     * @param manager
     */
    public DownloadSubmission(String groupname, Project project,
            Assignment assignment, ExerciseSubmitterManager manager) {
        this.groupname = groupname;
        this.project = project;
        this.assignment = assignment;
        this.manager = manager;
    }
    /**
     * Start the download.
     * 
     * @return Project
     * @throws CourseNotSelectedException
     */
    public Project start() {
        replay();       
        return project;    
    }
    /**
     * Replay the submission from the group in the course.
     * 
     * @throws CourseNotSelectedException
     */
    private void replay() {
        try {
            Replayer replayer = manager.getReplayer(this.assignment, groupname);
                   
            File temporaryCheckout = replayer.replayLatest();
        
            project.setFile(temporaryCheckout);
            
            createIProject();
            
            copyProject(project.getFile().get().toPath(), project.getProject().get().getLocation().toFile().toPath());
            copyDefaultClasspath(project.getProject().get().getLocation().toFile().toPath());
            project.getProject().get().refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
            replayer.close();
            
        } catch (ReplayException | CoreException | IOException e) {
            project.setException(e);
        }
    }
    /**
     *  Creates for the downloaded submission a IProject in
     *  Eclipse.
     *        
     *        
     * @return boolean
     * @throws CoreException
     */
    private boolean createIProject() throws CoreException {
        boolean isCreated = false;
    
        String groupname = project.getGroupName();
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
     * Called when stumgmtjob is ready.
     * 
     * @param job
     */
    public static void onFinishedStuMgmtJob(StuMgmtJob<List<Project>> job) {
        Display.getDefault().syncExec(() -> createResultDialog(job.getShell().orElse(new Shell())
                , job.getOutput()));
    }
    /**
     * Creates the resultdialog.
     * 
     * @param shell
     * @param projects
     */
    public static void createResultDialog(Shell shell, List<Project> projects) {
        DownloadAllResultDialog dialog = new DownloadAllResultDialog(shell, projects);
        dialog.open();
    }

}
