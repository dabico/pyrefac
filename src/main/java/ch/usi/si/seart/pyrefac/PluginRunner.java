package ch.usi.si.seart.pyrefac;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.application.ApplicationStarter;
import com.intellij.openapi.diagnostic.Logger;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
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
                description = "The URL of the Git repository"
        )
        private String url;

        @Parameters(
                index = "1",
                description = "The path to the Python file to refactor"
        )
        private Path path;

        @Parameters(
                index = "2",
                description = "The name of the refactoring to perform"
        )
        private Refactoring refactoring;

        @Parameters(
                index = "3",
                converter = JsonNodeConverter.class,
                description = "The configuration file, containing refactoring inputs"
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

            try (AutomaticDirectoryCleaner ignored = new AutomaticDirectoryCleaner(workdir)) {
                switch (refactoring) {
                    case ADD_COMMENT:
                    case RENAME_LITERAL:
                    case RENAME_FUNCTION_PARAMETER:
                        break;
                    default:
                        throw new UnsupportedOperationException("Refactoring type not implemented: " + refactoring);
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }

            return 0;
        }

        private enum Refactoring {

            ADD_COMMENT,
            RENAME_LITERAL,
            RENAME_FUNCTION_PARAMETER;

            @Override
            public String toString() {
                return name().toLowerCase();
            }
        }
    }
}
