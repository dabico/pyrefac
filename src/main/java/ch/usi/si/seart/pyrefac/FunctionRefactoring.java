package ch.usi.si.seart.pyrefac;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyRecursiveElementVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

abstract class FunctionRefactoring implements Refactoring {

    private final String className;
    private final String functionName;

    protected FunctionRefactoring(String className, String functionName) {
        this.className = className;
        this.functionName = functionName;
    }

    protected void perform(Project ignored, PyFunction node) {
        Logger log = Logger.getInstance(getClass());
        TextRange range = node.getTextRange();
        log.warn("Targeting node at: " + range);
    }

    @Override
    public final void perform(Project project, PyFile file) {
        file.accept(new PyRecursiveElementVisitor() {

            @Override
            public void visitPyFunction(@NotNull PyFunction node) {
                String visitedFunctionName = node.getName();
                if (Objects.equals(visitedFunctionName, functionName)) {
                    PyClass containingClass = node.getContainingClass();
                    String containingClassName = containingClass != null ? containingClass.getName() : null;
                    if (Objects.equals(containingClassName, className)) {
                        perform(project, node);
                    }
                }
            }
        });
    }
}
