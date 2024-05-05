package ch.usi.si.seart.pyrefac.cli;

import groovyjarjarpicocli.CommandLine.IParameterExceptionHandler;
import groovyjarjarpicocli.CommandLine.ParameterException;

public final class ParameterExceptionHandler implements IParameterExceptionHandler {

    @Override
    public int handleParseException(ParameterException ex, String[] ignored) {
        Throwable cause = ex.getCause();
        System.err.println(cause.getMessage());
        return 1;
    }
}
