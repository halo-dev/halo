package run.halo.app.identity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A controller should ONLY be used during testing for this PR.
 * TODO It'll be deleted next time
 *
 * @author guqing
 * @since 2.0.0
 */
@RestController
@RequestMapping
public class TestController {

    @GetMapping("/posts")
    public String hello() {
        return "list posts.";
    }

    @GetMapping("/posts/{name}")
    public String getPostByName(@PathVariable String name) {
        return "Gets a post with the name: " + name;
    }

    @GetMapping("/categories/{name}")
    public String getCategoryByName(@PathVariable String name) {
        return "Gets a category with the name: " + name;
    }

    @GetMapping("/categories")
    public String listCategories() {
        return "list categories.";
    }

    @GetMapping("/tags")
    public String tags() {
        return "list tags";
    }

    @GetMapping("/{name}")
    public String getByName(@PathVariable String name) {
        return "Gets a tag with the name: " + name;
    }

    @RequestMapping("/healthy")
    public String healthy() {
        return "That's very healthy";
    }
}
