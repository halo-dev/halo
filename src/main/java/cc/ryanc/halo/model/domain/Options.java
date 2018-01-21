package cc.ryanc.halo.model.domain;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @author : RYAN0UP
 * @date : 2017/11/14
 * @version : 1.0
 * description : 设置相关的实体类
 */
@Data
@Entity
@Table(name = "halo_options")
public class Options implements Serializable {
    @Id
    private String optionName;
    @Lob
    private String optionValue;
}
