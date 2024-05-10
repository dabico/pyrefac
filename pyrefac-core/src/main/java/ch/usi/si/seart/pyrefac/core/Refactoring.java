package ch.usi.si.seart.pyrefac.core;

import com.jetbrains.python.psi.PyFile;

@FunctionalInterface
public interface Refactoring {

    void perform(PyFile file);
}
