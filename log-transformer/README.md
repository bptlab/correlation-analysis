#

## Configuration

```
{
    "folder": "/home/user/data/procurement",
    "modelFile": "procurement.bpmn", // default: model.bpmn
    "eventLogFile": "procurement.csv", // default: eventlog.csv
    "caseAttributesFiles": [ "attributes1.csv", "attributes2.csv" ], // default: empty
    "dateFormat": "yyyy-MM-dd HH:mm", // default: yyyy-MM-dd HH:mm:ssX
    "separator": ";", // default: ,
    "caseIdName": "caseID", // default: caseid
    "timestampName": "finishedAt", // default: timestamp
    "activityName": "eventCode", // default: name
    "resourceName": "resource" // default: null (= no resource column present)
}
```

# Steps

- Add type information to CSV headers.
- Sort event log by caseid, timestamp using `head -n 1 > event_sorted.csv; and sed 1d event.csv | sort -t ";" -k 1,3 >> event_sorted.csv`.
- Specify paths in constants in `main-runner/.../Main.java`
- Make sure that activity names in model and event log are the same. Find unique names in the log with `cut -d ";" -f 2 orig_data/ReturnsEventLog_v2.csv | sort | uniq`
