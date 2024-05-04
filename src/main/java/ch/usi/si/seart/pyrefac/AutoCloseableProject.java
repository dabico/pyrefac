package ch.usi.si.seart.pyrefac;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.jdom.JDOMException;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

final class AutoCloseableProject implements AutoCloseable {

    private final ProjectManager manager = ProjectManager.getInstance();

    private final Path path;
    private final Project project;

    AutoCloseableProject(Path path) throws JDOMException, IOException {
        this.path = path;
        this.project = manager.loadAndOpenProject(path.toString());
    }

    public Project getProjectInstance() {
        return project;
    }

    @Override
    public void close() throws IOException {
        manager.closeAndDispose(project);
        Files.walkFileTree(path, new SimpleFileVisitor<>() {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
