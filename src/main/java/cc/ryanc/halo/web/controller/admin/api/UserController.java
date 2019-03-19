package cc.ryanc.halo.web.controller.admin.api;

import cc.ryanc.halo.model.dto.UserOutputDTO;
import cc.ryanc.halo.model.entity.User;
import cc.ryanc.halo.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author johnniang
 * @date 3/19/19
 */
@RestController
@RequestMapping("/admin/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("me")
    public UserOutputDTO getOwnDetail(User user) {
        return new UserOutputDTO().convertFrom(user);
    }
}
