#! /usr/bin/env bash

RULES=$(cat ./$1/rules.txt)
CLUSTERS=$(cat ./$1/clusters.txt)
DIRECT_DEPENDENCIES=$(cat ./$1/directdependencies.txt)
RESULT=$(cat result.tpl \
| awk -v r="$1" '{gsub(/{{FOLDER}}/,r)}1' \
| awk -v r="${RULES//$'\n'/<br/>}" '{gsub(/{{RULES}}/,r)}1' \
| awk -v r="${CLUSTERS//$'\n'/<br/>}" '{gsub(/{{CLUSTERS}}/,r)}1' \
| awk -v r="${DIRECT_DEPENDENCIES//$'\n'/<br/>}" '{gsub(/{{DIRECT_DEPENDENCIES}}/,r)}1')
echo ${RESULT} > result.html

firefox file:///home/jonas/Data/result.html