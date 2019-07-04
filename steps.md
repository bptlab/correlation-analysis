1. Add type information to CSV headers
2. Sort event log by caseid, timestamp using `csvsort -c "CaseId:string","Timestamp:date" event.csv > event_sorted.csv`
3. Load event log into log-transformer, do case log transformation
4. Join transformed case log and attributes, using `csvjoin -c "CaseId:string" case.csv case_attributes.csv --left > case_joined.csv`
5. Transform into ARFF file using log-transformer
6. Load ARFF file into feature-evaluator and run