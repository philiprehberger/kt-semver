# Changelog

## 0.1.4

- Add badges and Development section to README

## [0.1.2] - 2026-03-18

- Upgrade to Kotlin 2.0.21 and Gradle 8.12
- Enable explicitApi() for stricter public API surface
- Add issueManagement to POM metadata

## 0.1.0 (2026-03-17)

- Add `Semver` data class with parsing, comparison, and string representation
- Add SemVer 2.0.0 compliant ordering including pre-release precedence
- Add `bumpMajor()`, `bumpMinor()`, `bumpPatch()` extension functions
- Add `withPreRelease()` and `withoutPreRelease()` extension functions
- Add `SemverRange` with support for `>=`, `<=`, `>`, `<`, `=`, `^`, `~`, AND, and OR operators
- Add `maxSatisfying()` to find highest version matching a range
