package ch.usi.si.seart.pyrefac.core;

import ch.usi.si.seart.pyrefac.core.exception.NameAlreadyInUseException;
import ch.usi.si.seart.pyrefac.core.exception.PyNamedParameterNotFoundException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.LocalSearchScope;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyNamedParameter;
import com.jetbrains.python.psi.PyParameterList;
import com.jetbrains.python.refactoring.PyRefactoringUtil;

import java.util.Objects;

public final class RenameFunctionParameters extends FunctionRefactoring {

    private final String oldName;
    private final String newName;

    @JsonCreator
    RenameFunctionParameters(
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
        PyParameterList parameters = node.getParameterList();
        PyNamedParameter target = parameters.findParameterByName(oldName);
        if (target == null) throw new PyNamedParameterNotFoundException(oldName);
        boolean canRename = PyRefactoringUtil.isValidNewName(newName, target);
        if (!canRename) throw new NameAlreadyInUseException(newName);
        LocalSearchScope scope = new LocalSearchScope(node);
        ThrowableComputable<PsiElement, RuntimeException> action = getRenameAction(scope, target, newName);
        WriteCommandAction.runWriteCommandAction(project, action);
    }
}
