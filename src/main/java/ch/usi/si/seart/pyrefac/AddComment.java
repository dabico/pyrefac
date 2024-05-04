package ch.usi.si.seart.pyrefac;

import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("unused")
record AddComment(
        @JsonProperty("class") String className,
        @JsonProperty("function") String functionName,
        @JsonProperty("comment") String comment
) implements Refactoring {
}
