package com.example.messagerecovery.data.deduplication

import com.example.messagerecovery.domain.deduplication.DeduplicationEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DeduplicationEngineTest {

    @Before
    fun setUp() {
        DeduplicationEngine.clearCache()
    }

    @Test
    fun identicalMessageWithin2Seconds_producesIdenticalHash() {
        val pkg = "com.whatsapp"
        val thread = "com.whatsapp_+123456789"
        val text = "Hello, are you there?"
        val timeBase = 1000000L

        val hash1 = DeduplicationEngine.computeHash(pkg, thread, text, timeBase)
        val hash2 = DeduplicationEngine.computeHash(pkg, thread, text, timeBase + 1500L) // 1.5s later (same bucket)

        assertEquals("Hashes within 2-second bucket must match", hash1, hash2)
    }

    @Test
    fun identicalMessageAcrossTimeBuckets_producesIdenticalHash() {
        val pkg = "com.whatsapp"
        val thread = "com.whatsapp_+123456789"
        val text = "Hello again!"
        val time1 = 1000000L
        val time2 = 1003000L // 3s later

        val hash1 = DeduplicationEngine.computeHash(pkg, thread, text, time1)
        val hash2 = DeduplicationEngine.computeHash(pkg, thread, text, time2)

        assertEquals("Hashes across time must match to ensure deduplication", hash1, hash2)
    }

    @Test
    fun differentPackages_producesDifferentHash() {
        val thread = "chat_user_1"
        val text = "Same text payload"
        val time = 1000000L

        val hashWA = DeduplicationEngine.computeHash("com.whatsapp", thread, text, time)
        val hashWAB = DeduplicationEngine.computeHash("com.whatsapp.w4b", thread, text, time)
        val hashFB = DeduplicationEngine.computeHash("com.facebook.orca", thread, text, time)

        assertNotEquals(hashWA, hashWAB)
        assertNotEquals(hashWA, hashFB)
    }

    @Test
    fun lruCache_correctlyIdentifiesDuplicates() {
        val hash = "sample_sha256_hash_12345"

        val firstCheck = DeduplicationEngine.isDuplicateOrRecord(hash)
        assertFalse("First event must be accepted as unique", firstCheck)

        val secondCheck = DeduplicationEngine.isDuplicateOrRecord(hash)
        assertTrue("Immediate duplicate event must be flagged as duplicate", secondCheck)

        val differentHash = "different_sha256_hash_67890"
        val thirdCheck = DeduplicationEngine.isDuplicateOrRecord(differentHash)
        assertFalse("Different event must be accepted as unique", thirdCheck)
    }
}
