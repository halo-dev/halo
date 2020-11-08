package run.halo.app.aspect;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import run.halo.app.model.entity.PostComment;
import run.halo.app.model.entity.User;
import run.halo.app.security.authentication.AuthenticationImpl;
import run.halo.app.security.context.SecurityContextHolder;
import run.halo.app.security.context.SecurityContextImpl;
import run.halo.app.security.support.UserDetail;
import run.halo.app.service.PostCommentService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author giveup
 * @description SensitiveConcealAspectTest
 * @date 1:14 AM 27/5/2020
 */
@SpringBootTest
@Disabled("Due to ip address: [0:0:0:0:0:0:0:1]")
class SensitiveConcealAspectTest {

    @Autowired
    PostCommentService postCommentService;

    @Test
    void testGuest() {
        List<PostComment> postComments = postCommentService.listBy(1);
        for (PostComment postComment : postComments) {
            assertEquals("", postComment.getIpAddress());
            assertEquals("", postComment.getEmail());
        }
    }

    @Test
    void testAdmin() {
        SecurityContextHolder.setContext(new SecurityContextImpl(new AuthenticationImpl(new UserDetail(new User()))));

        List<PostComment> postComments = postCommentService.listBy(1);
        for (PostComment postComment : postComments) {
            assertEquals("127.0.0.1", postComment.getIpAddress());
            assertEquals("hi@halo.run", postComment.getEmail());
        }
    }

}
