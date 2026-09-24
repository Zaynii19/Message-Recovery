package com.example.messagerecovery.domain.deduplication

import androidx.collection.LruCache
import java.security.MessageDigest

object DeduplicationEngine {

    private const val CACHE_CAPACITY = 2000

    // Thread-safe memory cache of recent message hashes
    private val memoryCache = LruCache<String, Boolean>(CACHE_CAPACITY)

    /**
     * Computes a deterministic SHA-256 hash for an incoming message event.
     * Removing the volatile timestamp ensures that capture from notifications and screen
     * scraping will produce identical hashes for the same sender, thread, and text.
     */
    fun computeHash(
        packageName: String,
        threadId: String,
        senderName: String,
        rawText: String
    ): String {
        val normalizedText = rawText.trim()
        val normalizedSender = senderName.trim().lowercase()
        val rawInput = "$packageName|$threadId|$normalizedSender|$normalizedText"

        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(rawInput.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun computeHash(
        packageName: String,
        threadId: String,
        rawText: String,
        timestamp: Long = 0L
    ): String {
        return computeHash(packageName, threadId, "", rawText)
    }

    /**
     * Checks if the hash is already in the memory cache.
     * If not present, records it and returns false (meaning it is a unique, new event).
     * If present, returns true (meaning it is a duplicate that should be dropped).
     */
    @Synchronized
    fun isDuplicateOrRecord(hash: String): Boolean {
        if (memoryCache[hash] == true) {
            return true
        }
        memoryCache.put(hash, true)
        return false
    }

    @Synchronized
    fun isDuplicate(hash: String): Boolean {
        return memoryCache[hash] == true
    }

    @Synchronized
    fun clearCache() {
        memoryCache.evictAll()
    }
}
