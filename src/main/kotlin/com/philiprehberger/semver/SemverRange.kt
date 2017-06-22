package com.philiprehberger.semver

/**
 * Represents a semantic version range expression that can match against [Semver] instances.
 *
 * Supports the following operators:
 * - `>=`, `<=`, `>`, `<`, `=` — comparison operators
 * - `^` (caret) — compatible with version (same major for >= 1.0.0, same major.minor for 0.x)
 * - `~` (tilde) — approximately equivalent (same major.minor)
 * - Space for AND (intersection)
 * - `||` for OR (union)
 *
 * @property expression the range expression string
 */
public class SemverRange(public val expression: String) {

    private val matcher: (Semver) -> Boolean = parseExpression(expression.trim())

    /**
     * Tests whether the given [version] satisfies this range expression.
     *
     * @param version the version to test
     * @return `true` if the version matches the range
     */
    public fun matches(version: Semver): Boolean = matcher(version)

    /**
     * Returns the highest version from [versions] that satisfies this range,
     * or `null` if no version matches.
     *
     * @param versions the list of versions to search
     * @return the maximum satisfying version, or `null`
     */
    public fun maxSatisfying(versions: List<Semver>): Semver? =
        versions.filter { matches(it) }.maxOrNull()

    override fun toString(): String = expression

    public companion object {

        private fun parseExpression(expr: String): (Semver) -> Boolean {
            // Split on || for OR
            val orParts = expr.split("||").map { it.trim() }
            if (orParts.size > 1) {
                val matchers = orParts.map { parseExpression(it) }
                return { version -> matchers.any { it(version) } }
            }

            // Split on space for AND
            val andParts = expr.split(" ").map { it.trim() }.filter { it.isNotEmpty() }
            if (andParts.size > 1) {
                val matchers = andParts.map { parseSingle(it) }
                return { version -> matchers.all { it(version) } }
            }

            return parseSingle(expr)
        }

        private fun parseSingle(expr: String): (Semver) -> Boolean {
            when {
                expr.startsWith(">=") -> {
                    val target = Semver.parse(expr.removePrefix(">=").trim())
                    return { v -> v >= target }
                }
                expr.startsWith("<=") -> {
                    val target = Semver.parse(expr.removePrefix("<=").trim())
                    return { v -> v <= target }
                }
                expr.startsWith(">") -> {
                    val target = Semver.parse(expr.removePrefix(">").trim())
                    return { v -> v > target }
                }
                expr.startsWith("<") -> {
                    val target = Semver.parse(expr.removePrefix("<").trim())
                    return { v -> v < target }
                }
                expr.startsWith("=") -> {
                    val target = Semver.parse(expr.removePrefix("=").trim())
                    return { v -> v.major == target.major && v.minor == target.minor && v.patch == target.patch && v.preRelease == target.preRelease }
                }
                expr.startsWith("^") -> {
                    val target = Semver.parse(expr.removePrefix("^").trim())
                    return caretRange(target)
                }
                expr.startsWith("~") -> {
                    val target = Semver.parse(expr.removePrefix("~").trim())
                    return tildeRange(target)
                }
                else -> {
                    val target = Semver.parse(expr)
                    return { v -> v.major == target.major && v.minor == target.minor && v.patch == target.patch && v.preRelease == target.preRelease }
                }
            }
        }

        /**
         * Caret range: allows changes that do not modify the left-most non-zero digit.
         * ^1.2.3 := >=1.2.3, <2.0.0
         * ^0.2.3 := >=0.2.3, <0.3.0
         * ^0.0.3 := >=0.0.3, <0.0.4
         */
        private fun caretRange(target: Semver): (Semver) -> Boolean = { v ->
            when {
                target.major != 0 -> v >= target && v.major == target.major
                target.minor != 0 -> v >= target && v.major == 0 && v.minor == target.minor
                else -> v >= target && v.major == 0 && v.minor == 0 && v.patch == target.patch
            }
        }

        /**
         * Tilde range: allows patch-level changes.
         * ~1.2.3 := >=1.2.3, <1.3.0
         */
        private fun tildeRange(target: Semver): (Semver) -> Boolean = { v ->
            v >= target && v.major == target.major && v.minor == target.minor
        }
    }
}
