# Demo of RESTful CRUD Operations for a Map of String Keys and Values

## Build

Linux

```
./gradlew build
```

Windows

```
.\gradlew.bat build
```

All unit tests will be run with `build`. Or if you want to run test specifically, use `gradlew(.bat) test`.

## Run

To start the micro service on Linux,

```
./gradlew bootRun
```

On Windows

```
.\gradlew.bat bootRun
```

The service will bind to TCP port 8080 upon successful start, available at `http://localhost:8080/mappings`.

## Manual Test

Once the microservice is started, you may use many tools available to test the RESTful API. For example, with `curl` command.

### CREATE a Mapping

```
curl -i -H 'Content-Type:application/json' -X POST -d '{"key":"your key", "value":"your value"}' http://localhost:8080/mappings
```

### UPDATE a Mapping

```
curl -i -H 'Content-Type:application/json' -X PUT -d '{"key":"your key", "value":"new value"}' http://localhost:8080/mappings/1
```

### DELETE a Mapping

```
curl -i -X DELETE http://localhost:8080/mappings/1
```

### RETRIEVE all Mappings

```
curl -i -X GET http://localhost:8080/mappings
```

### RETRIEVE all Mappings with pagination and sorting

```
curl -i -X GET http://localhost:8080/mappings?page=0&size=100&sort=key,desc
```

### RETRIEVE a Mappings

```
curl -i -X GET http://localhost:8080/mappings/1
```

### SEARCH for a Specific Mapping by key or by value

```
curl -i -X GET http://localhost:8080/mappings/search/findByKey?key=your%20key
curl -i -X GET http://localhost:8080/mappings/search/findByValue?value=your%20value
```
