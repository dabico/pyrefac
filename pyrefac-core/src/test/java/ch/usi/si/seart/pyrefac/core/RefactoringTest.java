package ch.usi.si.seart.pyrefac.core;

import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.psi.PsiFileFactory;
import com.jetbrains.python.PythonLanguage;
import com.jetbrains.python.psi.PyFile;

abstract class RefactoringTest {

    protected final static String NAME = ".py";

    protected PyFile createLightPythonFile(String name, String text) {
        ProjectManager projectManager = ProjectManager.getInstance();
        Project project = projectManager.getDefaultProject();
        PsiFileFactory fileFactory = PsiFileFactory.getInstance(project);
        Language language = PythonLanguage.getInstance();
        return (PyFile) fileFactory.createFileFromText(name, language, text, false, true);
    }
}
