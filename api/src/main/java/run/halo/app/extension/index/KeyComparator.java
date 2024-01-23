package run.halo.app.extension.index;

import java.util.Comparator;
import org.springframework.lang.Nullable;

public class KeyComparator implements Comparator<String> {
    public static final KeyComparator INSTANCE = new KeyComparator();

    @Override
    public int compare(@Nullable  String a, @Nullable String b) {
        if (a == null && b == null) {
            return 0;
        } else if (a == null) {
            // null less than everything
            return 1;
        } else if (b == null) {
            // null less than everything
            return -1;
        }

        int i = 0;
        int j = 0;
        while (i < a.length() && j < b.length()) {
            if (Character.isDigit(a.charAt(i)) && Character.isDigit(b.charAt(j))) {
                // handle number part
                int num1 = 0;
                int num2 = 0;
                while (i < a.length() && Character.isDigit(a.charAt(i))) {
                    num1 = num1 * 10 + (a.charAt(i++) - '0');
                }
                while (j < b.length() && Character.isDigit(b.charAt(j))) {
                    num2 = num2 * 10 + (b.charAt(j++) - '0');
                }
                if (num1 != num2) {
                    return num1 - num2;
                }
            } else if (a.charAt(i) != b.charAt(j)) {
                // handle non-number part
                return a.charAt(i) - b.charAt(j);
            } else {
                i++;
                j++;
            }
        }
        return a.length() - b.length();
    }
}
