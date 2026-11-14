package run.halo.app.core.attachment.thumbnail;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.reactive.ServerWebExchangeContextFilter;
import reactor.core.publisher.Mono;
import run.halo.app.core.attachment.AttachmentChangedEvent;
import run.halo.app.core.attachment.ThumbnailSize;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.index.query.Queries;
import run.halo.app.infra.ExternalUrlSupplier;
import run.halo.app.infra.properties.HaloProperties;

/**
 * Implementation of {@link ThumbnailService}.
 *
 * <p>Caches thumbnail links in memory for better performance.
 *
 * @author johnniang
 * @since 2.22.0
 */
@Slf4j
@Component
class DefaultThumbnailService implements ThumbnailService {

    private static final Map<ThumbnailSize, URI> EMPTY_THUMBNAILS = Map.of();

    private static final Duration CACHE_TTL = Duration.ofHours(1);

    private static final CachedThumbnails NOT_FOUND = new CachedThumbnails(null, false, null, EMPTY_THUMBNAILS);

    private static final int MAX_LOOKUP_ATTEMPTS = 2;

    private static final int LOCK_STRIPES = 64;

    private static final int CHANGE_STRIPES = 1024;

    /**
     * Maximum percent-decoding rounds applied to a relative reference. Every round strictly shortens the value, so the
     * loop terminates by itself; the bound only limits the work spent on hostile input and documents how many nested
     * encodings are expected to be decoded by a downstream consumer.
     */
    private static final int MAX_DECODE_ROUNDS = 8;

    /** RFC 3986 scheme prefix. A relative-path reference must not start with one. */
    private static final Pattern SCHEME_PREFIX = Pattern.compile("^[A-Za-z][A-Za-z0-9+.\\-]*:");

    private final Cache<String, CachedThumbnails> thumbnailCache;

    /**
     * Striped locks that serialize a snapshot's cache write with change events for the same attachment. A plain
     * {@code compute} on the permalink key cannot coordinate with an event that only knows the new permalink, so an
     * in-flight lookup could otherwise write the old permalink back after the event moved on to the new one.
     */
    private final Object[] attachmentLocks = new Object[LOCK_STRIPES];

    /**
     * Reverse associations for entries currently held by {@link #thumbnailCache}. Each association refers to the exact
     * cached object so a removal notification for an older entry cannot remove its replacement. This map has no
     * independent expiration policy and retains neither evicted entries nor deletion markers.
     */
    private final Map<String, CachedThumbnails> attachmentPermalinkIndex = new ConcurrentHashMap<>();

    /**
     * A lookup captures the sequence before querying, then checks the returned attachment's stripe under its lock.
     * Events and accepted snapshots advance that stripe, protecting in-flight queries even after the reverse
     * association has been removed. Hash collisions may exhaust the bounded retries and cause a temporary miss (404 for
     * the redirect endpoint); that result is not cached. Memory does not grow with the number of attachments ever seen.
     */
    private final AtomicLong changeSequence = new AtomicLong();

    private final AtomicLong[] attachmentChangeSequences = new AtomicLong[CHANGE_STRIPES];

    /**
     * Bumped per permalink whenever an attachment changes. A lookup captures the version of the requested permalink
     * before querying and discards a result that raced with an update or deletion, so a stale snapshot can never
     * repopulate the cache with no-longer-trusted thumbnails. Versions are striped by permalink so a change to an
     * unrelated attachment does not invalidate in-flight lookups. Two permalinks sharing a stripe may still cause a
     * retry, and after both attempts a request can fail with 404; that is the accepted cost of bounded memory.
     */
    private final AtomicLong[] changeVersions = new AtomicLong[CHANGE_STRIPES];

    private final ReactiveExtensionClient client;

    private final ExternalUrlSupplier externalUrlSupplier;

    private final HaloProperties haloProperties;

    public DefaultThumbnailService(
            ReactiveExtensionClient client, ExternalUrlSupplier externalUrlSupplier, HaloProperties haloProperties) {
        this.client = client;
        this.externalUrlSupplier = externalUrlSupplier;
        this.haloProperties = haloProperties;
        this.thumbnailCache = Caffeine.newBuilder()
                // TODO make it configurable
                .maximumSize(10_000)
                .expireAfterWrite(CACHE_TTL)
                // The listener only updates the reverse map; it never acquires attachment or cache locks.
                .executor(Runnable::run)
                .<String, CachedThumbnails>removalListener((key, value, cause) -> removePermalinkIndex(value))
                .build();
        for (var i = 0; i < attachmentLocks.length; i++) {
            attachmentLocks[i] = new Object();
        }
        for (var i = 0; i < changeVersions.length; i++) {
            changeVersions[i] = new AtomicLong();
            attachmentChangeSequences[i] = new AtomicLong();
        }
    }

    @Order(Ordered.HIGHEST_PRECEDENCE)
    @EventListener
    void handleAttachmentChangedEvent(AttachmentChangedEvent event) {
        updateCache(event.getAttachment());
    }

    void updateCache(Attachment attachment) {
        if (attachment.getStatus() == null || attachment.getMetadata() == null) {
            return;
        }
        var name = attachment.getMetadata().getName();
        if (!StringUtils.hasText(name)) {
            return;
        }
        synchronized (attachmentLock(name)) {
            recordAttachmentChange(name);
            var permalink = attachment.getStatus().getPermalink();
            var previousEntry = attachmentPermalinkIndex.get(name);
            var previousUri = previousEntry != null ? previousEntry.trustedPermalink() : null;
            var previousPermalink = previousUri != null ? previousUri.toString() : null;
            if (StringUtils.hasText(previousPermalink) && !Objects.equals(previousPermalink, permalink)) {
                changeVersion(previousPermalink).incrementAndGet();
            }
            if (StringUtils.hasText(permalink)) {
                changeVersion(permalink).incrementAndGet();
            }
            if (StringUtils.hasText(previousPermalink)
                    && (!StringUtils.hasText(permalink) || !Objects.equals(previousPermalink, permalink))) {
                thumbnailCache.invalidate(previousPermalink);
            }
            if (!StringUtils.hasText(permalink)) {
                return;
            }
            if (ExtensionUtil.isDeleted(attachment)) {
                // Invalidate instead of caching NOT_FOUND: the permalink may be shared with another live attachment.
                thumbnailCache.invalidate(permalink);
                return;
            }
            cacheAttachment(name, permalink, attachment.getStatus().getThumbnails());
        }
    }

    private Object attachmentLock(String name) {
        return attachmentLocks[Math.floorMod(name.hashCode(), attachmentLocks.length)];
    }

    private AtomicLong changeVersion(String permalink) {
        return changeVersions[Math.floorMod(permalink.hashCode(), changeVersions.length)];
    }

    private AtomicLong attachmentChangeSequence(String name) {
        return attachmentChangeSequences[Math.floorMod(name.hashCode(), attachmentChangeSequences.length)];
    }

    private void recordAttachmentChange(String name) {
        attachmentChangeSequence(name).accumulateAndGet(changeSequence.incrementAndGet(), Math::max);
    }

    private CachedThumbnails cacheAttachment(String name, String permalink, @Nullable Map<String, String> thumbnails) {
        var cached = CachedThumbnails.fromAttachment(name, permalink, thumbnails);
        recordAttachmentChange(name);
        // Register before publishing: eviction may run during put and must be able to remove this association.
        attachmentPermalinkIndex.put(name, cached);
        thumbnailCache.put(permalink, cached);
        return cached;
    }

    private void removePermalinkIndex(@Nullable CachedThumbnails cached) {
        if (cached != null && cached.attachmentName() != null) {
            attachmentPermalinkIndex.computeIfPresent(
                    cached.attachmentName(), (name, current) -> current == cached ? null : current);
        }
    }

    @Override
    public Mono<URI> get(URI permalink, ThumbnailSize size) {
        return get(permalink).mapNotNull(thumbnails -> thumbnails.get(size));
    }

    @Override
    public Mono<Map<ThumbnailSize, URI>> get(URI permalink) {
        if (isThumbnailDisabled()) {
            return Mono.just(EMPTY_THUMBNAILS);
        }
        return resolve(permalink).map(CachedThumbnails::thumbnails);
    }

    @Override
    public Mono<URI> getSafeRedirectUri(URI permalink, ThumbnailSize size) {
        var encodedPermalink = URI.create(permalink.toASCIIString());
        // Only trust the configured external URL here. Deriving it from the request would let a spoofed Host or
        // X-Forwarded-Host header make an arbitrary external host look site-local.
        var externalUrl = externalUrlSupplier.getRaw();
        if (isSiteLocal(encodedPermalink, externalUrl)) {
            if (isThumbnailDisabled()) {
                return Mono.just(encodedPermalink);
            }
            var thumbnails = ThumbnailUtils.buildSrcsetMap(encodedPermalink);
            var thumbnail = thumbnails.get(size);
            return Mono.just(thumbnail != null ? thumbnail : encodedPermalink);
        }
        // External / scheme-relative: Location may only come from a recorded Attachment.
        return lookup(encodedPermalink).flatMap(cached -> {
            if (!cached.attachmentPresent()) {
                return Mono.empty();
            }
            if (!isThumbnailDisabled()) {
                var thumbnail = cached.thumbnails().get(size);
                if (isSafeRedirectTarget(thumbnail, externalUrl)) {
                    return Mono.just(thumbnail);
                }
            }
            var trustedPermalink = cached.trustedPermalink();
            return isSafeRedirectTarget(trustedPermalink, externalUrl) ? Mono.just(trustedPermalink) : Mono.empty();
        });
    }

    private Mono<CachedThumbnails> resolve(URI permalink) {
        var encodedPermalink = URI.create(permalink.toASCIIString());
        return Mono.deferContextual(contextView -> {
            var externalUrl = ServerWebExchangeContextFilter.getExchange(contextView)
                    .map(exchange -> externalUrlSupplier.getURL(exchange.getRequest()))
                    .orElseGet(externalUrlSupplier::getRaw);
            if (isSiteLocal(encodedPermalink, externalUrl)) {
                return Mono.just(CachedThumbnails.siteLocal(ThumbnailUtils.buildSrcsetMap(encodedPermalink)));
            }
            return lookup(encodedPermalink);
        });
    }

    private Mono<CachedThumbnails> lookup(URI encodedPermalink) {
        return Mono.defer(() -> lookup(encodedPermalink, 0));
    }

    private Mono<CachedThumbnails> lookup(URI encodedPermalink, int attempt) {
        var permalinkString = encodedPermalink.toASCIIString();
        var cached = thumbnailCache.getIfPresent(permalinkString);
        if (cached != null) {
            return Mono.just(cached);
        }
        // query from attachments
        var listOptions = ListOptions.builder()
                .andQuery(Queries.equal("status.permalink", permalinkString))
                .andQuery(ExtensionUtil.notDeleting())
                .build();
        var permalinkVersion = changeVersion(permalinkString).get();
        var sequenceAtStart = changeSequence.get();
        return client.listAll(Attachment.class, listOptions, ExtensionUtil.defaultSort())
                .next()
                .flatMap(attachment -> {
                    var resolved = cacheIfCurrent(attachment, permalinkString, permalinkVersion, sequenceAtStart);
                    if (resolved != null) {
                        return Mono.just(resolved);
                    }
                    if (attempt + 1 < MAX_LOOKUP_ATTEMPTS) {
                        // A change raced with this query: retry instead of mistaking the result for "not found".
                        return lookup(encodedPermalink, attempt + 1);
                    }
                    return Mono.just(NOT_FOUND);
                })
                .switchIfEmpty(Mono.fromSupplier(() -> cacheNotFound(permalinkString, permalinkVersion)));
    }

    private CachedThumbnails cacheNotFound(String permalinkString, long version) {
        var current = thumbnailCache.asMap().compute(permalinkString, (key, existing) -> {
            if (existing != null || changeVersion(permalinkString).get() != version) {
                return existing;
            }
            return NOT_FOUND;
        });
        return current != null ? current : NOT_FOUND;
    }

    /**
     * Atomically checks the change versions and caches a query result. The check and the write must run under the same
     * attachment lock as the change events; otherwise a deletion or permalink change landing between them could
     * resurrect a stale trusted permalink. The requested key must still match the attachment's permalink, and its name
     * stripe must not have changed after this query started. This check remains effective after cache eviction or
     * deletion has removed the reverse association. A query started after a change may repair the cached permalink from
     * a newer database snapshot, including a status-only update with no change event.
     */
    @Nullable
    private CachedThumbnails cacheIfCurrent(
            Attachment attachment, String permalinkString, long permalinkVersion, long sequenceAtStart) {
        if (attachment.getStatus() == null || attachment.getMetadata() == null) {
            return null;
        }
        var name = attachment.getMetadata().getName();
        var permalink = attachment.getStatus().getPermalink();
        if (!StringUtils.hasText(name) || !StringUtils.hasText(permalink) || ExtensionUtil.isDeleted(attachment)) {
            return null;
        }
        synchronized (attachmentLock(name)) {
            if (changeVersion(permalinkString).get() != permalinkVersion
                    || attachmentChangeSequence(name).get() > sequenceAtStart
                    || !Objects.equals(permalink, permalinkString)) {
                // Stale query result or the index matched an outdated permalink: never cache it under this key.
                return null;
            }
            var indexedEntry = attachmentPermalinkIndex.get(name);
            var previousUri = indexedEntry != null ? indexedEntry.trustedPermalink() : null;
            if (previousUri != null && !Objects.equals(previousUri.toString(), permalink)) {
                var previousPermalink = previousUri.toString();
                changeVersion(previousPermalink).incrementAndGet();
                thumbnailCache.invalidate(previousPermalink);
            }
            return cacheAttachment(name, permalink, attachment.getStatus().getThumbnails());
        }
    }

    /**
     * Site-local images are relative references without an authority, or http(s) absolute URLs whose host and port
     * match the configured external URL. Scheme-relative URLs like {@code //evil.example/a.png} have a host while
     * {@link URI#isAbsolute()} is false, and URLs like {@code https:///evil.example/a.png} have a scheme but no
     * authority — both are treated as external so they can never become open redirects.
     */
    private static boolean isSiteLocal(URI uri, @Nullable URL externalUrl) {
        if (uri.getScheme() != null) {
            return isHttpScheme(uri.getScheme()) && externalUrl != null && isSameAuthority(externalUrl, uri);
        }
        // Reject protocol-relative references (//, ///) and backslash-based authority smuggling in the path. The
        // check is repeated for several percent-decoding rounds because a downstream decoder could otherwise turn an
        // encoded slash or backslash back into an authority separator.
        return isSafeRelativeReference(uri.toASCIIString());
    }

    private static boolean isSafeRelativeReference(String raw) {
        var value = raw;
        for (var i = 0; i < MAX_DECODE_ROUNDS; i++) {
            if (!isSafeRelativePrefix(value)) {
                return false;
            }
            var decoded = decodePercentEscapes(value);
            if (decoded.equals(value)) {
                return true;
            }
            value = decoded;
        }
        return isSafeRelativePrefix(value);
    }

    /**
     * Decodes only well-formed ASCII percent escapes and leaves everything else, including an isolated {@code %}
     * produced by an earlier decoding round, untouched. A decoder that rejected such a literal would misclassify a
     * legitimate file name like {@code 100%25.png} as malformed.
     */
    private static String decodePercentEscapes(String value) {
        var decoded = new StringBuilder(value.length());
        for (var i = 0; i < value.length(); i++) {
            var current = value.charAt(i);
            if (current == '%'
                    && i + 2 < value.length()
                    && isAsciiHexDigit(value.charAt(i + 1))
                    && isAsciiHexDigit(value.charAt(i + 2))) {
                decoded.append((char)
                        (Character.digit(value.charAt(i + 1), 16) * 16 + Character.digit(value.charAt(i + 2), 16)));
                i += 2;
            } else {
                decoded.append(current);
            }
        }
        return decoded.toString();
    }

    private static boolean isAsciiHexDigit(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    private static boolean isSafeRelativePrefix(String value) {
        // URL consumers remove ASCII tab and newlines anywhere and strip leading C0 control characters and spaces,
        // so none of them may hide an authority or a scheme from this check.
        var candidate = value.replace("\t", "").replace("\n", "").replace("\r", "");
        var start = 0;
        while (start < candidate.length() && candidate.charAt(start) <= 0x20) {
            start++;
        }
        candidate = candidate.substring(start);
        // A relative reference must not carry an authority (//), a backslash, or the "scheme:" prefix that would make
        // a downstream decoder interpret it as an absolute URI.
        return !candidate.startsWith("//")
                && candidate.indexOf('\\') < 0
                && !SCHEME_PREFIX.matcher(candidate).find();
    }

    private static boolean isHttpScheme(@Nullable String scheme) {
        return "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
    }

    private static boolean isSameAuthority(URL externalUrl, URI uri) {
        var externalHost = externalUrl.getHost();
        var host = uri.getHost();
        if (!StringUtils.hasText(externalHost) || !StringUtils.hasText(host) || !externalHost.equalsIgnoreCase(host)) {
            return false;
        }
        return effectivePort(externalUrl.getProtocol(), externalUrl.getPort())
                == effectivePort(uri.getScheme(), uri.getPort());
    }

    private static int effectivePort(@Nullable String scheme, int port) {
        if (port != -1) {
            return port;
        }
        return "https".equalsIgnoreCase(scheme) ? 443 : 80;
    }

    /**
     * Absolute http(s) URI with an authority, or a strictly site-local relative reference. Used to validate redirect
     * targets stored on attachments.
     */
    private static boolean isSafeRedirectTarget(@Nullable URI uri, @Nullable URL externalUrl) {
        return uri != null && (isSiteLocal(uri, externalUrl) || isSafeAbsoluteHttpUri(uri));
    }

    /** Absolute http(s) URI with an authority. */
    private static boolean isSafeAbsoluteHttpUri(@Nullable URI uri) {
        return uri != null && isHttpScheme(uri.getScheme()) && StringUtils.hasText(uri.getAuthority());
    }

    private boolean isThumbnailDisabled() {
        return haloProperties.getAttachment().getThumbnail().isDisabled();
    }

    private record CachedThumbnails(
            @Nullable String attachmentName,
            boolean attachmentPresent,
            @Nullable URI trustedPermalink,
            Map<ThumbnailSize, URI> thumbnails) {

        static CachedThumbnails siteLocal(Map<ThumbnailSize, URI> thumbnails) {
            return new CachedThumbnails(null, true, null, thumbnails);
        }

        static CachedThumbnails fromAttachment(
                String name, String permalink, @Nullable Map<String, String> rawThumbnails) {
            var trustedPermalink = URI.create(permalink);
            if (CollectionUtils.isEmpty(rawThumbnails)) {
                return new CachedThumbnails(name, true, trustedPermalink, EMPTY_THUMBNAILS);
            }
            Map<ThumbnailSize, URI> validThumbnails = new HashMap<>();
            rawThumbnails.forEach((key, value) -> {
                var size = ThumbnailSize.optionalValueOf(key);
                if (size.isPresent() && StringUtils.hasText(value)) {
                    validThumbnails.put(size.get(), URI.create(value));
                }
            });
            if (validThumbnails.isEmpty()) {
                return new CachedThumbnails(name, true, trustedPermalink, EMPTY_THUMBNAILS);
            }
            return new CachedThumbnails(name, true, trustedPermalink, Collections.unmodifiableMap(validThumbnails));
        }
    }
}
