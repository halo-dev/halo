package run.halo.app.extension.index;

import java.util.Set;
import run.halo.app.extension.Extension;

public interface IndexAttribute {

    /**
     * Specify this class is belonged to which extension.
     *
     * @return the extension class.
     */
    Class<? extends Extension> getObjectType();

    /**
     * Get the value of the attribute.
     *
     * @param object the object to get value from.
     * @param <E> the type of the object.
     * @return the value of the attribute must not be null.
     */
    <E extends Extension> Set<String> getValues(E object);
}
