package cc.ryanc.halo.web.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Backup controller
 *
 * @author : RYAN0UP
 * @date : 2019-03-14
 */
@Controller
@RequestMapping(value = "/admin/backup")
public class BackupController {

    /**
     * backup manage
     *
     * @return template path: admin/admin_backup.ftl
     */
    @GetMapping
    public String backup() {
        return "admin/admin_backup";
    }
}
