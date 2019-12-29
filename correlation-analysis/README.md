# Correlation Analysis

## Setup

The application requires JVM version 11.

The server can be started using the packaged gradle wrapper by executing:
```
./gradlew run       # on linux
gradlew.bat run     # on windows
```

Alternatively, load the gradle project with an IDE ([IntelliJ IDEA](https://www.jetbrains.com/idea/download) is recommended), and execute the `main` method of the `Demo` class.

## Usage

Navigate to `http://localhost:8080` in your browser to start the analysis.

The case data needs to be provided as `ARFF` file, as produced by the [log transformation](../log-transformer/README.md) application.

## Implementation Pointers

This implementation relies heavily on the [WEKA](https://www.cs.waikato.ac.nz/ml/weka/) machine learning library.
Data preparation, feature selection and classification is implemented in the classes inside the `framework` package.
They are called from the `CorrelationAnalysisRuner`.

A [Pippo](http://www.pippo.ro/doc/server.html) server is started, that delivers static, server-side rendered pages from `resources/templates`,
filling them with the results of the correlation analysis.
The server, with its endpoints and responses is defined in `CorrelationAnalysisApplication.java`.
The endpoints are called from the HTML forms defined in the templates.