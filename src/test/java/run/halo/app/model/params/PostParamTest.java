package run.halo.app.model.params;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import run.halo.app.model.entity.Post;
import run.halo.app.model.enums.PostEditorType;
import run.halo.app.model.enums.PostStatus;

/**
 * Test for {@link PostParam}.
 *
 * @author guqing
 * @date 2022-02-21
 */
public class PostParamTest {

    private Validator validator;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void validationTest() {
        PostParam postParam = new PostParam();
        postParam.setTitle("Title");
        postParam.setSlug("Slug");
        postParam.setPassword("123");
        postParam.setTopPriority(-1);

        Set<ConstraintViolation<PostParam>> validate = validator.validate(postParam);
        assertThat(validate).isNotNull();
        assertThat(validate).hasSize(1);
        assertThat(validate.iterator().next().getMessage()).isEqualTo("排序字段值不能小于 0");
    }

    @Test
    public void validatePassword() {
        PostParam postParam = new PostParam();
        postParam.setTitle("Title");
        postParam.setSlug("Slug");
        postParam.setPassword(" 123");
        postParam.setTopPriority(0);

        Set<ConstraintViolation<PostParam>> validate = validator.validate(postParam);
        assertThat(validate).isNotNull();
        assertThat(validate).hasSize(1);
        assertThat(validate.iterator().next().getMessage()).isEqualTo("密码开头和结尾不能包含空字符串");

        postParam.setPassword("123 ");
        validate = validator.validate(postParam);
        assertThat(validate).isNotNull();
        assertThat(validate).hasSize(1);
        assertThat(validate.iterator().next().getMessage()).isEqualTo("密码开头和结尾不能包含空字符串");

        postParam.setPassword("");
        validate = validator.validate(postParam);
        assertThat(validate).isEmpty();

        postParam.setPassword("123 hello");
        validate = validator.validate(postParam);
        assertThat(validate).isEmpty();
    }

    @Test
    public void convertToTest() {
        PostParam postParam = new PostParam();
        postParam.setTitle("Title");
        postParam.setSlug("Slug");
        postParam.setPassword("123");
        postParam.setStatus(PostStatus.INTIMATE);
        postParam.setMetaDescription("Meta description");
        postParam.setTagIds(Set.of(1, 2, 3));

        Post post = postParam.convertTo();
        assertThat(post).isNotNull();
        assertThat(post.getTitle()).isEqualTo(postParam.getTitle());
        assertThat(post.getSlug()).isEqualTo(postParam.getSlug());
        assertThat(post.getPassword()).isEqualTo(postParam.getPassword());
    }

    @Test
    public void shouldServerSideRender() {
        PostParam postParam = new PostParam();
        postParam.setSlug("slug");
        // server side rendering
        postParam.setKeepRaw(false);
        postParam.setOriginalContent("两个黄鹂鸣翠柳，一行白鹭上青天。");

        Post post = postParam.convertTo();
        assertThat(post).isNotNull();
        assertThat(post.getContent().getContent()).isEqualTo("<p>两个黄鹂鸣翠柳，一行白鹭上青天。</p>\n");
    }

    @Test
    public void shouldNotServerSideRenderTest() {
        PostParam postParam = new PostParam();
        postParam.setSlug("slug");
        // server side render
        postParam.setKeepRaw(false);
        postParam.setOriginalContent("两个黄鹂鸣翠柳，一行白鹭上青天。");

        // The keepRaw is true value and edit type is equals to markdown
        postParam.setKeepRaw(true);
        postParam.setContent("front-end rendering");
        Post post1 = postParam.convertTo();
        assertThat(post1).isNotNull();
        assertThat(post1.getContent().getContent()).isEqualTo(postParam.getContent());

        // Edit type not equals to markdown and keepRaw is true
        postParam.setKeepRaw(true);
        postParam.setEditorType(PostEditorType.RICHTEXT);
        Post post2 = postParam.convertTo();
        assertThat(post2).isNotNull();
        assertThat(post2.getContent().getContent()).isEqualTo(postParam.getOriginalContent());

        // Edit type not equals to markdown but want to let server rendering
        postParam.setKeepRaw(false);
        Post post3 = postParam.convertTo();
        assertThat(post3).isNotNull();
        assertThat(post3.getContent().getContent()).isEqualTo(postParam.getOriginalContent());
    }
}
