package ch.usi.si.seart.pyrefac;

import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("unused")
record RenameLiteral(
        @JsonProperty("class") String className,
        @JsonProperty("function") String functionName,
        @JsonProperty("old_name") String oldName,
        @JsonProperty("new_name") String newName
) implements Refactoring {
}
