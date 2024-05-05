package ch.usi.si.seart.pyrefac;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.application.ApplicationStarter;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiManager;
import com.jetbrains.python.psi.PyFile;
import git4idea.commands.Git;
import git4idea.commands.GitCommandResult;
import groovyjarjarpicocli.CommandLine;
import groovyjarjarpicocli.CommandLine.Command;
import groovyjarjarpicocli.CommandLine.IExecutionExceptionHandler;
import groovyjarjarpicocli.CommandLine.IParameterExceptionHandler;
import groovyjarjarpicocli.CommandLine.ITypeConverter;
import groovyjarjarpicocli.CommandLine.ParameterException;
import groovyjarjarpicocli.CommandLine.Parameters;
import groovyjarjarpicocli.CommandLine.ParseResult;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

public class PluginRunner implements ApplicationStarter {

    private static final Logger LOG = Logger.getInstance(PluginRunner.class);

    private static final ParameterExceptionHandler PARAMETER_HANDLER = new ParameterExceptionHandler();

    private static final class ParameterExceptionHandler implements IParameterExceptionHandler {

        @Override
        public int handleParseException(ParameterException ex, String[] ignored) {
            LOG.error("An error occurred while parsing the command line arguments", ex);
            return 1;
        }
    }

    private static final ExecutionExceptionHandler EXCEPTION_HANDLER = new ExecutionExceptionHandler();

    private static final class ExecutionExceptionHandler implements IExecutionExceptionHandler {

        @Override
        public int handleExecutionException(Exception ex, CommandLine cmd, ParseResult parseResult) {
            LOG.error("An error occurred while executing the command", ex);
            return 1;
        }
    }

    @Override
    @Nullable
    public String getCommandName() {
        return "pyrefac";
    }

    @Override
    public void main(@NotNull List<String> arguments) {
        String[] strings = arguments.stream().skip(1).toArray(String[]::new);
        int code = new CommandLine(new PyRefac())
                .setCaseInsensitiveEnumValuesAllowed(true)
                .setParameterExceptionHandler(PARAMETER_HANDLER)
                .setExecutionExceptionHandler(EXCEPTION_HANDLER)
                .execute(strings);
        System.exit(code);
    }

    @Command(
            name = "pyrefac",
            separator = " ",
            version = "1.0.0",
            mixinStandardHelpOptions = true,
            description = "Performs various refactorings on Python code"
    )
    private static final class PyRefac implements Callable<Integer> {

        private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
        public Integer call() {
            String tmpdir = System.getProperty("java.io.tmpdir");
            Path parent = Path.of(tmpdir);
            String dirname = "pyrefac-" + System.currentTimeMillis();
            Path workdir = Paths.get(tmpdir, dirname);

            Git git = Git.getInstance();
            GitCommandResult result = git.clone(null, parent.toFile(), url, dirname);
            if (!result.success()) {
                LOG.error(result.getErrorOutputAsJoinedString());
                return result.getExitCode();
            }

            try (AutoCloseableProject closable = new AutoCloseableProject(workdir)) {
                Path absolute = workdir.resolve(relative);
                Project project = closable.getProjectInstance();
                Refactoring refactoring = OBJECT_MAPPER.treeToValue(config, type);
                PsiManager psiManager = PsiManager.getInstance(project);
                VirtualFileManager fileManager = VirtualFileManager.getInstance();
                VirtualFile virtualFile = fileManager.findFileByNioPath(absolute);
                PyFile pyFile = Optional.ofNullable(virtualFile)
                        .map(psiManager::findFile)
                        .map(PyFile.class::cast)
                        .orElseThrow(() -> new FileNotFoundException("Not found: " + absolute));
                refactoring.perform(project, pyFile);
                FileDocumentManager documentManager = FileDocumentManager.getInstance();
                boolean modified = Optional.of(virtualFile)
                        .map(documentManager::isFileModified)
                        .orElse(false);
                if (!modified) return 1;
                documentManager.saveAllDocuments();
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            } catch (JDOMException ex) {
                throw new IllegalStateException(ex);
            }

            return 0;
        }
    }
}
