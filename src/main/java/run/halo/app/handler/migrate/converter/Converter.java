package run.halo.app.handler.migrate.converter;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 博客迁移数据转换器,只定义从T转U的单向转换
 * @author guqing
 * @date 2020-01-18 16:45
 */
public class Converter<T, U> {
    /**
     *  从 T 转换为 U
     */
    private Function<T, U> fromDto;

    public Converter(final Function<T, U> fromDto) {
        this.fromDto = fromDto;
    }

    public final U converterFromDto(final T dto){
        return fromDto.apply(dto);
    }

    public final List<U> batchConverterFromDto(final List<T> dtos){
        return dtos.stream().map(this::converterFromDto).collect(Collectors.toList());
    }
}

