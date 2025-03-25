package com.personal.voicememo.data.models

data class PineconeVector(
    val id: String,
    val values: List<Float>,
    val metadata: Map<String, String>? = null
) 