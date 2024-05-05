package ch.usi.si.seart.pyrefac;

import groovyjarjarpicocli.CommandLine.IParameterExceptionHandler;
import groovyjarjarpicocli.CommandLine.ParameterException;

class ParameterExceptionHandler implements IParameterExceptionHandler {

    @Override
    public int handleParseException(ParameterException ex, String[] ignored) {
        Throwable cause = ex.getCause();
        System.err.println(cause.getMessage());
        return 1;
    }
}
