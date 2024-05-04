package ch.usi.si.seart.pyrefac;

import com.intellij.openapi.application.ApplicationStarter;
import com.intellij.openapi.diagnostic.Logger;
import groovyjarjarpicocli.CommandLine;
import groovyjarjarpicocli.CommandLine.Command;
import groovyjarjarpicocli.CommandLine.IExecutionExceptionHandler;
import groovyjarjarpicocli.CommandLine.IParameterExceptionHandler;
import groovyjarjarpicocli.CommandLine.ParameterException;
import groovyjarjarpicocli.CommandLine.Parameters;
import groovyjarjarpicocli.CommandLine.ParseResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
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

        @Parameters(index = "0", description = "The URL of the Git repository")
        private String url;

        @Parameters(index = "1", description = "The path to the Python file to refactor")
        private Path path;

        @Parameters(index = "2", description = "The name of the refactoring to perform")
        private Refactoring refactoring;

        @Parameters(index = "3", description = "The configuration file, containing refactoring inputs")
        private Path config;

        @Override
        public Integer call() {
            LOG.warn("Repository:  " + url);
            LOG.warn("File Path:   " + path);
            LOG.warn("Refactoring: " + refactoring);
            LOG.warn("Config:      " + config);
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
