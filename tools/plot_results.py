
import csv
import math
from pathlib import Path
import matplotlib.pyplot as plt

ROOT = Path(__file__).resolve().parents[1]

with (ROOT / "results" / "results.csv").open(newline="") as f:
    records = list(csv.DictReader(f))

with (ROOT / "results" / "bonus_metrics.csv").open(newline="") as f:
    bonus = list(csv.DictReader(f))

out = ROOT / "results" / "plots"
out.mkdir(parents=True, exist_ok=True)

# Plot 1: Kruskal vs Prim comparisons
fig, ax = plt.subplots(figsize=(9, 5.5))

for density in ("sparse", "dense"):
    for method in ("Kruskal", "Prim"):
        recs = sorted(
            (
                r for r in records
                if r["algorithm"] == method
                   and density in r["graph"]
            ),
            key=lambda r: int(r["n"])
        )

        ax.plot(
            [int(r["n"]) for r in recs],
            [int(r["comparisons"]) for r in recs],
            marker="o",
            label=f"{method} measured ({density})"
        )

    recs = sorted(
        (
            r for r in records
            if r["algorithm"] == "Kruskal"
               and density in r["graph"]
        ),
        key=lambda r: int(r["n"])
    )

    ax.plot(
        [int(r["n"]) for r in recs],
        [
            int(r["m"]) * math.log2(int(r["m"]))
            for r in recs
        ],
        linestyle="--",
        label=f"m log2(m) model ({density})"
    )

ax.set(
    xlabel="Vertices (n)",
    ylabel="Weight comparisons",
    title="MST: comparisons vs complexity"
)

ax.set_xscale("log")
ax.set_yscale("log")
ax.grid(True, alpha=0.25)
ax.legend(fontsize=8)

fig.tight_layout()
fig.savefig(out / "mst_comparisons.png", dpi=160)
plt.close(fig)


# Plot 2: Tarjan vs Kosaraju
fig, ax = plt.subplots(figsize=(9, 5.5))

for density in ("sparse", "dense"):
    a = sorted(
        (r for r in bonus if r["density"] == density),
        key=lambda r: int(r["n"])
    )

    xs = [int(r["n"]) for r in a]

    ax.plot(
        xs,
        [int(r["kosaraju_edge_scans"]) for r in a],
        marker="o",
        label=f"Kosaraju 2m ({density})"
    )

    ax.plot(
        xs,
        [int(r["tarjan_edge_scans"]) for r in a],
        marker="x",
        linestyle="--",
        label=f"Tarjan m ({density})"
    )

ax.set(
    xlabel="Vertices (n)",
    ylabel="Edge scans",
    title="SCC: Kosaraju vs Tarjan"
)

ax.set_xscale("log")
ax.set_yscale("log")
ax.grid(True, alpha=0.25)
ax.legend()

fig.tight_layout()
fig.savefig(out / "scc_edge_scans.png", dpi=160)
plt.close(fig)


# Plot 3: Kahn vs DAG Shortest Paths
fig, ax = plt.subplots(figsize=(9, 5.5))

for density in ("sparse", "dense"):
    a = sorted(
        (
            r for r in records
            if r["algorithm"] == "Kahn"
               and density in r["graph"]
        ),
        key=lambda r: int(r["n"])
    )

    b = sorted(
        (
            r for r in records
            if r["algorithm"] == "DagShortestPath"
               and density in r["graph"]
        ),
        key=lambda r: int(r["n"])
    )

    ax.plot(
        [int(r["n"]) for r in a],
        [int(r["edge_scans"]) for r in a],
        marker="o",
        label=f"Kahn ({density})"
    )

    ax.plot(
        [int(r["n"]) for r in b],
        [int(r["edge_scans"]) for r in b],
        marker="x",
        linestyle="--",
        label=f"DAG relaxation ({density})"
    )

ax.set(
    xlabel="Vertices (n)",
    ylabel="Edge scans",
    title="DAG: Topological Order vs Relaxation"
)

ax.set_xscale("log")
ax.set_yscale("log")
ax.grid(True, alpha=0.25)
ax.legend()

fig.tight_layout()
fig.savefig(out / "dag_edge_scans.png", dpi=160)
plt.close(fig)

print("Generated plots in", out)
