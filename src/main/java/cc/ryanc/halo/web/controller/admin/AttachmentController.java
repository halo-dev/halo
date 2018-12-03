package cc.ryanc.halo.web.controller.admin;

import cc.ryanc.halo.model.domain.Attachment;
import cc.ryanc.halo.model.domain.Logs;
import cc.ryanc.halo.model.dto.JsonResult;
import cc.ryanc.halo.model.dto.LogsRecord;
import cc.ryanc.halo.model.enums.PostTypeEnum;
import cc.ryanc.halo.model.enums.ResultCodeEnum;
import cc.ryanc.halo.service.AttachmentService;
import cc.ryanc.halo.service.LogsService;
import cc.ryanc.halo.utils.HaloUtils;
import cc.ryanc.halo.utils.LocaleMessageUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.ImageUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
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
import java.util.*;

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
     * 获取upload的所有图片资源并渲染页面
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
    public String uploadModal(){
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
        String dateString = DateUtil.format(DateUtil.date(), "yyyyMMddHHmmss");
        if (!file.isEmpty()) {
            try {
                //用户目录
                StrBuilder uploadPath = new StrBuilder(System.getProperties().getProperty("user.home"));
                uploadPath.append("/halo/");
                uploadPath.append("upload/");

                //获取当前年月以创建目录，如果没有该目录则创建
                uploadPath.append(DateUtil.thisYear()).append("/").append(DateUtil.thisMonth()).append("/");
                File mediaPath = new File(uploadPath.toString());
                if (!mediaPath.exists()) {
                    if (!mediaPath.mkdirs()) {
                        result.put("success",0);
                        return result;
                    }
                }

                //不带后缀
                StrBuilder nameWithOutSuffix = new StrBuilder(file.getOriginalFilename().substring(0, file.getOriginalFilename().lastIndexOf('.')).replaceAll(" ", "_").replaceAll(",", ""));
                nameWithOutSuffix.append(dateString);
                nameWithOutSuffix.append(new Random().nextInt(1000));

                //文件后缀
                String fileSuffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.') + 1);

                //带后缀
                StrBuilder fileName = new StrBuilder(nameWithOutSuffix);
                fileName.append(".");
                fileName.append(fileSuffix);
                file.transferTo(new File(mediaPath.getAbsoluteFile(), fileName.toString()));

                //文件原路径
                StrBuilder fullPath = new StrBuilder(mediaPath.getAbsolutePath());
                fullPath.append("/");
                fullPath.append(fileName);

                //压缩文件路径
                StrBuilder fullSmallPath = new StrBuilder(mediaPath.getAbsolutePath());
                fullSmallPath.append("/");
                fullSmallPath.append(nameWithOutSuffix);
                fullSmallPath.append("_small.");
                fullSmallPath.append(fileSuffix);

                //压缩文件
                Thumbnails.of(fullPath.toString()).size(256, 256).keepAspectRatio(false).toFile(fullSmallPath.toString());

                //映射路径
                StrBuilder filePath = new StrBuilder("/upload/");
                filePath.append(DateUtil.thisYear());
                filePath.append("/");
                filePath.append(DateUtil.thisMonth());
                filePath.append("/");
                filePath.append(fileName);

                //缩略图映射路径
                StrBuilder fileSmallPath = new StrBuilder("/upload/");
                fileSmallPath.append(DateUtil.thisYear());
                fileSmallPath.append("/");
                fileSmallPath.append(DateUtil.thisMonth());
                fileSmallPath.append("/");
                fileSmallPath.append(nameWithOutSuffix);
                fileSmallPath.append("_small.");
                fileSmallPath.append(fileSuffix);

                //保存在数据库
                Attachment attachment = new Attachment();
                attachment.setAttachName(fileName.toString());
                attachment.setAttachPath(filePath.toString());
                attachment.setAttachSmallPath(fileSmallPath.toString());
                attachment.setAttachType(file.getContentType());
                attachment.setAttachSuffix(new StrBuilder(".").append(fileSuffix).toString());
                attachment.setAttachCreated(DateUtil.date());
                attachment.setAttachSize(HaloUtils.parseSize(new File(fullPath.toString()).length()));
                attachment.setAttachWh(HaloUtils.getImageWh(new File(fullPath.toString())));
                attachmentService.saveByAttachment(attachment);
                log.info("Upload file {} to {} successfully", fileName, mediaPath.getAbsolutePath());
                logsService.saveByLogs(
                        new Logs(LogsRecord.UPLOAD_FILE, fileName.toString(), ServletUtil.getClientIP(request), DateUtil.date())
                );

                result.put("success", 1);
                result.put("message", localeMessageUtil.getMessage("code.admin.attachment.upload-success"));
                result.put("url", attachment.getAttachPath());
                result.put("filename", filePath);
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
     * 添加外部url附件
     *
     * @param attachment attachment
     * @return JsonResult
     */
    @PostMapping(value = "/addFromUrl")
    @ResponseBody
    public JsonResult addFromUrl(@ModelAttribute(value = "attachment") Attachment attachment) {
        attachment.setAttachCreated(new Date());
        attachment.setAttachOrigin(1);
        attachment = attachmentService.saveByAttachment(attachment);
        if (null != attachment) {
            return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), localeMessageUtil.getMessage("code.admin.common.save-success"));
        } else {
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.save-failed"));
        }
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
        Optional<Attachment> attachment = attachmentService.findByAttachId(attachId);
        String delFileName = attachment.get().getAttachName();
        String delSmallFileName = delFileName.substring(0, delFileName.lastIndexOf('.')) + "_small" + attachment.get().getAttachSuffix();
        try {
            //删除数据库中的内容
            attachmentService.removeByAttachId(attachId);
            //删除文件
            String userPath = System.getProperties().getProperty("user.home") + "/halo";
            File mediaPath = new File(userPath, attachment.get().getAttachPath().substring(0, attachment.get().getAttachPath().lastIndexOf('/')));
            File delFile = new File(new StringBuffer(mediaPath.getAbsolutePath()).append("/").append(delFileName).toString());
            File delSmallFile = new File(new StringBuffer(mediaPath.getAbsolutePath()).append("/").append(delSmallFileName).toString());
            if (delFile.exists() && delFile.isFile()) {
                if (delFile.delete() && delSmallFile.delete()) {
                    log.info("Delete file {} successfully!", delFileName);
                    logsService.saveByLogs(
                            new Logs(LogsRecord.REMOVE_FILE, delFileName, ServletUtil.getClientIP(request), DateUtil.date())
                    );
                } else {
                    log.error("Deleting attachment {} failed!", delFileName);
                    return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.delete-failed"));
                }
            }
        } catch (Exception e) {
            log.error("Deleting attachment {} failed: {}", delFileName, e.getMessage());
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.delete-failed"));
        }
        return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), localeMessageUtil.getMessage("code.admin.common.delete-success"));
    }
}
