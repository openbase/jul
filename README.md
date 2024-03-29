# JUL - A Java Utility Library 

[![Dev](https://github.com/openbase/jul/actions/workflows/build-and-test.yml/badge.svg?branch=dev)](https://github.com/openbase/jul/actions/workflows/build-and-test.yml)
[![Maven Central](https://img.shields.io/maven-central/v/org.openbase/jul.svg?label=Latest%20Version)](https://search.maven.org/artifact/org.openbase/jul)

A collection of java / kotlin utilities featured by [openbase.org](https://openbase.org).

## Features
* MQTT RPC Communication Pattern
* MQTT Server - Client Communication Pattern
* Advanced Exception Handling Utilities
* Protobuf in Memory Repositories with JSon persistence. 

## Contribution

Feel free to report feature requests and discovered bugs via [github](https://github.com/openbase/jul/issues/new).
- If you want to contribute to jul, just fork the repositories, apply your changes and create a new pull request.
- For long term contribution you are welcome to apply for an openbase membership via support@openbase.org or by joining our [Discord Server](https://discord.com/invite/M48eh76f?utm_source=Discord%20Widget&utm_medium=Connect).

## Development

### Update Gradle Dependencies

We are using a plugin called `Gradle refreshVersions` to manage all our backend dependencies. Thus, all dependencies
declared within `build.gradle.kts` provide a placeholder `_` for their version while each version is maintained within
the `versions.properties`.

```
testImplementation("io.mockk:mockk:_")
```

In order to check for updates just execute `gradle refreshVersions`. Afterwards, you will find all latest versions
within the `versions.properties` file.

```
version.mockk=1.11.0
### available=1.12.0
```

In order to update a dependency, just add the version you prefer to the version declaration in `versions.properties`.

```
version.mockk=1.12.0
```

The next gradle build will use the new dependency version without any further steps being required. Don't forget to sync
your gradle settings within IntelliJ in case you are not using the gradle `auto-reload setting` feature.

Further details about the plugin can be found at: https://jmfayard.github.io/refreshVersions/

