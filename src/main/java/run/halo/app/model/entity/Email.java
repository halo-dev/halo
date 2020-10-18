package run.halo.app.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * Email entity
 *
 * @author ryanwang
 * @date 2019-03-12
 */
@Data
@Entity
@Table(name = "emails", indexes = {@Index(name = "email_index", columnList = "value")})
@ToString
@EqualsAndHashCode(callSuper = true)
public class Email extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "custom-id")
    @GenericGenerator(name = "custom-id", strategy = "run.halo.app.model.entity.support.CustomIdGenerator")
    private Integer id;

    /**
     * Email name.
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Email value.
     */
    @Column(name = "value", length = 127, nullable = false)
    private String value;

    /**
     * Website description.
     */
    @Column(name = "description")
    private String description;

    @Override
    public void prePersist() {
        super.prePersist();
        if (description == null) {
            description = "";
        }
    }
}
