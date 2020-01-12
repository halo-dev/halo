package run.halo.app.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import run.halo.app.model.entity.IpLocation;
import run.halo.app.model.entity.RequestRecord;
import run.halo.app.service.RequestRecordService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class RecordTask {

    private static final int TWELVE_HOUR = 1000 * 60 * 60 * 12;

    private final RequestRecordService recordService;

    private RestTemplate restTemplate = new RestTemplate();

    public RecordTask(RequestRecordService recordService) {
        this.recordService = recordService;
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(Collections.singletonList(MediaType.ALL));
        messageConverters.add(converter);
        this.restTemplate.setMessageConverters(messageConverters);
    }

    @Async
    @Scheduled(fixedRate = TWELVE_HOUR)
    public void completeRecord() {
        List<RequestRecord> list = recordService.getIncompleteRecordList();
        log.info("list size: " + list.size());
        list.forEach(requestRecord -> {
            Optional<RequestRecord> optional = recordService.getRecordByIp(requestRecord.getIp());
            if (optional.isPresent()) {
                RequestRecord origin = optional.get();
                requestRecord.setCountry(origin.getCountry());
                requestRecord.setRegion(origin.getRegion());
                requestRecord.setCity(origin.getCity());
                log.info("ip: " + requestRecord.getIp());
                recordService.save(requestRecord);
            } else {
                try {
                    IpLocation location = restTemplate.getForObject(
                            "http://ip-api.com/json/" + requestRecord.getIp() + "?lang=zh-CN",
                            IpLocation.class
                    );
                    requestRecord.setCountry(location.getCountry());
                    requestRecord.setRegion(location.getRegion());
                    requestRecord.setCity(location.getCity());
                    recordService.save(requestRecord);
//                  sleeping due to frequency limitation of API, 45 request per minute
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    log.error("occurred error when execute record task", e);
                }
            }
        });
    }
}
