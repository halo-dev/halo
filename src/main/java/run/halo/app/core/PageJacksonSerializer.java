package run.halo.app.core;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import org.springframework.data.domain.Page;
import run.halo.app.model.support.CommentPage;

/**
 * Custom serializer for Page object.
 *
 * @author johnniang
 * @date 3/19/19
 */
public class PageJacksonSerializer extends JsonSerializer<Page> {

    @Override
    public void serialize(Page page, JsonGenerator generator, SerializerProvider serializers)
        throws IOException {
        generator.writeStartObject();

        generator.writeObjectField("content", page.getContent());
        generator.writeNumberField("pages", page.getTotalPages());
        generator.writeNumberField("total", page.getTotalElements());
        generator.writeNumberField("page", page.getNumber());
        generator.writeNumberField("rpp", page.getSize());
        generator.writeBooleanField("hasNext", page.hasNext());
        generator.writeBooleanField("hasPrevious", page.hasPrevious());
        generator.writeBooleanField("isFirst", page.isFirst());
        generator.writeBooleanField("isLast", page.isLast());
        generator.writeBooleanField("isEmpty", page.isEmpty());
        generator.writeBooleanField("hasContent", page.hasContent());

        // Handle comment page
        if (page instanceof CommentPage) {
            CommentPage commentPage = (CommentPage) page;
            generator.writeNumberField("commentCount", commentPage.getCommentCount());
        }

        generator.writeEndObject();
    }
}
