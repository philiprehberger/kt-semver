package com.philiprehberger.semver

/**
 * Returns a new [Semver] with the major version incremented by one.
 *
 * Minor and patch are reset to zero. Pre-release and build metadata are cleared.
 */
fun Semver.bumpMajor(): Semver = Semver(major + 1, 0, 0)

/**
 * Returns a new [Semver] with the minor version incremented by one.
 *
 * Patch is reset to zero. Pre-release and build metadata are cleared.
 */
fun Semver.bumpMinor(): Semver = Semver(major, minor + 1, 0)

/**
 * Returns a new [Semver] with the patch version incremented by one.
 *
 * Pre-release and build metadata are cleared.
 */
fun Semver.bumpPatch(): Semver = Semver(major, minor, patch + 1)

/**
 * Returns a new [Semver] with the given pre-release identifier set.
 *
 * Build metadata is preserved.
 *
 * @param pre the pre-release identifier (e.g. "alpha.1", "rc.2")
 */
fun Semver.withPreRelease(pre: String): Semver = copy(preRelease = pre)

/**
 * Returns a new [Semver] with the pre-release identifier removed.
 *
 * Build metadata is preserved.
 */
fun Semver.withoutPreRelease(): Semver = copy(preRelease = null)
