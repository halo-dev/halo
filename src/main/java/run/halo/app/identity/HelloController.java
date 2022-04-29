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
@RequestMapping("/posts")
public class HelloController {

    @GetMapping
    public String hello() {
        return "Now you see me.";
    }

    @GetMapping("/{name}")
    public String getByName(@PathVariable String name) {
        return "Name:" + name + "-->Now you see me.";
    }
}
