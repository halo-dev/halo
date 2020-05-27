package run.halo.app.aspect;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import run.halo.app.model.entity.PostComment;
import run.halo.app.model.entity.User;
import run.halo.app.security.authentication.AuthenticationImpl;
import run.halo.app.security.context.SecurityContextHolder;
import run.halo.app.security.context.SecurityContextImpl;
import run.halo.app.security.support.UserDetail;
import run.halo.app.service.PostCommentService;

import java.util.List;


/**
 * @author giveup
 * @description SensitiveConcealAspectTest
 * @date 1:14 AM 27/5/2020
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class SensitiveConcealAspectTest {


    @Autowired
    private PostCommentService postCommentService;


    @Test
    public void testGuest() {

        List<PostComment> postComments = postCommentService.listBy(1);
        for (PostComment postComment : postComments) {
            Assert.assertEquals("", postComment.getIpAddress());
            Assert.assertEquals("", postComment.getEmail());
        }

    }

    @Test
    public void testAdmin() {

        SecurityContextHolder.setContext(new SecurityContextImpl(new AuthenticationImpl(new UserDetail(new User()))));

        List<PostComment> postComments = postCommentService.listBy(1);
        for (PostComment postComment : postComments) {
            Assert.assertEquals("127.0.0.1", postComment.getIpAddress());
            Assert.assertEquals("hi@halo.run", postComment.getEmail());
        }

    }

}
