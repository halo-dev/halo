package run.halo.app.handler.file;

import java.util.function.Predicate;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import run.halo.app.utils.FilenameUtils;

/**
 * File path descriptor.
 *
 * @author guqing
 * @since 2021-10-21
 */
@Slf4j
@Data
@Accessors(chain = true)
public final class FilePathDescriptor {

    private String name;
    private String extension;
    private String relativePath;
    private String basePath;
    private String fullName;
    private String fullPath;
    private String subPath;

    public static final class Builder {

        private String name;
        private String extension;
        private String subPath;
        private String basePath;
        private String nameSuffix = StringUtils.EMPTY;
        private String separator = "/";
        private boolean automaticRename;
        private Predicate<String> renamePredicate;
        private String relativePath;

        public Builder setSubPath(String subPath) {
            this.subPath = StringUtils.removeEnd(subPath, separator);
            return this;
        }

        public Builder setAutomaticRename(Boolean automaticRename) {
            this.automaticRename = automaticRename != null && automaticRename;
            return this;
        }

        public Builder setRenamePredicate(Predicate<String> predicate) {
            this.renamePredicate = predicate;
            return this;
        }

        /**
         * Set path separator, <code>NULL</code> value is not allowed.
         *
         * @param separator path separator
         * @return builder
         */
        public Builder setSeparator(String separator) {
            if (separator == null) {
                throw new IllegalArgumentException("The separator must not be null.");
            }
            this.separator = separator;
            return this;
        }

        /**
         * Set original file name.
         *
         * @param originalFileName original file name
         * @return file path builder
         */
        public Builder setOriginalName(String originalFileName) {
            Assert.notNull(originalFileName, "The originalFileName must not be null.");
            this.name = FilenameUtils.getBasename(originalFileName);
            this.extension = FilenameUtils.getExtension(originalFileName);
            return this;
        }

        public Builder setBasePath(String basePath) {
            this.basePath = StringUtils.removeEnd(basePath, separator);
            return this;
        }

        /**
         * Set file base name suffix.
         *
         * @param nameSuffix file base name suffix
         * @return builder
         */
        public Builder setNameSuffix(String nameSuffix) {
            if (nameSuffix == null) {
                nameSuffix = StringUtils.EMPTY;
            }
            this.nameSuffix = nameSuffix;
            return this;
        }

        String buildName() {
            StringBuilder sb = new StringBuilder()
                .append(this.name);
            if (shouldRename()) {
                String timestamp = String.valueOf(System.currentTimeMillis());
                sb.append('-').append(timestamp);
            }
            sb.append(this.nameSuffix);
            return sb.toString();
        }

        String getFullName() {
            // eg. hello.jpg -> hello-uuid-thumbnail.jpg
            if (StringUtils.isNotBlank(this.extension)) {
                return this.name + '.' + this.extension;
            }
            return this.name;
        }

        String getFullPath() {
            if (StringUtils.isNotBlank(this.basePath)) {
                return getPath(this.basePath, this.subPath, this.getFullName());
            }
            return getPath(this.subPath, this.getFullName());
        }

        String getRelativePath() {
            return getPath(this.subPath, getFullName());
        }

        private boolean shouldRename() {
            if (!automaticRename) {
                return false;
            }
            // automaticRename is true
            if (renamePredicate == null) {
                return true;
            }
            // renamePredicate not null
            return renamePredicate.test(this.relativePath);
        }

        private String getPath(String first, String... more) {
            String path;
            if (more.length == 0) {
                path = first;
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append(first);
                for (String segment : more) {
                    if (StringUtils.isNotBlank(segment)) {
                        if (sb.length() > 0 && !endsWith(sb, separator)) {
                            sb.append(separator);
                        }
                        sb.append(segment);
                    }
                }
                path = sb.toString();
            }
            return path;
        }

        static boolean endsWith(StringBuilder sb, String str) {
            Assert.notNull(sb, "The stringBuilder must not be null.");
            Assert.notNull(str, "The str must not be null.");
            int len = sb.length();
            int strLen = str.length();
            return (len >= strLen && sb.substring(len - strLen).equals(str));
        }

        /**
         * build file path object.
         *
         * @return file path
         */
        public FilePathDescriptor build() {
            // build relative path first, used to determine if it needs to be renamed
            this.relativePath = getRelativePath();
            // then build name, returns a new name if the relative path exists
            this.name = buildName();

            FilePathDescriptor descriptor = new FilePathDescriptor()
                .setBasePath(this.basePath)
                .setSubPath(this.subPath)
                // regenerate relative path
                .setRelativePath(getRelativePath())
                .setName(this.name)
                .setExtension(extension)
                .setFullPath(getFullPath())
                .setFullName(getFullName());
            log.info("FilePathDescriptor: [{}]", descriptor);
            return descriptor;
        }
    }
}
