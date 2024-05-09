package ch.usi.si.seart.pyrefac.core.exception;

import java.util.NoSuchElementException;

public final class PsiNamedElementNotFoundException extends NoSuchElementException {

    public PsiNamedElementNotFoundException(String name) {
        super("Could not find PSI element with name: \"" + name + "\"");
    }
}
