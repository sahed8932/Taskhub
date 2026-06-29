package com.example.data

data class TaskStep(
    val stepNumber: Int,
    val text: String,
    val type: String = "text", // "text", "bold", "warning", "note"
    val imageResOrUrl: String? = null
)

object TaskStepSerializer {
    fun serialize(steps: List<TaskStep>): String {
        return steps.joinToString("###") { step ->
            "${step.stepNumber}||${step.text}||${step.type}||${step.imageResOrUrl ?: ""}"
        }
    }

    fun deserialize(serialized: String): List<TaskStep> {
        if (serialized.isEmpty()) return emptyList()
        return serialized.split("###").mapNotNull { block ->
            val parts = block.split("||")
            if (parts.size >= 3) {
                TaskStep(
                    stepNumber = parts[0].toIntOrNull() ?: 1,
                    text = parts[1],
                    type = parts[2],
                    imageResOrUrl = if (parts.size > 3 && parts[3].isNotEmpty()) parts[3] else null
                )
            } else null
        }
    }
}
