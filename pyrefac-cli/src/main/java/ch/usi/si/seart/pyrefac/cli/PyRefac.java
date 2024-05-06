package ch.usi.si.seart.pyrefac.cli;

import ch.usi.si.seart.pyrefac.core.Refactoring;
import ch.usi.si.seart.pyrefac.jackson.PyRefacModule;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiManager;
import com.jetbrains.python.psi.PyFile;
import git4idea.commands.Git;
import git4idea.commands.GitCommand;
import git4idea.commands.GitCommandResult;
import git4idea.commands.GitLineHandler;
import groovyjarjarpicocli.CommandLine.Command;
import groovyjarjarpicocli.CommandLine.ITypeConverter;
import groovyjarjarpicocli.CommandLine.Parameters;
import org.jdom.JDOMException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.Callable;

@Command(
        name = "pyrefac",
        separator = " ",
        version = "1.0.0",
        mixinStandardHelpOptions = true,
        description = "Performs various refactorings on Python code"
)
public final class PyRefac implements Callable<Integer> {

    private static final ObjectMapper JSON_MAPPER = JsonMapper.builder().addModule(new PyRefacModule()).build();

    private final Git git = Git.getInstance();
    private final ProjectManager projectManager = ProjectManager.getInstance();
    private final VirtualFileManager fileManager = VirtualFileManager.getInstance();
    private final FileDocumentManager documentManager = FileDocumentManager.getInstance();

    @Parameters(
            index = "0",
            description = "URL of the Git repository"
    )
    private String url;

    @Parameters(
            index = "1",
            description = "Path of the Python file to refactor, relative to the repository root"
    )
    private Path relative;

    @Parameters(
            index = "2",
            converter = RefactoringClassConverter.class,
            description = "Name of the refactoring to perform, in `snake_case` format"
    )
    private Class<? extends Refactoring> type;

    private static final class RefactoringClassConverter implements ITypeConverter<Class<? extends Refactoring>> {

        @Override
        public Class<? extends Refactoring> convert(String value) {
            String className = StringUtil.toTitleCase(value).replace("_", "");
            String packageName = Refactoring.class.getPackageName();
            String fullyQualifiedName = packageName + "." + className;

            try {
                return Class.forName(fullyQualifiedName).asSubclass(Refactoring.class);
            } catch (ClassNotFoundException ex) {
                throw new UnsupportedOperationException("Unsupported refactoring: " + value, ex);
            } catch (ClassCastException ex) {
                throw new UnsupportedOperationException("Can not be used for refactoring: " + className, ex);
            }
        }
    }

    @Parameters(
            index = "3",
            converter = JsonNodeConverter.class,
            description = "Configuration file, containing refactoring-specific inputs"
    )
    private JsonNode config;

    private static final class JsonNodeConverter implements ITypeConverter<JsonNode> {

        @Override
        public JsonNode convert(String value) throws IOException {
            File file = Paths.get(value).toFile();
            if (!file.exists()) throw new FileNotFoundException("Not found: " + value);
            if (!file.isFile()) throw new IllegalArgumentException("Not a file: " + value);
            return JSON_MAPPER.readTree(file);
        }
    }

    @Override
    public Integer call() throws IOException, JDOMException {
        Path workdir = getWorkdir();
        Path parent = workdir.getParent();
        GitCommandResult result = git.clone(null, parent.toFile(), url, workdir.getFileName().toString());
        if (!result.success()) throw new IOException(result.getErrorOutputAsJoinedString());
        Project project = projectManager.loadAndOpenProject(workdir.toString());
        if (project == null) throw new IOException("Failed to open project: " + workdir);
        try {
            Integer code = call(workdir, project);
            if (code != 0) return code;
            GitLineHandler handler = new GitLineHandler(project, workdir.toFile(), GitCommand.DIFF);
            result = git.runCommand(handler);
            if (!result.success()) throw new IOException(result.getErrorOutputAsJoinedString());
            System.out.println(result.getOutputAsJoinedString());
            return code;
        } finally {
            projectManager.closeAndDispose(project);
            FileUtil.delete(workdir);
        }
    }

    private static Path getWorkdir() {
        String tmpdir = System.getProperty("java.io.tmpdir");
        String prefix = PyRefac.class.getSimpleName().toLowerCase();
        String dirname = prefix + "-" + System.currentTimeMillis();
        return Paths.get(tmpdir, dirname);
    }

    private Integer call(Path workdir, Project project) throws IOException {
        PsiManager psiManager = PsiManager.getInstance(project);
        Path absolute = workdir.resolve(relative);
        Refactoring refactoring = JSON_MAPPER.treeToValue(config, type);
        VirtualFile virtualFile = fileManager.findFileByNioPath(absolute);
        PyFile pyFile = Optional.ofNullable(virtualFile)
                .map(psiManager::findFile)
                .map(PyFile.class::cast)
                .orElseThrow(() -> new FileNotFoundException("Not found: " + absolute));
        refactoring.perform(pyFile);
        return Optional.of(virtualFile)
                .map(documentManager::getDocument)
                .filter(documentManager::isDocumentUnsaved)
                .map(document -> {
                    documentManager.saveDocument(document);
                    return 0;
                })
                .orElse(1);
    }
}
