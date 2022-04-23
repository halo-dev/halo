package run.halo.app.config.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.apache.commons.text.StringEscapeUtils;
import java.io.IOException;


public class XssJacksonDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser jp, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        return StringEscapeUtils.escapeHtml4(jp.getText());
    }
}
