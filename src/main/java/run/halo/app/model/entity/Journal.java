package run.halo.app.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.model.enums.JournalType;

import javax.persistence.*;

/**
 * Journal entity
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-03-22
 */
@Data
@Entity
@Table(name = "journals")
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Journal extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "source_content", columnDefinition = "varchar(1023) not null default ''")
    private String sourceContent;

    @Column(name = "content", columnDefinition = "text not null")
    private String content;

    @Column(name = "likes", columnDefinition = "bigint default 0")
    private Long likes;

    @Column(name = "type", columnDefinition = "int default 1")
    private JournalType type;

    @Override
    public void prePersist() {
        super.prePersist();

        id = null;

        if (likes == null || likes < 0) {
            likes = 0L;
        }

        if (type == null) {
            type = JournalType.PUBLIC;
        }
    }
}
