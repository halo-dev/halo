package cc.ryanc.halo.web.controller.admin;

import cc.ryanc.halo.model.domain.Attachment;
import cc.ryanc.halo.model.domain.Logs;
import cc.ryanc.halo.model.dto.JsonResult;
import cc.ryanc.halo.model.dto.LogsRecord;
import cc.ryanc.halo.model.enums.PostTypeEnum;
import cc.ryanc.halo.model.enums.ResultCodeEnum;
import cc.ryanc.halo.service.AttachmentService;
import cc.ryanc.halo.service.LogsService;
import cc.ryanc.halo.utils.LocaleMessageUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
                              @RequestParam(value = "page", defaultValue = "0") Integer page,
                              @RequestParam(value = "size", defaultValue = "18") Integer size) {
        Sort sort = new Sort(Sort.Direction.DESC, "attachId");
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Attachment> attachments = attachmentService.findAllAttachments(pageable);
        model.addAttribute("attachments", attachments);
        return "admin/admin_attachment";
    }

    /**
     * 跳转选择附件页面
     *
     * @param model model
     * @param page  page 当前页码
     * @return 模板路径admin/widget/_attachment-select
     */
    @GetMapping(value = "/select")
    public String selectAttachment(Model model,
                                   @RequestParam(value = "page", defaultValue = "0") Integer page,
                                   @RequestParam(value = "id", defaultValue = "none") String id,
                                   @RequestParam(value = "type", defaultValue = "normal") String type) {
        Sort sort = new Sort(Sort.Direction.DESC, "attachId");
        Pageable pageable = PageRequest.of(page, 18, sort);
        Page<Attachment> attachments = attachmentService.findAllAttachments(pageable);
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
        Map<String, Object> result = new HashMap<>(3);
        System.out.println("源地址" + file.getOriginalFilename() + "类型" + file.getContentType() + "文件名" + file.getName() + "文件大小" + file.getSize());
        if (!file.isEmpty()) {
            try {
                Map<String, String> resultMap = attachmentService.upload(file, request);
                if (resultMap == null || resultMap.isEmpty()) {
                    log.error("文件上传失败");
                    result.put("success", 0);
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
                attachment.setAttachCreated(DateUtil.date());
                attachment.setAttachSize(resultMap.get("size"));
                attachment.setAttachWh(resultMap.get("wh"));
                attachment.setAttachLocation(resultMap.get("location"));
                attachmentService.saveByAttachment(attachment);
                log.info("Upload file {} to {} successfully", resultMap.get("fileName"), resultMap.get("filePath"));
                logsService.saveByLogs(
                        new Logs(LogsRecord.UPLOAD_FILE, resultMap.get("fileName"), ServletUtil.getClientIP(request), DateUtil.date())
                );
                result.put("success", 1);
                result.put("message", localeMessageUtil.getMessage("code.admin.attachment.upload-success"));
                result.put("url", attachment.getAttachPath());
                result.put("filename", resultMap.get("filePath"));
            } catch (Exception e) {
                log.error("Upload file failed:{}", e.getMessage());
                result.put("success", 0);
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
        Optional<Attachment> attachment = attachmentService.findByAttachId(attachId);
        model.addAttribute("attachment", attachment.get());
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
        Optional<Attachment> attachment = attachmentService.findByAttachId(attachId);
        String attachLocation = attachment.get().getAttachLocation();
        String delFileName = attachment.get().getAttachName();
        boolean flag = true;
        try {
            //删除数据库中的内容
            attachmentService.removeByAttachId(attachId);
            if (attachLocation != null) {
                if (attachLocation.equals(SERVER.getDesc())) {
                    String delSmallFileName = delFileName.substring(0, delFileName.lastIndexOf('.')) + "_small" + attachment.get().getAttachSuffix();
                    //删除文件
                    String userPath = System.getProperties().getProperty("user.home") + "/halo";
                    File mediaPath = new File(userPath, attachment.get().getAttachPath().substring(0, attachment.get().getAttachPath().lastIndexOf('/')));
                    File delFile = new File(new StringBuffer(mediaPath.getAbsolutePath()).append("/").append(delFileName).toString());
                    File delSmallFile = new File(new StringBuffer(mediaPath.getAbsolutePath()).append("/").append(delSmallFileName).toString());
                    if (delFile.exists() && delFile.isFile()) {
                        flag = delFile.delete() && delSmallFile.delete();
                    }
                } else if (attachLocation.equals(QINIU.getDesc())) {
                    //七牛删除
                    String attachPath = attachment.get().getAttachPath();
                    String key = attachPath.substring(attachPath.lastIndexOf("/") + 1);
                    flag = attachmentService.deleteQiNiuAttachment(key);
                } else if (attachLocation.equals(UPYUN.getDesc())) {
                    //又拍删除
                    String attachPath = attachment.get().getAttachPath();
                    String fileName = attachPath.substring(attachPath.lastIndexOf("/") + 1);
                    flag = attachmentService.deleteUpYunAttachment(fileName);
                } else {
                    //..
                }
            }
            if (flag) {
                log.info("Delete file {} successfully!", delFileName);
                logsService.saveByLogs(
                        new Logs(LogsRecord.REMOVE_FILE, delFileName, ServletUtil.getClientIP(request), DateUtil.date())
                );
            } else {
                log.error("Deleting attachment {} failed!", delFileName);
                return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.delete-failed"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Deleting attachment {} failed: {}", delFileName, e.getMessage());
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.delete-failed"));
        }
        return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), localeMessageUtil.getMessage("code.admin.common.delete-success"));
    }
}
