package ch.usi.si.seart.pyrefac.core;

import com.intellij.openapi.project.Project;
import com.jetbrains.python.psi.PyFile;

public interface Refactoring {

    void perform(Project project, PyFile file);
}
