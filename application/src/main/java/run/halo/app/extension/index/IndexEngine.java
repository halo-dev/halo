package run.halo.app.extension.index;

import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequest;

public interface IndexEngine {

    <E extends Extension> void insert(Iterable<E> extensions);

    <E extends Extension> void update(Iterable<E> extension);

    <E extends Extension> void delete(Iterable<E> extensions);

    <E extends Extension> ListResult<String> retrieve(
        Class<E> type, @Nullable ListOptions options, @NonNull PageRequest page
    );

    <E extends Extension> Iterable<String> retrieveAll(
        Class<E> type, @Nullable ListOptions options, @Nullable Sort sort
    );

    <E extends Extension> Iterable<String> retrieveTopN(
        Class<E> type, @Nullable ListOptions options, @Nullable Sort sort, int topN
    );

    <E extends Extension> long count(Class<E> type, ListOptions options);

    @NonNull
    IndicesManager getIndicesManager();

}
