## Design decisions

TODO("Explain in details the time perspective between the server vs the client")
Time is relative. Decided to get booking time as a UTC time from the server side.

## Assumptions

1. Since it is an all-day parking, the rule of 24hr in advance is assumed to mean 1 day prior, i.e. if on the 17th of
   April at 23:59 a customer book for the 18th, the booking will be successful and valid from midnight to 23:59 on the
   18th, provided bays are available. In that scenario, only the date part is significant.


2. For the rule where a customer can only make one booking a day, it is assumed that the exact time is observed, i.e. if
   a customer makes a booking on the 17th at 20:00, they will only be able to make another booking on the 18th after 20:00.
   

3. An assumption is made that this application will only serve local users, and therefore `LocalDateTime` for the local
   time zone will be used, whether created client-side or server-side. In this case, it will be created server-side (
   KISS and YAGNI principles).


4. A customer can only book one bay on any given days with their registration plate, i.e. no double bookings on the same
   day with the same plates.
