package run.halo.app.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IpLocation {

    String country;

    @JsonProperty("regionName")
    String region;

    String city;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
