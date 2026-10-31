# Java Support Library
General-purpose Java support code, useable in most Java projects.

The intention is for this library to have as few third-party dependencies as possible, in order to not burden
down-stream users with these same dependencies.

Published as a Maven package on [GitHub Packages](https://github.com/gpellicciotta/support.java/packages),
licensed under the [MIT License](LICENSE.md). Requires JDK 25 or later.

## Using the Library
Add the GitHub Packages repository and the dependency to your `build.gradle`:

```groovy
repositories {
  maven {
    url = 'https://maven.pkg.github.com/gpellicciotta/support.java'
    credentials {
      username = project.findProperty('github.username') ?: System.getenv('GITHUB_USERNAME')
      password = project.findProperty('github.token') ?: System.getenv('GITHUB_TOKEN')
    }
  }
}

dependencies {
  implementation 'com.pellicciotta:support.java:1.0.0'
}
```

A GitHub personal access token with `read:packages` is needed, set in `{user.home}/.gradle/gradle.properties` or
as the `GITHUB_USERNAME` and `GITHUB_TOKEN` environment variables.

## Packages
Everything lives under `com.pellicciotta.support`, one package per concern: `logging`, `config`, `codecs`, `app`,
`http`, `sql`, `mail`, `diff`, `text`, `utils`, `cache`, `io`, `exceptions`, and `annotations`. Each package's
`package-info.java` documents its design.

## Development
Open a shell in the project root and run:

```shell
python scripts/bootstrap-dev-environment.py
```

This initializes Git if needed and runs `gradlew build`, which compiles, checks formatting and style, runs the tests, and
produces the jars in `build/libs/`. Day-to-day operations go through `python scripts/devops.py help`.

See also:
- [CHANGELOG](CHANGELOG.md)
- [Documentation index](docs/index.md)
- [PLAN](PLAN.md)
