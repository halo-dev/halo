package run.halo.app.config.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.commons.text.StringEscapeUtils;
import java.io.IOException;


/**
 * 处理向前端发送的JSON数据，将数据进行转译后发送
 */
public class XssJacksonSerializer extends JsonSerializer<String> {
    @Override
    public void serialize(String value, JsonGenerator jsonGenerator, SerializerProvider provider) throws IOException {
        jsonGenerator.writeString(StringEscapeUtils.escapeHtml4(value));
    }
}
