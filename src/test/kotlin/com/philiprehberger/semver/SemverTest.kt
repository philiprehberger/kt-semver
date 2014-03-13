package com.philiprehberger.semver

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SemverTest {

    // --- Parsing ---

    @Test
    fun `parse valid version`() {
        val v = Semver.parse("1.2.3")
        assertEquals(1, v.major)
        assertEquals(2, v.minor)
        assertEquals(3, v.patch)
        assertNull(v.preRelease)
        assertNull(v.buildMetadata)
    }

    @Test
    fun `parse version with v prefix`() {
        val v = Semver.parse("v1.2.3")
        assertEquals(1, v.major)
        assertEquals(2, v.minor)
        assertEquals(3, v.patch)
    }

    @Test
    fun `parse version with pre-release`() {
        val v = Semver.parse("1.2.3-alpha.1")
        assertEquals("alpha.1", v.preRelease)
    }

    @Test
    fun `parse version with build metadata`() {
        val v = Semver.parse("1.2.3+build.42")
        assertEquals("build.42", v.buildMetadata)
    }

    @Test
    fun `parse version with pre-release and build metadata`() {
        val v = Semver.parse("1.2.3-beta.2+sha.abc123")
        assertEquals("beta.2", v.preRelease)
        assertEquals("sha.abc123", v.buildMetadata)
    }

    @Test
    fun `parse invalid version throws`() {
        assertFailsWith<IllegalArgumentException> { Semver.parse("not.a.version") }
        assertFailsWith<IllegalArgumentException> { Semver.parse("1.2") }
        assertFailsWith<IllegalArgumentException> { Semver.parse("") }
        assertFailsWith<IllegalArgumentException> { Semver.parse("1.2.3.4") }
    }

    @Test
    fun `constructor from string`() {
        val v = Semver("2.0.0-rc.1")
        assertEquals(2, v.major)
        assertEquals(0, v.minor)
        assertEquals(0, v.patch)
        assertEquals("rc.1", v.preRelease)
    }

    // --- toString ---

    @Test
    fun `toString produces canonical format`() {
        assertEquals("1.2.3", Semver(1, 2, 3).toString())
        assertEquals("1.2.3-alpha", Semver(1, 2, 3, preRelease = "alpha").toString())
        assertEquals("1.2.3+build", Semver(1, 2, 3, buildMetadata = "build").toString())
        assertEquals("1.2.3-alpha+build", Semver(1, 2, 3, "alpha", "build").toString())
    }

    // --- Comparison ---

    @Test
    fun `major version ordering`() {
        assertTrue(Semver(1, 0, 0) < Semver(2, 0, 0))
    }

    @Test
    fun `minor version ordering`() {
        assertTrue(Semver(1, 1, 0) < Semver(1, 2, 0))
    }

    @Test
    fun `patch version ordering`() {
        assertTrue(Semver(1, 0, 1) < Semver(1, 0, 2))
    }

    @Test
    fun `pre-release has lower precedence than release`() {
        assertTrue(Semver(1, 0, 0, preRelease = "alpha") < Semver(1, 0, 0))
    }

    @Test
    fun `pre-release identifiers compared numerically`() {
        assertTrue(Semver(1, 0, 0, preRelease = "alpha.1") < Semver(1, 0, 0, preRelease = "alpha.2"))
        assertTrue(Semver(1, 0, 0, preRelease = "alpha.9") < Semver(1, 0, 0, preRelease = "alpha.10"))
    }

    @Test
    fun `numeric pre-release lower than alphanumeric`() {
        assertTrue(Semver(1, 0, 0, preRelease = "1") < Semver(1, 0, 0, preRelease = "alpha"))
    }

    @Test
    fun `pre-release with fewer fields is lower`() {
        assertTrue(Semver(1, 0, 0, preRelease = "alpha") < Semver(1, 0, 0, preRelease = "alpha.1"))
    }

    @Test
    fun `equal versions compare to zero`() {
        assertEquals(0, Semver(1, 2, 3).compareTo(Semver(1, 2, 3)))
    }

    @Test
    fun `build metadata is ignored in comparison`() {
        assertEquals(0, Semver(1, 2, 3, buildMetadata = "a").compareTo(Semver(1, 2, 3, buildMetadata = "b")))
    }

    @Test
    fun `sorting a list of versions`() {
        val versions = listOf(
            Semver.parse("1.0.0"),
            Semver.parse("1.0.0-alpha"),
            Semver.parse("0.9.0"),
            Semver.parse("1.0.0-beta"),
            Semver.parse("1.1.0"),
        )
        val sorted = versions.sorted()
        assertEquals(
            listOf("0.9.0", "1.0.0-alpha", "1.0.0-beta", "1.0.0", "1.1.0"),
            sorted.map { it.toString() },
        )
    }

    // --- Bumping ---

    @Test
    fun `bumpMajor`() {
        assertEquals(Semver(2, 0, 0), Semver(1, 2, 3).bumpMajor())
    }

    @Test
    fun `bumpMinor`() {
        assertEquals(Semver(1, 3, 0), Semver(1, 2, 3).bumpMinor())
    }

    @Test
    fun `bumpPatch`() {
        assertEquals(Semver(1, 2, 4), Semver(1, 2, 3).bumpPatch())
    }

    @Test
    fun `bump clears pre-release`() {
        val v = Semver(1, 2, 3, preRelease = "alpha")
        assertNull(v.bumpPatch().preRelease)
        assertNull(v.bumpMinor().preRelease)
        assertNull(v.bumpMajor().preRelease)
    }

    @Test
    fun `withPreRelease`() {
        val v = Semver(1, 2, 3).withPreRelease("rc.1")
        assertEquals("rc.1", v.preRelease)
        assertEquals(1, v.major)
    }

    @Test
    fun `withoutPreRelease`() {
        val v = Semver(1, 2, 3, preRelease = "alpha").withoutPreRelease()
        assertNull(v.preRelease)
    }

    // --- Range matching ---

    @Test
    fun `exact range`() {
        val range = SemverRange("1.2.3")
        assertTrue(range.matches(Semver(1, 2, 3)))
        assertFalse(range.matches(Semver(1, 2, 4)))
    }

    @Test
    fun `gte operator`() {
        val range = SemverRange(">=1.2.0")
        assertTrue(range.matches(Semver(1, 2, 0)))
        assertTrue(range.matches(Semver(1, 3, 0)))
        assertTrue(range.matches(Semver(2, 0, 0)))
        assertFalse(range.matches(Semver(1, 1, 9)))
    }

    @Test
    fun `lt operator`() {
        val range = SemverRange("<2.0.0")
        assertTrue(range.matches(Semver(1, 9, 9)))
        assertFalse(range.matches(Semver(2, 0, 0)))
    }

    @Test
    fun `caret range major`() {
        val range = SemverRange("^1.2.3")
        assertTrue(range.matches(Semver(1, 2, 3)))
        assertTrue(range.matches(Semver(1, 9, 9)))
        assertFalse(range.matches(Semver(2, 0, 0)))
        assertFalse(range.matches(Semver(1, 2, 2)))
    }

    @Test
    fun `caret range zero major`() {
        val range = SemverRange("^0.2.3")
        assertTrue(range.matches(Semver(0, 2, 3)))
        assertTrue(range.matches(Semver(0, 2, 9)))
        assertFalse(range.matches(Semver(0, 3, 0)))
    }

    @Test
    fun `caret range zero minor`() {
        val range = SemverRange("^0.0.3")
        assertTrue(range.matches(Semver(0, 0, 3)))
        assertFalse(range.matches(Semver(0, 0, 4)))
    }

    @Test
    fun `tilde range`() {
        val range = SemverRange("~1.2.3")
        assertTrue(range.matches(Semver(1, 2, 3)))
        assertTrue(range.matches(Semver(1, 2, 9)))
        assertFalse(range.matches(Semver(1, 3, 0)))
        assertFalse(range.matches(Semver(1, 2, 2)))
    }

    @Test
    fun `AND range with space`() {
        val range = SemverRange(">=1.0.0 <2.0.0")
        assertTrue(range.matches(Semver(1, 0, 0)))
        assertTrue(range.matches(Semver(1, 9, 9)))
        assertFalse(range.matches(Semver(0, 9, 9)))
        assertFalse(range.matches(Semver(2, 0, 0)))
    }

    @Test
    fun `OR range`() {
        val range = SemverRange("1.0.0 || 2.0.0")
        assertTrue(range.matches(Semver(1, 0, 0)))
        assertTrue(range.matches(Semver(2, 0, 0)))
        assertFalse(range.matches(Semver(1, 5, 0)))
    }

    @Test
    fun `maxSatisfying returns highest match`() {
        val range = SemverRange("^1.0.0")
        val versions = listOf(
            Semver(0, 9, 0),
            Semver(1, 0, 0),
            Semver(1, 5, 0),
            Semver(1, 9, 9),
            Semver(2, 0, 0),
        )
        assertEquals(Semver(1, 9, 9), range.maxSatisfying(versions))
    }

    @Test
    fun `maxSatisfying returns null when no match`() {
        val range = SemverRange("^3.0.0")
        val versions = listOf(Semver(1, 0, 0), Semver(2, 0, 0))
        assertNull(range.maxSatisfying(versions))
    }

    @Test
    fun `equals operator`() {
        val range = SemverRange("=1.2.3")
        assertTrue(range.matches(Semver(1, 2, 3)))
        assertFalse(range.matches(Semver(1, 2, 4)))
    }

    @Test
    fun `lte operator`() {
        val range = SemverRange("<=1.5.0")
        assertTrue(range.matches(Semver(1, 5, 0)))
        assertTrue(range.matches(Semver(1, 4, 9)))
        assertFalse(range.matches(Semver(1, 5, 1)))
    }

    @Test
    fun `gt operator`() {
        val range = SemverRange(">1.0.0")
        assertFalse(range.matches(Semver(1, 0, 0)))
        assertTrue(range.matches(Semver(1, 0, 1)))
    }
}
