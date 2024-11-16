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
                int compareResult = compareNumbers(a, b, i, j);
                if (compareResult != 0) {
                    return compareResult;
                }

                // Move indices past the compared number segments
                i = moveIndexToNextNonDigit(a, i);
                j = moveIndexToNextNonDigit(b, j);
            } else if (charA == charB) {
                // Characters are the same, continue
                i++;
                j++;
            } else if (Character.isDigit(charA)) {
                // If charA is digit and charB is not, digit comes first
                return -1;
            } else if (Character.isDigit(charB)) {
                // If charB is digit and charA is not, digit comes first
                return 1;
            } else {
                // Both are non-digits, compare directly
                return Character.compare(charA, charB);
            }
        }

        return Integer.compare(a.length(), b.length());
    }

    private int compareNumbers(String a, String b, int startA, int startB) {
        int i = startA;
        int j = startB;

        // Compare lengths of remaining digits
        int lengthA = countDigits(a, i);
        int lengthB = countDigits(b, j);
        if (lengthA != lengthB) {
            return Integer.compare(lengthA, lengthB);
        }

        // Compare digits one by one
        for (int k = 0; k < lengthA && i < a.length() && j < b.length(); k++, i++, j++) {
            char charA = a.charAt(i);
            char charB = b.charAt(j);
            if (charA != charB) {
                return Character.compare(charA, charB);
            }
        }

        // If both numbers have decimal points, compare decimal parts
        boolean hasDecimalA = i < a.length() && a.charAt(i) == '.';
        boolean hasDecimalB = j < b.length() && b.charAt(j) == '.';
        if (hasDecimalA || hasDecimalB) {
            return compareDecimalNumbers(a, b, i, j);
        }

        return 0;
    }

    private int compareDecimalNumbers(String a, String b, int startA, int startB) {
        // Find decimal point positions
        int pointA = a.indexOf('.', startA);
        int pointB = b.indexOf('.', startB);

        // Compare integer parts before the decimal point
        int integerComparison = compareIntegerPart(a, b, startA, startB, pointA, pointB);
        if (integerComparison != 0) {
            return integerComparison;
        }

        // Compare fractional parts after the decimal point
        return compareFractionalPart(a, b, pointA + 1, pointB + 1);
    }

    private int compareIntegerPart(String a, String b, int startA, int startB, int pointA,
        int pointB) {
        int i = startA;
        int j = startB;

        int lengthA = pointA - i;
        int lengthB = pointB - j;
        if (lengthA != lengthB) {
            return Integer.compare(lengthA, lengthB);
        }

        while (i < pointA && j < pointB) {
            char charA = a.charAt(i);
            char charB = b.charAt(j);
            if (charA != charB) {
                return Character.compare(charA, charB);
            }
            i++;
            j++;
        }

        return 0;
    }

    private int compareFractionalPart(String a, String b, int i, int j) {
        while (i < a.length() && j < b.length()
            && Character.isDigit(a.charAt(i)) && Character.isDigit(b.charAt(j))) {
            if (a.charAt(i) != b.charAt(j)) {
                return Character.compare(a.charAt(i), b.charAt(j));
            }
            i++;
            j++;
        }

        // If one number has more digits left, and they're not all zeroes, it is larger
        while (i < a.length() && Character.isDigit(a.charAt(i))) {
            if (a.charAt(i) != '0') {
                return 1;
            }
            i++;
        }
        while (j < b.length() && Character.isDigit(b.charAt(j))) {
            if (b.charAt(j) != '0') {
                return -1;
            }
            j++;
        }

        return 0;
    }

    private int countDigits(String s, int start) {
        int count = 0;
        while (start < s.length() && Character.isDigit(s.charAt(start))) {
            count++;
            start++;
        }
        return count;
    }

    private int moveIndexToNextNonDigit(String s, int index) {
        while (index < s.length() && (Character.isDigit(s.charAt(index))
            || s.charAt(index) == '.')) {
            index++;
        }
        return index;
    }
}
