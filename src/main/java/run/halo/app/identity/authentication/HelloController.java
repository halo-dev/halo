package run.halo.app.identity.authentication;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author guqing
 * @date 2022-04-12
 */
@RestController
@RequestMapping("/hello")
public class HelloController {

    @GetMapping
    public String test() {
        return "you can not see me.";
    }
}
