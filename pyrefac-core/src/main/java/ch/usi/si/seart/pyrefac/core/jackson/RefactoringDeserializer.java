package ch.usi.si.seart.pyrefac.core.jackson;

import ch.usi.si.seart.pyrefac.core.Refactoring;
import ch.usi.si.seart.pyrefac.core.RefactoringUtil;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

class RefactoringDeserializer extends JsonDeserializer<Refactoring> {

    @Override
    public Refactoring deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);
        JsonNode type = node.path("type");
        if (type.isMissingNode()) throw new JsonParseException(parser, "Missing \"type\" field");
        JsonNode parameters = node.path("parameters");
        if (parameters.isMissingNode()) throw new JsonParseException(parser, "Missing \"parameters\" field");
        return context.readTreeAsValue(parameters, RefactoringUtil.getImplementationClass(type.asText()));
    }
}
