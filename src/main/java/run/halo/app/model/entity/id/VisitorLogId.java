package run.halo.app.model.entity.id;

import java.io.Serializable;
import java.util.Date;

public class VisitorLogId implements Serializable {

    private Date accessDate;

    private String ipAddress;

    public VisitorLogId() {}

    public VisitorLogId(Date accessDate, String ipAddress) {
        this.accessDate = accessDate;
        this.ipAddress = ipAddress;
    }
}