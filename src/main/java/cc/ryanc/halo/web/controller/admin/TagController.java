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
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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
     *
     * @return JsonResult
     */
    @PostMapping(value = "/save")
    @ResponseBody
    public JsonResult saveTag(@Valid Tag tag, BindingResult result) {
        if (result.hasErrors()) {
            for (ObjectError error : result.getAllErrors()) {
                return new JsonResult(ResultCodeEnum.FAIL.getCode(), error.getDefaultMessage());
            }
        }
        final Tag tempTag = tagService.findByTagUrl(tag.getTagUrl());
        if (null != tag.getTagId()) {
            if (null != tempTag && !tag.getTagId().equals(tempTag.getTagId())) {
                return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.url-is-exists"));
            }
        } else {
            if (null != tempTag) {
                return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.url-is-exists"));
            }
        }
        tag = tagService.save(tag);
        if (null == tag) {
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.save-failed"));
        }
        return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), localeMessageUtil.getMessage("code.admin.common.save-success"));
    }

    /**
     * 处理删除标签的请求
     *
     * @param tagId 标签编号
     *
     * @return 重定向到/admin/tag
     */
    @GetMapping(value = "/remove")
    public String removeTag(@RequestParam("tagId") Long tagId) {
        try {
            tagService.remove(tagId);
        } catch (Exception e) {
            log.error("Failed to delete tag: {}", e.getMessage());
        }
        return "redirect:/admin/tag";
    }

    /**
     * 跳转到修改标签页面
     *
     * @param model model
     * @param tagId 标签编号
     *
     * @return 模板路径admin/admin_tag
     */
    @GetMapping(value = "/edit")
    public String toEditTag(Model model, @RequestParam("tagId") Long tagId) {
        final Tag tag = tagService.findByTagId(tagId).orElse(new Tag());
        model.addAttribute("updateTag", tag);
        return "admin/admin_tag";
    }
}
