# An implementation of a distributed job queue based on Java Spring and Redis

This is an demonstration of lightweight yet well-functioned implementation of distributed job queue based on Redis and Spring java.

I used Lettuce to be the Redis client, Jackson for serializing/deserializing jobs when being stored in Redis. They are all included in Spring's various
modules as default option so no need for extra depenency.
