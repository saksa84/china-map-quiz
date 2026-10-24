package io.github.saksa84.chinamapquiz.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EliminateSessionTest {

    private var now = 0L
    private val clock = { now }

    @Test
    fun wrongTaps_keepTheSameQuestion() {
        val s = EliminateSession(listOf("A", "B"), clock)
        assertFalse(s.tap("B")!!.correct)
        assertFalse(s.tap("C")!!.correct)
        assertEquals("A", s.currentTarget)
        assertEquals(2, s.errors)
    }

    @Test
    fun correctTap_eliminatesAndMovesOn() {
        val s = EliminateSession(listOf("A", "B"), clock)
        assertTrue(s.tap("A")!!.correct)
        assertEquals(setOf("A"), s.eliminated)
        assertEquals("B", s.currentTarget)
    }

    @Test
    fun eliminatedRegionsCannotBeTappedAgain() {
        val s = EliminateSession(listOf("A", "B"), clock)
        s.tap("A")
        assertNull("Déjà éliminée : ce n'est pas une erreur", s.tap("A"))
        assertEquals(0, s.errors)
    }

    @Test
    fun gameEndsWhenEverythingIsFound_andTimerStops() {
        val s = EliminateSession(listOf("A", "B"), clock)
        now = 1_000; s.tap("A")
        now = 2_500; s.tap("X")
        now = 3_000; s.tap("B")
        now = 99_000
        assertTrue(s.isFinished)
        assertNull(s.currentTarget)
        assertNull(s.tap("A"))
        assertEquals(3_000, s.elapsedMs())
    }

    @Test
    fun summary_countsFirstTrySuccessesAndListsEachMistake() {
        val s = EliminateSession(listOf("A", "B", "C"), clock)
        s.tap("A")
        s.tap("C"); s.tap("A"); s.tap("B") // B : une erreur (C), puis A ignoré car éliminé
        s.tap("C")
        val summary = s.summary()
        assertEquals(3, summary.total)
        assertEquals(2, summary.correct)
        assertEquals(listOf("B"), summary.missedTargetIds)
        assertEquals(listOf("C"), summary.mistakes.map { it.selectedId })
    }

    @Test
    fun emptyGame_isFinished() {
        val s = EliminateSession(emptyList(), clock)
        assertTrue(s.isFinished)
        assertEquals(0, s.summary().total)
    }
}
