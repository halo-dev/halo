package run.halo.app.theme.router;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.Data;

/**
 * Implementation for {@link RadixTree Radix tree}.
 *
 * @author guqing
 */
@Data
public class RadixTree<T> {
    protected RadixTreeNode<T> root;
    protected long size;

    /**
     * Create a Radix Tree with only the default node root.
     */
    public RadixTree() {
        root = new RadixTreeNode<T>();
        root.setKey("/");
        root.setPriority(0);
        root.setIndices("");
        size = 0;
    }

    /**
     * Find the node value with the given key.
     *
     * @param key the key to search
     * @return value of the node with the given key if found, otherwise null
     */
    public T find(String key) {
        Visitor<T, T> visitor = new Visitor<>() {
            public void visit(String key, RadixTreeNode<T> parent,
                RadixTreeNode<T> node) {
                if (node.isReal()) {
                    result = node.getValue();
                }
            }
        };
        visit(key, visitor);
        return visitor.getResult();
    }

    /**
     * Replace value by the given key.
     *
     * @param key the key to search
     * @param value the value to replace
     * @return {@code true} if replaced, otherwise {@code false}
     */
    public boolean replace(String key, final T value) {
        Visitor<T, T> visitor = new Visitor<>() {
            public void visit(String key, RadixTreeNode<T> parent, RadixTreeNode<T> node) {
                if (node.isReal()) {
                    node.setValue(value);
                    result = value;
                } else {
                    result = null;
                }
            }
        };
        visit(key, visitor);
        return visitor.getResult() != null;
    }

    /**
     * Delete the tree node with the given key.
     *
     * @param key the key to delete
     * @return @{code true} if deleted, otherwise {@code false}
     */
    public boolean delete(String key) {
        Visitor<T, Boolean> visitor = new Visitor<>(Boolean.FALSE) {
            public void visit(String key, RadixTreeNode<T> parent,
                RadixTreeNode<T> node) {
                result = node.isReal();
                // if it is a real node
                if (result) {
                    // If there are no children of the node we need to
                    // delete it from the parent children list
                    if (node.getChildren().size() == 0) {
                        Iterator<RadixTreeNode<T>> it = parent.getChildren()
                            .iterator();
                        for (int index = 0; it.hasNext(); index++) {
                            if (it.next().getKey().equals(node.getKey())) {
                                // delete node
                                it.remove();

                                // update indices and priority
                                parent.setPriority(parent.getPriority() - 1);
                                StringBuilder indices = new StringBuilder(parent.getIndices());
                                indices.deleteCharAt(index);
                                parent.setIndices(indices.toString());
                                break;
                            }
                        }

                        // if parent is not real node and has only one child
                        // then they need to be merged.
                        if (parent.getChildren().size() == 1
                            && !parent.isReal()) {
                            mergeNodes(parent, parent.getChildren().get(0));
                        }
                    } else if (node.getChildren().size() == 1) {
                        // we need to merge the only child of this node with
                        // itself
                        mergeNodes(node, node.getChildren().get(0));
                    } else { // we jus need to mark the node as non-real.
                        node.setReal(false);
                    }
                }
            }

            /**
             * Merge a child into its parent node. Operation only valid if it is
             * only child of the parent node and parent node is not a real node.
             *
             * @param parent The parent Node
             * @param child  The child Node
             */
            private void mergeNodes(RadixTreeNode<T> parent,
                RadixTreeNode<T> child) {
                parent.setKey(parent.getKey() + child.getKey());
                parent.setReal(child.isReal());
                parent.setValue(child.getValue());
                parent.setChildren(child.getChildren());
                parent.setIndices(child.getIndices());
                parent.setPriority(child.getPriority());
            }
        };
        visit(key, visitor);
        if (visitor.getResult()) {
            size--;
            calcRootPriority();
        }
        return visitor.getResult();
    }

    private void calcRootPriority() {
        int priority = root.isReal() ? 1 : 0;
        for (RadixTreeNode<T> child : root.getChildren()) {
            priority += child.getPriority();
        }
        root.setPriority(priority);
    }

    /**
     * Recursively insert the key in the radix tree.
     *
     * @see #insert(String, Object)
     */
    public void insert(String key, T value) throws IllegalArgumentException {
        try {
            insert(key, root, value);
            calcRootPriority();
        } catch (IllegalArgumentException e) {
            // re-throw the exception with 'key' in the message
            throw new IllegalArgumentException("A handle is already registered for key:" + key);
        }
        size++;
    }

    /**
     * Recursively insert the key in the radix tree.
     *
     * @param key The key to be inserted
     * @param node The current node
     * @param value The value associated with the key
     * @throws IllegalArgumentException If the key already exists in the database.
     */
    private void insert(String key, RadixTreeNode<T> node, T value)
        throws IllegalArgumentException {
        int numberOfMatchingCharacters = node.getNumberOfMatchingCharacters(key);
        // we are either at the root node
        // or we need to go down the tree
        if (node.getKey().equals("") || numberOfMatchingCharacters == 0
            || (numberOfMatchingCharacters < key.length()
            && numberOfMatchingCharacters >= node.getKey().length())) {
            boolean flag = false;
            String newText = key.substring(numberOfMatchingCharacters);

            // 递归查找插入位置
            char idxc = newText.charAt(0);
            for (int i = 0; i < node.getIndices().length(); i++) {
                if (node.getIndices().charAt(i) == idxc) {
                    int pos = incrementChildPriority(node, i);
                    RadixTreeNode<T> child = node.getChildren().get(pos);
                    flag = true;
                    insert(newText, child, value);
                    break;
                }
            }
            // just add the node as the child of the current node
            if (!flag) {
                RadixTreeNode<T> n = new RadixTreeNode<T>();
                n.setKey(newText);
                n.setReal(true);
                n.setPriority(1);
                n.setValue(value);
                // 往后追加与child对于的首字母到 indices
                node.setIndices(node.getIndices() + idxc);
                node.getChildren().add(n);
            }
        } else if (numberOfMatchingCharacters == key.length()
            && numberOfMatchingCharacters == node.getKey().length()) {
            // there is an exact match just make the current node as data node
            if (node.isReal()) {
                throw new IllegalArgumentException("Duplicate key.");
            }
            node.setReal(true);
            node.setValue(value);
        } else if (numberOfMatchingCharacters > 0 && numberOfMatchingCharacters < node.getKey()
            .length()) {
            // This node need to be split as the key to be inserted
            // is a prefix of the current node key
            RadixTreeNode<T> n1 = new RadixTreeNode<>();
            n1.setKey(node.getKey().substring(numberOfMatchingCharacters));
            n1.setReal(node.isReal());
            n1.setValue(node.getValue());
            n1.setPriority(node.getPriority() - 1);
            n1.setIndices(node.getIndices());
            n1.setChildren(node.getChildren());

            node.setKey(key.substring(0, numberOfMatchingCharacters));
            node.setReal(false);
            node.setChildren(new ArrayList<>());
            node.getChildren().add(n1);
            node.setIndices("");
            node.setPriority(n1.getPriority());
            // 往后追加与child对于的首字母到 indices
            node.setIndices(node.getIndices() + n1.getKey().charAt(0));
            // 新公共前缀比原公共前缀短，需要将当前的节点按公共前缀分开
            if (numberOfMatchingCharacters < key.length()) {
                RadixTreeNode<T> n2 = new RadixTreeNode<>();
                n2.setKey(key.substring(numberOfMatchingCharacters));
                n2.setReal(true);
                n2.setValue(value);
                n2.setPriority(1);

                node.getChildren().add(n2);
                node.setPriority(node.getPriority() + 1);
                node.setIndices(node.getIndices() + n2.getKey().charAt(0));
            } else {
                node.setValue(value);
                node.setReal(true);
                node.setPriority(node.getPriority() + 1);
            }
        } else {
            // this key need to be added as the child of the current node
            RadixTreeNode<T> n = new RadixTreeNode<T>();
            n.setKey(node.getKey().substring(numberOfMatchingCharacters));
            n.setChildren(node.getChildren());
            n.setPriority(node.getPriority());
            incrementChildPriority(n, n.getIndices().length() - 1);
            n.setReal(node.isReal());
            n.setValue(node.getValue());

            node.setKey(key);
            node.setReal(true);
            node.setValue(value);
            node.getChildren().add(n);
            char idxc = node.getKey().charAt(0);
            // 往后追加与child对于的首字母到 indices
            n.setIndices(n.getIndices() + idxc);
            incrementChildPriority(node, node.getIndices().length() - 1);
        }
    }

    int incrementChildPriority(RadixTreeNode<T> n, int pos) {
        List<RadixTreeNode<T>> cs = n.getChildren();
        cs.get(pos).priority++;
        int prio = cs.get(pos).priority;

        // Adjust position (move to front)
        int newPos = pos;
        for (; newPos > 0 && cs.get(newPos - 1).priority < prio; newPos--) {
            // Swap node positions
            RadixTreeNode<T> temp = cs.get(newPos);
            cs.set(newPos, cs.get(newPos - 1));
            cs.set(newPos - 1, temp);
        }

        // Build new index char string
        if (newPos != pos) {
            n.indices = n.indices.substring(0, newPos) // Unchanged prefix, might be empty
                + n.indices.charAt(pos)// The index char we move
                + n.indices.substring(newPos, pos)
                + n.indices.substring(pos + 1); // Rest without char at 'pos'
        }

        return newPos;
    }

    /**
     * The tree contains the key.
     *
     * @param key the key to search
     * @return {@code true} if the tree contains the key, otherwise {@code false}
     */
    public boolean contains(String key) {
        Visitor<T, Boolean> visitor = new Visitor<>(Boolean.FALSE) {
            public void visit(String key, RadixTreeNode<T> parent,
                RadixTreeNode<T> node) {
                result = node.isReal();
            }
        };
        visit(key, visitor);
        return visitor.getResult();
    }

    /**
     * visit the node those key matches the given key.
     *
     * @param key The key that need to be visited
     * @param visitor The visitor object
     */
    public <R> void visit(String key, Visitor<T, R> visitor) {
        if (root != null) {
            visit(key, visitor, null, root);
        }
    }

    /**
     * recursively visit the tree based on the supplied "key". calls the Visitor
     * for the node those key matches the given prefix.
     *
     * @param prefix The key of prefix to search in the tree
     * @param visitor The Visitor that will be called if a node with "key" as its key is found
     * @param node The Node from where onward to search
     */
    private <R> void visit(String prefix, Visitor<T, R> visitor,
        RadixTreeNode<T> parent, RadixTreeNode<T> node) {
        int numberOfMatchingCharacters = node.getNumberOfMatchingCharacters(prefix);
        // if the node key and prefix match, we found a match!
        if (numberOfMatchingCharacters == prefix.length()
            && numberOfMatchingCharacters == node.getKey().length()) {
            visitor.visit(prefix, parent, node);
        } else if (node.getKey().equals("") // either we are at the
            // root
            || (numberOfMatchingCharacters < prefix.length()
            && numberOfMatchingCharacters >= node.getKey().length())) {
            // OR we need to traverse the children
            String newText = prefix.substring(numberOfMatchingCharacters);
            for (RadixTreeNode<T> child : node.getChildren()) {
                // recursively search the child nodes
                if (child.getKey().startsWith(newText.charAt(0) + "")) {
                    visit(newText, visitor, node, child);
                    break;
                }
            }
        }
    }

    public long getSize() {
        return size;
    }

    /**
     * <p>Display the Trie on console.</p>
     * WARNING! Do not use this for a large Trie, it's for testing purpose only.
     */
    @Deprecated
    public String display() {
        StringBuilder buffer = new StringBuilder();
        root.print(buffer, "", "");
        return buffer.toString();
    }

    /**
     * Only used for testing purpose.
     */
    public void checkIndices() {
        this.checkIndices(root);
    }

    void checkIndices(RadixTreeNode<T> node) {
        if (node == null) {
            //base condition
            return;
        }
        System.out.println(node.getKey());
        node.checkIndices();
        for (RadixTreeNode<T> child : node.getChildren()) {
            checkIndices(child);
        }
    }

    /**
     * Only used for testing purpose.
     */
    public void checkPriorities() {
        checkPriorities(root);
    }

    /**
     * Check the correctness of priority in node of the tree.
     *
     * @param n node to check
     * @return priority in the node {@param #n}
     */
    public int checkPriorities(RadixTreeNode<T> n) {
        int prio = 0;
        for (RadixTreeNode<T> child : n.getChildren()) {
            prio += checkPriorities(child);
        }

        if (n.isReal()) {
            prio++;
        }

        if (n.getPriority() != prio) {
            throw new IllegalStateException(
                String.format("priority mismatch for node '%s': is %d, should be %d", n.getKey(),
                    n.getPriority(), prio));
        }
        return prio;
    }

    public abstract static class Visitor<T, R> {

        protected R result;

        public Visitor() {
            this.result = null;
        }

        public Visitor(R initialValue) {
            this.result = initialValue;
        }

        public R getResult() {
            return result;
        }

        public abstract void visit(String key, RadixTreeNode<T> parent, RadixTreeNode<T> node);
    }
}
