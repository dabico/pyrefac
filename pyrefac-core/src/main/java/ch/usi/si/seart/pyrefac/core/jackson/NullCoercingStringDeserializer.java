package ch.usi.si.seart.pyrefac.core.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import com.intellij.openapi.util.text.StringUtil;

import java.io.IOException;

class NullCoercingStringDeserializer extends StdDeserializer<String> {

    NullCoercingStringDeserializer() {
        super(String.class);
    }

    @Override
    public String deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String result = StringDeserializer.instance.deserialize(parser, context);
        return StringUtil.nullize(result, true);
    }
}
