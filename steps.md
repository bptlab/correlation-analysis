# Steps

- Add type information to CSV headers.
- Sort event log by caseid, timestamp using `head -n 1 > event_sorted.csv; and sed 1d event.csv | sort -t ";" -k 1,3 >> event_sorted.csv`.
- Specify paths in constants in `main-runner/.../Main.java`
- Make sure that activity names in model and event log are the same. Find unique names in the log with `cut -d ";" -f 2 orig_data/ReturnsEventLog_v2.csv | sort | uniq`
