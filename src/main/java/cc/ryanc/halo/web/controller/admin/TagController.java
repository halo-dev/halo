package cc.ryanc.halo.web.controller.admin;

import cc.ryanc.halo.model.domain.Tag;
import cc.ryanc.halo.model.dto.JsonResult;
import cc.ryanc.halo.model.enums.ResultCodeEnum;
import cc.ryanc.halo.service.TagService;
import cc.ryanc.halo.utils.LocaleMessageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * <pre>
 *     后台标签管理控制器
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2017/12/10
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin/tag")
public class TagController {

    @Autowired
    private TagService tagService;

    @Autowired
    private LocaleMessageUtil localeMessageUtil;

    /**
     * 渲染标签管理页面
     *
     * @return 模板路径admin/admin_tag
     */
    @GetMapping
    public String tags() {
        return "admin/admin_tag";
    }

    /**
     * 新增/修改标签
     *
     * @param tag tag
     * @return 重定向到/admin/tag
     */
    @PostMapping(value = "/save")
    public String saveTag(@ModelAttribute Tag tag) {
        try {
            tagService.saveByTag(tag);
        } catch (Exception e) {
            log.error("新增/修改标签失败：{}", e.getMessage());
        }
        return "redirect:/admin/tag";
    }

    /**
     * 验证是否存在该路径
     *
     * @param tagUrl 标签路径名
     * @return true：不存在，false：已存在
     */
    @GetMapping(value = "/checkUrl")
    @ResponseBody
    public JsonResult checkTagUrlExists(@RequestParam("tagUrl") String tagUrl) {
        Tag tag = tagService.findByTagUrl(tagUrl);
        if (null != tag) {
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.url-is-exists"));
        }
        return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), "");
    }

    /**
     * 处理删除标签的请求
     *
     * @param tagId 标签编号
     * @return 重定向到/admin/tag
     */
    @GetMapping(value = "/remove")
    public String removeTag(@RequestParam("tagId") Long tagId) {
        try {
            tagService.removeByTagId(tagId);
        } catch (Exception e) {
            log.error("删除标签失败：{}", e.getMessage());
        }
        return "redirect:/admin/tag";
    }

    /**
     * 跳转到修改标签页面
     *
     * @param model model
     * @param tagId 标签编号
     * @return 模板路径admin/admin_tag
     */
    @GetMapping(value = "/edit")
    public String toEditTag(Model model, @RequestParam("tagId") Long tagId) {
        Tag tag = tagService.findByTagId(tagId).get();
        model.addAttribute("updateTag", tag);
        return "admin/admin_tag";
    }
}
