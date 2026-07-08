package run.halo.app.core.attachment;

import static run.halo.app.extension.index.query.Queries.in;

import java.net.URI;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ReactiveExtensionClient;

@Component
@RequiredArgsConstructor
public class AttachmentPermalinkMatcher {

    private static final Set<String> SUPPORTED_ABSOLUTE_URI_SCHEMES = Set.of("http", "https");

    private final ReactiveExtensionClient client;

    public Mono<List<AttachmentPermalinkMatchResult>> match(List<String> urls, URL siteUrl) {
        var candidates = createCandidates(urls, siteUrl);
        var uniqueCandidates = candidates.stream()
                .flatMap(candidate -> candidate.permalinks().stream())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        var listOptions = ListOptions.builder()
                .andQuery(in("status.permalink", uniqueCandidates))
                .build();
        return client.listAll(Attachment.class, listOptions, Sort.unsorted())
                .mapNotNull(AttachmentPermalinkMatcher::getPermalink)
                .collect(Collectors.toSet())
                .map(matchedPermalinks -> candidates.stream()
                        .map(candidate -> new AttachmentPermalinkMatchResult(
                                candidate.url(), candidate.matches(matchedPermalinks)))
                        .toList());
    }

    private static List<Candidate> createCandidates(List<String> urls, URL siteUrl) {
        if (urls == null || urls.isEmpty()) {
            throw new ServerWebInputException("urls must not be empty.");
        }
        return urls.stream().map(url -> createCandidate(url, siteUrl)).toList();
    }

    private static Candidate createCandidate(String url, URL siteUrl) {
        if (!StringUtils.hasText(url)) {
            throw new ServerWebInputException("url must not be blank.");
        }
        var value = url.strip();

        URI valueUri;
        try {
            valueUri = URI.create(value);
        } catch (IllegalArgumentException e) {
            var permalinks = new LinkedHashSet<String>();
            permalinks.add(value);
            return new Candidate(url, permalinks);
        }
        if (isUnsupportedAbsoluteUri(valueUri)) {
            throw new ServerWebInputException("Unsupported URL protocol: " + value);
        }

        var permalinks = new LinkedHashSet<String>();
        permalinks.add(value);
        var siteUri = URI.create(siteUrl.toString());
        if (valueUri.isAbsolute()) {
            if (sameAuthority(siteUri, valueUri)) {
                permalinks.add(pathAndQuery(valueUri));
            }
        } else {
            permalinks.add(siteUri.resolve(valueUri).normalize().toString());
        }
        return new Candidate(url, permalinks);
    }

    private static boolean isUnsupportedAbsoluteUri(URI uri) {
        if (!uri.isAbsolute()) {
            return false;
        }
        var scheme = uri.getScheme().toLowerCase(Locale.ROOT);
        return !SUPPORTED_ABSOLUTE_URI_SCHEMES.contains(scheme);
    }

    private static boolean sameAuthority(URI siteUri, URI valueUri) {
        return siteUri.getAuthority() != null && siteUri.getAuthority().equals(valueUri.getAuthority());
    }

    private static String pathAndQuery(URI uri) {
        var path = uri.getRawPath();
        if (!StringUtils.hasText(path)) {
            path = "/";
        }
        var query = uri.getRawQuery();
        return query == null ? path : path + "?" + query;
    }

    private static String getPermalink(Attachment attachment) {
        var status = attachment.getStatus();
        return status == null ? null : status.getPermalink();
    }

    private record Candidate(String url, Set<String> permalinks) {
        boolean matches(Set<String> matchedPermalinks) {
            return permalinks.stream().anyMatch(matchedPermalinks::contains);
        }
    }
}
