package run.halo.app.model.vo;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import run.halo.app.model.entity.Post;

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

    private Post prePost;
    private Post nextPost;

    public Optional<Post> getOptionalPrePost(){
        return Optional.ofNullable(this.getPrePost());
    }

    public Optional<Post> getOptionalNextPost() {
        return Optional.ofNullable(this.getNextPost());
    }

}
