# Log Transformation

## Usage

The application requires JVM version 11.

It can be executed using the packaged gradle wrapper by executing:
```
./gradlew run       # on linux
gradlew.bat run     # on windows
```

A configuration file can be provided using the `--args` flag:
```
./gradlew run --args="config.json"
```

Alternatively, load the gradle project with an IDE ([IntelliJ IDEA](https://www.jetbrains.com/idea/download) is recommended), and execute the `main` method of the `Main` class.


## Configuration

When running from command line, a configuration file can be provided.
It specifies options like model file, event log file, CSV separator etc.
The following options are possible:

```
{
     // required, specifies folder that contains model, event log and case attributes files
    "folder": "/home/user/data/procurement",

    // optional, specifies name of process model file in BPMN format, default: "model.bpmn"
    "modelFile": "procurement.bpmn",

    // optional, specifies name of event log file in CSV format, default: "eventlog.csv"
    "eventLogFile": "procurement.csv",

    // optional, specifies names of case attributes files in CSV format, default: empty
    "caseAttributesFiles": [ "attributes1.csv", "attributes2.csv" ],

    // optional, specifies ISO date format for timestamps inside the event and case attributes logs, default: "yyyy-MM-dd HH:mm:ssX"
    "dateFormat": "yyyy-MM-dd HH:mm",

    // optional, specifies CSV field separator, default: ","
    "separator": ";",

    // optional, specifies name of case id field in event log, default: "caseid"
    "caseIdName": "caseID",

    // optional, specifies name of timestamp field in event log, default: "timestamp"
    "timestampName": "finishedAt",

    // optional, specifies name of activity name field in event log, default: "name"
    "activityName": "eventCode",

    // optional, specifies name of resource field in event log, default: null (meaning "no resource column present")
    "resourceName": "resource"
}
```

An example configuration can be found in `config-example.json`.

When running from inside an IDE, it is also possible to specify the configuration in the code. See `Project.java`.

# Implementation Pointers

The application implements model analysis and log transformation.
Entry point is the `LogTransformationRunner.java`, which coordinates those steps.

Model analysis steps, as described in the thesis, can be found in the `de.hpi.bpt.logtransformer.modelanalysis.analysis` package.

Transformation operations, as described in the thesis, can be found in the `de.hpi.bpt.logtransformer.transformation.operations` package.
The implementation uses a self-written column-based in-memory format for performing operations on an event log and producing a case log.

# Input Requirements

CSV fields are treated as strings, if not otherwise specified in the CSV header.
Types can be specified by adding a `:<typename>` at the end of the field name, example: `timestamp:date`.
Possible types are (cf. `CsvLogReader.java`):

- `int`
- `string`
- `date`
- `boolean`
- `double`

For simplicity, the implementation requires the event log to be sorted by case id and timestamp, so that all events of a case appear after another in the log, in ascending timestamp order.

The activity names in the event log need to match the activity names in the model.
Events with activity names that do not have a corresponding activity in the model will be ignored for model-based features.
Hint: find unique names in the log with `cut -d "<CSV_DELIMITER>" -f <ACTIVITY_NAME_FIELD_INDEX> eventlog.csv | sort | uniq`.
