package com.farmlens.anarai.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForumReply(
    @SerialName("id")
    val id: String = "",
    @SerialName("post_id")
    val postId: String,
    @SerialName("author_name")
    val authorName: String,
    @SerialName("content")
    val content: String,
    @SerialName("created_at")
    val createdAt: String? = null
)
