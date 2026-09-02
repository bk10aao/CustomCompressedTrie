import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
from scipy.stats import gmean

df1 = pd.read_csv('CustomTrie_jmh_performance.csv', sep=';')
df2 = pd.read_csv('CustomCompressedTrie_jmh_performance.csv', sep=';')

v1_df = df1.copy()
v2_df = df2.copy()

v1_df['Size'] = pd.to_numeric(v1_df['Size'])
v2_df['Size'] = pd.to_numeric(v2_df['Size'])

v1_pivot = v1_df.set_index('Size')
v2_pivot = v2_df.set_index('Size')

common_sizes = sorted(list(set(v1_pivot.index).intersection(set(v2_pivot.index))))
v1_pivot = v1_pivot.loc[common_sizes]
v2_pivot = v2_pivot.loc[common_sizes]

DISPLAY_NAME_MAP = {
    'search(Hit)': 'search(Present Key)',
    'search(Miss)': 'search(Absent Key)',
    'search(Key Found)': 'search(Present Key)',
    'search(Key Not Found)': 'search(Absent Key)',
}

benchmarks = [b for b in v1_pivot.columns if b in v2_pivot.columns]

v1_fixed = v1_pivot.copy()
v2_fixed = v2_pivot.copy()

for b in benchmarks:
    v1_fixed[b] = pd.to_numeric(v1_fixed[b], errors='coerce').fillna(1).replace(0, 1)
    v2_fixed[b] = pd.to_numeric(v2_fixed[b], errors='coerce').fillna(1).replace(0, 1)

v1_win_color = '#E53E3E'   # Red for V1
v2_win_color = '#3B82F6'   # Blue for V2
tie_color    = '#888888'   # Gray for Tie

ratios = []
labels = []
colors = []

for b in benchmarks:
    v1_vals = v1_fixed[b].dropna()
    v2_vals = v2_fixed[b].dropna()
    if v1_vals.empty or v2_vals.empty:
        continue

    per_size_ratios = v2_vals / v1_vals
    g_ratio = gmean(per_size_ratios)

    if abs(g_ratio - 1.0) < 1e-4:
        ratios.append(0.0)
        colors.append(tie_color)
    elif g_ratio < 1.0:
        speedup = 1.0 / g_ratio
        ratios.append(speedup - 1)
        colors.append(v2_win_color)
    else:
        ratios.append(-(g_ratio - 1))
        colors.append(v1_win_color)

    clean_label = DISPLAY_NAME_MAP.get(b, b)
    labels.append(clean_label)

sorted_indices = np.argsort(ratios)
sorted_ratios = [ratios[idx] for idx in sorted_indices]
sorted_labels = [labels[idx] for idx in sorted_indices]
sorted_colors = [colors[idx] for idx in sorted_indices]

TEXT_COLOR = '#ffffff' # Unified text color for ALL text elements

fig_height = max(6.5, len(sorted_labels) * 0.45)

fig, ax = plt.subplots(figsize=(12, fig_height), facecolor='none')
ax.set_facecolor('none')

bars = ax.barh(
    range(len(sorted_labels)),
    sorted_ratios,
    color=sorted_colors,
    alpha=0.9,
    height=0.65,
)

ax.axvline(x=0, color=TEXT_COLOR, linewidth=1.4, zorder=1)

left_limit = -0.55
right_limit = 4.85
ax.set_xlim(left_limit, right_limit)

ticks = [-0.25, 0.0, 1.0, 2.0, 3.0, 4.0]
tick_labels = ['1.25x', 'Tie', '2x', '3x', '4x', '5x']

ax.set_xticks(ticks)
ax.set_xticklabels(tick_labels, color=TEXT_COLOR, fontsize=10, fontweight='bold')
ax.set_ylim(-0.5, len(sorted_labels) - 0.5)
ax.set_yticks(range(len(sorted_labels)))
ax.set_yticklabels(sorted_labels, color=TEXT_COLOR, fontsize=10)

for idx, r in enumerate(sorted_ratios):
    val = abs(r)
    text_str = 'Tie' if val < 0.02 else f'{val + 1:.2f}x'

    if r < 0:
        ax.text(r - 0.02, idx, f'{text_str} ', va='center', ha='right', color=TEXT_COLOR, fontsize=9, fontweight='bold')
    elif r > 0:
        ax.text(r + 0.05, idx, f' {text_str}', va='center', ha='left', color=TEXT_COLOR, fontsize=9, fontweight='bold')
    else:
        ax.text(0.02, idx, f' {text_str}', va='center', ha='left', color=TEXT_COLOR, fontsize=9, fontweight='bold')

ax.set_title(
    'Overall Relative Performance Comparison (CustomTrie V1 vs CustomCompressedTrie V2)\n'
    '(Geometric Mean Speedup Factor Across All Sizes)',
    fontsize=13, fontweight='bold', pad=15, color=TEXT_COLOR
)

ax.set_xlabel(
    '← V1 (Non-Compressed) Faster [Red]   |   Relative Speedup Factor   |   V2 (Compressed) Faster [Blue] →',
    fontsize=11, labelpad=10, color=TEXT_COLOR, fontweight='bold'
)

ax.grid(False)
ax.tick_params(colors=TEXT_COLOR, which='both', length=0)

for spine in ax.spines.values():
    spine.set_edgecolor('#555555')
    spine.set_linewidth(0.8)

plt.tight_layout()
plt.savefig('geometric.png', dpi=300, bbox_inches='tight', transparent=True)
plt.close()

print("Saved geometric.png")