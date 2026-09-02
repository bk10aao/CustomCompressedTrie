#!/usr/bin/env python3
"""
Generate performance benchmark charts as PNG files comparing V1 (CustomTrie)
and V2 (CustomCompressedTrie) with a transparent background, wide-format CSV loading,
custom integer-formatted y-axes starting at 0, and pure marker circles in the legend.
"""

import matplotlib

matplotlib.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.ticker as mticker
from matplotlib.lines import Line2D
import pandas as pd
import os
import sys
import numpy as np

# ──────────────────────────────────────────────────────────────────────────────
# Configuration
# ──────────────────────────────────────────────────────────────────────────────

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
TRIE_CSV_PATH = os.path.join(SCRIPT_DIR, "CustomTrie_jmh_performance_3.csv")
COMPRESSED_TRIE_CSV_PATH = os.path.join(SCRIPT_DIR, "CustomCompressedTrie_jmh_performance_3.csv")
OUTPUT_DIR = SCRIPT_DIR

COLORS = {
    'blue': '#4DA6FF',      # V1 (Non-Compressed)
    'purple': '#9B6EF3',    # V2 (Compressed)
    'bg': '#0D0D0D',
    'grid': '#252525',
}

FIGURE_SIZE = (12, 6.2)
DPI = 150

OPERATIONS = {
    'constructor': {
        'title': 'Constructor()',
        'cols': ['Constructor()', 'DefaultConstructor']
    },
    'constructor_array': {
        'title': 'Constructor(String[])',
        'cols': ['Constructor(String[])', 'ArrayConstructor']
    },
    'constructor_list': {
        'title': 'Constructor(List)',
        'cols': ['Constructor(List)', 'ListConstructor']
    },
    'copy_constructor': {
        'title': 'Constructor(Trie)',
        'cols': ['Constructor(Trie)', 'Constructor(CompressedTrie)', 'CopyConstructor']
    },
    'clear': {
        'title': 'clear()',
        'cols': ['clear()', 'Clear']
    },
    'delete': {
        'title': 'delete(String)',
        'cols': ['delete(String)', 'Delete']
    },
    'equals': {
        'title': 'equals(Object)',
        'cols': ['equals(Object)', 'Equals']
    },
    'hashCode': {
        'title': 'hashCode()',
        'cols': ['hashCode()', 'HashCode']
    },
    'insert': {
        'title': 'insert(String)',
        'cols': ['insert(String)', 'Insert']
    },
    'isEmpty': {
        'title': 'isEmpty()',
        'cols': ['isEmpty()', 'IsEmpty']
    },
    'search_hit': {
        'title': 'search(Present Key)',
        'cols': ['search(Present Key)', 'search(Key Found)', 'search(Hit)', 'SearchHit']
    },
    'search_miss': {
        'title': 'search(Absent Key)',
        'cols': ['search(Absent Key)', 'search(Key Not Found)', 'search(Miss)', 'SearchMiss']
    },
    'size': {
        'title': 'size()',
        'cols': ['size()', 'Size', 'SizeMethod']
    },
    'startsWith': {
        'title': 'startsWith(String)',
        'cols': ['startsWith(String)', 'StartsWith']
    },
    'toString': {
        'title': 'toString()',
        'cols': ['toString()', 'ToString']
    },
}


def load_wide_jmh_csv(filepath):
    """Load wide-format CSV and return dict: {size: {col_name: score_value}}"""
    with open(filepath, 'r') as f:
        lines = [line.strip() for line in f if line.strip()]

    sample_line = lines[1] if len(lines) > 1 else lines[0]
    sep = ';' if ';' in sample_line else ','

    df = pd.read_csv(filepath, sep=sep)
    if len(df.columns) == 1 and len(lines) > 0:
        alt_sep = ',' if sep == ';' else ';'
        df = pd.read_csv(filepath, sep=alt_sep)

    df.columns = [c.strip() for c in df.columns]

    data = {}
    for _, row in df.iterrows():
        try:
            size = int(row['Size'])
        except (ValueError, KeyError, TypeError):
            continue
        data[size] = {}
        for col in df.columns:
            if col != 'Size':
                try:
                    data[size][col] = float(row[col])
                except (ValueError, TypeError):
                    data[size][col] = np.nan
    return data


def resolve_file_path(default_path, alt_filenames):
    """Find the first existing file among default and alternatives in script directory."""
    if os.path.exists(default_path):
        return default_path
    for alt in alt_filenames:
        alt_path = os.path.join(SCRIPT_DIR, alt)
        if os.path.exists(alt_path):
            return alt_path
    return default_path


def extract_series_values(data, canonical_sizes, possible_cols):
    """Extract numeric values for a list of possible column name matches."""
    values = []
    for s in canonical_sizes:
        val = np.nan
        if s in data:
            for col in possible_cols:
                if col in data[s] and not np.isnan(data[s][col]):
                    val = data[s][col]
                    break
        values.append(val)
    return values


def create_chart(title, possible_cols, trie_data, comp_data,
                 canonical_sizes, output_path):
    trie_values = extract_series_values(trie_data, canonical_sizes, possible_cols)
    comp_values = extract_series_values(comp_data, canonical_sizes, possible_cols)

    if np.all(np.isnan(trie_values)) and np.all(np.isnan(comp_values)):
        return False

    fig, ax = plt.subplots(figsize=FIGURE_SIZE, dpi=DPI)
    fig.patch.set_alpha(0)
    ax.set_facecolor('none')

    x_positions = list(range(len(canonical_sizes)))

    ax.plot(x_positions, trie_values, color=COLORS['blue'], linewidth=1.5, zorder=2)
    ax.plot(x_positions, comp_values, color=COLORS['purple'], linewidth=1.5, zorder=2)

    ax.scatter(x_positions, trie_values, color=COLORS['blue'], s=35, marker='o', edgecolors=COLORS['blue'], linewidths=1.5, zorder=3)
    ax.scatter(x_positions, comp_values, color=COLORS['purple'], s=35, marker='o', edgecolors=COLORS['purple'], linewidths=1.5, zorder=3)

    ax.grid(True, color=COLORS['grid'], linewidth=0.8, linestyle='-', zorder=0)
    ax.set_axisbelow(True)

    ax.set_xticks(x_positions)
    ax.set_xticklabels([f'{s:,}' for s in canonical_sizes], color='white', fontsize=10)
    ax.tick_params(axis='x', colors='white', length=0, pad=8)
    ax.set_xlim(-0.5, len(canonical_sizes) - 0.5)

    ax.set_ylim(bottom=0)
    ax.ticklabel_format(style='plain', axis='y')
    ax.yaxis.set_major_formatter(mticker.FuncFormatter(lambda x, p: f'{int(x):,}'))

    if ax.yaxis.get_offset_text():
        ax.yaxis.get_offset_text().set_color('white')
        ax.yaxis.get_offset_text().set_fontsize(10)

    ax.tick_params(axis='y', colors='white', length=0, pad=8)
    for label in ax.get_yticklabels():
        label.set_color('white')
        label.set_fontsize(10)

    for spine in ax.spines.values():
        spine.set_visible(False)

    ax.set_xlabel('Size', color='white', fontsize=12, labelpad=12)
    ax.set_ylabel('Time (ns/op)', color='white', fontsize=11, labelpad=10)
    ax.set_title(title, color='white', fontsize=15, fontweight='bold', pad=14)

    legend_elements = [
        Line2D([0], [0], marker='o', color='none', markerfacecolor=COLORS['blue'], markeredgecolor=COLORS['blue'], markeredgewidth=1.5, markersize=8, label='V1', linestyle='none'),
        Line2D([0], [0], marker='o', color='none', markerfacecolor=COLORS['purple'], markeredgecolor=COLORS['purple'], markeredgewidth=1.5, markersize=8, label='V2', linestyle='none'),
    ]

    leg = ax.legend(
        handles=legend_elements,
        loc='upper center',
        bbox_to_anchor=(0.5, -0.26),
        ncol=2,
        frameon=False,
        fontsize=12,
        handlelength=1.5,
        handletextpad=0.6,
        columnspacing=2.0
    )

    for text in leg.get_texts():
        text.set_color('white')
        text.set_fontsize(12)

    plt.tight_layout(rect=[0, 0.18, 1, 1])
    fig.savefig(output_path, dpi=DPI, transparent=True, bbox_inches='tight', facecolor='none', edgecolor='none')
    plt.close(fig)
    return True


def main():
    trie_path = resolve_file_path(
        TRIE_CSV_PATH,
        ["CustomTrie_jmh_performance_3.csv", "CustomTrie_jmh_performance_2.csv", "CustomTrie_jmh_matrix.csv", "CustomTrie_jmh_performance.csv"]
    )
    comp_path = resolve_file_path(
        COMPRESSED_TRIE_CSV_PATH,
        ["CustomCompressedTrie_jmh_performance_3.csv", "CustomCompressedTrie_jmh_performance_2.csv", "CustomCompressedTrie_jmh_matrix.csv", "CustomCompressedTrie_jmh_performance.csv"]
    )

    if not os.path.exists(trie_path):
        print(f"Error: Required file '{trie_path}' not found.")
        sys.exit(1)
    if not os.path.exists(comp_path):
        print(f"Error: Required file '{comp_path}' not found.")
        sys.exit(1)

    print(f"Loading {trie_path} (V1)...")
    trie_data = load_wide_jmh_csv(trie_path)

    print(f"Loading {comp_path} (V2)...")
    comp_data = load_wide_jmh_csv(comp_path)

    canonical_sizes = sorted(list(set(trie_data.keys()) | set(comp_data.keys())))

    for file_slug, config in OPERATIONS.items():
        title = config['title']
        possible_cols = config['cols']
        output_path = os.path.join(OUTPUT_DIR, f'{file_slug}.png')

        success = create_chart(title, possible_cols, trie_data, comp_data, canonical_sizes, output_path)
        if success:
            print(f"  ✓ {file_slug}.png ({title})")
        else:
            print(f"  ⚠ Skipping '{file_slug}' (columns {possible_cols} not found in CSVs)")


if __name__ == '__main__':
    main()