package ch.usi.si.seart.pyrefac.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;

public final class PyRefacModule extends SimpleModule {

    public PyRefacModule() {
        super(PyRefacModule.class.getName());
        addDeserializer(String.class, new NullCoercingStringDeserializer());
    }
}
