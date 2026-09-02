package CompressedTrie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CustomCompressedTrieTest {

    private CustomCompressedTrie trie;

    @BeforeEach
    void setUp() {
        trie = new CustomCompressedTrie();
    }

    @Test
    @DisplayName("no-arg constructor should create an empty trie")
    void testNoArgConstructorCreatesEmptyTrie() {
        CustomCompressedTrie empty = new CustomCompressedTrie();
        assertTrue(empty.isEmpty());
        assertEquals(0, empty.size());
        assertEquals(List.of(), empty.startsWith(""));
    }

    @Test
    @DisplayName("List constructor should insert all given words")
    void testListConstructorInsertsWords() {
        CustomCompressedTrie t = new CustomCompressedTrie(List.of("cat", "dog", "car"));
        assertEquals(3, t.size());
        assertTrue(t.search("cat"));
        assertTrue(t.search("dog"));
        assertTrue(t.search("car"));
        assertEquals(List.of("car", "cat", "dog"), t.startsWith(""));
    }

    @Test
    @DisplayName("List constructor should apply the same sanitization rules as insert")
    void testListConstructorSanitizesAndIgnoresInvalidWords() {
        CustomCompressedTrie t = new CustomCompressedTrie(Arrays.asList("  cat  ", "", "   ", "dog"));
        assertEquals(2, t.size());
        assertTrue(t.search("cat"));
        assertTrue(t.search("dog"));
    }

    @Test
    @DisplayName("List constructor should create an empty trie when given an empty list")
    void testListConstructorWithEmptyList() {
        CustomCompressedTrie t = new CustomCompressedTrie(List.of());
        assertTrue(t.isEmpty());
    }

    @Test
    @DisplayName("List constructor should throw NullPointerException for a null list")
    void testListConstructorThrowsOnNullList() {
        assertThrows(NullPointerException.class, () -> new CustomCompressedTrie((List<String>) null));
    }

    @Test
    @DisplayName("Array constructor should insert all given words")
    void testArrayConstructorInsertsWords() {
        CustomCompressedTrie t = new CustomCompressedTrie(new String[] {"cat", "dog", "car"});
        assertEquals(3, t.size());
        assertTrue(t.search("cat"));
        assertTrue(t.search("dog"));
        assertTrue(t.search("car"));
        assertEquals(List.of("car", "cat", "dog"), t.startsWith(""));
    }

    @Test
    @DisplayName("Array constructor should apply the same sanitization rules as insert")
    void testArrayConstructorSanitizesAndIgnoresInvalidWords() {
        CustomCompressedTrie t = new CustomCompressedTrie(new String[] {"  cat  ", "", "   ", "dog"});
        assertEquals(2, t.size());
        assertTrue(t.search("cat"));
        assertTrue(t.search("dog"));
    }

    @Test
    @DisplayName("Array constructor should create an empty trie when given an empty array")
    void testArrayConstructorWithEmptyArray() {
        CustomCompressedTrie t = new CustomCompressedTrie(new String[0]);
        assertTrue(t.isEmpty());
    }

    @Test
    @DisplayName("Array constructor should throw NullPointerException for a null array")
    void testArrayConstructorThrowsOnNullArray() {
        assertThrows(NullPointerException.class, () -> new CustomCompressedTrie((String[]) null));
    }

    @Test
    @DisplayName("Copy constructor should produce a trie equal to the original")
    void testCopyConstructorProducesEqualTrie() {
        CustomCompressedTrie original = new CustomCompressedTrie(List.of("cat", "dog"));
        CustomCompressedTrie copy = new CustomCompressedTrie(original);
        assertEquals(original, copy);
        assertEquals(original.size(), copy.size());
        assertEquals(original.startsWith(""), copy.startsWith(""));
    }

    @Test
    @DisplayName("Copy constructor should create an independent trie")
    void testCopyConstructorIsIndependent() {
        CustomCompressedTrie original = new CustomCompressedTrie(List.of("cat", "dog"));
        CustomCompressedTrie copy = new CustomCompressedTrie(original);

        copy.insert("bird");
        assertFalse(original.search("bird"));
        assertEquals(2, original.size());
        assertEquals(3, copy.size());

        original.delete("cat");
        assertTrue(copy.search("cat"));
        assertEquals(1, original.size());
    }

    @Test
    @DisplayName("Copy constructor should work correctly on an empty trie")
    void testCopyConstructorOnEmptyTrie() {
        CustomCompressedTrie original = new CustomCompressedTrie();
        CustomCompressedTrie copy = new CustomCompressedTrie(original);
        assertTrue(copy.isEmpty());
        assertEquals(original, copy);
    }

    @Test
    @DisplayName("Copy constructor should throw NullPointerException for a null trie")
    void testCopyConstructorThrowsOnNullTrie() {
        assertThrows(NullPointerException.class, () -> new CustomCompressedTrie((CustomCompressedTrie) null));
    }

    @Test
    @DisplayName("Should return true when searching for inserted word 'hello'")
    public void givenWord_hello_onSearchFor_hello_returnsTrue() {
        CustomCompressedTrie CustomCompressedTrie = new CustomCompressedTrie();
        CustomCompressedTrie.insert("hello");
        assertTrue(CustomCompressedTrie.search("hello"));
    }

    @Test
    @DisplayName("Should return false when searching for non-existent word 'world'")
    public void givenWord_hello_onSearchFor_world_returnsFalse() {
        CustomCompressedTrie CustomCompressedTrie = new CustomCompressedTrie();
        CustomCompressedTrie.insert("hello");
        assertFalse(CustomCompressedTrie.search("world"));
    }

    @Test
    @DisplayName("Should return false when searching for prefix 'hell' that is not a complete word")
    public void givenWord_hello_onSearchFor_hell_returnsFalse() {
        CustomCompressedTrie CustomCompressedTrie = new CustomCompressedTrie();
        CustomCompressedTrie.insert("hello");
        assertFalse(CustomCompressedTrie.search("hell"));
    }

    @Test
    @DisplayName("Should return true when searching for second inserted word 'help'")
    public void givenWord_hello_and_help_onSearchFor_help_returnsTrue() {
        CustomCompressedTrie CustomCompressedTrie = new CustomCompressedTrie();
        CustomCompressedTrie.insert("hello");
        CustomCompressedTrie.insert("help");

        assertTrue(CustomCompressedTrie.search("help"));
    }

    @Test
    @DisplayName("Should return true when searching for each of multiple inserted words")
    public void givenWord_hello__help_dog_onSearchForEachWord_returnsTrue() {
        CustomCompressedTrie CustomCompressedTrie = new CustomCompressedTrie();
        CustomCompressedTrie.insert("hello");
        CustomCompressedTrie.insert("help");
        CustomCompressedTrie.insert("dog");

        assertTrue(CustomCompressedTrie.search("help"));
        assertTrue(CustomCompressedTrie.search("hello"));
        assertTrue(CustomCompressedTrie.search("dog"));
    }

    @Test
    @DisplayName("Should return false when searching for null")
    void testSearchNull() {
        assertFalse(trie.search(null));
    }

    @Test
    @DisplayName("Should return false when searching for empty string")
    void testSearchEmpty() {
        assertFalse(trie.search(""));
    }

    @Test
    @DisplayName("Should return false when first character is not in trie")
    void testSearchMissingFirstChar() {
        trie.insert("hello");
        assertFalse(trie.search("world"));
    }

    @Test
    @DisplayName("Should return false when middle character is not in trie path")
    void testSearchMissingMiddleChar() {
        trie.insert("hello");
        assertFalse(trie.search("hex"));
    }

    @Test
    @DisplayName("Should return false when prefix exists but is not marked as end of word")
    void testSearchPrefixOnlyNotWord() {
        trie.insert("hello");
        assertFalse(trie.search("hel"));
    }

    @Test
    @DisplayName("Should return matching words 'hello' and 'help' for prefix 'hel'")
    public void givenWord_hello_help_dog_onSearchForStartingWith_hel_returns_hello_help() {
        CustomCompressedTrie CustomCompressedTrie = new CustomCompressedTrie();
        CustomCompressedTrie.insert("hello");
        CustomCompressedTrie.insert("help");
        CustomCompressedTrie.insert("dog");
        List<String> results = CustomCompressedTrie.startsWith("hel");
        assertEquals(2, results.size());
        assertEquals("hello", results.get(0));
        assertEquals("help", results.get(1));
    }

    @Test
    @DisplayName("Should return empty list when prefix is null")
    void testStartsWithNull() {
        List<String> results = trie.startsWith(null);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Should return empty list when prefix is empty")
    void testStartsWithEmpty() {
        List<String> results = trie.startsWith("");
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Should return empty list when root character does not match prefix")
    void testStartsWithMissingFirstChar() {
        trie.insert("hello");
        List<String> results = trie.startsWith("z");
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Should return empty list when intermediate character in prefix is missing")
    void testStartsWithMissingMiddleChar() {
        trie.insert("hello");
        List<String> results = trie.startsWith("hex");
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Should return alphabetically sorted matching words and navigate recursive subtrees")
    void testStartsWithMultipleMatchesSorted() {
        trie.insert("cater");
        trie.insert("cat");
        trie.insert("cart");
        trie.insert("dog");

        List<String> results = trie.startsWith("ca");

        assertEquals(3, results.size());
        assertEquals("cart", results.get(0));
        assertEquals("cat", results.get(1));
        assertEquals("cater", results.get(2));
    }

    @Test
    @DisplayName("Should include prefix itself if prefix is also a valid word")
    void testStartsWithPrefixIsAWord() {
        trie.insert("car");
        trie.insert("card");
        trie.insert("care");

        List<String> results = trie.startsWith("car");

        assertEquals(3, results.size());
        assertEquals("car", results.get(0));
        assertEquals("card", results.get(1));
        assertEquals("care", results.get(2));
    }

    @Test
    @DisplayName("Should insert a multi-character word successfully")
    void testInsertMultiCharWord() {
        trie.insert("hello");
        assertTrue(trie.search("hello"));
        assertEquals(1, trie.size());
        assertFalse(trie.isEmpty());
    }

    @Test
    @DisplayName("Should insert a single-character word successfully")
    void testInsertSingleCharWord() {
        trie.insert("a");
        assertTrue(trie.search("a"));
        assertEquals(1, trie.size());
    }

    @Test
    @DisplayName("Should trim whitespace before inserting")
    void testInsertTrimsWhitespace() {
        trie.insert("  world  ");
        assertTrue(trie.search("world"));
        assertTrue(trie.search("  world  "));
        assertEquals(1, trie.size());
    }

    @Test
    @DisplayName("Should return immediately when inserting null")
    void testInsertNull() {
        trie.insert(null);
        assertEquals(0, trie.size());
        assertTrue(trie.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "\t", "\n"})
    @DisplayName("Should return immediately for empty or blank strings")
    void testInsertEmptyOrBlank(String word) {
        trie.insert(word);
        assertEquals(0, trie.size());
        assertTrue(trie.isEmpty());
    }

    @Test
    @DisplayName("Should not increment size when inserting duplicate words")
    void testInsertDuplicate() {
        trie.insert("java");
        trie.insert("java");
        assertEquals(1, trie.size());
        assertTrue(trie.search("java"));
    }

    @Test
    @DisplayName("Should correctly insert words that share prefixes")
    void testInsertSharedPrefix() {
        trie.insert("car");
        trie.insert("card");

        assertEquals(2, trie.size());
        assertTrue(trie.search("car"));
        assertTrue(trie.search("card"));
        assertFalse(trie.search("ca"));
    }

    @Test
    @DisplayName("Should reset size to zero and remove all words when clear is called")
    void testClear() {
        trie.insert("apple");
        trie.insert("banana");

        trie.clear();

        assertEquals(0, trie.size());
        assertTrue(trie.isEmpty());
        assertFalse(trie.search("apple"));
        assertFalse(trie.search("banana"));
    }

    @Test
    @DisplayName("Should return true for isEmpty on a newly initialized trie")
    void testIsEmptyTrueWhenNew() {
        assertTrue(trie.isEmpty());
    }

    @Test
    @DisplayName("Should return false for isEmpty after inserting a word")
    void testIsEmptyFalseAfterInsert() {
        trie.insert("hello");
        assertFalse(trie.isEmpty());
    }

    @Test
    @DisplayName("Should return 0 for size on a newly initialized trie")
    void testSizeZeroWhenNew() {
        assertEquals(0, trie.size());
    }

    @Test
    @DisplayName("Should accurately report size incrementing as unique words are added")
    void testSizeIncrementsCorrectly() {
        assertEquals(0, trie.size());
        trie.insert("cat");
        assertEquals(1, trie.size());
        trie.insert("dog");
        assertEquals(2, trie.size());
    }

    @Test
    @DisplayName("Should return false when attempting to delete null")
    void testDeleteNull() {
        assertFalse(trie.delete(null));
    }

    @Test
    @DisplayName("Should return false when attempting to delete empty or blank strings")
    void testDeleteEmptyOrBlank() {
        assertFalse(trie.delete(""));
        assertFalse(trie.delete("   "));
    }

    @Test
    @DisplayName("Should return false when deleting a word not present in the trie")
    void testDeleteNonExistentWord() {
        trie.insert("hello");
        assertFalse(trie.delete("world"));
    }

    @Test
    @DisplayName("Should return false when deleting a prefix of an inserted word that is not marked as a word")
    void testDeletePrefixThatIsNotAWord() {
        trie.insert("hello");
        assertFalse(trie.delete("hel"));
        assertTrue(trie.search("hello"));
        assertEquals(1, trie.size());
    }

    @Test
    @DisplayName("Should successfully delete an existing word and update size")
    void testDeleteExistingWord() {
        trie.insert("hello");
        assertTrue(trie.delete("hello"));
        assertFalse(trie.search("hello"));
        assertEquals(0, trie.size());
        assertTrue(trie.isEmpty());
    }

    @Test
    @DisplayName("Should delete a word with trailing/leading spaces after trimming")
    void testDeleteTrimsWhitespace() {
        trie.insert("world");
        assertTrue(trie.delete("  world  "));
        assertFalse(trie.search("world"));
        assertEquals(0, trie.size());
    }

    @Test
    @DisplayName("Should delete a longer word without removing shared prefix word")
    void testDeleteLongerWordPreservesPrefix() {
        trie.insert("car");
        trie.insert("card");

        assertTrue(trie.delete("card"));
        assertFalse(trie.search("card"));
        assertTrue(trie.search("car"));
        assertEquals(1, trie.size());
    }

    @Test
    @DisplayName("Should delete a prefix word without removing longer extending words")
    void testDeletePrefixPreservesLongerWord() {
        trie.insert("car");
        trie.insert("card");

        assertTrue(trie.delete("car"));
        assertFalse(trie.search("car"));
        assertTrue(trie.search("card"));
        assertEquals(1, trie.size());
    }

    @Test
    @DisplayName("toString should show size 0 and empty words list when trie is empty")
    void testToStringWhenEmpty() {
        assertEquals("CustomCompressedTrie{size=0, words=[]}", trie.toString());
    }

    @Test
    @DisplayName("toString should show size 1 and the single inserted word")
    void testToStringWithOneWord() {
        trie.insert("cat");
        assertEquals("CustomCompressedTrie{size=1, words=[cat]}", trie.toString());
    }

    @Test
    @DisplayName("toString should show correct size and all words in lexicographic order")
    void testToStringWithMultipleWords() {
        trie.insert("cat");
        trie.insert("car");
        trie.insert("dog");
        assertEquals("CustomCompressedTrie{size=3, words=[car, cat, dog]}", trie.toString());
    }

    @Test
    @DisplayName("toString should reflect updated size and words after a deletion")
    void testToStringAfterDelete() {
        trie.insert("cat");
        trie.insert("car");
        trie.delete("cat");
        assertEquals("CustomCompressedTrie{size=1, words=[car]}", trie.toString());
    }

    @Test
    @DisplayName("hashCode should be consistent across repeated calls with no mutation")
    void testHashCodeConsistentAcrossCalls() {
        trie.insert("cat");
        trie.insert("dog");
        int first = trie.hashCode();
        int second = trie.hashCode();
        assertEquals(first, second);
    }

    @Test
    @DisplayName("hashCode should match for two tries with the same contents")
    void testHashCodeEqualForEqualTries() {
        CustomCompressedTrie other = new CustomCompressedTrie();
        trie.insert("cat");
        trie.insert("dog");
        other.insert("cat");
        other.insert("dog");
        assertEquals(trie, other);
        assertEquals(trie.hashCode(), other.hashCode());
    }

    @Test
    @DisplayName("hashCode should differ for tries with different contents")
    void testHashCodeDiffersForDifferentTries() {
        CustomCompressedTrie other = new CustomCompressedTrie();
        trie.insert("cat");
        other.insert("dog");
        assertNotEquals(trie.hashCode(), other.hashCode());
    }

    @Test
    @DisplayName("hashCode should change after a word is inserted")
    void testHashCodeChangesAfterInsert() {
        trie.insert("cat");
        int before = trie.hashCode();
        trie.insert("dog");
        int after = trie.hashCode();
        assertNotEquals(before, after);
    }

    @Test
    @DisplayName("hashCode should change after a word is deleted")
    void testHashCodeChangesAfterDelete() {
        trie.insert("cat");
        trie.insert("dog");
        int before = trie.hashCode();
        trie.delete("dog");
        int after = trie.hashCode();
        assertNotEquals(before, after);
    }

    @Test
    @DisplayName("hashCode should be stable and reproducible for an empty trie")
    void testHashCodeForEmptyTrie() {
        CustomCompressedTrie other = new CustomCompressedTrie();
        assertEquals(trie.hashCode(), other.hashCode());
    }

    @Test
    @DisplayName("equals should return true when compared to itself")
    void testEqualsReflexive() {
        trie.insert("cat");
        assertEquals(trie, trie);
    }

    @Test
    @DisplayName("equals should return false when compared to null")
    void testEqualsNull() {
        trie.insert("cat");
        assertFalse(trie.equals(null));
    }

    @Test
    @DisplayName("equals should return false when compared to a different type")
    void testEqualsDifferentType() {
        trie.insert("cat");
        assertNotEquals("cat", trie);
    }

    @Test
    @DisplayName("equals should return true for two empty tries")
    void testEqualsBothEmpty() {
        CustomCompressedTrie other = new CustomCompressedTrie();
        assertEquals(trie, other);
    }

    @Test
    @DisplayName("equals should return true for tries with identical contents")
    void testEqualsSameContents() {
        CustomCompressedTrie other = new CustomCompressedTrie();
        trie.insert("cat");
        trie.insert("dog");
        other.insert("cat");
        other.insert("dog");
        assertEquals(trie, other);
    }

    @Test
    @DisplayName("equals should be symmetric regardless of insertion order")
    void testEqualsSymmetricDifferentInsertionOrder() {
        CustomCompressedTrie other = new CustomCompressedTrie();
        trie.insert("cat");
        trie.insert("dog");
        other.insert("dog");
        other.insert("cat");
        assertEquals(trie, other);
        assertEquals(other, trie);
    }

    @Test
    @DisplayName("equals should return false when sizes differ")
    void testEqualsDifferentSize() {
        CustomCompressedTrie other = new CustomCompressedTrie();
        trie.insert("cat");
        trie.insert("dog");
        other.insert("cat");
        assertNotEquals(trie, other);
    }

    @Test
    @DisplayName("equals should return false when sizes match but words differ")
    void testEqualsSameSizeDifferentWords() {
        CustomCompressedTrie other = new CustomCompressedTrie();
        trie.insert("cat");
        other.insert("dog");
        assertNotEquals(trie, other);
    }

    @Test
    @DisplayName("equals should return false after one trie has a word deleted")
    void testEqualsAfterDeleteBreaksEquality() {
        CustomCompressedTrie other = new CustomCompressedTrie();
        trie.insert("cat");
        trie.insert("dog");
        other.insert("cat");
        other.insert("dog");
        assertEquals(trie, other);
        other.delete("dog");
        assertNotEquals(trie, other);
    }

    @Test
    @DisplayName("equals should be consistent across repeated calls with no mutation")
    void testEqualsConsistent() {
        CustomCompressedTrie other = new CustomCompressedTrie();
        trie.insert("cat");
        other.insert("cat");
        assertEquals(trie, other);
        assertEquals(trie, other);
    }
}