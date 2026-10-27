package com.example.data.db

import androidx.room.TypeConverter
import com.example.data.model.Priority
import com.example.data.model.TaskNote
import com.example.data.model.TaskStatus
import com.example.data.model.TaskType
import org.json.JSONArray
import org.json.JSONObject

class Converters {
    @TypeConverter
    fun fromPriority(priority: Priority): String = priority.name

    @TypeConverter
    fun toPriority(value: String): Priority = try {
        Priority.valueOf(value)
    } catch (e: Exception) {
        Priority.SREDNI
    }

    @TypeConverter
    fun fromStatus(status: TaskStatus): String = status.name

    @TypeConverter
    fun toStatus(value: String): TaskStatus = try {
        TaskStatus.valueOf(value)
    } catch (e: Exception) {
        TaskStatus.NOWE
    }

    @TypeConverter
    fun fromTaskType(type: TaskType): String = type.name

    @TypeConverter
    fun toTaskType(value: String): TaskType = try {
        TaskType.valueOf(value)
    } catch (e: Exception) {
        TaskType.AWARIA
    }

    companion object {
        fun serializeNotes(notes: List<TaskNote>): String {
            val array = JSONArray()
            for (note in notes) {
                val obj = JSONObject().apply {
                    put("id", note.id)
                    put("text", note.text)
                    put("timestamp", note.timestamp)
                    if (note.photoUri != null) {
                        put("photoUri", note.photoUri)
                    }
                }
                array.put(obj)
            }
            return array.toString()
        }

        fun deserializeNotes(json: String?): List<TaskNote> {
            if (json.isNullOrBlank()) return emptyList()
            val list = mutableListOf<TaskNote>()
            try {
                val array = JSONArray(json)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        TaskNote(
                            id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                            text = obj.optString("text", ""),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            photoUri = if (obj.has("photoUri") && !obj.isNull("photoUri")) obj.getString("photoUri") else null
                        )
                    )
                }
            } catch (e: Exception) {
                // Return empty list on parse issue
            }
            return list
        }
    }
}
