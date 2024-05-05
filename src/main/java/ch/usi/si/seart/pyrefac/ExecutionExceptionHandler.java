package ch.usi.si.seart.pyrefac;

import groovyjarjarpicocli.CommandLine;
import groovyjarjarpicocli.CommandLine.IExecutionExceptionHandler;
import groovyjarjarpicocli.CommandLine.ParseResult;

import java.io.FileNotFoundException;
import java.util.NoSuchElementException;

class ExecutionExceptionHandler implements IExecutionExceptionHandler {

    private static final int USER_ERROR = 1;
    private static final int PLUGIN_ERROR = 127;

    @Override
    public int handleExecutionException(Exception ex, CommandLine cmd, ParseResult parseResult) {
        System.err.println(ex.getMessage());
        if (
                ex instanceof FileNotFoundException ||
                        ex instanceof NoSuchElementException ||
                        ex instanceof IllegalArgumentException
        ) {
            return USER_ERROR;
        } else {
            return PLUGIN_ERROR;
        }
    }
}
