package ch.usi.si.seart.pyrefac.server;

import ch.usi.si.seart.pyrefac.core.Refactoring;
import ch.usi.si.seart.pyrefac.server.exception.NotRegularFileException;
import com.intellij.lang.Language;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiFileFactory;
import com.jetbrains.python.PythonLanguage;
import com.jetbrains.python.psi.PyFile;
import git4idea.commands.Git;
import git4idea.commands.GitCommand;
import git4idea.commands.GitCommandResult;
import git4idea.commands.GitLineHandler;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class RefactoringHandler implements Handler {

    public static final String PATH = "/{owner}/{name}/*";

    private final Git git = Git.getInstance();
    ProjectManager projectManager = ProjectManager.getInstance();

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
            String original = Files.readString(absolute);

            StringBuilder builder = new StringBuilder();
            ApplicationManager.getApplication().invokeAndWait(() -> {
                Project project = projectManager.getDefaultProject();
                PsiFileFactory fileFactory = PsiFileFactory.getInstance(project);
                Language language = PythonLanguage.getInstance();
                PyFile pyFile = (PyFile) fileFactory.createFileFromText(".py", language, original, false, true);
                refactoring.perform(pyFile);
                builder.append(pyFile.getText());
            });
            FileUtils.writeStringToFile(absolute.toFile(), builder.toString(), StandardCharsets.UTF_8);

            handler = new GitLineHandler(null, root.path.toFile(), GitCommand.DIFF);
            result = git.runCommand(handler);
            if (!result.success()) throw new IOException(result.getErrorOutputAsJoinedString());
            String diff = result.getOutputAsJoinedString();
            ctx.status(diff.isEmpty() ? 204 : 200);
            ctx.result(diff);
        }
    }

    private record TemporaryDirectory(Path path) implements AutoCloseable {

        public static TemporaryDirectory create() throws IOException {
            Path path = Files.createTempDirectory("pyrefac-server-");
            return new TemporaryDirectory(path);
        }

        @Override
        public void close() throws IOException {
            FileUtil.delete(path);
        }
    }
}
