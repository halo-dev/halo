package run.halo.app.handler.file;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import run.halo.app.exception.FileOperationException;
import run.halo.app.model.enums.AttachmentType;
import run.halo.app.model.support.UploadResult;
import run.halo.app.utils.FilenameUtils;
import run.halo.app.utils.HttpClientUtils;

import java.io.IOException;
import java.util.Objects;

/**
 * Sm.ms file handler.
 *
 * @author johnniang
 * @date 3/29/19
 */
@Slf4j
@Component
public class SmmsFileHandler implements FileHandler {

    private final static String UPLOAD_API = "https://sm.ms/api/upload";

    private final static String DELETE_API = "https://sm.ms/api/delete/%s";

    private final static String SUCCESS_CODE = "success";

    private final static String DEFAULT_USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.86 Safari/537.36";

    private final RestTemplate httpsRestTemplate;

    public SmmsFileHandler(RestTemplate httpsRestTemplate) {
        this.httpsRestTemplate = httpsRestTemplate;
    }

    @Override
    public UploadResult upload(MultipartFile file) {
        Assert.notNull(file, "Multipart file must not be null");

        if (!FileHandler.isImageType(file.getContentType())) {
            log.error("Invalid extension: [{}]", file.getContentType());
            throw new FileOperationException("Invalid extension for file " + file.getOriginalFilename() + ". Only \"jpeg, jpg, png, gif, bmp\" files are supported");
        }

        HttpHeaders headers = new HttpHeaders();
        // Set content type
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        // Set user agent manually
        headers.set(HttpHeaders.USER_AGENT, DEFAULT_USER_AGENT);

        LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        try {
            body.add("smfile", new HttpClientUtils.MultipartFileResource(file.getBytes(), file.getOriginalFilename()));
        } catch (IOException e) {
            log.error("Failed to get file input stream", e);
            throw new FileOperationException("Failed to upload " + file.getOriginalFilename() + " file", e);
        }

        body.add("ssl", false);
        body.add("format", "json");

        HttpEntity<LinkedMultiValueMap<String, Object>> httpEntity = new HttpEntity<>(body, headers);

        // Upload file
        ResponseEntity<SmmsResponse> mapResponseEntity = httpsRestTemplate.postForEntity(UPLOAD_API, httpEntity, SmmsResponse.class);

        // Check status
        if (mapResponseEntity.getStatusCode().isError()) {
            log.error("Server response detail: [{}]", mapResponseEntity.toString());
            throw new FileOperationException("Smms server response error. status: " + mapResponseEntity.getStatusCodeValue());
        }

        // Get smms response
        SmmsResponse smmsResponse = mapResponseEntity.getBody();

        // Check error
        if (!isResponseSuccessfully(smmsResponse)) {
            log.error("Smms response detail: [{}]", smmsResponse);
            throw new FileOperationException(smmsResponse == null ? "Smms response is null" : smmsResponse.getMsg()).setErrorData(smmsResponse);
        }

        // Get response data
        SmmsResponseData data = smmsResponse.getData();

        // Build result
        UploadResult result = new UploadResult();
        result.setFilename(FilenameUtils.getBasename(file.getOriginalFilename()));
        result.setSuffix(FilenameUtils.getExtension(file.getOriginalFilename()));
        result.setMediaType(MediaType.valueOf(Objects.requireNonNull(file.getContentType())));

        result.setFilePath(data.getUrl());
        result.setThumbPath(data.getUrl());
        result.setKey(data.getHash());
        result.setWidth(data.getWidth());
        result.setHeight(data.getHeight());
        result.setSize(data.getSize().longValue());

        log.info("File: [{}] uploaded successfully", file.getOriginalFilename());

        return result;
    }

    @Override
    public void delete(String key) {
        Assert.hasText(key, "Deleting key must not be blank");

        // Build delete url
        String url = String.format(DELETE_API, key);

        // Set user agent manually
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, DEFAULT_USER_AGENT);

        // Delete the file
        ResponseEntity<String> responseEntity = httpsRestTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(null, headers), String.class);

        if (responseEntity.getStatusCode().isError()) {
            log.debug("Smms server response error: [{}]", responseEntity.toString());
            throw new FileOperationException("Smms server response error");
        }

        log.debug("Smms response detail: [{}]", responseEntity.getBody());

        // Deleted successfully or have been deleted already
        log.info("File was deleted successfully or had been deleted already");
    }

    @Override
    public boolean supportType(AttachmentType type) {
        return AttachmentType.SMMS.equals(type);
    }

    /**
     * Check if the response is response successfully or not.
     *
     * @param smmsResponse smms response must not be null
     * @return true if response successfully; false otherwise
     */
    private boolean isResponseSuccessfully(@Nullable SmmsResponse smmsResponse) {
        return smmsResponse != null && smmsResponse.getCode().equals(SUCCESS_CODE);
    }

    @Data
    @ToString
    @NoArgsConstructor
    static class SmmsResponse {

        private String code;

        private String msg;

        private SmmsResponseData data;

    }

    @Data
    @ToString(callSuper = true)
    @NoArgsConstructor
    static class SmmsResponseData {

        private String filename;

        private String storename;

        private Integer size;

        private Integer width;

        private Integer height;

        private String hash;

        private String delete;

        private String url;

        private String path;

    }
}
