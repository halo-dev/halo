package run.halo.app.model.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

/**
 * comment_black_list
 *
 * @author Lei XinXin
 * @date 2020/1/3
 */
@Data
@Entity
@Table(name = "comment_black_list")
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentBlackList extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ip_address", columnDefinition = "VARCHAR(127) NOT NULL")
    private String ipAddress;

    /**
     * 封禁时间
     */
    @Column(name = "ban_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date banTime;
}
