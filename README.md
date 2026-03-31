# semver

[![Tests](https://github.com/philiprehberger/kt-semver/actions/workflows/publish.yml/badge.svg)](https://github.com/philiprehberger/kt-semver/actions/workflows/publish.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.philiprehberger/semver.svg)](https://central.sonatype.com/artifact/com.philiprehberger/semver)
[![Last updated](https://img.shields.io/github/last-commit/philiprehberger/kt-semver)](https://github.com/philiprehberger/kt-semver/commits/main)

Semantic version parsing, comparison, and range matching for Kotlin.

## Installation

### Gradle Kotlin DSL

```kotlin
implementation("com.philiprehberger:semver:0.1.6")
```

### Maven

```xml
<dependency>
    <groupId>com.philiprehberger</groupId>
    <artifactId>semver</artifactId>
    <version>0.1.6</version>
</dependency>
```

## Usage

```kotlin
import com.philiprehberger.semver.*

// Parse and compare
val v1 = Semver.parse("1.2.3")
val v2 = Semver.parse("2.0.0-alpha")
println(v1 < v2)  // true

// Construct directly
val v = Semver(1, 2, 3, preRelease = "beta.1")

// Bump versions
v.bumpMajor()  // 2.0.0
v.bumpMinor()  // 1.3.0
v.bumpPatch()  // 1.2.4

// Pre-release
v.withPreRelease("rc.1")  // 1.2.3-rc.1
v.withoutPreRelease()     // 1.2.3

// Range matching
val range = SemverRange("^1.2.0")
range.matches(Semver(1, 5, 0))  // true
range.matches(Semver(2, 0, 0))  // false

// Find highest matching version
val versions = listOf(Semver(1, 0, 0), Semver(1, 5, 0), Semver(2, 0, 0))
range.maxSatisfying(versions)  // Semver(1, 5, 0)
```

## API

| Function / Class | Description |
|---|---|
| `Semver(major, minor, patch, preRelease?, buildMetadata?)` | Construct a version directly |
| `Semver.parse(version)` | Parse a version string (accepts optional `v` prefix) |
| `Semver.compareTo(other)` | SemVer 2.0.0 precedence comparison |
| `Semver.bumpMajor()` | Increment major, reset minor and patch |
| `Semver.bumpMinor()` | Increment minor, reset patch |
| `Semver.bumpPatch()` | Increment patch |
| `Semver.withPreRelease(pre)` | Set pre-release identifier |
| `Semver.withoutPreRelease()` | Remove pre-release identifier |
| `SemverRange(expression)` | Create a range from expression string |
| `SemverRange.matches(version)` | Test if version satisfies the range |
| `SemverRange.maxSatisfying(versions)` | Find highest matching version |

### Range operators

| Operator | Example | Meaning |
|---|---|---|
| `>=` | `>=1.2.0` | Greater than or equal |
| `<=` | `<=2.0.0` | Less than or equal |
| `>` | `>1.0.0` | Greater than |
| `<` | `<2.0.0` | Less than |
| `=` | `=1.2.3` | Exact match |
| `^` | `^1.2.3` | Compatible (same left-most non-zero digit) |
| `~` | `~1.2.3` | Approximately (same major.minor) |
| ` ` (space) | `>=1.0.0 <2.0.0` | AND (intersection) |
| `\|\|` | `1.0.0 \|\| 2.0.0` | OR (union) |

## Development

```bash
./gradlew build
./gradlew test
```

## Support

If you find this project useful:

⭐ [Star the repo](https://github.com/philiprehberger/kt-semver)

🐛 [Report issues](https://github.com/philiprehberger/kt-semver/issues?q=is%3Aissue+is%3Aopen+label%3Abug)

💡 [Suggest features](https://github.com/philiprehberger/kt-semver/issues?q=is%3Aissue+is%3Aopen+label%3Aenhancement)

❤️ [Sponsor development](https://github.com/sponsors/philiprehberger)

🌐 [All Open Source Projects](https://philiprehberger.com/open-source-packages)

💻 [GitHub Profile](https://github.com/philiprehberger)

🔗 [LinkedIn Profile](https://www.linkedin.com/in/philiprehberger)

## License

[MIT](LICENSE)
