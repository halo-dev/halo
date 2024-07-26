package run.halo.app.infra;

import java.util.Set;

/**
 * <p>Classifies files based on their MIME types.</p>
 * <p>It provides different categories such as IMAGE, SVG, AUDIO, VIDEO, ARCHIVE, and DOCUMENT.
 * Each category has a <code>match</code> method that checks if a given MIME type belongs to that
 * category.</p>
 * <p>The categories are defined as follows:</p>
 * <pre>
 * - IMAGE: Matches all image MIME types except for SVG.
 * - SVG: Specifically matches the SVG image MIME type.
 * - AUDIO: Matches all audio MIME types.
 * - VIDEO: Matches all video MIME types.
 * - ARCHIVE: Matches common archive MIME types like zip, rar, tar, etc.
 * - DOCUMENT: Matches common document MIME types like plain text, PDF, Word, Excel, etc.
 * </pre>
 *
 * @author guqing
 * @since 2.18.0
 */
public enum FileCategoryMatcher {
    ALL {
        @Override
        public boolean match(String mimeType) {
            return true;
        }
    },
    IMAGE {
        @Override
        public boolean match(String mimeType) {
            return mimeType.startsWith("image/") && !mimeType.equals("image/svg+xml");
        }
    },
    SVG {
        @Override
        public boolean match(String mimeType) {
            return mimeType.equals("image/svg+xml");
        }
    },
    AUDIO {
        @Override
        public boolean match(String mimeType) {
            return mimeType.startsWith("audio/");
        }
    },
    VIDEO {
        @Override
        public boolean match(String mimeType) {
            return mimeType.startsWith("video/");
        }
    },
    ARCHIVE {
        static final Set<String> ARCHIVE_MIME_TYPES = Set.of(
            "application/zip",
            "application/x-rar-compressed",
            "application/x-tar",
            "application/gzip",
            "application/x-bzip2",
            "application/x-xz",
            "application/x-7z-compressed"
        );

        @Override
        public boolean match(String mimeType) {
            return ARCHIVE_MIME_TYPES.contains(mimeType);
        }
    },
    DOCUMENT {
        static final Set<String> DOCUMENT_MIME_TYPES = Set.of(
            "text/plain",
            "application/rtf",
            "text/csv",
            "text/xml",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/vnd.oasis.opendocument.text",
            "application/vnd.oasis.opendocument.spreadsheet",
            "application/vnd.oasis.opendocument.presentation"
        );

        @Override
        public boolean match(String mimeType) {
            return DOCUMENT_MIME_TYPES.contains(mimeType);
        }
    };

    public abstract boolean match(String mimeType);

    /**
     * Get the file category matcher by name.
     */
    public static FileCategoryMatcher of(String name) {
        for (var matcher : values()) {
            if (matcher.name().equalsIgnoreCase(name)) {
                return matcher;
            }
        }
        throw new IllegalArgumentException("Unsupported file category matcher for name: " + name);
    }
}
