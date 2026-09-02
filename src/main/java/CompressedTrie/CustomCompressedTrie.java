package CompressedTrie;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Consumer;

import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;

/**
 * A concrete implementation of a <b>compressed trie</b> (also known as a radix trie
 * or Patricia-style trie), where each edge is labeled with a non-empty string
 * (rather than a single character), allowing chains of single-child nodes to be
 * collapsed into a single edge. This reduces the height of the tree and the number
 * of nodes compared to a classic character-by-character trie.
 * <p>
 * The trie supports the standard trie operations:
 * <ul>
 *     <li>{@link #insert(String)} &mdash; add a word to the trie</li>
 *     <li>{@link #delete(String)} &mdash; remove a word from the trie</li>
 *     <li>{@link #search(String)} &mdash; test whether an exact word exists in the trie</li>
 *     <li>{@link #startsWith(String)} &mdash; retrieve all words sharing a given prefix</li>
 * </ul>
 * <p>
 * Internally, each {@link CompressedTrieNode} stores a string {@code value} representing
 * the label of the edge leading into it, a flag marking whether a word ends at that node,
 * and a sorted map of children keyed by the first character of each child's edge label.
 * Edges are automatically split when a new word diverges partway through an existing edge,
 * and nodes are automatically merged ("compressed") when deletions leave a node with only
 * one child and no word terminating there.
 * <p>
 * This class is <b>not thread-safe</b>. External synchronization is required if instances
 * are accessed concurrently from multiple threads.
 */
public class CustomCompressedTrie implements CompressedTrie {

    /**
     * The root node of the trie. The root's own value is always the empty string.
     */
    private CompressedTrieNode root;

    /**
     * The number of distinct words currently stored in the trie.
     */
    private int size;

    /**
     * Constructs a new, empty compressed trie.
     */
    public CustomCompressedTrie() {
        this.root = new CompressedTrieNode("");
        this.size = 0;
    }

    /**
     * Constructs a new compressed trie containing a copy of every word stored in
     * the given trie. The resulting trie is independent of {@code trie}; subsequent
     * modifications to either instance do not affect the other.
     *
     * @param trie the trie whose words should be copied into this new trie
     * @throws NullPointerException if {@code trie} is {@code null}
     */
    public CustomCompressedTrie(final CustomCompressedTrie trie) {
        this();
        requireNonNull(trie);
        trie.startsWith("").forEach(this::insert);
    }

    /**
     * Constructs a new compressed trie containing every word in the given list.
     * Words that are {@code null} or blank (after trimming) are silently ignored.
     *
     * @param words the list of words to insert into the new trie
     * @throws NullPointerException if {@code words} is {@code null}
     */
    public CustomCompressedTrie(final List<String> words) {
        this();
        requireNonNull(words);
        words.forEach(this::insert);
    }

    /**
     * Constructs a new compressed trie containing every word in the given array.
     * Words that are {@code null} or blank (after trimming) are silently ignored.
     *
     * @param words the array of words to insert into the new trie
     * @throws NullPointerException if {@code words} is {@code null}
     */
    public CustomCompressedTrie(final String[] words) {
        this();
        requireNonNull(words);
        stream(words).forEach(this::insert);
    }

    /**
     * Removes all words from the trie, resetting it to an empty state.
     * This is equivalent to discarding the current root and starting over.
     */
    public void clear() {
        this.root = new CompressedTrieNode("");
        this.size = 0;
    }

    /**
     * Deletes the given word from the trie, if present.
     * <p>
     * The word is first trimmed and sanitized; {@code null} or blank input results
     * in no deletion. If the word exists in the trie, it is unmarked as an end-of-word
     * node, and any nodes that become unnecessary as a result (i.e. leaf nodes with no
     * word ending there, or nodes with a single remaining child) are pruned or merged
     * to keep the trie compressed.
     *
     * @param word the word to delete
     * @return {@code true} if the word was present and was successfully deleted,
     *         {@code false} otherwise
     */
    public boolean delete(final String word) {
        String cleanWord = sanitize(word);
        if (cleanWord == null)
            return false;
        DeletionResult result = deleteRecursive(root, cleanWord, 0);
        if (result.wordDeleted())
            size--;
        return result.wordDeleted();
    }

    /**
     * Compares this trie to another object for equality. Two tries are considered
     * equal if they have the same {@link #size()} and contain exactly the same set
     * of words (as returned by {@code startsWith("")}).
     *
     * @param o the object to compare against
     * @return {@code true} if {@code o} is a {@code CustomCompressedTrie} containing
     *         the same words as this trie, {@code false} otherwise
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CustomCompressedTrie that = (CustomCompressedTrie) o;
        return this.size == that.size && this.startsWith("").equals(that.startsWith(""));
    }

    /**
     * Returns a hash code consistent with {@link #equals(Object)}, derived from the
     * trie's size and its full set of words.
     *
     * @return the hash code for this trie
     */
    @Override
    public int hashCode() {
        return Objects.hash(size, startsWith(""));
    }

    /**
     * Inserts the given word into the trie.
     * <p>
     * The word is first trimmed and sanitized; {@code null} or blank input is silently
     * ignored. If the word (after trimming) already exists in the trie, this method has
     * no effect on {@link #size()}. Otherwise, edges are split and new nodes are created
     * as needed so that the trie remains correctly compressed after the insertion.
     *
     * @param word the word to insert
     */
    public void insert(final String word) {
        String cleanWord = sanitize(word);
        if (cleanWord != null && insertRecursive(root, cleanWord, 0))
            size++;
    }

    /**
     * Returns whether the trie currently contains no words.
     *
     * @return {@code true} if {@link #size()} is zero, {@code false} otherwise
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Tests whether the exact given word exists in the trie.
     * <p>
     * The word is first trimmed and sanitized; {@code null} or blank input always
     * returns {@code false}. A word is considered present only if traversal reaches
     * a node exactly at the end of an edge and that node is marked as an end-of-word.
     *
     * @param word the word to search for
     * @return {@code true} if the exact word is present in the trie, {@code false} otherwise
     */
    public boolean search(final String word) {
        String cleanWord = sanitize(word);
        if (cleanWord == null)
            return false;
        TraversalResult result = traverse(root, cleanWord, 0, new StringBuilder());
        return result != null && result.isExactMatch() && result.node().isEndOfWord();
    }

    /**
     * Returns the number of distinct words currently stored in the trie.
     *
     * @return the current word count
     */
    public int size() {
        return size;
    }

    /**
     * Returns all words in the trie that begin with the given prefix.
     * <p>
     * Passing an empty string as the prefix returns every word in the trie.
     * If {@code prefix} is {@code null}, or if no word in the trie starts with it,
     * an empty list is returned.
     *
     * @param prefix the prefix to search for; unlike {@link #insert(String)} and
     *               {@link #search(String)}, only leading/trailing whitespace is
     *               trimmed and blank strings are treated as a valid empty prefix
     * @return a list of all words sharing the given prefix, in trie traversal order;
     *         never {@code null}, but may be empty
     */
    public List<String> startsWith(final String prefix) {
        if (prefix == null)
            return List.of();
        String cleanPrefix = prefix.trim();
        TraversalResult result = traverse(root, cleanPrefix, 0, new StringBuilder());
        if (result == null || !result.isValidPrefixMatch())
            return List.of();
        List<String> results = new ArrayList<>();
        collectWords(result.node(), result.pathBuilder(), results);
        return results;
    }

    /**
     * Returns a string representation of this trie, including its size and the
     * full list of words it contains.
     *
     * @return a debug-friendly string representation of this trie
     */
    @Override
    public String toString() {
        return "CustomCompressedTrie{size=" + size + ", words=" + startsWith("") + "}";
    }

    /**
     * Recursively walks the subtree rooted at {@code node}, appending any complete
     * words found to {@code results}. The path accumulated so far is tracked via
     * {@code currentPath}, which is mutated and restored (backtracked) as the
     * recursion explores and then leaves each child.
     *
     * @param node        the node currently being visited
     * @param currentPath a mutable buffer holding the characters accumulated from
     *                    the root (or search starting point) down to {@code node}
     * @param results     the list to which complete words are appended
     */
    private void collectWords(final CompressedTrieNode node, final StringBuilder currentPath, final List<String> results) {
        if (node.isEndOfWord())
            results.add(currentPath.toString());
        node.forEachChild(child -> {
            int lengthBefore = currentPath.length();
            currentPath.append(child.getValue());
            collectWords(child, currentPath, results);
            currentPath.setLength(lengthBefore);
        });
    }

    /**
     * Recursively locates and deletes {@code word} within the subtree rooted at
     * {@code node}, starting the character comparison at {@code index} within
     * {@code word}. After removing an end-of-word marker (or a child made
     * unnecessary by a deeper deletion), the affected node is compressed via
     * {@link CompressedTrieNode#tryCompress(CompressedTrieNode)} to maintain the
     * trie's compressed invariant.
     *
     * @param node  the node currently being visited
     * @param word  the full word being deleted
     * @param index the current offset into {@code word} being matched against {@code node}
     * @return a {@link DeletionResult} indicating whether the word was actually deleted
     *         and whether {@code node} itself is now prunable (a childless, non-word leaf)
     */
    private DeletionResult deleteRecursive(final CompressedTrieNode node, final String word, final int index) {
        if (index == word.length()) {
            boolean deleted = node.unmarkEndOfWord();
            node.tryCompress(root);
            return new DeletionResult(deleted, node.isPrunable());
        }
        char currentChar = word.charAt(index);
        CompressedTrieNode child = node.getChild(currentChar);
        if (child == null)
            return new DeletionResult(false, false);
        int commonLength = child.commonPrefixLength(word, index);
        if (commonLength != child.getValue().length())
            return new DeletionResult(false, false);
        DeletionResult childResult = deleteRecursive(child, word, index + commonLength);
        if (childResult.shouldPruneNode())
            node.removeChild(currentChar);
        node.tryCompress(root);
        return new DeletionResult(childResult.wordDeleted(), node.isPrunable());
    }

    /**
     * Recursively inserts {@code word} into the subtree rooted at {@code node},
     * starting the character comparison at {@code index} within {@code word}.
     * <p>
     * If no child edge begins with the next character of {@code word}, a brand-new
     * child edge is created holding the remainder of the word. If a matching child
     * edge exists but only partially matches, that edge is split at the point of
     * divergence via {@link CompressedTrieNode#split(int)} before continuing.
     *
     * @param node  the node currently being visited
     * @param word  the full word being inserted
     * @param index the current offset into {@code word} being matched against {@code node}
     * @return {@code true} if this insertion added a brand-new word to the trie,
     *         {@code false} if the word was already present
     */
    private boolean insertRecursive(final CompressedTrieNode node, final String word, final int index) {
        if (index == word.length())
            return node.markAsEndOfWord();
        char currentChar = word.charAt(index);
        CompressedTrieNode child = node.getChild(currentChar);
        if (child == null) {
            node.addChild(word.substring(index), true);
            return true;
        }
        int commonLength = child.commonPrefixLength(word, index);
        if (commonLength < child.getValue().length())
            child.split(commonLength);
        int nextIndex = index + commonLength;
        if (nextIndex == word.length())
            return child.markAsEndOfWord();
        return insertRecursive(child, word, nextIndex);
    }

    /**
     * Normalizes an input word by trimming surrounding whitespace and rejecting
     * {@code null} or blank values.
     *
     * @param word the raw word to sanitize
     * @return the trimmed word, or {@code null} if {@code word} was {@code null}
     *         or consisted entirely of whitespace
     */
    private String sanitize(final String word) {
        if (word == null)
            return null;
        String trimmed = word.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    /**
     * Recursively traverses the trie from {@code node}, attempting to match
     * {@code text} starting at {@code index}, and accumulates the matched edge
     * labels into {@code path}.
     * <p>
     * Traversal stops successfully either when all of {@code text} has been consumed
     * (a valid prefix or exact match, depending on whether the match lands exactly on
     * a node boundary) or fails (returns {@code null}) when no child edge matches the
     * next character of {@code text}, or when the matched portion of an edge diverges
     * from {@code text} before either is exhausted.
     *
     * @param node  the node currently being visited
     * @param text  the word or prefix being matched
     * @param index the current offset into {@code text} being matched against {@code node}
     * @param path  a mutable buffer accumulating the edge labels traversed so far
     * @return a {@link TraversalResult} describing where traversal ended and whether
     *         it constitutes an exact match and/or a valid prefix match, or {@code null}
     *         if {@code text} is not present as a prefix in the trie
     */
    private TraversalResult traverse(final CompressedTrieNode node, final String text, final int index, final StringBuilder path) {
        if (index == text.length())
            return new TraversalResult(node, true, true, path);
        CompressedTrieNode child = node.getChild(text.charAt(index));
        if (child == null)
            return null;
        int commonLength = child.commonPrefixLength(text, index);
        path.append(child.getValue());
        if (commonLength == text.length() - index) {
            boolean exactEdgeMatch = commonLength == child.getValue().length();
            return new TraversalResult(child, exactEdgeMatch, true, path);
        }
        if (commonLength == child.getValue().length())
            return traverse(child, text, index + commonLength, path);
        return null;
    }

    /**
     * The outcome of a single step in the recursive deletion process.
     *
     * @param wordDeleted     {@code true} if this step actually removed a word's
     *                        end-of-word marker from the trie
     * @param shouldPruneNode {@code true} if the node processed at this step is now
     *                        empty (no children, not an end-of-word) and should be
     *                        detached from its parent
     */
    private record DeletionResult(boolean wordDeleted, boolean shouldPruneNode) {}

    /**
     * The outcome of a traversal attempt for a given word or prefix.
     *
     * @param node               the node where traversal terminated
     * @param isExactMatch       {@code true} if traversal ended precisely at a node
     *                           boundary (i.e. not partway through an edge label)
     * @param isValidPrefixMatch {@code true} if the searched text is a valid prefix
     *                           reachable within the trie (whether or not it ends
     *                           exactly on a node boundary)
     * @param pathBuilder        the accumulated edge labels from the traversal's
     *                           starting point down to {@code node}
     */
    private record TraversalResult(CompressedTrieNode node,
                                   boolean isExactMatch,
                                   boolean isValidPrefixMatch,
                                   StringBuilder pathBuilder) {}

    /**
     * A single node in the compressed trie.
     * <p>
     * Unlike a classic trie node, the label leading into a {@code CompressedTrieNode}
     * (stored in {@link #value}) may be a multi-character string, representing a
     * collapsed chain of single-child nodes. Children are indexed by the first
     * character of their respective edge labels, which guarantees that no two
     * sibling edges can begin with the same character.
     */
    private static class CompressedTrieNode {

        /**
         * Children of this node, keyed by the first character of each child's edge label.
         */
        private Map<Character, CompressedTrieNode> children = new TreeMap<>();

        /**
         * Whether a complete word ends at this node.
         */
        private boolean isEndOfWord;

        /**
         * The edge label leading into this node from its parent.
         */
        private String value;

        /**
         * Constructs a new node with the given edge label. The node is not marked
         * as an end-of-word and has no children.
         *
         * @param value the edge label leading into this node
         */
        public CompressedTrieNode(final String value) {
            this.value = value;
            this.isEndOfWord = false;
        }

        /**
         * Creates and attaches a new child node with the given edge label, indexed
         * by the label's first character. Any existing child sharing that first
         * character is overwritten.
         *
         * @param childValue the edge label for the new child
         * @param isEnd      whether the new child should be marked as an end-of-word
         */
        public void addChild(final String childValue, final boolean isEnd) {
            CompressedTrieNode child = new CompressedTrieNode(childValue);
            child.isEndOfWord = isEnd;
            children.put(childValue.charAt(0), child);
        }

        /**
         * Computes the length of the common prefix shared between this node's edge
         * label ({@link #value}) and the substring of {@code word} starting at
         * {@code offset}.
         *
         * @param word   the word being matched against this node's edge label
         * @param offset the starting index within {@code word} to compare from
         * @return the number of leading characters that match, which may be as small
         *         as zero or as large as the shorter of the two compared strings
         */
        public int commonPrefixLength(final String word, final int offset) {
            int maxLen = Math.min(word.length() - offset, value.length());
            int i = 0;
            while (i < maxLen && word.charAt(offset + i) == value.charAt(i))
                i++;
            return i;
        }

        /**
         * Applies the given action to each child of this node, in ascending order
         * of the child's key character (as maintained by the underlying {@link TreeMap}).
         *
         * @param action the action to perform on each child node
         */
        public void forEachChild(final Consumer<CompressedTrieNode> action) {
            children.values().forEach(action);
        }

        /**
         * Returns the child node whose edge label begins with the given character,
         * if one exists.
         *
         * @param ch the first character of the desired child's edge label
         * @return the matching child node, or {@code null} if no such child exists
         */
        public CompressedTrieNode getChild(final char ch) {
            return children.get(ch);
        }

        /**
         * Returns the edge label leading into this node.
         *
         * @return this node's edge label
         */
        public String getValue() {
            return value;
        }

        /**
         * Returns whether a complete word ends at this node.
         *
         * @return {@code true} if this node marks the end of a word, {@code false} otherwise
         */
        public boolean isEndOfWord() {
            return isEndOfWord;
        }

        /**
         * Returns whether this node is a candidate for pruning, i.e. it has no
         * children and does not mark the end of a word, making it structurally
         * unnecessary.
         *
         * @return {@code true} if this node can be safely removed from its parent
         */
        public boolean isPrunable() {
            return children.isEmpty() && !isEndOfWord;
        }

        /**
         * Marks this node as the end of a word, if it is not already.
         *
         * @return {@code true} if this call changed the node's state (i.e. it was
         *         not previously an end-of-word), {@code false} if it was already
         *         marked as an end-of-word
         */
        public boolean markAsEndOfWord() {
            if (!isEndOfWord) {
                isEndOfWord = true;
                return true;
            }
            return false;
        }

        /**
         * Detaches the child whose edge label begins with the given character.
         *
         * @param ch the first character of the child's edge label to remove
         */
        public void removeChild(final char ch) {
            children.remove(ch);
        }

        /**
         * Splits this node's edge label at {@code prefixLength}, inserting a new
         * intermediate node.
         * <p>
         * This node keeps only the first {@code prefixLength} characters of its
         * original label, becomes a non-word node, and gains a single new child
         * holding the remaining suffix of the original label. That new child
         * inherits this node's original end-of-word flag and all of its original
         * children, effectively becoming what this node used to represent.
         * <p>
         * This operation is used during insertion when a new word diverges from
         * an existing edge partway through its label.
         *
         * @param prefixLength the number of leading characters of the current edge
         *                     label to retain on this node before the split
         */
        public void split(final int prefixLength) {
            String commonPrefix = value.substring(0, prefixLength);
            String suffix = value.substring(prefixLength);
            CompressedTrieNode suffixNode = new CompressedTrieNode(suffix);
            suffixNode.isEndOfWord = this.isEndOfWord;
            suffixNode.children = this.children;
            this.value = commonPrefix;
            this.isEndOfWord = false;
            this.children = new TreeMap<>();
            this.children.put(suffix.charAt(0), suffixNode);
        }

        /**
         * Attempts to merge this node with its single child, restoring the trie's
         * compressed invariant after a deletion.
         * <p>
         * A merge is performed only if this node is not the trie's root, does not
         * itself mark the end of a word, and has exactly one child. In that case,
         * this node's edge label is extended with the child's label, this node
         * adopts the child's end-of-word flag and children, and the child is
         * effectively absorbed into this node.
         * <p>
         * If any of the above conditions do not hold, this method has no effect.
         *
         * @param root the root node of the trie, which is never itself compressed
         */
        public void tryCompress(final CompressedTrieNode root) {
            if (this == root || this.isEndOfWord || this.children.size() != 1)
                return;
            CompressedTrieNode singleChild = this.children.values().iterator().next();
            this.value = this.value + singleChild.value;
            this.isEndOfWord = singleChild.isEndOfWord;
            this.children = singleChild.children;
        }

        /**
         * Unmarks this node as the end of a word, if it currently is one.
         *
         * @return {@code true} if this call changed the node's state (i.e. it was
         *         previously an end-of-word), {@code false} if it was not marked
         *         as an end-of-word
         */
        public boolean unmarkEndOfWord() {
            if (isEndOfWord) {
                isEndOfWord = false;
                return true;
            }
            return false;
        }
    }
}