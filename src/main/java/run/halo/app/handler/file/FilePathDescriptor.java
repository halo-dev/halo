package run.halo.app.handler.file;

import java.util.function.Predicate;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import run.halo.app.utils.FilenameUtils;
import run.halo.app.utils.HaloUtils;

/**
 * File path descriptor.
 *
 * @author guqing
 * @since 2021-10-21
 */
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
        private String path;
        private String basePath;
        private String nameSuffix = StringUtils.EMPTY;
        private String separator = "/";
        private boolean automaticRename;
        private Predicate<Builder> renamePredicate;

        public Builder setSubPath(String subPath) {
            this.path = StringUtils.removeEnd(subPath, separator);
            return this;
        }

        public Builder setAutomaticRename(Boolean automaticRename) {
            this.automaticRename = automaticRename != null && automaticRename;
            return this;
        }

        public Builder setRenamePredicate(Predicate<Builder> predicate) {
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
            this.basePath = basePath;
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

        String getName() {
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
                return getName() + '.' + this.extension;
            }
            return getName();
        }

        String getFullPath() {
            if (StringUtils.isNotBlank(this.basePath)) {
                return getPath(this.basePath, this.path, this.getFullName());
            }
            return getPath(this.path, this.getFullName());
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
            return renamePredicate.test(this);
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
                        if (sb.length() > 0) {
                            sb.append(separator);
                        }
                        sb.append(segment);
                    }
                }
                path = sb.toString();
            }
            return path;
        }

        /**
         * build file path object.
         *
         * @return file path
         */
        public FilePathDescriptor build() {
            return new FilePathDescriptor()
                .setBasePath(this.basePath)
                .setSubPath(this.path)
                .setRelativePath(getPath(this.path, getFullName()))
                .setName(FilenameUtils.getBasename(getName()))
                .setExtension(extension)
                .setFullPath(getFullPath())
                .setFullName(getFullName());
        }
    }
}
