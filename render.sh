#! /usr/bin/env bash

cp ./result.tpl ./result.html
RULES=$(cat ./$1/rules.txt)
CLUSTERS=$(cat ./$1/clusters.txt)
DIRECT_DEPENDENCIES=$(cat ./$1/directdependencies.txt)
gawk -i inplace -v r="$1" '{gsub(/{{FOLDER}}/,r)}1' result.html
gawk -i inplace -v r="${RULES}" '{gsub(/{{RULES}}/,r)}1' result.html
gawk -i inplace -v r="${CLUSTERS}" '{gsub(/{{CLUSTERS}}/,r)}1' result.html
gawk -i inplace -v r="${DIRECT_DEPENDENCIES}" '{gsub(/{{DIRECT_DEPENDENCIES}}/,r)}1' result.html

firefox file:///home/jonas/Data/result.html