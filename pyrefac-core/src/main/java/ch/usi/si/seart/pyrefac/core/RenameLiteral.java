package ch.usi.si.seart.pyrefac.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jetbrains.python.psi.PyFunction;

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
        super(className, functionName);
        this.oldName = oldName;
        this.newName = newName;
    }

    @Override
    protected void perform(PyFunction node) {
    }
}
