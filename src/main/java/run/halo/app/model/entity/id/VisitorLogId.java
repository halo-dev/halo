package run.halo.app.model.entity.id;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class VisitorLogId implements Serializable {

    private Date accessDate;

    private String ipAddress;

    public VisitorLogId() {}

    public VisitorLogId(Date accessDate, String ipAddress) {
        this.accessDate = accessDate;
        this.ipAddress = ipAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VisitorLogId visitorLogId = (VisitorLogId) o;
        return accessDate.equals(visitorLogId.accessDate) &&
            ipAddress.equals(visitorLogId.ipAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessDate, ipAddress);
    }
}