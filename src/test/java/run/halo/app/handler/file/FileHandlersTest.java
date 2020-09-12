package run.halo.app.handler.file;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import run.halo.app.model.dto.AttachmentDTO;
import run.halo.app.service.AttachmentService;


@Slf4j
@SpringBootTest
@ActiveProfiles("dev")
class FileHandlersTest {
    @Autowired
    private AttachmentService attachmentService;

    @Test
    public void whenFileUploaded_thenVerifyStatus() {

        MockMultipartFile file
                = new MockMultipartFile(
                "file",
                "hello.tar.gz",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );

        MockMultipartFile file2
                = new MockMultipartFile(
                "file",
                "hel.l.o.tar.gz",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );

        MockMultipartFile file3
                = new MockMultipartFile(
                "file",
                "hel.l.o.zip",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );
        MockMultipartFile file4
                = new MockMultipartFile(
                "file",
                "hello.zip",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );

        MockMultipartFile file5
                = new MockMultipartFile(
                "file",
                "hello.tar.bz2",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );
        AttachmentDTO attachmentDTO = attachmentService.convertToDto(attachmentService.upload(file));
        AttachmentDTO attachmentDTO2 = attachmentService.convertToDto(attachmentService.upload(file2));
        AttachmentDTO attachmentDTO3 = attachmentService.convertToDto(attachmentService.upload(file3));
        AttachmentDTO attachmentDTO4 = attachmentService.convertToDto(attachmentService.upload(file4));
        AttachmentDTO attachmentDTO5 = attachmentService.convertToDto(attachmentService.upload(file5));
        Assertions.assertEquals(attachmentDTO.getSuffix(), "tar.gz");
        Assertions.assertEquals(attachmentDTO2.getSuffix(), "tar.gz");
        Assertions.assertEquals(attachmentDTO3.getSuffix(), "zip");
        Assertions.assertEquals(attachmentDTO4.getSuffix(), "zip");
        Assertions.assertEquals(attachmentDTO5.getSuffix(), "tar.bz2");
    }
}