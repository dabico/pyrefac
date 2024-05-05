package ch.usi.si.seart.pyrefac.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.util.text.LineColumn;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyElementGenerator;
import com.jetbrains.python.psi.PyExpressionStatement;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyStatementList;
import com.jetbrains.python.psi.PyStringLiteralExpression;

import java.util.stream.Collectors;

public final class AddComment extends FunctionRefactoring {

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
        int relativeStartOffset = statements.getStartOffsetInParent();
        LineColumn lineColumn = StringUtil.offsetToLineColumn(node.getText(), relativeStartOffset);
        String indentation = StringUtil.repeatSymbol(' ', lineColumn.column);
        PyElementGenerator generator = PyElementGenerator.getInstance(project);
        String indented = wrap(comment).lines()
                .collect(Collectors.joining("\n" + indentation, "", "\n"));
        PyExpressionStatement created = generator.createDocstring(indented);
        ThrowableComputable<PsiElement, RuntimeException> action = () -> existing == null
                ? statements.addBefore(created, statements.getFirstChild())
                : existing.replace(created);
        WriteCommandAction.runWriteCommandAction(project, action);
    }

    private static String wrap(String comment) {
        String quotes = "\"\"\"";
        int newlines = StringUtil.countNewLines(comment);
        return newlines > 0
                ? quotes + "\n" + comment + "\n" + quotes
                : quotes + comment + quotes;
    }
}
