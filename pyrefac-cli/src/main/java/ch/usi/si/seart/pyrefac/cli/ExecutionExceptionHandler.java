package ch.usi.si.seart.pyrefac.cli;

import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import picocli.CommandLine;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.ParseResult;

import java.io.FileNotFoundException;
import java.util.NoSuchElementException;

public final class ExecutionExceptionHandler implements IExecutionExceptionHandler {

    private static final int USER_ERROR = 1;
    private static final int PLUGIN_ERROR = 127;

    @Override
    public int handleExecutionException(Exception ex, CommandLine cmd, ParseResult parseResult) {
        int code = PLUGIN_ERROR;
        String message = ex.getMessage();

        if (
                ex instanceof FileNotFoundException ||
                ex instanceof NoSuchElementException ||
                ex instanceof IllegalArgumentException
        ) {
            code = USER_ERROR;
        } else if (ex instanceof ValueInstantiationException vex) {
            Throwable cause = vex.getCause();
            message = cause != null ? cause.getMessage() : vex.getMessage();
            code = USER_ERROR;
        }

        System.err.println(message);
        return code;
    }
}
