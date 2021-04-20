# Parking reservations

This repository is
a [coding exercise](https://docs.google.com/document/d/1GzN2bWTtpzNb6xgnidLlxYMscXDbX6bIBrx0DqAiBZ4/edit) received from
Adapptor as part of their recruitment process.

## Table of Contents

- [Technologies](#technologies)
- [Running the tests](#running-the-tests)
- [Assumptions](#assumptions)
- [Design decisions](#design-decisions)
- [Trade-offs](#trade-offs)

## Technologies

- Kotlin
- IntelliJ IDE
- Gradle build

## Running the tests

Execute the tests with:

```bash
$ ./gradlew test
```

> Use `gradlew.bat` if you're on Windows

A test report will be generated at `./build/reports/tests/test/index.html`

## Design decisions

I have approached the exercise from a client/server perspective, i.e. a customer uses a mobile application to book a car
bay. All design decisions were made along the lines of this assumption.

For example:

The app displays a `DatePicker` for the user to choose their booking date.

A network request is sent to the server to satisfy the booking request.

On the server, all the business rules are checked against the booking date and the customer. If all checks pass, the
booking is a success. If the booking fails to satisfy a rule, the server sends back an error response with the reason
for the failure.

It is assumed that, in such an architecture and as we scale, different servers and users could be located in different
timezones. In such a scenario, there is always a question of which date and time to use as timestamp. I my case, after
[reading up on the subject](https://kotlinfrompython.com/2020/07/14/dates-datetimes-timestamps/), I decided to use the
UTC time zone everywhere.

## Trade-offs

The trade-off here is that a feature to show a customer all their previous bookings would show the date and time in UTC.
Ideally, if this was an exercise in implementing both the front-end and the back-end, the mobile app would send through
the timezone of the client to be stored by the server.

Instead, querying all bookings for a specific user will show the date and time with respect to the UTC timezone.

As the requirement is instead to query all bookings for any customer on a date, this is not much of a trade-off.

## Assumptions

1. Since it is an all-day parking, the rule of 24hr in advance is assumed to mean 1 day prior, i.e. if on the 17th of
   April at 23:59 a customer book for the 18th, the booking will be successful and valid from 00:00 to 23:59 on the
   18th, provided bays are available. In that scenario, only the date part is significant.


2. For the rule where a customer can only make one booking a day, it is assumed that the exact time is observed, i.e. if
   a customer makes a booking on the 17th at 20:00, they will only be able to make another booking at 20:00 on the 18th.


4. A customer can only book one bay on any given days with their registration plate, i.e. no double bookings on the same
   day with the same plates.


5. Assumed that there is a client/server relationship and that the client could be in a different timezone than the
   server instances. In which case, the UTC time zone was used whenever a timestamp is required.


6. Assumed that when a customer books a bay in the car park, it can be any available bay, i.e. bays do not have unique
   identifiers for customers to book a specific bay.
