# Readme

## Build instructions

### With tests and the jars included

```
mvn assembly:assembly -DdescriptorId=jar-with-dependencies
```

### Without tests (for debugging and tryouts)

```
mvn assembly:assembly -DdescriptorId=jar-with-dependencies -DskipTests
```

