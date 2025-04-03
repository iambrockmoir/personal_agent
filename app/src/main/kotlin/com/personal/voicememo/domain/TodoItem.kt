package com.personal.voicememo.domain

import com.google.gson.annotations.SerializedName

data class TodoItem(
    @SerializedName("item")
    val item: String,
    @SerializedName("timeEstimate")
    val timeEstimate: String
) 