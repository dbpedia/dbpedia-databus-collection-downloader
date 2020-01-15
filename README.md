# Minimal Download Client

This is a light-weight dockerized data-downloader for the DBpedia Databus. This container takes a collection URI and simply pulls its data to a local directory. It does not support any format or compression conversion. If your application requires data in a format that is not available on the Databus, please use the full DBpedia Download Client. Run the container with the following two environment variables:
* `TARGET_DIR`: The target directory for the downloaded files
* `COLLECTION_URI`: A collection URI on the DBpedia Databus

In order to retrieve the downloaded files on your local machine you should mount a volume to the specified `TARGET_DIR`.

## Docker Image

You can find the docker image on Docker Hub [here]() or build it yourself by running

```docker build -t databus-download-min .``` 

in the projects root directory.

## Lock File

In order to make this container interoperable with others, the process creates a `download.lck` file in the `TARGET_DIR` directory
on startup and removes it once the download has finished.
Other containers with access to the mounted folder can check if this file exists to wait for the download process to terminate.

## Example
The `docker-compose.yml` in this repo shows a possible configuration for the download client.

```
version: "3.0"
services:
  databus-download-min:
    image: databus-download-min:latest
    environment:
      COLLECTION_URI: https://databus.dbpedia.org/dbpedia/collections/pre-release-2019-08-30/
      TARGET_DIR: /root/data
    volumes:
      - ./download:/root/data
```

This configuration will download the DBpedia Pre-Release-Dataset v.2019-08-30 into a `download` folder next to your `docker-compose.yml`

In order to access the files on your local machine you should mount a volume to the `TARGET_DIR` folder.
