package run.halo.app.identity;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * A controller should ONLY be used during testing for this PR.
 * TODO It'll be deleted next time
 *
 * @author guqing
 * @since 2.0.0
 */
@Controller
@ResponseBody
@RequestMapping
public class HealthyController {

    @RequestMapping("/healthy")
    public String hello() {
        return "I am very healthy.";
    }

    @GetMapping("/static/js/test.js")
    public String fakeJs() {
        return "console.log('hello world!')";
    }
}
