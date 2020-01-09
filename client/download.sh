echo "Creating LOCK at ${TARGET_DIR}"
touch "${TARGET_DIR}/download.lck"

cd /client/target

java -jar minimal-download-client-0.0.1-SNAPSHOT.jar -p ${TARGET_DIR} -c ${COLLECTION_URI}

echo "Removing LOCK at ${TARGET_DIR}"
rm "${TARGET_DIR}/download.lck"
