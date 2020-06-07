package run.halo.app.mail;


import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Mail properties.
 *
 * @author johnniang
 */
public class MailProperties extends org.springframework.boot.autoconfigure.mail.MailProperties {

    private Map<String, String> properties;

    public MailProperties() {
        this(false);
    }

    public MailProperties(boolean needDebug) {
        // set some default properties
        addProperties("mail.debug", Boolean.toString(needDebug));
        addProperties("mail.smtp.auth", Boolean.TRUE.toString());
        addProperties("mail.smtp.ssl.enable", Boolean.TRUE.toString());
        addProperties("mail.smtp.timeout", "10000");
    }

    public void addProperties(String key, String value) {
        if (properties == null) {
            properties = new HashMap<>();
        }
        properties.put(key, value);
    }

    @Override
    public Map<String, String> getProperties() {
        return this.properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        final String lineSuffix = ",\n";
        final StringBuffer sb = new StringBuffer();
        sb.append("MailProperties{").append(lineSuffix);
        sb.append("host=").append(getHost()).append(lineSuffix);
        sb.append("username=").append(getUsername()).append(lineSuffix);
        sb.append("password=").append(StringUtils.isBlank(getPassword()) ? "<null>" : "<non-null>");
        sb.append("port=").append(getPort()).append(lineSuffix);
        sb.append("protocol=").append(getProtocol()).append(lineSuffix);
        sb.append("defaultEncoding=").append(getDefaultEncoding()).append(lineSuffix);
        sb.append("properties=").append(properties).append(lineSuffix);
        sb.append('}');
        return sb.toString();
    }
}
