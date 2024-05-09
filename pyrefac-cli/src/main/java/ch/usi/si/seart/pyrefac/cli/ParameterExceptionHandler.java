package ch.usi.si.seart.pyrefac.cli;

import picocli.CommandLine.IParameterExceptionHandler;
import picocli.CommandLine.ParameterException;

public final class ParameterExceptionHandler implements IParameterExceptionHandler {

    @Override
    public int handleParseException(ParameterException ex, String[] ignored) {
        Throwable cause = ex.getCause();
        System.err.println(cause.getMessage());
        return 1;
    }
}
