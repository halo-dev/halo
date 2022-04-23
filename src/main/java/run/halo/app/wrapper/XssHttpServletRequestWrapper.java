package run.halo.app.wrapper;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.EscapeUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    /**
     * Constructs a request object wrapping the given request.
     *
     * @param request the {@link HttpServletRequest} to be wrapped.
     * @throws IllegalArgumentException if the request is null
     */

    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        return EscapeUtil.escape(value);
    }

    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        return EscapeUtil.escape(value);
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values != null) {
            int length = values.length;
            String[] escapeValues = new String[length];
            for (int i = 0; i < length; i++) {
                escapeValues[i] = EscapeUtil.escape(values[i]);
            }
            return escapeValues;
        }
        return super.getParameterValues(name);
    }


    @Override
    public ServletInputStream getInputStream() throws IOException {
        // not json encode.
        if (!super.getHeader(HttpHeaders.CONTENT_TYPE)
            .equalsIgnoreCase(MediaType.APPLICATION_JSON_VALUE)) {
            return super.getInputStream();
        }


        String json = IoUtil.read(super.getInputStream(), "utf-8");
        if (json == null) {
            return super.getInputStream();
        }
        json = cleanXSS(json, true).trim();
        final ByteArrayInputStream bis =
            new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return true;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
            }

            @Override
            public int read() {
                return bis.read();
            }
        };
    }

    public static String cleanXSS(String value, Boolean json) {
        value = value.replaceAll("&", "%26");
        value = value.replaceAll("<", "%3c");
        value = value.replaceAll(">", "%3e");
        value = value.replaceAll("'", "%27");
        // value = value.replaceAll(":", "%3a");
        if (!json) {
            // value = value.replaceAll("\"", "%22");
        }
        // value = value.replaceAll("/", "%2f");
        return value;
    }


}
