package run.halo.app.service.impl;

import org.springframework.stereotype.Service;
import run.halo.app.model.entity.ThemeSetting;
import run.halo.app.repository.ThemeSettingRepository;
import run.halo.app.service.ThemeSettingService;
import run.halo.app.service.base.AbstractCrudService;

/**
 * Theme setting service implementation.
 *
 * @author johnniang
 * @date 4/8/19
 */
@Service
public class ThemeSettingServiceImpl extends AbstractCrudService<ThemeSetting, Integer> implements ThemeSettingService {

    private final ThemeSettingRepository themeSettingRepository;

    public ThemeSettingServiceImpl(ThemeSettingRepository themeSettingRepository) {
        super(themeSettingRepository);
        this.themeSettingRepository = themeSettingRepository;
    }

}
