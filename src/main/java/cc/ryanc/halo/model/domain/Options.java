package cc.ryanc.halo.model.domain;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * <pre>
 *     系统设置
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2017/11/14
 */
@Data
@Entity
@Table(name = "halo_options")
public class Options implements Serializable {

    private static final long serialVersionUID = -4065369084341893446L;

    /**
     * 设置项名称
     */
    @Id
    @Column(length = 127)
    private String optionName;

    /**
     * 设置项的值
     */
    @Lob
    private String optionValue;
}
