package run.halo.app.infra.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.ListResult;

class GenericClassUtilsTest {

    @Test
    void generateConcreteClass() {
        var clazz = GenericClassUtils.generateConcreteClass(ListResult.class, Post.class,
            () -> Post.class.getName() + "List");
        assertEquals("run.halo.app.core.extension.content.PostList", clazz.getName());
        assertEquals("run.halo.app.core.extension.content", clazz.getPackageName());
    }

}