#!/usr/bin/env bash
if [ -z ${GRAPH_MODE+x} ]; then GRAPH_MODE='no'; fi
if [ -z ${TARGET_DIR+x} ]; then TARGET_DIR='/root/data'; fi

echo "Creating LOCK at ${TARGET_DIR}"
touch "${TARGET_DIR}/download.lck"

cd /client/target

java -jar minimal-download-client-0.0.1-SNAPSHOT.jar -p ${TARGET_DIR} -c ${COLLECTION_URI} -g ${GRAPH_MODE}

echo "Removing LOCK at ${TARGET_DIR}"
rm "${TARGET_DIR}/download.lck"
