#!/usr/bin/env python3
"""
Generate a performance comparison matrix heatmap between Compressed (V2) and Non-Compressed (V1) Trie implementations.
Dynamically extracts all benchmark methods from wide-format JMH CSV files and applies standard method display labels.
"""

import matplotlib

matplotlib.use('Agg')
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import seaborn as sns
import io
import os
import sys

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
OUTPUT_HEATMAP_PATH = os.path.join(SCRIPT_DIR, 'heatmap.png')

DEFAULT_NON_COMPRESSED_CSV = os.path.join(SCRIPT_DIR, 'CustomTrie_jmh_performance_3.csv')
DEFAULT_COMPRESSED_CSV = os.path.join(SCRIPT_DIR, 'CustomCompressedTrie_jmh_performance_3.csv')

ALT_NON_COMPRESSED_CSVS = ['CustomTrie_jmh_performance_3.csv', 'CustomTrie_jmh_performance_2.csv', 'CustomTrie_jmh_matrix.csv', 'CustomTrie_jmh_performance.csv']
ALT_COMPRESSED_CSVS = ['CustomCompressedTrie_jmh_performance_3.csv', 'CustomCompressedTrie_jmh_performance_2.csv', 'CustomCompressedTrie_jmh_matrix.csv', 'CustomCompressedTrie_jmh_performance.csv']

DISPLAY_NAME_MAP = {
    'search(Hit)': 'search(Present Key)',
    'search(Miss)': 'search(Absent Key)',
    'search(Key Found)': 'search(Present Key)',
    'search(Key Not Found)': 'search(Absent Key)',
    'SearchHit': 'search(Present Key)',
    'SearchMiss': 'search(Absent Key)',
}


def resolve_file_path(default_path, alt_filenames):
    """Resolve file path checking default path and script directory fallbacks."""
    if os.path.exists(default_path):
        return default_path
    for alt in alt_filenames:
        alt_path = os.path.join(SCRIPT_DIR, alt)
        if os.path.exists(alt_path):
            return alt_path
    return default_path


def load_wide_jmh_csv(filepath):
    """Load wide-format JMH CSV robustly regardless of delimiter."""
    with open(filepath, 'r') as f:
        lines = [line.strip() for line in f if line.strip()]
    sample_line = lines[1] if len(lines) > 1 else lines[0]
    sep = ';' if ';' in sample_line else ','
    df = pd.read_csv(io.StringIO('\n'.join(lines)), sep=sep)
    df.columns = [c.strip() for c in df.columns]
    return df


def get_canonical_name(col_name):
    """Normalize raw CSV column name into clean benchmark display title."""
    if 'Constructor(' in col_name and col_name not in ('Constructor()', 'Constructor(String[])', 'Constructor(List)'):
        return 'Constructor(Trie)'
    return DISPLAY_NAME_MAP.get(col_name, col_name)


def build_column_mappings(df1, df2):
    """Dynamically map all benchmark method columns across both datasets."""
    map1, map2 = {}, {}

    cols1 = [c for c in df1.columns if c != 'Size']
    cols2 = [c for c in df2.columns if c != 'Size']

    for c in cols1:
        canonical = get_canonical_name(c)
        map1[canonical] = c

    for c in cols2:
        canonical = get_canonical_name(c)
        map2[canonical] = c

    all_keys = list(dict.fromkeys(list(map1.keys()) + [k for k in map2.keys() if k not in map1]))
    return map1, map2, all_keys


def main():
    non_compressed_csv_path = resolve_file_path(DEFAULT_NON_COMPRESSED_CSV, ALT_NON_COMPRESSED_CSVS)
    compressed_csv_path = resolve_file_path(DEFAULT_COMPRESSED_CSV, ALT_COMPRESSED_CSVS)

    if not os.path.exists(non_compressed_csv_path):
        print(f"Error: Non-Compressed CSV not found at '{non_compressed_csv_path}'.")
        sys.exit(1)
    if not os.path.exists(compressed_csv_path):
        print(f"Error: Compressed CSV not found at '{compressed_csv_path}'.")
        sys.exit(1)

    non_comp_df = load_wide_jmh_csv(non_compressed_csv_path)
    comp_df = load_wide_jmh_csv(compressed_csv_path)

    non_comp_df['Size'] = pd.to_numeric(non_comp_df['Size'])
    comp_df['Size'] = pd.to_numeric(comp_df['Size'])

    sizes = sorted(list(set(non_comp_df['Size']).intersection(set(comp_df['Size']))))
    map_non_comp, map_comp, method_keys = build_column_mappings(non_comp_df, comp_df)

    heatmap_data = np.zeros((len(method_keys), len(sizes)))
    text_labels = []

    for i, m_key in enumerate(method_keys):
        row_labels = []
        non_comp_col = map_non_comp.get(m_key)
        comp_col = map_comp.get(m_key)

        for j, size in enumerate(sizes):
            non_comp_vals = non_comp_df.loc[non_comp_df['Size'] == size, non_comp_col].values if non_comp_col and non_comp_col in non_comp_df.columns else []
            comp_vals = comp_df.loc[comp_df['Size'] == size, comp_col].values if comp_col and comp_col in comp_df.columns else []

            non_comp_val = float(non_comp_vals[0]) if len(non_comp_vals) > 0 and pd.notna(non_comp_vals[0]) else 1.0
            comp_val = float(comp_vals[0]) if len(comp_vals) > 0 and pd.notna(comp_vals[0]) else 1.0

            if non_comp_val <= 0: non_comp_val = 1.0
            if comp_val <= 0: comp_val = 1.0

            ratio = np.log2(non_comp_val / comp_val)
            heatmap_data[i, j] = ratio

            if non_comp_val >= comp_val:
                factor = non_comp_val / comp_val
                row_labels.append(f"+{factor:.1f}x" if factor < 100 else f"+{factor:.0f}x")
            else:
                factor = comp_val / non_comp_val
                row_labels.append(f"-{factor:.1f}x" if factor < 100 else f"-{factor:.0f}x")
        text_labels.append(row_labels)

    text_labels = np.array(text_labels)

    avg_ratios = np.mean(heatmap_data, axis=1)
    sorted_idx = np.argsort(avg_ratios)
    heatmap_data = heatmap_data[sorted_idx]
    text_labels = text_labels[sorted_idx]
    sorted_methods = [method_keys[idx] for idx in sorted_idx]

    fig, ax = plt.subplots(figsize=(16, max(8, len(sorted_methods) * 0.7)), facecolor='none')
    ax.set_facecolor('none')

    clipped_data = np.clip(heatmap_data, -4.0, 4.0)
    cmap = sns.diverging_palette(15, 240, as_cmap=True)

    sns.heatmap(
        clipped_data,
        annot=text_labels,
        fmt="",
        cmap=cmap,
        center=0,
        xticklabels=[f'{s:,}' for s in sizes],
        yticklabels=sorted_methods,
        ax=ax,
        cbar_kws={
            'label': '← Non-Compressed (V1) Faster  |  Relative Speedup Scale (Clipped at 16x)  |  Compressed (V2) Faster →'
        },
        linewidths=0.6,
        linecolor='#444444',
        annot_kws={'size': 9, 'weight': 'bold'}
    )

    ax.set_title(
        'Non-Compressed (V1) vs Compressed (V2)\n'
        'Performance Comparison Matrix Heatmap\n'
        '(Blue/Positive = Compressed Faster, Red/Negative = Non-Compressed Faster)',
        color='#ffffff', fontsize=15, fontweight='bold', pad=20
    )
    ax.set_ylabel('Trie Interface Operations', color='#ffffff', fontsize=12, labelpad=10)
    ax.set_xlabel('Collection Size (Elements / Keys)', color='#ffffff', fontsize=12, labelpad=10)

    ax.tick_params(colors='#ffffff', labelsize=10)
    plt.xticks(rotation=45, ha='right')
    plt.yticks(rotation=0)

    cbar = ax.collections[0].colorbar
    cbar.ax.tick_params(colors='#ffffff', labelsize=10)
    cbar.ax.yaxis.label.set_color('#ffffff')
    cbar.ax.yaxis.label.set_fontsize(11)

    plt.tight_layout()
    plt.savefig(OUTPUT_HEATMAP_PATH, dpi=300, transparent=True)
    plt.close()


if __name__ == '__main__':
    main()