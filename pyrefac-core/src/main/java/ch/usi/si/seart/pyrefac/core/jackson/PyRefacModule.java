package ch.usi.si.seart.pyrefac.core.jackson;

import ch.usi.si.seart.pyrefac.core.Refactoring;
import com.fasterxml.jackson.databind.module.SimpleModule;

public final class PyRefacModule extends SimpleModule {

    public PyRefacModule() {
        super(PyRefacModule.class.getName());
        addDeserializer(String.class, new NullCoercingStringDeserializer());
        addDeserializer(Refactoring.class, new RefactoringDeserializer());
    }
}
