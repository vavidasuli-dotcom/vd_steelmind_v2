package com.vd.steelmind.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.vd.steelmind.data.AppDatabase
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object ProjectExporter {
    suspend fun exportProject(context: Context, db: AppDatabase, projectId: Long): Uri {
        val dao = db.appDao()
        val project = dao.listProjects().find { it.id == projectId } ?: throw IllegalArgumentException("Projekt nem található")
        val stock = dao.stockOptions(projectId)
        val demands = dao.listDemands(projectId)

        val root = JSONObject()
        root.put("project", JSONObject().apply {
            put("id", project.id)
            put("name", project.name)
            put("materialType", project.materialType)
            put("useInventory", project.useInventory)
            put("kerfMmDefault", project.kerfMmDefault)
            put("minOffcutMm", project.minOffcutMm)
            put("createdAt", project.createdAt)
        })
        root.put("stockOptions", JSONArray(stock.map { JSONObject().apply {
            put("lengthMm", it.lengthMm); put("availableCount", it.availableCount)
        } }))
        root.put("demands", JSONArray(demands.map { JSONObject().apply {
            put("itemTypeId", it.itemTypeId); put("lengthMm", it.lengthMm); put("quantity", it.quantity); put("kerfOverrideMm", it.kerfOverrideMm)
        } }))

        val file = File(context.cacheDir, "project_${project.id}.json")
        file.writeText(root.toString(2))

        return FileProvider.getUriForFile(context, context.packageName + ".provider", file)
    }
}