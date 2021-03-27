package run.halo.app.attributeconverter;

import run.halo.app.model.enums.ValueEnum;

/**
 * City level.
 *
 * @author johnniang
 */
enum CityLevel implements ValueEnum<Integer> {

    PROVINCE(123),

    CITY(456),

    DISTRICT(789);

    private final int value;

    CityLevel(int value) {
        this.value = value;
    }


    @Override
    public Integer getValue() {
        return value;
    }
}
