package run.halo.app.service.impl;

import run.halo.app.model.entity.Option;
import run.halo.app.model.properties.QnYunProperties;
import run.halo.app.repository.OptionRepository;
import com.qiniu.common.Zone;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/**
 * OptionService test.
 *
 * @author johnniang
 * @date 3/22/19
 */
@RunWith(MockitoJUnitRunner.class)
public class OptionServiceImplTest {

    @Mock
    private OptionRepository optionRepository;

    @InjectMocks
    private OptionServiceImpl optionService;

    @Test
    public void getQiniuAutoZoneTest() {
        getQiniuZoneTest("", Zone.autoZone());
    }

    @Test
    public void getQiniuAutoZoneOfNullOptionTest() {
        getQiniuZoneTest(Zone.autoZone(), null);
    }

    @Test
    public void getQiniuZ0ZoneTest() {
        getQiniuZoneTest("z0", Zone.zone0());
    }

    @Test
    public void getQiniuZ1ZoneTest() {
        getQiniuZoneTest("z1", Zone.zone1());
    }

    @Test
    public void getQiniuZ2ZoneTest() {
        getQiniuZoneTest("z2", Zone.zone2());
    }

    @Test
    public void getQiniuAs0ZoneTest() {
        getQiniuZoneTest("as0", Zone.zoneAs0());
    }

    @Test
    public void getQiniuNa0ZoneTest() {
        getQiniuZoneTest("na0", Zone.zoneNa0());
    }

    private void getQiniuZoneTest(String region, Zone actualZone) {
        getQiniuZoneTest(actualZone, new Option("", region));
    }

    private void getQiniuZoneTest(Zone actualZone, Option option) {
        QnYunProperties zoneProperty = QnYunProperties.ZONE;

        // Given
        given(optionRepository.findByKey(zoneProperty.getValue())).willReturn(Optional.ofNullable(option));

        // When
        Zone zone = optionService.getQnYunZone();

        // Then
        then(optionRepository).should().findByKey(zoneProperty.getValue());

        assertNotNull(zone);
        assertThat(zone.getRegion(), equalTo(actualZone.getRegion()));
    }
}
