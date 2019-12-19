echo "Creating LOCK at ${SRC_VOLUME_DIR}"
touch "${SRC_VOLUME_DIR}/download.lck"

cd /client
mvn exec:java -Dexec.mainClass="org.dbpedia.download.Client" -Dexec.args="-p ${SRC_VOLUME_DIR} -c ${COLLECTION_URI}"

echo "Removing LOCK at ${SRC_VOLUME_DIR}"
rm "${SRC_VOLUME_DIR}/download.lck"
