package com.vd.steelmind.logic

data class Demand(var lengthMm: Int, var quantity: Int, val kerfMm: Int)
data class BarPlan(val stockLength: Int, val cuts: List<Int>, val offcut: Int)
data class PlanResult(
    val bars: List<BarPlan>,
    val totalWaste: Int,
    val utilizationPercent: Int,
    val missing: Map<Int, Int>
)

object PlanGenerator {
    fun generate(
        demandsRaw: Map<Int, Pair<Int, Int?>>, // length -> (qty, kerfOverrideMm?)
        stockLengths: Map<Int, Int?>, // length -> availableCount (null = unlimited)
        defaultKerfMm: Int,
        minOffcutMm: Int,
        useInventory: Boolean
    ): PlanResult {
        val demands = demandsRaw.entries.map {
            Demand(it.key, it.value.first, it.value.second ?: defaultKerfMm)
        }.sortedByDescending { it.lengthMm }.toMutableList()

        val stockOptions = stockLengths.keys.sortedDescending()
        val stockRemaining = stockLengths.toMutableMap()

        val bars = mutableListOf<BarPlan>()
        val missing = mutableMapOf<Int, Int>()

        fun hasRemaining() = demands.any { it.quantity > 0 }

        while (hasRemaining()) {
            val stock = stockOptions.firstOrNull { len ->
                val avail = stockRemaining[len]
                avail == null || (avail ?: 0) > 0
            } ?: break

            if (stockRemaining[stock] != null) {
                stockRemaining[stock] = (stockRemaining[stock] ?: 0) - 1
            }

            var remaining = stock
            val cuts = mutableListOf<Int>()

            while (true) {
                val idx = demands.indexOfFirst { d ->
                    if (d.quantity <= 0) false else {
                        val extraKerf = if (cuts.isEmpty()) 0 else d.kerfMm
                        d.lengthMm + extraKerf <= remaining
                    }
                }
                if (idx == -1) break
                val d = demands[idx]
                val extraKerf = if (cuts.isEmpty()) 0 else d.kerfMm
                cuts.add(d.lengthMm)
                remaining -= (d.lengthMm + extraKerf)
                d.quantity--
                demands.sortByDescending { it.lengthMm }
            }

            val offcut = remaining
            bars.add(BarPlan(stock, cuts, offcut))
        }

        if (useInventory) {
            demands.filter { it.quantity > 0 }.forEach { d ->
                missing[d.lengthMm] = (missing[d.lengthMm] ?: 0) + d.quantity
            }
        } else {
            while (demands.any { it.quantity > 0 }) {
                val stock = stockOptions.firstOrNull() ?: break
                var remaining = stock
                val cuts = mutableListOf<Int>()
                while (true) {
                    val idx = demands.indexOfFirst { d ->
                        if (d.quantity <= 0) false else {
                            val extraKerf = if (cuts.isEmpty()) 0 else d.kerfMm
                            d.lengthMm + extraKerf <= remaining
                        }
                    }
                    if (idx == -1) break
                    val d = demands[idx]
                    val extraKerf = if (cuts.isEmpty()) 0 else d.kerfMm
                    cuts.add(d.lengthMm)
                    remaining -= (d.lengthMm + extraKerf)
                    d.quantity--
                    demands.sortByDescending { it.lengthMm }
                }
                bars.add(BarPlan(stock, cuts, remaining))
            }
        }

        val totalStock = bars.sumOf { it.stockLength }
        val totalCuts = bars.sumOf { it.cuts.sum() }
        val totalWaste = totalStock - totalCuts
        val utilization = if (totalStock > 0) ((totalCuts * 100.0) / totalStock).toInt() else 0

        return PlanResult(bars, totalWaste, utilization, missing)
    }
}