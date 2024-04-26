package run.halo.app.content;

public record ContentUpdateParam(Long version, String raw, String content, String rawType) {

    public static ContentUpdateParam from(Content content) {
        return new ContentUpdateParam(null, content.raw(), content.content(),
            content.rawType());
    }
}
