package ch.usi.si.seart.pyrefac.core.exception;

import com.intellij.psi.PsiNamedElement;

import java.util.NoSuchElementException;

public class PsiNamedElementNotFoundException extends NoSuchElementException {

    public PsiNamedElementNotFoundException(String name) {
        this(PsiNamedElement.class, name);
    }

    protected <T extends PsiNamedElement> PsiNamedElementNotFoundException(Class<T> type, String name) {
        super("Could not find " + type.getSimpleName() + ": \"" + name + "\"");
    }
}
