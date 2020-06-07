package run.halo.app.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import run.halo.app.model.entity.Post;

import java.util.Optional;

/**
 * AdjacentPost class
 *
 * @author zhouchunjie
 * @date 2020/1/12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdjacentPostVO {

    private Post prevPost;
    private Post nextPost;

    public Optional<Post> getOptionalPrevPost() {
        return Optional.ofNullable(this.getPrevPost());
    }

    public Optional<Post> getOptionalNextPost() {
        return Optional.ofNullable(this.getNextPost());
    }

}
