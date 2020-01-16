package run.halo.app.handler.migrate.converter;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 博客迁移数据转换器,只定义从SOURCE转TARGET的单向转换
 *
 * @author guqing
 * @date 2020-01-18 16:45
 */
public interface Converter<SOURCE, TARGET> {

    /**
     * 将source转换为target
     *
     * @param s 需要转换的源对象
     * @return 返回转换得到的结果对象
     */
    TARGET convertFrom(SOURCE s);

    /**
     * 从SOURCE转为TARGET
     *
     * @param s        source,需要转换的原始SOURCE集合
     * @param function 具体转换逻辑
     * @return 返回转换得到的TARGET对象
     */
    default TARGET convertFrom(SOURCE s, Function<SOURCE, TARGET> function) {
        return function.apply(s);
    }

    /**
     * 批量从SOURCE转换得到TARGET
     *
     * @param list     需要转换的原始SOURCE集合
     * @param function 具体转换逻辑
     * @return 返回转换得到的TARGET集合结果
     */
    default List<TARGET> batchConverterFromDto(List<SOURCE> list, Function<SOURCE, TARGET> function) {
        return list.stream().map(s -> convertFrom(s, function)).collect(Collectors.toList());
    }
}

