package run.halo.app.identity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A controller should ONLY be used during testing for this PR.
 *
 * @author guqing
 * @since 2.0.0
 */
@RestController
@RequestMapping("/tags")
public class TagController {

    @GetMapping
    public String hello() {
        return "Tag you see me.";
    }

    @GetMapping("/{name}")
    public String getByName(@PathVariable String name) {
        return "Tag name:" + name + "-->Now you see me.";
    }
}
