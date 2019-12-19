echo "Creating LOCK at ${TARGET_DIR}"
touch "${TARGET_DIR}/download.lck"

cd /client
mvn exec:java -Dexec.mainClass="org.dbpedia.download.Client" -Dexec.args="-p ${TARGET_DIR} -c ${COLLECTION_URI}"

echo "Removing LOCK at ${TARGET_DIR}"
rm "${TARGET_DIR}/download.lck"
