package ch.usi.si.seart.pyrefac;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyElementGenerator;
import com.jetbrains.python.psi.PyExpressionStatement;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyStatementList;
import com.jetbrains.python.psi.PyStringLiteralExpression;

final class AddComment extends FunctionRefactoring {

    private final String comment;

    @JsonCreator
    AddComment(
            @JsonProperty("class") String className,
            @JsonProperty("function") String functionName,
            @JsonProperty("comment") String comment
    ) {
        super(className, functionName);
        this.comment = comment;
    }

    @Override
    protected void perform(Project project, PyFunction node) {
        PyStringLiteralExpression existing = node.getDocStringExpression();
        PyStatementList statements = node.getStatementList();
        PyElementGenerator generator = PyElementGenerator.getInstance(project);
        String content = "\"\"\"" + comment + "\"\"\"\n";
        PyExpressionStatement created = generator.createDocstring(content);
        ThrowableComputable<PsiElement, RuntimeException> action = () -> existing == null
                ? statements.addBefore(created, statements.getFirstChild())
                : existing.replace(created);
        WriteCommandAction.runWriteCommandAction(project, action);
    }
}
