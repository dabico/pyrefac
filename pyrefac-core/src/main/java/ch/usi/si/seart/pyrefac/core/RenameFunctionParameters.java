package ch.usi.si.seart.pyrefac.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Query;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyNamedParameter;
import com.jetbrains.python.psi.PyParameterList;

import java.util.NoSuchElementException;

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
        super(className, functionName);
        this.oldName = oldName;
        this.newName = newName;
    }

    @Override
    protected void perform(PyFunction node) {
        Project project = node.getProject();
        PyParameterList parameters = node.getParameterList();
        PyNamedParameter conflicting = parameters.findParameterByName(newName);
        if (conflicting != null) {
            String message = "Parameter " + newName + " already exists in function " + node.getName();
            throw new IllegalArgumentException(message);
        }
        PyNamedParameter target = parameters.findParameterByName(oldName);
        if (target == null) {
            String message = "Parameter " + oldName + " not found in function " + node.getName();
            throw new NoSuchElementException(message);
        }
        ThrowableComputable<PsiElement, RuntimeException> action = () -> {
            LocalSearchScope searchScope = new LocalSearchScope(node);
            Query<PsiReference> references = ReferencesSearch.search(target, searchScope);
            references.forEach(reference -> {
                reference.handleElementRename(newName);
            });
            return target.setName(newName);
        };
        WriteCommandAction.runWriteCommandAction(project, action);
    }
}
