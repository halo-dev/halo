package run.halo.app.config.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import org.springframework.web.util.HtmlUtils;


public class XssJacksonDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser jp, DeserializationContext deserializationContext)
        throws IOException {
        String currentName = jp.getParsingContext().getCurrentName();
        if (jp.getText() != null
            && (!"content".equals(currentName)
            && !"originalContent".equals(currentName))) {
            return HtmlUtils.htmlEscape(jp.getText());
        } else {
            return jp.getText();
        }
    }
}
