package run.halo.app.theme.router;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * Represents a node of a Radix tree {@link RadixTree}.
 *
 * @param <T> value type
 * @author guqing
 */
@Data
public class RadixTreeNode<T> {
    private String key;
    private List<RadixTreeNode<T>> children;
    private boolean real;
    private T value;

    protected String indices;

    /**
     * intailize the fields with default values to avoid null reference checks
     * all over the places.
     */
    public RadixTreeNode() {
        key = "";
        children = new ArrayList<>();
        real = false;
        indices = "";
    }

    protected int getNumberOfMatchingCharacters(String key) {
        int numberOfMatchingCharacters = 0;
        while (numberOfMatchingCharacters < key.length()
            && numberOfMatchingCharacters < this.getKey().length()) {
            if (key.charAt(numberOfMatchingCharacters) != this.getKey()
                .charAt(numberOfMatchingCharacters)) {
                break;
            }
            numberOfMatchingCharacters++;
        }
        return numberOfMatchingCharacters;
    }

    void print(StringBuilder buffer, String prefix, String childrenPrefix) {
        buffer.append(prefix);
        buffer.append(printNode());
        buffer.append('\n');
        for (Iterator<RadixTreeNode<T>> it = children.iterator(); it.hasNext(); ) {
            RadixTreeNode<T> next = it.next();
            if (it.hasNext()) {
                next.print(buffer, childrenPrefix + "├── ", childrenPrefix + "│   ");
            } else {
                next.print(buffer, childrenPrefix + "└── ", childrenPrefix + "    ");
            }
        }
    }

    String printNode() {
        if (isReal()) {
            return String.format("%s [value=%s]*", getKey(), getValue());
        } else {
            return String.format("%s [indices=%s]", getKey(), getIndices());
        }
    }

    /**
     * Check whether {@link #indices} matches the {@link #children} items prefix.
     */
    public void checkIndices() {
        StringBuilder indices = new StringBuilder();
        for (RadixTreeNode<T> child : this.getChildren()) {
            indices.append(child.getKey().charAt(0));
        }

        if (!StringUtils.equals(this.getIndices(), indices.toString())) {
            throw new IllegalStateException(
                String.format("indices mismatch for node '%s': is %s, should be %s", this.getKey(),
                    this.getIndices(), indices));
        }
    }
}
