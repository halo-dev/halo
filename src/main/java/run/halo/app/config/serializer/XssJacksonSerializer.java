package run.halo.app.config.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import org.apache.commons.text.StringEscapeUtils;


public class XssJacksonSerializer extends JsonSerializer<String> {
    @Override
    public void serialize(String value, JsonGenerator jsonGenerator, SerializerProvider provider)
        throws IOException {
        jsonGenerator.writeString(StringEscapeUtils.escapeHtml4(value));
    }
}
