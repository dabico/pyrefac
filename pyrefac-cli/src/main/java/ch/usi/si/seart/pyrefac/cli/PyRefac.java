package ch.usi.si.seart.pyrefac.cli;

import ch.usi.si.seart.pyrefac.core.Refactoring;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import git4idea.commands.GitCommandResult;
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

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
            return OBJECT_MAPPER.readTree(file);
        }
    }

    @Override
    public Integer call() throws IOException, JDOMException {
        String tmpdir = System.getProperty("java.io.tmpdir");
        Path parent = Path.of(tmpdir);
        String dirname = getClass().getSimpleName().toLowerCase() + "-" + System.currentTimeMillis();
        Path workdir = Paths.get(tmpdir, dirname);

        GitCommandResult result = git.clone(null, parent.toFile(), url, dirname);
        if (!result.success()) throw new IOException(result.getErrorOutputAsJoinedString());

        try (AutoCloseableProject closable = new AutoCloseableProject(workdir)) {
            Path absolute = workdir.resolve(relative);
            Project project = closable.getProjectInstance();
            Refactoring refactoring = OBJECT_MAPPER.treeToValue(config, type);
            PsiManager psiManager = PsiManager.getInstance(project);
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

    private final class AutoCloseableProject implements AutoCloseable {

        private final Path path;
        private final Project project;

        AutoCloseableProject(Path path) throws JDOMException, IOException {
            this.path = path;
            this.project = projectManager.loadAndOpenProject(path.toString());
        }

        public Project getProjectInstance() {
            return project;
        }

        @Override
        public void close() throws IOException {
            projectManager.closeAndDispose(project);
            FileUtil.delete(path);
        }
    }
}
