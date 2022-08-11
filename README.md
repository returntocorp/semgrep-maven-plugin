# semgrep-maven-plugin

`semgrep-maven-plugin` is a Maven plugin for running Semgrep scans during the Java build process.

## Local testing

1. Build and locally install the plugin:

```
mvn clean install
```

2. Run the plugin against a Java project!

```
mvn dev.r2c:semgrep-maven-plugin:1.0-SNAPSHOT:scan -Dsemgrep.config=auto
```

OR

```
mvn dev.r2c:semgrep-maven-plugin:1.0-SNAPSHOT:ci -DsemgrepAppToken=XXX
```