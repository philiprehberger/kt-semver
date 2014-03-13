package com.philiprehberger.semver

/**
 * Represents a semantic version following the [SemVer 2.0.0](https://semver.org/) specification.
 *
 * Versions are comparable: pre-release versions have lower precedence than the associated
 * normal version. Pre-release identifiers are compared numerically when both are numeric,
 * and lexically otherwise.
 *
 * @property major the major version number
 * @property minor the minor version number
 * @property patch the patch version number
 * @property preRelease the optional pre-release identifier (e.g. "alpha.1")
 * @property buildMetadata the optional build metadata (ignored in comparisons)
 */
data class Semver(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val preRelease: String? = null,
    val buildMetadata: String? = null,
) : Comparable<Semver> {

    init {
        require(major >= 0) { "Major version must be non-negative" }
        require(minor >= 0) { "Minor version must be non-negative" }
        require(patch >= 0) { "Patch version must be non-negative" }
    }

    /**
     * Constructs a [Semver] by parsing a version string.
     *
     * @param version the version string to parse (e.g. "1.2.3-alpha.1+build.42")
     * @throws IllegalArgumentException if the string is not a valid semantic version
     */
    constructor(version: String) : this(
        major = parse(version).major,
        minor = parse(version).minor,
        patch = parse(version).patch,
        preRelease = parse(version).preRelease,
        buildMetadata = parse(version).buildMetadata,
    )

    /**
     * Compares this version with [other] according to SemVer 2.0.0 precedence rules.
     *
     * Build metadata is ignored. Pre-release versions have lower precedence than
     * the associated normal version. Pre-release identifiers are compared
     * numerically when both are numeric, and lexically otherwise.
     */
    override fun compareTo(other: Semver): Int {
        // Compare major.minor.patch
        major.compareTo(other.major).let { if (it != 0) return it }
        minor.compareTo(other.minor).let { if (it != 0) return it }
        patch.compareTo(other.patch).let { if (it != 0) return it }

        // Pre-release comparison
        return when {
            preRelease == null && other.preRelease == null -> 0
            preRelease == null -> 1  // no pre-release > with pre-release
            other.preRelease == null -> -1
            else -> comparePreRelease(preRelease, other.preRelease)
        }
    }

    /**
     * Returns the canonical string representation of this version.
     */
    override fun toString(): String = buildString {
        append("$major.$minor.$patch")
        if (preRelease != null) append("-$preRelease")
        if (buildMetadata != null) append("+$buildMetadata")
    }

    companion object {
        private val SEMVER_REGEX = Regex(
            """^v?(\d+)\.(\d+)\.(\d+)(?:-([a-zA-Z0-9]+(?:\.[a-zA-Z0-9]+)*))?(?:\+([a-zA-Z0-9]+(?:\.[a-zA-Z0-9]+)*))?$"""
        )

        /**
         * Parses a version string into a [Semver] instance.
         *
         * Accepts an optional leading `v` prefix (e.g. "v1.2.3").
         *
         * @param version the version string to parse
         * @return the parsed [Semver]
         * @throws IllegalArgumentException if the string is not a valid semantic version
         */
        fun parse(version: String): Semver {
            val match = SEMVER_REGEX.matchEntire(version.trim())
                ?: throw IllegalArgumentException("Invalid semantic version: '$version'")

            return Semver(
                major = match.groupValues[1].toInt(),
                minor = match.groupValues[2].toInt(),
                patch = match.groupValues[3].toInt(),
                preRelease = match.groupValues[4].ifEmpty { null },
                buildMetadata = match.groupValues[5].ifEmpty { null },
            )
        }

        private fun comparePreRelease(a: String, b: String): Int {
            val partsA = a.split(".")
            val partsB = b.split(".")
            val len = minOf(partsA.size, partsB.size)

            for (i in 0 until len) {
                val numA = partsA[i].toLongOrNull()
                val numB = partsB[i].toLongOrNull()

                val cmp = when {
                    numA != null && numB != null -> numA.compareTo(numB)
                    numA != null -> -1  // numeric < alphanumeric
                    numB != null -> 1
                    else -> partsA[i].compareTo(partsB[i])
                }
                if (cmp != 0) return cmp
            }

            return partsA.size.compareTo(partsB.size)
        }
    }
}
