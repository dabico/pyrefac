package ch.usi.si.seart.pyrefac.server;

import ch.usi.si.seart.pyrefac.core.Refactoring;
import ch.usi.si.seart.pyrefac.server.exception.NotRegularFileException;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiManager;
import com.jetbrains.python.psi.PyFile;
import git4idea.commands.Git;
import git4idea.commands.GitCommand;
import git4idea.commands.GitCommandResult;
import git4idea.commands.GitLineHandler;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.apache.commons.lang3.StringUtils;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public final class RefactoringHandler implements Handler {

    public static final String PATH = "/{owner}/{name}/*";

    private final Git git = Git.getInstance();
    private final Application application = ApplicationManager.getApplication();
    private final ProjectManager projectManager = ProjectManager.getInstance();
    private final VirtualFileManager fileManager = VirtualFileManager.getInstance();
    private final FileDocumentManager documentManager = FileDocumentManager.getInstance();

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        String owner = ctx.pathParam("owner");
        String name = ctx.pathParam("name");
        String prefix = String.format("/%s/%s/", owner, name);
        String url = String.format("https://github.com/%s/%s.git", owner, name);
        String path = ctx.path();
        String file = StringUtils.removeStart(path, prefix);
        Refactoring refactoring = ctx.bodyAsClass(Refactoring.class);
        try (TemporaryDirectory root = TemporaryDirectory.create()) {
            GitLineHandler handler = new GitLineHandler(null, root.path.getParent().toFile(), GitCommand.CLONE);
            handler.addParameters(url, root.path.toString(), "--depth=1");
            GitCommandResult result = git.runCommand(handler);
            if (!result.success()) throw new IOException(result.getErrorOutputAsJoinedString());
            Path absolute = root.path().resolve(file);
            if (!Files.exists(absolute))
                throw new FileNotFoundException(absolute.toString());
            if (!Files.isRegularFile(absolute))
                throw new NotRegularFileException(absolute.toString());
            application.invokeAndWait(() -> handle(ctx, root.path, absolute, refactoring));
        }
    }

    public void handle(
            @NotNull Context ctx,
            @NotNull Path workdir,
            @NotNull Path absolute,
            @NotNull Refactoring refactoring
    ) {
        try (TemporaryProject temp = new TemporaryProject(workdir.toString())) {
            PsiManager psiManager = PsiManager.getInstance(temp.project);
            VirtualFile virtualFile = fileManager.findFileByNioPath(absolute);
            PyFile pyFile = Optional.ofNullable(virtualFile)
                    .map(psiManager::findFile)
                    .map(PyFile.class::cast)
                    .orElseThrow(IllegalStateException::new);
            refactoring.perform(pyFile);
            Optional.of(virtualFile)
                    .map(documentManager::getDocument)
                    .ifPresent(documentManager::saveDocument);
            GitLineHandler handler = new GitLineHandler(temp.project, workdir.toFile(), GitCommand.DIFF);
            GitCommandResult result = git.runCommand(handler);
            if (!result.success()) throw new IOException(result.getErrorOutputAsJoinedString());
            String diff = result.getOutputAsJoinedString();
            ctx.status(diff.isEmpty() ? 204 : 200);
            ctx.result(diff);
        } catch (IOException | JDOMException ignored) {
            ctx.status(500);
        }
    }

    private class TemporaryProject implements AutoCloseable {

        private final Project project;

        TemporaryProject(String path) throws IOException, JDOMException {
            Project project = projectManager.loadAndOpenProject(path);
            if (project == null) throw new IOException("Failed to open project: " + path);
            this.project = project;
        }

        @Override
        public void close() {
            projectManager.closeAndDispose(project);
        }
    }

    private record TemporaryDirectory(Path path) implements AutoCloseable {

        static TemporaryDirectory create() throws IOException {
            Path path = Files.createTempDirectory("pyrefac-server-");
            return new TemporaryDirectory(path);
        }

        @Override
        public void close() throws IOException {
            FileUtil.delete(path);
        }
    }
}
