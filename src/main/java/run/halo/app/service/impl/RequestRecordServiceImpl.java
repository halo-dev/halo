package run.halo.app.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import run.halo.app.model.entity.RequestRecord;
import run.halo.app.repository.RequestRecordRepository;
import run.halo.app.service.RequestRecordService;
import run.halo.app.utils.ServletUtils;
import ua_parser.Client;
import ua_parser.Parser;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class RequestRecordServiceImpl implements RequestRecordService {

    public static final String EMPTY = "EMPTY";

    private static final String USER_AGENT = "User-Agent";

    private final RequestRecordRepository requestRecordRepository;

    public RequestRecordServiceImpl(RequestRecordRepository requestRecordRepository) {
        this.requestRecordRepository = requestRecordRepository;
    }

    @Override
    public void save(HttpServletRequest request) {
        Parser parser = null;
        try {
            parser = new Parser();
        } catch (IOException e) {
            e.printStackTrace();
        }

        RequestRecord record = getRecord(request, parser);
        if (record != null) {
            requestRecordRepository.save(record);
        }
    }

    private RequestRecord getRecord(HttpServletRequest request, Parser parser) {
        if (isNullOrEmpty(request.getHeader(USER_AGENT))) {
            return null;
        } else {
            Client client = parser.parse(request.getHeader(USER_AGENT));
            String operatingSystem = client.os.family;
            String operatingSystemVersion = client.os.major == null ? "unknown" : client.os.major;
            String browser = client.userAgent.family;
            String browserVersion = client.userAgent.major;
            String device = client.device.family;
            Date time = new Date();
            String ip = ServletUtils.getRequestIp();

            if (ip == null || !isIp(ip)) {
                return null;
            }

            String tmpUrl = request.getRequestURI()
                    .replace("https://", "")
                    .replace("http://", "");
            String url = tmpUrl.substring(tmpUrl.indexOf("/"));
            String method = request.getMethod();
            RequestRecord record = new RequestRecord();
            record.setOperatingSystem(operatingSystem);
            record.setOperatingSystemVersion(operatingSystemVersion);
            record.setBrowser(browser);
            record.setBrowserVersion(browserVersion);
            record.setDevice(device);
            record.setTime(time);
            record.setIp(ip);
            record.setUrl(url);
            record.setHttpMethod(method);
            return record;
        }
    }

    private boolean isNullOrEmpty(String value) {
        return value == null || value.isEmpty();
    }

    private boolean isIp(String ip) {
        String regex = "^((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(ip);
        return matcher.find() && !ip.equals("127.0.0.1");
    }

    @Override
    public void save(RequestRecord record) {
        requestRecordRepository.save(record);
    }

    @Override
    public Optional<RequestRecord> getRecordByIp(String ip) {
        return this.requestRecordRepository.findFirstByIpAndCityNotContaining(ip, EMPTY);
    }

    @Override
    public List<RequestRecord> getIncompleteRecordList() {
        return this.requestRecordRepository.findAllByCity(EMPTY);
    }
}
