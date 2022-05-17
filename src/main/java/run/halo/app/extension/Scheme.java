package run.halo.app.extension;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.util.Assert;

/**
 * This class represents scheme of an Extension.
 *
 * @param type is Extension type.
 * @param groupVersionKind is GroupVersionKind of Extension.
 * @param plural is plural name of Extension.
 * @param singular is singular name of Extension.
 * @param jsonSchema is JSON schema of Extension.
 * @author johnniang
 */
public record Scheme(Class<? extends Extension> type,
                     GroupVersionKind groupVersionKind,
                     String plural,
                     String singular,
                     ObjectNode jsonSchema) {
    public Scheme {
        Assert.notNull(type, "Type of Extension must not be null");
        Assert.notNull(groupVersionKind, "GroupVersionKind of Extension must not be null");
        Assert.hasText(plural, "Plural name of Extension must not be blank");
        Assert.hasText(singular, "Singular name of Extension must not be blank");

        //TODO Validate the json schema when we plan to integrate Extension validation.
        // Assert.notNull(jsonSchema, "Json Schema must not be null");
    }

}
