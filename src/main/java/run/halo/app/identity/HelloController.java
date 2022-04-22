package run.halo.app.identity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A controller should ONLY be used during testing for this PR.
 *
 * @author guqing
 * @since 2.0.0
 */
@RestController
@RequestMapping("/tests")
public class HelloController {

    @GetMapping
    public String hello() {
        return "Now you see me.";
    }
}
