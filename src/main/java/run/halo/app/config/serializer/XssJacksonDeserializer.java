package run.halo.app.config.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import java.io.IOException;


@Slf4j
public class XssJacksonDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser jp, DeserializationContext deserializationContext)
        throws IOException, JsonProcessingException {
        String currentName = jp.getParsingContext().getCurrentName();
        if (!"content".equals(currentName)) {
            return StringEscapeUtils.escapeHtml4(jp.getText());
        } else {
            return jp.getText();
        }
    }
}
