package ch.usi.si.seart.pyrefac.core.exception;

public final class NameAlreadyInUseException extends IllegalArgumentException {

    public NameAlreadyInUseException(String name) {
        super("Identifier already in use: \"" + name + "\"");
    }
}
