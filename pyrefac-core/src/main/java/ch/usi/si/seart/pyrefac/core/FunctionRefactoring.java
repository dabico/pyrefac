package ch.usi.si.seart.pyrefac.core;

import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Query;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyRecursiveElementVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;

abstract class FunctionRefactoring implements Refactoring {

    private final String className;
    private final String functionName;

    protected FunctionRefactoring(String className, String functionName) {
        this.className = className;
        this.functionName = functionName;
    }

    protected abstract void perform(PyFunction node);

    @Override
    public final void perform(PyFile file) {
        file.accept(new PyRecursiveElementVisitor() {

            @Override
            public void visitPyFunction(@NotNull PyFunction node) {
                String visitedFunctionName = node.getName();
                if (Objects.equals(visitedFunctionName, functionName)) {
                    PyClass containingClass = node.getContainingClass();
                    String containingClassName = containingClass != null ? containingClass.getName() : null;
                    if (Objects.equals(containingClassName, className)) {
                        perform(node);
                    }
                }
            }
        });
    }

    protected static <P extends PsiNamedElement> ThrowableComputable<PsiElement, RuntimeException> getRenameAction(
            SearchScope searchScope, P target, String name
    ) {
        return () -> {
            Query<PsiReference> references = ReferencesSearch.search(target, searchScope);
            Consumer<PsiReference> consumer = reference -> reference.handleElementRename(name);
            references.forEach(consumer);
            return target.setName(name);
        };
    }
}
