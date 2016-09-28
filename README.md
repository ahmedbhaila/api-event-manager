# event-api-manager
A RESTful API for creating:

* Events
* Users
* Registering a User with an Event

For a full list of allowed operations and their syntax, please refer to: https://event-api-manager.herokuapp.com/api-guide.html (mirror: https://cdn.rawgit.com/ahmedbhaila/event-api-manager/master/src/main/resources/static/api-guide.html)

# Live Demo
The REST endpoints are available at: https://event-api-manager.herokuapp.com. Follow the api-guide.html for syntax
* Events -> /v1/event(s)
* User -> /v1/user
* Register -> /v1/register


# Project Setup
event-api-manager is a Spring Boot app. The application uses Redis for data storage. All endpoints are secured using Basic HTTP Auth. You will need to supply username: admin and password: admin in order to successfully access the endpoints.To build the application from command line, you will first need to export a REDISCLOUD_URI datasource.

> export REDISCLOUD_URI=redis://username:password@{redis_ip}:{redis_port}

After exporting, you can now build the application using gradle:
> gradle clean build jar -- this will compile and run unit and integration tests before building an executable jar. 

Next, run the executable jar from command-line:
> java -jar build/libs/deus-ex-machina-0.0.1-SNAPSHOT.jar

Alternatively, you can also run the application using gradle bootRun:

> gradle bootRun

If you are using an IDE to setup this project, you will need to install Project Lombok which is used to automatically generate Constructors, Builder Pattern object among other things. Please refer to https://projectlombok.org/ for installation.

# The REST API was developed using
* Spring Boot - Core development
* Spring Security - Securing all endpoints using Http Basic Auth
* Spring Data Redis - Using Redis as a datastore with supporting APIs
* Project Lombok - For auto-generation of accessors/mutators,builders
* Hibernate Validator - For Data validation
* Spring Test - For Unit + Integration tests
* Spring REST Docs - For auto-generation of documentation
* Java 8 Streams



