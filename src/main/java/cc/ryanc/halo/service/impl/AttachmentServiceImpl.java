package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.domain.Attachment;
import cc.ryanc.halo.model.dto.HaloConst;
import cc.ryanc.halo.model.dto.QiNiuPutSet;
import cc.ryanc.halo.model.enums.AttachLocationEnum;
import cc.ryanc.halo.model.enums.BlogPropertiesEnum;
import cc.ryanc.halo.repository.AttachmentRepository;
import cc.ryanc.halo.service.AttachmentService;
import cc.ryanc.halo.utils.HaloUtils;
import cc.ryanc.halo.utils.Md5Util;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.StrUtil;
import com.UpYun;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.persistent.FileRecorder;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.upyun.UpException;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

/**
 * <pre>
 *     附件业务逻辑实现类
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2018/1/10
 */
@Service
public class AttachmentServiceImpl implements AttachmentService {

    private static final String ATTACHMENTS_CACHE_NAME = "attachments";

    @Autowired
    private AttachmentRepository attachmentRepository;

    /**
     * 新增附件信息
     *
     * @param attachment attachment
     * @return Attachment
     */
    @Override
    @CacheEvict(value = ATTACHMENTS_CACHE_NAME, allEntries = true, beforeInvocation = true)
    public Attachment save(Attachment attachment) {
        return attachmentRepository.save(attachment);
    }

    /**
     * 获取所有附件信息
     *
     * @return List
     */
    @Override
    @Cacheable(value = ATTACHMENTS_CACHE_NAME, key = "'attachment'")
    public List<Attachment> findAll() {
        return attachmentRepository.findAll();
    }

    /**
     * 获取所有附件信息 分页
     *
     * @param pageable pageable
     * @return Page
     */
    @Override
    public Page<Attachment> findAll(Pageable pageable) {
        return attachmentRepository.findAll(pageable);
    }

    /**
     * 根据附件id查询附件
     *
     * @param attachId attachId
     * @return Optional
     */
    @Override
    public Optional<Attachment> findByAttachId(Long attachId) {
        return attachmentRepository.findById(attachId);
    }

    /**
     * 根据编号移除附件
     *
     * @param attachId attachId
     * @return Attachment
     */
    @Override
    @CacheEvict(value = ATTACHMENTS_CACHE_NAME, allEntries = true, beforeInvocation = true)
    public Attachment remove(Long attachId) {
        Optional<Attachment> attachment = this.findByAttachId(attachId);
        attachmentRepository.delete(attachment.orElse(null));
        return attachment.orElse(null);
    }

    /**
     * 上传转发
     *
     * @param file    file
     * @param request request
     * @return Map
     */
    @Override
    public Map<String, String> upload(MultipartFile file, HttpServletRequest request) {
        Map<String, String> resultMap;
        String attachLoc = HaloConst.OPTIONS.get(BlogPropertiesEnum.ATTACH_LOC.getProp());
        if (StrUtil.isEmpty(attachLoc)) {
            attachLoc = "server";
        }
        switch (attachLoc) {
            case "server":
                resultMap = this.attachUpload(file, request);
                break;
            case "qiniu":
                resultMap = this.attachQiNiuUpload(file, request);
                break;
            case "upyun":
                resultMap = this.attachUpYunUpload(file, request);
                break;
            default:
                resultMap = this.attachUpload(file, request);
                break;
        }
        return resultMap;
    }

    /**
     * 原生服务器上传
     *
     * @param file    file
     * @param request request
     * @return Map
     */
    @Override
    public Map<String, String> attachUpload(MultipartFile file, HttpServletRequest request) {
        final Map<String, String> resultMap = new HashMap<>(6);
        final String dateString = DateUtil.format(DateUtil.date(), "yyyyMMddHHmmss");
        try {
            //用户目录
            final StrBuilder uploadPath = new StrBuilder(System.getProperties().getProperty("user.home"));
            uploadPath.append("/halo/");
            uploadPath.append("upload/");

            //获取当前年月以创建目录，如果没有该目录则创建
            uploadPath.append(DateUtil.thisYear()).append("/").append(DateUtil.thisMonth()).append("/");
            final File mediaPath = new File(uploadPath.toString());
            if (!mediaPath.exists()) {
                if (!mediaPath.mkdirs()) {
                    resultMap.put("success", "0");
                    return resultMap;
                }
            }

            //不带后缀
            final StrBuilder nameWithOutSuffix = new StrBuilder(file.getOriginalFilename().substring(0, file.getOriginalFilename().lastIndexOf('.')).replaceAll(" ", "_").replaceAll(",", ""));
            nameWithOutSuffix.append(dateString);
            nameWithOutSuffix.append(new Random().nextInt(1000));

            //文件后缀
            final String fileSuffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.') + 1);

            //带后缀
            final StrBuilder fileName = new StrBuilder(nameWithOutSuffix);
            fileName.append(".");
            fileName.append(fileSuffix);

            file.transferTo(new File(mediaPath.getAbsoluteFile(), fileName.toString()));

            //文件原路径
            final StrBuilder fullPath = new StrBuilder(mediaPath.getAbsolutePath());
            fullPath.append("/");
            fullPath.append(fileName);

            //压缩文件路径
            final StrBuilder fullSmallPath = new StrBuilder(mediaPath.getAbsolutePath());
            fullSmallPath.append("/");
            fullSmallPath.append(nameWithOutSuffix);
            fullSmallPath.append("_small.");
            fullSmallPath.append(fileSuffix);

            //压缩图片
            Thumbnails.of(fullPath.toString()).size(256, 256).keepAspectRatio(false).toFile(fullSmallPath.toString());

            //映射路径
            final StrBuilder filePath = new StrBuilder("/upload/");
            filePath.append(DateUtil.thisYear());
            filePath.append("/");
            filePath.append(DateUtil.thisMonth());
            filePath.append("/");
            filePath.append(fileName);

            //缩略图映射路径
            final StrBuilder fileSmallPath = new StrBuilder("/upload/");
            fileSmallPath.append(DateUtil.thisYear());
            fileSmallPath.append("/");
            fileSmallPath.append(DateUtil.thisMonth());
            fileSmallPath.append("/");
            fileSmallPath.append(nameWithOutSuffix);
            fileSmallPath.append("_small.");
            fileSmallPath.append(fileSuffix);

            final String size = HaloUtils.parseSize(new File(fullPath.toString()).length());
            final String wh = HaloUtils.getImageWh(new File(fullPath.toString()));

            resultMap.put("fileName", fileName.toString());
            resultMap.put("filePath", filePath.toString());
            resultMap.put("smallPath", fileSmallPath.toString());
            resultMap.put("suffix", fileSuffix);
            resultMap.put("size", size);
            resultMap.put("wh", wh);
            resultMap.put("location", AttachLocationEnum.SERVER.getDesc());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultMap;
    }

    /**
     * 七牛云上传
     *
     * @param file    file
     * @param request request
     * @return Map
     */
    @Override
    public Map<String, String> attachQiNiuUpload(MultipartFile file, HttpServletRequest request) {
        final Map<String, String> resultMap = new HashMap<>(6);
        try {
            final Configuration cfg = new Configuration(Zone.zone0());
            final String key = Md5Util.getMD5Checksum(file);
            final String accessKey = HaloConst.OPTIONS.get("qiniu_access_key");
            final String secretKey = HaloConst.OPTIONS.get("qiniu_secret_key");
            final String domain = HaloConst.OPTIONS.get("qiniu_domain");
            final String bucket = HaloConst.OPTIONS.get("qiniu_bucket");
            final String smallUrl = HaloConst.OPTIONS.get("qiniu_small_url");
            if (StrUtil.isEmpty(accessKey) || StrUtil.isEmpty(secretKey) || StrUtil.isEmpty(domain) || StrUtil.isEmpty(bucket)) {
                return resultMap;
            }
            final Auth auth = Auth.create(accessKey, secretKey);
            final StringMap putPolicy = new StringMap();
            putPolicy.put("returnBody", "{\"size\":$(fsize),\"w\":$(imageInfo.width),\"h\":$(imageInfo.height)}");
            final String upToken = auth.uploadToken(bucket, null, 3600, putPolicy);
            final String localTempDir = Paths.get(System.getenv("java.io.tmpdir"), bucket).toString();
            QiNiuPutSet putSet = new QiNiuPutSet();
            try {
                final FileRecorder fileRecorder = new FileRecorder(localTempDir);
                final UploadManager uploadManager = new UploadManager(cfg, fileRecorder);
                final Response response = uploadManager.put(file.getInputStream(), key, upToken, null, null);
                //解析上传成功的结果
                putSet = new Gson().fromJson(response.bodyString(), QiNiuPutSet.class);
            } catch (QiniuException e) {
                final Response r = e.response;
                System.err.println(r.toString());
                try {
                    System.err.println(r.bodyString());
                } catch (QiniuException ex2) {
                    //ignore
                }
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            final String filePath = domain.trim() + "/" + key;
            resultMap.put("fileName", file.getOriginalFilename());
            resultMap.put("filePath", filePath.trim());
            resultMap.put("smallPath", smallUrl == null ? filePath.trim() : (filePath + smallUrl).trim());
            resultMap.put("suffix", file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.')));
            resultMap.put("size", HaloUtils.parseSize(file.getSize()));
            resultMap.put("wh", putSet.getW() + "x" + putSet.getH());
            resultMap.put("location", AttachLocationEnum.QINIU.getDesc());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultMap;
    }

    /**
     * 又拍云上传
     *
     * @param file    file
     * @param request request
     * @return Map
     */
    @Override
    public Map<String, String> attachUpYunUpload(MultipartFile file, HttpServletRequest request) {
        final Map<String, String> resultMap = new HashMap<>(6);
        try {
            final String key = Md5Util.getMD5Checksum(file);
            final String ossSrc = HaloConst.OPTIONS.get("upyun_oss_src");
            final String ossPwd = HaloConst.OPTIONS.get("upyun_oss_pwd");
            final String bucket = HaloConst.OPTIONS.get("upyun_oss_bucket");
            final String domain = HaloConst.OPTIONS.get("upyun_oss_domain");
            final String operator = HaloConst.OPTIONS.get("upyun_oss_operator");
            final String smallUrl = HaloConst.OPTIONS.get("upyun_oss_small");
            if (StrUtil.isEmpty(ossSrc) || StrUtil.isEmpty(ossPwd) || StrUtil.isEmpty(domain) || StrUtil.isEmpty(bucket) || StrUtil.isEmpty(operator)) {
                return resultMap;
            }
            final String fileName = file.getOriginalFilename();
            final String fileSuffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.'));
            final UpYun upYun = new UpYun(bucket, operator, ossPwd);
            upYun.setTimeout(60);
            upYun.setApiDomain(UpYun.ED_AUTO);
            upYun.setDebug(true);
            upYun.writeFile(ossSrc + key + fileSuffix, file.getBytes(), true, null);
            final String filePath = domain.trim() + ossSrc + key + fileSuffix;
            String smallPath = filePath;
            if (smallUrl != null) {
                smallPath += smallUrl;
            }
            final BufferedImage image = ImageIO.read(file.getInputStream());
            if (image != null) {
                resultMap.put("wh", image.getWidth() + "x" + image.getHeight());
            }
            resultMap.put("fileName", fileName);
            resultMap.put("filePath", filePath.trim());
            resultMap.put("smallPath", smallPath.trim());
            resultMap.put("suffix", fileSuffix);
            resultMap.put("size", HaloUtils.parseSize(file.getSize()));
            resultMap.put("location", AttachLocationEnum.UPYUN.getDesc());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultMap;
    }

    /**
     * 七牛云删除附件
     *
     * @param key key
     * @return boolean
     */
    @Override
    public boolean deleteQiNiuAttachment(String key) {
        boolean flag = true;
        final Configuration cfg = new Configuration(Zone.zone0());
        final String accessKey = HaloConst.OPTIONS.get("qiniu_access_key");
        final String secretKey = HaloConst.OPTIONS.get("qiniu_secret_key");
        final String bucket = HaloConst.OPTIONS.get("qiniu_bucket");
        if (StrUtil.isEmpty(accessKey) || StrUtil.isEmpty(secretKey) || StrUtil.isEmpty(bucket)) {
            return false;
        }
        final Auth auth = Auth.create(accessKey, secretKey);
        final BucketManager bucketManager = new BucketManager(auth, cfg);
        try {
            bucketManager.delete(bucket, key);
        } catch (QiniuException ex) {
            System.err.println(ex.code());
            System.err.println(ex.response.toString());
            flag = false;
        }
        return flag;
    }

    /**
     * 又拍云删除附件
     *
     * @param fileName fileName
     * @return boolean
     */
    @Override
    public boolean deleteUpYunAttachment(String fileName) {
        boolean flag = true;
        final String ossSrc = HaloConst.OPTIONS.get("upyun_oss_src");
        final String ossPwd = HaloConst.OPTIONS.get("upyun_oss_pwd");
        final String bucket = HaloConst.OPTIONS.get("upyun_oss_bucket");
        final String operator = HaloConst.OPTIONS.get("upyun_oss_operator");
        if (StrUtil.isEmpty(ossSrc) || StrUtil.isEmpty(ossPwd) || StrUtil.isEmpty(bucket) || StrUtil.isEmpty(operator)) {
            return false;
        }
        final UpYun upYun = new UpYun(bucket, operator, ossPwd);
        upYun.setApiDomain(UpYun.ED_AUTO);
        try {
            flag = upYun.deleteFile(ossSrc + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UpException e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 获取附件总数
     *
     * @return Long
     */
    @Override
    public Long getCount() {
        return attachmentRepository.count();
    }
}
