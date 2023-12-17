package run.halo.app.extension.store;

import jakarta.persistence.Lob;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

/**
 * ExtensionStore is an entity for storing Extension data into database.
 *
 * @author johnniang
 */
@Data
@Table(name = "extensions")
public class ExtensionStore {

    /**
     * Extension store name, which is globally unique.
     * We will use it to query Extensions by using left-like query clause.
     */
    @Id
    private String name;

    /**
     * Exactly Extension body, which might be base64 format.
     */
    @Lob
    private byte[] data;

    /**
     * This field only for serving optimistic lock value.
     */
    @Version
    private Long version;

    public ExtensionStore() {
    }

    public ExtensionStore(String name, byte[] data) {
        this.name = name;
        this.data = data;
    }

    public ExtensionStore(String name, Long version) {
        this.name = name;
        this.version = version;
    }

    public ExtensionStore(String name, byte[] data, Long version) {
        this.name = name;
        this.data = data;
        this.version = version;
    }
}
