# Custom Compressed Trie

An implementation of a compressed Prefix-Trie.

*For the standard, non-compressed implementation, see [Custom Trie](https://github.com/bk10aao/CustomTrie).*

# Build and Test

To build and test the project run command `./gradlew clean build`

# Time Complexity

| Method                      |                        V1                        |                 V2 (Compressed)                  |  Winner  |
|:----------------------------|:------------------------------------------------:|:------------------------------------------------:|:--------:|
| `Constructor()`             |                      $O(1)$                      |                      $O(1)$                      | **Tie**  |
| `Constructor(List<String>)` |                $O(W \log \Sigma)$                |                $O(W \log \Sigma)$                | **Tie**  |
| `Constructor(String[])`     |                $O(W \log \Sigma)$                |                $O(W \log \Sigma)$                | **Tie**  |
| `Constructor(Trie)`         |                $O(S \log \Sigma)$                |                $O(S \log \Sigma)$                | **Tie**  |
| `clear()`                   |                      $O(1)$                      |                      $O(1)$                      | **Tie**  |
| `delete(String)`            |                $O(L \log \Sigma)$                |                $O(L \log \Sigma)$                | **Tie**  |
| `equals(Object)`            |                $O(S \log \Sigma)$                |                $O(S \log \Sigma)$                | **Tie**  |
| `hashCode()`                |                $O(S \log \Sigma)$                |                $O(S \log \Sigma)$                | **Tie**  |
| `insert(String)`            |                $O(L \log \Sigma)$                |                $O(L \log \Sigma)$                | **Tie**  |
| `isEmpty()`                 |                      $O(1)$                      |                      $O(1)$                      | **Tie**  |
| `search(String)`            |                $O(L \log \Sigma)$                |                $O(L \log \Sigma)$                | **Tie**  |
| `size()`                    |                      $O(1)$                      |                      $O(1)$                      | **Tie**  |
| `startsWith(String)`        | $O(P \log \Sigma + L_{\text{out}} \log \Sigma)$  | $O(P \log \Sigma + L_{\text{out}} \log \Sigma)$  | **Tie**  |
| `toString()`                |                $O(S \log \Sigma)$                |                $O(S \log \Sigma)$                | **Tie**  |

# Space Complexity

| Method                               |               V1               |        V2 (Compressed)         | Winner  |
|:-------------------------------------|:------------------------------:|:------------------------------:|:-------:|
| `Constructor()`                      |             $O(1)$             |             $O(1)$             | **Tie** |
| `Constructor(List<String>)`          |             $O(1)$             |         $O(L_{\max})$          | **V1**  |
| `Constructor(String[])`              |             $O(1)$             |         $O(L_{\max})$          | **V1**  |
| `Constructor(Trie)`                  |             $O(S)$             |             $O(S)$             | **Tie** |
| `clear()`                            |             $O(1)$             |             $O(1)$             | **Tie** |
| `delete(String)`                     |             $O(L)$             |             $O(L)$             | **Tie** |
| `equals(Object)`                     |             $O(S)$             |             $O(S)$             | **Tie** |
| `hashCode()`                         |             $O(S)$             |             $O(S)$             | **Tie** |
| `insert(String)`                     |             $O(1)$             |             $O(L)$             | **V1**  |
| `isEmpty()`                          |             $O(1)$             |             $O(1)$             | **Tie** |
| `search(String)`                     |             $O(1)$             |             $O(L)$             | **V1**  |
| `size()`                             |             $O(1)$             |             $O(1)$             | **Tie** |
| `startsWith(String)`                 | $O(L_{\text{out}} + L_{\max})$ | $O(L_{\text{out}} + L_{\max})$ | **Tie** |
| `toString()`                         |             $O(S)$             |             $O(S)$             | **Tie** |

### Notes & Variable Definitions
- **$L$**: Length of the target word / input string parameter.
- **$P$**: Length of the search prefix.
- **$\Sigma$**: Alphabet size / maximum branching factor.
- **$W$**: Total character count across an input list or array ($\sum L_i$).
- **$S$**: Total character count across all words stored in the trie ($\sum L_i$).
- **$N$**: Total number of distinct words stored in the trie.
- **$L_{\text{out}}$**: Total character count of all matching words returned by prefix matching.
- **$L_{\max}$**: Maximum word length in the collection.

# Performance

Below performance is a comparison made at 100,000 operations per method.

Note: all data is evaluated at 100,000 operations scale.

| Method                  | Non-compressed Trie (ns) | Compressed Trie (ns) |            Winner            | Margin |
|:------------------------|:-------------------------|:---------------------|:----------------------------:|:------:|
| `Constructor()`         | 4                        | 5                    | **Statistically Equivalent** | 1.25x  |
| `Constructor(Trie)`     | 37,037,991               | 14,252,349           |            **V2**            | 2.60x  |
| `Constructor(List)`     | 31,221,635               | 25,719,680           |            **V2**            | 1.21x  |
| `Constructor(String[])` | 32,054,071               | 27,426,442           |            **V2**            | 1.17x  |
| `clear()`               | 6,487                    | 886                  |            **V2**            | 7.32x  |
| `delete(String)`        | 4,050                    | 8,564                |            **V1**            | 2.11x  |
| `equals(Object)`        | 50,091,344               | 10,526,838           |            **V2**            | 4.76x  |
| `hashCode()`            | 26,603,385               | 6,195,431            |            **V2**            | 4.29x  |
| `insert(String)`        | 35,099,565               | 28,190,140           |            **V2**            | 1.25x  |
| `isEmpty()`             | 1                        | 1                    | **Statistically Equivalent** | 1.00x  |
| `search(Key Found)`     | 85                       | 49                   |            **V2**            | 1.73x  |
| `search(Key Not Found)` | 6                        | 6                    | **Statistically Equivalent** | 1.00x  |
| `size()`                | 1                        | 1                    | **Statistically Equivalent** | 1.00x  |
| `startsWith(String)`    | 246                      | 122                  |            **V2**            | 2.02x  |
| `toString()`            | 26,229,529               | 5,749,411            |            **V2**            | 4.56x  |

# Performance Charts

#### Note: The following performance charts are designed to be viewed in dark mode.
![geometric_performance.png](PerformanceCharts/geometric.png)
![heatmap](PerformanceCharts/heatmap.png)
![constructor.png](PerformanceCharts/constructor.png)
![constructor_collection.png](PerformanceCharts/constructor_array.png)
![constructor_int.png](PerformanceCharts/constructor_list.png)
![constructor_int_float.png](PerformanceCharts/copy_constructor.png)
![add.png](PerformanceCharts/clear.png)
![addAll.png](PerformanceCharts/delete.png)
![clear.png](PerformanceCharts/equals.png)
![clone.png](PerformanceCharts/hashCode.png)
![contains.png](PerformanceCharts/insert.png)
![containsAll.png](PerformanceCharts/isEmpty.png)
![equals.png](PerformanceCharts/search_hit.png)
![hashCode.png](PerformanceCharts/search_miss.png)
![isEmpty.png](PerformanceCharts/size.png)
![iterator.png](PerformanceCharts/startsWith.png)
