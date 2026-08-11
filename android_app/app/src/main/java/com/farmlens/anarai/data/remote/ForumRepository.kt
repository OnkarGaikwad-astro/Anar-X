package com.farmlens.anarai.data.remote

import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@kotlinx.serialization.Serializable
data class ReplyUpdate(val replies: Int)

class ForumRepository {
    private val supabase = SupabaseClient.client

    fun getAllPosts(): Flow<List<ForumPost>> = flow {
        val posts = supabase.postgrest["posts"]
            .select()
            .decodeList<ForumPost>()
        emit(posts)
    }

    suspend fun insertPost(post: ForumPost) {
        supabase.postgrest["posts"].insert(post)
    }

    fun getPost(postId: String): Flow<ForumPost?> = flow {
        val post = supabase.postgrest["posts"]
            .select { filter { eq("id", postId) } }
            .decodeSingleOrNull<ForumPost>()
        emit(post)
    }

    fun getReplies(postId: String): Flow<List<ForumReply>> = flow {
        val replies = supabase.postgrest["replies"]
            .select { filter { eq("post_id", postId) } }
            .decodeList<ForumReply>()
        emit(replies)
    }

    suspend fun insertReply(reply: ForumReply) {
        // Insert the reply
        supabase.postgrest["replies"].insert(reply)
        
        // Update the replies count on the post
        try {
            val post = supabase.postgrest["posts"]
                .select { filter { eq("id", reply.postId) } }
                .decodeSingle<ForumPost>()
            
            supabase.postgrest["posts"]
                .update(ReplyUpdate(post.replies + 1)) {
                    filter { eq("id", reply.postId) }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
