package cc.ryanc.halo.web.controller.admin;

import cc.ryanc.halo.model.domain.Attachment;
import cc.ryanc.halo.model.dto.HaloConst;
import cc.ryanc.halo.model.dto.RespStatus;
import cc.ryanc.halo.service.AttachmentService;
import cc.ryanc.halo.util.HaloUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.websocket.server.PathParam;
import java.io.File;

/**
 * @author : RYAN0UP
 * @date : 2017/12/19
 * @version : 1.0
 * description: 附件
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin/attachments")
public class AttachmentController {

    @Autowired
    private AttachmentService attachmentService;
    /**
     * 刷新HaloConst
     */
    private void updateConst(){
        HaloConst.ATTACHMENTS.clear();
        HaloConst.ATTACHMENTS = attachmentService.findAllAttachments();
    }

    /**
     * 获取upload的所有图片资源并渲染页面
     * @param model model
     * @return String
     */
    @GetMapping
    public String attachments(Model model,
                              @RequestParam(value = "page",defaultValue = "0") Integer page,
                              @RequestParam(value = "size",defaultValue = "18") Integer size){
        try {
            Sort sort = new Sort(Sort.Direction.DESC,"attachId");
            Pageable pageable = new PageRequest(page,size,sort);
            Page<Attachment> attachments = attachmentService.findAllAttachments(pageable);
            model.addAttribute("attachments",attachments);
            model.addAttribute("options", HaloConst.OPTIONS);
        }catch (Exception e){
            e.printStackTrace();
        }
        return "admin/attachment";
    }

    /**
     * 跳转选择附件页面
     * @param model model
     * @param page page
     * @return string
     */
    @GetMapping(value = "/select")
    public String selectAttachment(Model model,
                                   @RequestParam(value = "page",defaultValue = "0") Integer page){
        try {
            Sort sort = new Sort(Sort.Direction.DESC,"attachId");
            Pageable pageable = new PageRequest(page,18,sort);
            Page<Attachment> attachments = attachmentService.findAllAttachments(pageable);
            model.addAttribute("attachments",attachments);
        }catch (Exception e){
            e.printStackTrace();
        }
        return "admin/widget/_attachment-select";
    }


    /**
     * 上传文件
     * @param file file
     */
    @PostMapping(value = "/upload",produces = { "application/json;charset=UTF-8" })
    @ResponseBody
    public boolean uploadAttachment(@RequestParam("file") MultipartFile file){
        if(!file.isEmpty()){
            try{
                File basePath = new File(ResourceUtils.getURL("classpath:").getPath());
                File mediaPath = new File(basePath.getAbsolutePath(),"upload/"+ HaloUtil.YEAR+"/"+ HaloUtil.MONTH+"/");
                if(!mediaPath.exists()){
                    mediaPath.mkdirs();
                }
                file.transferTo(new File(mediaPath.getAbsoluteFile(),file.getOriginalFilename()));
                String fileName = file.getOriginalFilename();
                String nameWithOutSuffix = fileName.substring(0,fileName.lastIndexOf('.'));
                String fileSuffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.')+1);

                //保存在数据库
                Attachment attachment = new Attachment();
                attachment.setAttachName(fileName);
                attachment.setAttachPath("/upload/"+HaloUtil.YEAR+"/"+HaloUtil.MONTH+"/"+fileName);
                attachment.setAttachSmallPath("/upload/"+HaloUtil.YEAR+"/"+HaloUtil.MONTH+"/"+nameWithOutSuffix+"_small."+fileSuffix);
                attachment.setAttachType(file.getContentType());
                attachment.setAttachSuffix("."+fileSuffix);
                attachment.setAttachCreated(HaloUtil.getDate());
                attachmentService.saveByAttachment(attachment);

                //剪裁图片
                HaloUtil.cutCenterImage(mediaPath.getAbsolutePath()+"/"+fileName,mediaPath.getAbsolutePath()+"/"+nameWithOutSuffix+"_small."+fileSuffix,500,500,fileSuffix);

                updateConst();
                log.info("上传文件["+file.getOriginalFilename()+"]到["+mediaPath.getAbsolutePath()+"]成功");
                return true;
            }catch (Exception e){
                log.error("未知错误："+e.getMessage());
            }
        }else {
            log.error("文件不能为空");
        }
        return false;
    }

    /**
     * 处理获取附件详情的请求
     * @param attachId attachId
     * @return string
     */
    @GetMapping(value = "/attachment")
    public String attachmentDetail(Model model,@PathParam("attachId") Long attachId){
        Attachment attachment = attachmentService.findByAttachId(attachId);
        model.addAttribute("attachment",attachment);
        return "admin/widget/_attachment-detail";
    }

    /**
     * 移除附件的请求
     * @param attachId attachId
     * @return string
     */
    @GetMapping(value = "/remove")
    @ResponseBody
    public String removeAttachment(@PathParam("attachId") Long attachId){
        Attachment attachment = attachmentService.findByAttachId(attachId);
        String delFileName = attachment.getAttachName();
        String delSmallFileName = delFileName.substring(0,delFileName.lastIndexOf('.'))+"_small"+attachment.getAttachSuffix();
        try {
            //删除数据库中的内容
            attachmentService.removeByAttachId(attachId);
            //刷新HaloConst变量
            updateConst();
            //删除文件
            File basePath = new File(ResourceUtils.getURL("classpath:").getPath());
            File mediaPath = new File(basePath.getAbsolutePath(),attachment.getAttachPath().substring(0,attachment.getAttachPath().lastIndexOf('/')));
            File delFile = new File(mediaPath.getAbsolutePath()+"/"+delFileName);
            File delSmallFile = new File(mediaPath.getAbsolutePath()+"/"+delSmallFileName);
            if(delFile.exists() && delFile.isFile()){
                if(delFile.delete()&&delSmallFile.delete()){
                    updateConst();
                    log.info("删除文件["+delFileName+"]成功！");
                }else{
                    log.error("删除附件["+delFileName+"]失败！");
                    return RespStatus.ERROR;
                }
            }
        }catch (Exception e){
            log.error("删除附件["+delFileName+"]失败！"+e.getMessage());
            return RespStatus.ERROR;
        }
        return RespStatus.SUCCESS;
    }
}
