package com.vd.steelmind.ui

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.vd.steelmind.data.*
import com.vd.steelmind.logic.PlanGenerator
import com.vd.steelmind.logic.PlanResult
import com.vd.steelmind.pdf.PdfExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun PlanScreen(nav: NavController, db: AppDatabase, projectId: Long) {
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    var project by remember { mutableStateOf<ProjectEntity?>(null) }
    var stock by remember { mutableStateOf(listOf<StockOptionEntity>()) }
    var demands by remember { mutableStateOf(listOf<CutDemandEntity>()) }
    var inventory by remember { mutableStateOf(listOf<InventoryBarEntity>()) }
    var items by remember { mutableStateOf(listOf<ItemTypeEntity>()) }
    var plan by remember { mutableStateOf<PlanResult?>(null) }
    var pdfBars by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            project = db.appDao().listProjects().find { it.id == projectId }
            stock = db.appDao().stockOptions(projectId)
            demands = db.appDao().listDemands(projectId)
            inventory = db.appDao().listInventory()
            items = db.appDao().listItemTypes()
        }
        val prefs = ctx.getSharedPreferences("vd_cut_settings", android.content.Context.MODE_PRIVATE)
        pdfBars = prefs.getBoolean("pdfBars", true)
    }

    fun generate() {
        val p = project ?: return
        val dem = demands.groupBy { it.lengthMm }.mapValues { entry ->
            val qty = entry.value.sumOf { it.quantity }
            val kerfOverride: Int? = entry.value.firstOrNull { it.kerfOverrideMm != null }?.kerfOverrideMm
            qty to kerfOverride
        }
        val stockMap = mutableMapOf<Int, Int?>()
        stock.forEach { s -> stockMap[s.lengthMm] = s.availableCount }
        if (p.useInventory) { inventory.forEach { s -> stockMap[s.lengthMm] = s.count } }
        if (stockMap.isEmpty()) stockMap[6000] = null

        plan = PlanGenerator.generate(
            demandsRaw = dem,
            stockLengths = stockMap,
            defaultKerfMm = p.kerfMmDefault,
            minOffcutMm = p.minOffcutMm,
            useInventory = p.useInventory
        )
    }

    fun saveOffcuts() {
        val p = project ?: return
        val pr = plan ?: return
        scope.launch(Dispatchers.IO) {
            pr.bars.map { it.offcut }.filter { it >= p.minOffcutMm }.groupingBy { it }.eachCount().forEach { (len, cnt) ->
                db.appDao().upsertOffcut(OffcutEntity(lengthMm = len, count = cnt))
            }
        }
    }

    fun totalCostFt(): Int {
        val itemsById = items.associateBy { it.id }
        return demands.sumOf { d ->
            val pricePerM = itemsById[d.itemTypeId]?.priceFtPerM ?: 0
            (d.lengthMm * d.quantity) * pricePerM / 1000
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Darabolási terv") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                val p = plan ?: run { generate(); return@FloatingActionButton }
                val file = PdfExporter.export(ctx, project?.name ?: "Projekt", p, drawBars = pdfBars)
                val uri = PdfExporter.shareFile(ctx, file)
                val share = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                ctx.startActivity(Intent.createChooser(share, "PDF megosztása"))
            }) { Text("PDF") }
        }
    ) { pad ->
        Column(Modifier.padding(pad).padding(12.dp)) {
            Row {
                Button(onClick = { generate() }) { Text("Terv készítése") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { saveOffcuts() }, enabled = plan != null) { Text("Maradékok mentése") }
            }
            Spacer(Modifier.height(8.dp))
            Text("Kerf: ${project?.kerfMmDefault ?: "-"} mm  •  Min. maradék: ${project?.minOffcutMm ?: "-"} mm")
            Spacer(Modifier.height(8.dp))
            plan?.let { pr ->
                Text("Kihasználtság: ${pr.utilizationPercent}%   Hulladék (mm): ${pr.totalWaste}")
                if (pr.missing.isNotEmpty()) {
                    Text("Hiányzó (készlet üzemmód): " + pr.missing.entries.joinToString { "${it.key}×${it.value}" })
                }
                Spacer(Modifier.height(4.dp))
                Text("Becsült összköltség: ${totalCostFt()} Ft (nettó vágott hosszból)")
                Spacer(Modifier.height(8.dp))
                LazyColumn {
                    itemsIndexed(pr.bars) { idx, bar ->
                        Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                            Text("${idx + 1}. Szál (${bar.stockLength} mm): " + bar.cuts.joinToString("; ") + " (maradék: ${bar.offcut})")
                            Canvas(Modifier.fillMaxWidth().height(12.dp)) {
                                val total = bar.stockLength.toFloat()
                                var x = 0f
                                val widthPx = size.width
                                bar.cuts.forEach { cut ->
                                    val w = (cut / total) * widthPx
                                    drawRect(Color.DarkGray, topLeft = androidx.compose.ui.geometry.Offset(x, 0f), size = androidx.compose.ui.geometry.Size(w, size.height))
                                    x += w
                                }
                                drawRect(Color.LightGray, topLeft = androidx.compose.ui.geometry.Offset(x, 0f), size = androidx.compose.ui.geometry.Size(widthPx - x, size.height))
                            }
                        }
                    }
                }
            } ?: Text("Nyomd meg a „Terv készítése” gombot.")
        }
    }
}