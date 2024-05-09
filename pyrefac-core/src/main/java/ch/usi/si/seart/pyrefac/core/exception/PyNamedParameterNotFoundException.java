package ch.usi.si.seart.pyrefac.core.exception;

import com.jetbrains.python.psi.PyNamedParameter;

public final class PyNamedParameterNotFoundException extends PsiNamedElementNotFoundException {

    public PyNamedParameterNotFoundException(String name) {
        super(PyNamedParameter.class, name);
    }
}
