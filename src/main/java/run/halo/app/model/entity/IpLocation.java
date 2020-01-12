package run.halo.app.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class IpLocation {

    String country;

    @JsonProperty("regionName")
    String region;

    String city;
}
