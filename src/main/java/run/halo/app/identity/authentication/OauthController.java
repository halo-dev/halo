package run.halo.app.identity.authentication;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author guqing
 * @date 2022-04-12
 */
@RestController
@RequestMapping("/oauth")
public class OauthController {

    @GetMapping("login")
    public String login() {
        return "hello";
    }
}
