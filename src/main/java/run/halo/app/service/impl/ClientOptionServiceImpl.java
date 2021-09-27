package run.halo.app.service.impl;

import static java.util.stream.Collectors.toMap;

import com.qiniu.storage.Region;
import java.util.Locale;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import run.halo.app.model.dto.OptionSimpleDTO;
import run.halo.app.model.params.OptionQuery;
import run.halo.app.service.ClientOptionService;
import run.halo.app.service.OptionService;


/**
 * The client only provides filtered data
 *
 * @author LIlGG
 * @date 2021/8/2
 */
@Service
public class ClientOptionServiceImpl implements ClientOptionService {

    private final OptionService optionService;

    private final OptionFilter optionFilter;

    ClientOptionServiceImpl(OptionService optionService) {
        this.optionService = optionService;
        this.optionFilter = new OptionFilter(optionService);
    }

    @Override
    @Transactional
    public Map<String, Object> listOptions() {
        Map<String, Object> options = optionService.listOptions();
        return optionFilter.filter(options.keySet()).parallelStream()
            .collect(toMap(optionName -> optionName, options::get));
    }

    @Override
    public Page<OptionSimpleDTO> pageDtosBy(Pageable pageable, OptionQuery optionQuery) {
        return optionService.pageDtosBy(pageable, optionQuery);
    }

    @Override
    public int getPostPageSize() {
        return optionService.getPostPageSize();
    }

    @Override
    public int getArchivesPageSize() {
        return optionService.getArchivesPageSize();
    }

    @Override
    public int getCommentPageSize() {
        return optionService.getCommentPageSize();
    }

    @Override
    public int getRssPageSize() {
        return optionService.getRssPageSize();
    }

    @Override
    public Region getQiniuRegion() {
        return optionService.getQiniuRegion();
    }

    @Override
    public Locale getLocale() {
        return optionService.getLocale();
    }

    @Override
    public String getBlogBaseUrl() {
        return optionService.getBlogBaseUrl();
    }

    @Override
    public long getBirthday() {
        return optionService.getBirthday();
    }

    @Override
    public void flush() {
        optionService.flush();
    }
}
