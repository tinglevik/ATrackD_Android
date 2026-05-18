package com.example.actitracker.data.search

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

data class IconSearchResult(
    val iconId: String,
    val score: Float,
    val set: String,
    val assetPath: String
)

class IconSearchRepository(private val context: Context) {
    private val gson = Gson()
    
    // Inverted Index: Tag -> List of (IconKey to Score)
    // IconKey is the original key from JSON (e.g. "lucide_bike")
    private var tagToIcons: Map<String, List<Pair<String, Float>>> = emptyMap()
    
    // Cache for set and path to avoid re-parsing during search
    private data class IconMetadata(val set: String, val assetPath: String)
    private var iconMetadata: Map<String, IconMetadata> = emptyMap()

    suspend fun initialize() = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.assets.open("search_index.compact.json")
            val type = object : TypeToken<Map<String, Map<String, Float>>>() {}.type
            val rawIndex: Map<String, Map<String, Float>> = gson.fromJson(InputStreamReader(inputStream), type)
            
            val tempInverted = mutableMapOf<String, MutableList<Pair<String, Float>>>()
            val tempMetadata = mutableMapOf<String, IconMetadata>()

            rawIndex.forEach { (iconKey, tags) ->
                // iconKey format: "set_id" (e.g. "lucide_bike")
                // Note: some sets might have underscores in their name? 
                // Based on assets: lucide, phosphor, tabler-icons.
                // We split by the first underscore.
                val underscoreIndex = iconKey.indexOf('_')
                if (underscoreIndex != -1) {
                    var setName = iconKey.substring(0, underscoreIndex)
                    val id = iconKey.substring(underscoreIndex + 1)
                    
                    // Fix for tabler icons mismatch: JSON uses "tabler_", assets use "tabler-icons"
                    if (setName == "tabler") {
                        setName = "tabler-icons"
                    }

                    val displayId = "$setName:$id"
                    
                    tempMetadata[displayId] = IconMetadata(
                        set = setName,
                        assetPath = "file:///android_asset/icons/$setName/$id.svg"
                    )

                    tags.forEach { (tag, score) ->
                        tempInverted.getOrPut(tag.lowercase()) { mutableListOf() }.add(displayId to score)
                    }
                }
            }
            
            tagToIcons = tempInverted
            iconMetadata = tempMetadata
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getAllIcons(): List<IconSearchResult> {
        return iconMetadata.map { (key, meta) ->
            IconSearchResult(key, 0f, meta.set, meta.assetPath)
        }
    }

    fun search(query: String): List<IconSearchResult> {
        // If query is blank/space, return empty list (UI should use getAllIcons)
        if (query.isBlank()) return emptyList()
        
        val queryWords = query.lowercase().split(Regex("\\s+")).filter { it.isNotBlank() }
        if (queryWords.isEmpty()) return emptyList()

        val iconScores = mutableMapOf<String, Float>()
        
        // For each word in the query
        for (word in queryWords) {
            // Find all tags that start with this word (fast prefix match)
            val matchingTags = tagToIcons.keys.filter { it.startsWith(word) }
            
            for (tag in matchingTags) {
                val icons = tagToIcons[tag] ?: continue
                
                // Boost exact matches
                val relevanceMultiplier = if (tag == word) 2.0f else 1.0f
                
                for ((iconKey, baseScore) in icons) {
                    val currentScore = iconScores[iconKey] ?: 0f
                    iconScores[iconKey] = currentScore + (baseScore * relevanceMultiplier)
                }
            }
        }
        
        return iconScores.mapNotNull { (iconKey, score) ->
            val metadata = iconMetadata[iconKey] ?: return@mapNotNull null
            IconSearchResult(
                iconId = iconKey,
                score = score,
                set = metadata.set,
                assetPath = metadata.assetPath
            )
        }.sortedByDescending { it.score }
    }
}
