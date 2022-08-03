package run.halo.app.extension.router.selector;

import java.util.Set;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;

public enum Operator implements Converter<String, SelectorCriteria> {

    Equals("=", 3) {
        @Override
        @Nullable
        public SelectorCriteria convert(@Nullable String selector) {
            if (preFlightCheck(selector, 3)) {
                var i = selector.indexOf(getOperator());
                if (i > 0 && (i + getOperator().length()) < selector.length() - 1) {
                    String key = selector.substring(0, i);
                    String value = selector.substring(i + getOperator().length());
                    return new SelectorCriteria(key, this, Set.of(value));
                }
            }
            return null;
        }
    },
    IN("=(", 2) {
        @Override
        public SelectorCriteria convert(String selector) {
            if (preFlightCheck(selector, 5)) {
                var idx = selector.indexOf(getOperator());
                if (idx > 0 && (idx + getOperator().length()) < selector.length() - 2
                    && selector.charAt(selector.length() - 1) == ')') {
                    var key = selector.substring(0, idx);
                    var valuesString =
                        selector.substring(idx + getOperator().length(), selector.length() - 1);
                    String[] values = valuesString.split(",");
                    return new SelectorCriteria(key, this, Set.of(values));
                }
            }
            return null;
        }
    },
    NotEquals("!=", 1) {
        @Override
        @Nullable
        public SelectorCriteria convert(@Nullable String selector) {
            if (preFlightCheck(selector, 4)) {
                var i = selector.indexOf(getOperator());
                if (i > 0 && (i + getOperator().length()) < selector.length() - 1) {
                    String key = selector.substring(0, i);
                    String value = selector.substring(i + getOperator().length());
                    return new SelectorCriteria(key, this, Set.of(value));
                }
            }
            return null;
        }
    },
    NotExist("!", 0) {
        @Override
        @Nullable
        public SelectorCriteria convert(@Nullable String selector) {
            if (preFlightCheck(selector, 2)) {
                if (selector.startsWith(getOperator())) {
                    return new SelectorCriteria(selector.substring(1), this, Set.of());
                }
            }
            return null;
        }
    },
    Exist("", Integer.MAX_VALUE) {
        @Override
        public SelectorCriteria convert(String selector) {
            if (preFlightCheck(selector, 1)) {
                // TODO validate the source with regex in the future
                return new SelectorCriteria(selector, this, Set.of());
            }
            return null;
        }
    };
    private final String operator;

    /**
     * Parse order.
     */
    private final int order;

    Operator(String operator, int order) {
        this.operator = operator;
        this.order = order;
    }

    public String getOperator() {
        return operator;
    }

    public int getOrder() {
        return order;
    }

    protected boolean preFlightCheck(String selector, int minLength) {
        return selector != null && selector.length() >= minLength;
    }

}
