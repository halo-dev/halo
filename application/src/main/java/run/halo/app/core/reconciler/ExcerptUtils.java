package run.halo.app.core.reconciler;

final class ExcerptUtils {

    private ExcerptUtils() {}

    static String substringByCodePoints(String value, int maxLength) {
        int endIndex = 0;
        for (int i = 0; i < maxLength && endIndex < value.length(); i++) {
            endIndex += Character.charCount(value.codePointAt(endIndex));
        }
        return value.substring(0, endIndex);
    }

    static boolean containsUnpairedSurrogate(String value) {
        if (value == null) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (Character.isHighSurrogate(current)) {
                if (i + 1 >= value.length() || !Character.isLowSurrogate(value.charAt(i + 1))) {
                    return true;
                }
                i++;
            } else if (Character.isLowSurrogate(current)) {
                return true;
            }
        }
        return false;
    }
}
