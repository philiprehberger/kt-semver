# Changelog

## 0.1.0 (2026-03-17)

- Add `Semver` data class with parsing, comparison, and string representation
- Add SemVer 2.0.0 compliant ordering including pre-release precedence
- Add `bumpMajor()`, `bumpMinor()`, `bumpPatch()` extension functions
- Add `withPreRelease()` and `withoutPreRelease()` extension functions
- Add `SemverRange` with support for `>=`, `<=`, `>`, `<`, `=`, `^`, `~`, AND, and OR operators
- Add `maxSatisfying()` to find highest version matching a range
