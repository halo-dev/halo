package run.halo.app.extension.index;

import java.util.Comparator;
import org.springframework.lang.Nullable;

public class KeyComparator implements Comparator<String> {
    public static final KeyComparator INSTANCE = new KeyComparator();

    @Override
    public int compare(@Nullable String a, @Nullable String b) {
        if (a == null && b == null) {
            return 0;
        } else if (a == null) {
            // null less than everything
            return 1;
        } else if (b == null) {
            // null less than everything
            return -1;
        }
        return compareStrings(a, b);
    }

    private int compareStrings(String a, String b) {
        int i = 0;
        int j = 0;

        while (i < a.length() && j < b.length()) {
            char charA = a.charAt(i);
            char charB = b.charAt(j);

            if (Character.isDigit(charA) && Character.isDigit(charB)) {
                // Both characters are digits, compare as numbers
                int compareResult = compareNumberSegments(a, b, i, j);
                if (compareResult != 0) {
                    return compareResult;
                }

                // Move indices past the compared number segments
                i = skipNumberSegment(a, i);
                j = skipNumberSegment(b, j);
            } else if (charA == charB) {
                // Characters are the same, continue
                i++;
                j++;
            } else {
                // Different characters, compare directly
                return Character.compare(charA, charB);
            }
        }

        // If one string is a prefix of the other, the shorter one comes first
        return Integer.compare(a.length(), b.length());
    }

    private int compareNumberSegments(String a, String b, int startA, int startB) {
        // Extract pure number segments (no decimal handling)
        String numA = extractNumberSegment(a, startA);
        String numB = extractNumberSegment(b, startB);

        return compareIntegerSegments(numA, numB);
    }

    private String extractNumberSegment(String s, int start) {
        StringBuilder segment = new StringBuilder();
        int i = start;

        // Only extract continuous digits
        while (i < s.length() && Character.isDigit(s.charAt(i))) {
            segment.append(s.charAt(i));
            i++;
        }

        return segment.toString();
    }

    private int compareIntegerSegments(String a, String b) {
        // Remove leading zeros for comparison
        String trimmedA = a.replaceFirst("^0+", "");
        String trimmedB = b.replaceFirst("^0+", "");

        if (trimmedA.isEmpty()) {
            trimmedA = "0";
        }
        if (trimmedB.isEmpty()) {
            trimmedB = "0";
        }

        // Compare by length first (longer number is larger)
        if (trimmedA.length() != trimmedB.length()) {
            return Integer.compare(trimmedA.length(), trimmedB.length());
        }

        // Same length, compare lexicographically
        int comparison = trimmedA.compareTo(trimmedB);
        if (comparison != 0) {
            return comparison;
        }

        // If numbers are equal after removing leading zeros, compare original lengths
        // The one with fewer leading zeros (shorter original string) should come first
        return Integer.compare(a.length(), b.length());
    }

    private int skipNumberSegment(String s, int start) {
        int i = start;

        // Only skip continuous digits
        while (i < s.length() && Character.isDigit(s.charAt(i))) {
            i++;
        }

        return i;
    }
}
