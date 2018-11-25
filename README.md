
# Tiny (short) URL Proxy API

A RESTful API that can be used to access resources through a short URL  
In contrast to similar tools it does not just redirect to the long URL, but it acts as the proxy instead.  
The long URL is not visible to the client. 

# These operations supported

* Create a tiny URL from a long URL
```
$ curl -H "Content-Type: application/json" -d '{"url": "http://www.google.com/"}' http://localhost:8081/api/tinyurl
{
  "id": 1,
  "url": "https://www.google.com/",
  "hashedKey": "ipkfwz"
}
```
* Access the resource behind the long URL using the tiny URL through the API
```
$ curl http://localhost:8081/go/ipkfwz
```
* Delete a shortened URL
```
curl -X DELETE http://localhost:8081/api/tinyurl/ipkfwz
```

# Setup

The default configuration uses Spring Boot configured to talk to an in memory H2 database to store the results
The built war file is also an executable jar with an embedded tomcat >= 9.x

## Dependencies

You need Java 8+ installed.


## Building

```
$ mvn package
```


## Running

You can run the app through Maven:

```
$ mvn spring-boot:run
```

or you can run the war file from the build:

```
$ java -jar target/tiny-urlproxy-1.0.0-SNAPSHOT.war
```

**Note for IntelliJ:**  
Click the Maven Projects tab/button on the right side of IntelliJ, and under Profiles, select intellij. This helps IntelliJ to use the tomcat dependencies when running the application.



