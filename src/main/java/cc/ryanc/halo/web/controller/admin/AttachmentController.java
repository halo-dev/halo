package cc.ryanc.halo.web.controller.admin;

import cc.ryanc.halo.model.domain.Attachment;
import cc.ryanc.halo.model.dto.JsonResult;
import cc.ryanc.halo.model.dto.LogsRecord;
import cc.ryanc.halo.model.enums.PostTypeEnum;
import cc.ryanc.halo.model.enums.ResultCodeEnum;
import cc.ryanc.halo.service.AttachmentService;
import cc.ryanc.halo.service.LogsService;
import cc.ryanc.halo.utils.LocaleMessageUtil;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static cc.ryanc.halo.model.enums.AttachLocationEnum.*;

/**
 * <pre>
 *     后台附件控制器
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2017/12/19
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin/attachments")
public class AttachmentController {

    @Autowired
    private AttachmentService attachmentService;

    @Autowired
    private LogsService logsService;

    @Autowired
    private LocaleMessageUtil localeMessageUtil;

    /**
     * 复印件列表
     *
     * @param model model
     * @return 模板路径admin/admin_attachment
     */
    @GetMapping
    public String attachments(Model model,
                              @PageableDefault(size = 18, sort = "attachId", direction = Sort.Direction.DESC) Pageable pageable) {
        final Page<Attachment> attachments = attachmentService.listAll(pageable);
        model.addAttribute("attachments", attachments);
        return "admin/admin_attachment";
    }

    /**
     * 跳转选择附件页面
     *
     * @param model model
     * @return 模板路径admin/widget/_attachment-select
     */
    @GetMapping(value = "/select")
    public String selectAttachment(Model model,
                                   @PageableDefault(size = 18, sort = "attachId", direction = Sort.Direction.DESC) Pageable pageable,
                                   @RequestParam(value = "id", defaultValue = "none") String id,
                                   @RequestParam(value = "type", defaultValue = "normal") String type) {
        final Page<Attachment> attachments = attachmentService.listAll(pageable);
        model.addAttribute("attachments", attachments);
        model.addAttribute("id", id);
        if (StrUtil.equals(type, PostTypeEnum.POST_TYPE_POST.getDesc())) {
            return "admin/widget/_attachment-select-post";
        }
        return "admin/widget/_attachment-select";
    }


    /**
     * 上传附件窗口
     *
     * @return String
     */
    @GetMapping(value = "/uploadModal")
    public String uploadModal() {
        return "admin/widget/_attachment-upload";
    }

    /**
     * 上传附件
     *
     * @param file    file
     * @param request request
     * @return Map
     */
    @PostMapping(value = "/upload", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file,
                                      HttpServletRequest request) {
        final Map<String, Object> result = new HashMap<>(3);
        if (!file.isEmpty()) {
            try {
                final Map<String, String> resultMap = attachmentService.upload(file, request);
                if (resultMap == null || resultMap.isEmpty()) {
                    log.error("File upload failed");
                    result.put("success", ResultCodeEnum.FAIL.getCode());
                    result.put("message", localeMessageUtil.getMessage("code.admin.attachment.upload-failed"));
                    return result;
                }
                //保存在数据库
                Attachment attachment = new Attachment();
                attachment.setAttachName(resultMap.get("fileName"));
                attachment.setAttachPath(resultMap.get("filePath"));
                attachment.setAttachSmallPath(resultMap.get("smallPath"));
                attachment.setAttachType(file.getContentType());
                attachment.setAttachSuffix(resultMap.get("suffix"));
                attachment.setAttachSize(resultMap.get("size"));
                attachment.setAttachWh(resultMap.get("wh"));
                attachment.setAttachLocation(resultMap.get("location"));
                attachmentService.create(attachment);
                log.info("Upload file {} to {} successfully", resultMap.get("fileName"), resultMap.get("filePath"));
                result.put("success", ResultCodeEnum.SUCCESS.getCode());
                result.put("message", localeMessageUtil.getMessage("code.admin.attachment.upload-success"));
                result.put("url", attachment.getAttachPath());
                result.put("filename", resultMap.get("filePath"));
                logsService.save(LogsRecord.UPLOAD_FILE, resultMap.get("fileName"), request);
            } catch (Exception e) {
                log.error("Upload file failed:{}", e.getMessage());
                result.put("success", ResultCodeEnum.FAIL.getCode());
                result.put("message", localeMessageUtil.getMessage("code.admin.attachment.upload-failed"));
            }
        } else {
            log.error("File cannot be empty!");
        }
        return result;
    }

    /**
     * 处理获取附件详情的请求
     *
     * @param model    model
     * @param attachId 附件编号
     * @return 模板路径admin/widget/_attachment-detail
     */
    @GetMapping(value = "/attachment")
    public String attachmentDetail(Model model, @RequestParam("attachId") Long attachId) {
        final Optional<Attachment> attachment = attachmentService.fetchById(attachId);
        model.addAttribute("attachment", attachment.orElse(new Attachment()));
        return "admin/widget/_attachment-detail";
    }

    /**
     * 移除附件的请求
     *
     * @param attachId 附件编号
     * @param request  request
     * @return JsonResult
     */
    @GetMapping(value = "/remove")
    @ResponseBody
    public JsonResult removeAttachment(@RequestParam("attachId") Long attachId,
                                       HttpServletRequest request) {
        final Attachment attachment = attachmentService.fetchById(attachId).orElse(new Attachment());
        final String attachLocation = attachment.getAttachLocation();
        final String attachName = attachment.getAttachName();
        final String attachPath = attachment.getAttachPath();
        boolean flag = true;
        try {
            if (attachLocation != null) {
                if (attachLocation.equals(SERVER.getDesc())) {
                    StrBuilder userPath = new StrBuilder(System.getProperties().getProperty("user.home"));
                    userPath.append("/halo");
                    //图片物理地址
                    StrBuilder delPath = new StrBuilder(userPath);
                    delPath.append(attachPath);
                    //缩略图物理地址
                    StrBuilder delSmallPath = new StrBuilder(userPath);
                    delSmallPath.append(attachment.getAttachSmallPath());
                    File delFile = new File(delPath.toString());
                    File delSmallFile = new File(delSmallPath.toString());
                    if (delFile.exists() && delFile.isFile()) {
                        flag = delFile.delete() && delSmallFile.delete();
                    }
                } else if (attachLocation.equals(QINIU.getDesc())) {
                    String key = attachPath.substring(attachPath.lastIndexOf("/") + 1);
                    flag = attachmentService.deleteQiNiuAttachment(key);
                } else if (attachLocation.equals(UPYUN.getDesc())) {
                    String fileName = attachPath.substring(attachPath.lastIndexOf("/") + 1);
                    flag = attachmentService.deleteUpYunAttachment(fileName);
                }
            }
            if (flag) {
                attachmentService.removeById(attachId);
                log.info("Delete file {} successfully!", attachName);
                logsService.save(LogsRecord.REMOVE_FILE, attachName, request);
            } else {
                log.error("Deleting attachment {} failed!", attachName);
                return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.delete-failed"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Deleting attachment {} failed: {}", attachName, e.getMessage());
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.delete-failed"));
        }
        return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), localeMessageUtil.getMessage("code.admin.common.delete-success"));
    }
}
