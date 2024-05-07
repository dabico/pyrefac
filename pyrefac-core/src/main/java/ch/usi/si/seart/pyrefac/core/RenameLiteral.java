package ch.usi.si.seart.pyrefac.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.psi.PyAssignmentStatement;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyTargetExpression;
import com.jetbrains.python.refactoring.PyRefactoringUtil;

import java.util.Objects;

public final class RenameLiteral extends FunctionRefactoring {

    private final String oldName;
    private final String newName;

    @JsonCreator
    RenameLiteral(
            @JsonProperty("class") String className,
            @JsonProperty("function") String functionName,
            @JsonProperty("old_name") String oldName,
            @JsonProperty("new_name") String newName
    ) {
        super(className, Objects.requireNonNull(functionName, "Function name must not be empty or null"));
        this.oldName = Objects.requireNonNull(oldName, "Old parameter name must not be empty or null");
        this.newName = Objects.requireNonNull(newName, "New parameter name must not be empty or null");
    }

    @Override
    protected void perform(PyFunction node) {
        if (oldName.equals(newName)) return;
        Project project = node.getProject();
        PyAssignmentStatement assignment = PsiTreeUtil.findChildrenOfType(node, PyAssignmentStatement.class).stream()
                .filter(statement -> statement.isAssignmentTo(oldName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Literal \"" + oldName + "\" not found"));
        if (assignment.getLeftHandSideExpression() instanceof PyTargetExpression target) {
            boolean canRename = PyRefactoringUtil.isValidNewName(newName, target);
            if (!canRename) throw new IllegalArgumentException("Identifier \"" + newName + "\" already in use");
            PyClass parent = node.getContainingClass();
            LocalSearchScope scope = new LocalSearchScope(parent != null ? parent : node);
            ThrowableComputable<PsiElement, RuntimeException> action = getRenameAction(scope, target, newName);
            WriteCommandAction.runWriteCommandAction(project, action);
        }
    }
}
