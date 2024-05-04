package ch.usi.si.seart.pyrefac;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

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
}
