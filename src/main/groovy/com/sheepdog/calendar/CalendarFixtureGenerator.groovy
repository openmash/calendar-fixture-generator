package com.sheepdog.calendar

import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.model.AclRule
import com.google.api.services.calendar.model.CalendarList
import com.google.api.services.calendar.model.CalendarListEntry
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventAttendee
import com.google.api.services.calendar.model.EventDateTime;
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.LocalTime
import org.joda.time.Months
import org.joda.time.Weeks

enum EventFrequency {
    DAILY, WEEKLY, MONTHLY, ONCE, START, END

    def int[] getEventLength() {
        switch (this) {
            case DAILY:
                return [5, 10, 15, 20, 30]
            case WEEKLY:
                return [45, 60, 90, 115, 120]
            case MONTHLY:
                return [60, 90, 120, 180]
            case ONCE:
            case START:
            case END:
                return [60, 90, 120, 180, 240]
            default:
                return []
        }
    }
}

class EventDesc {
    String name
    EventFrequency frequency
    int attendance // 1 .. 100
    boolean isAllDay = false

    def String[] takeUsers(String[] users) {
        int numberSelected = users.length * attendance / 100
        if (numberSelected < 1)
            numberSelected = 1
        Collections.shuffle(Arrays.asList(users))
        users.take(numberSelected)
    }
}

class ProjectDesc {
    String name
    String[] users
    DateTime start
    DateTime end
    EventDesc[] events
}

def String[] makeUsers(int start, int end) {
    (start..end).collect {"user${it}@test3.sheepdoginc.ca"}
}

class ConcreteEvent {
    DateTime startTime
    DateTime endTime
    String recurrence = null
    String summary
    boolean isAllDay = false
    String[] users
}

class RandomState {
    static final Random random = new Random()

    static long between(long low, long high) {
        low + random.nextInt((int)(high - low))
    }

    static <T> T choose(T[] list) {
        list[random.nextInt(list.length)]
    }
}

def DateTime randomDate(DateTime startDate, DateTime endDate) {
    long start = startDate.withMillisOfDay(0).getMillis()
    long end = endDate.plusDays(1).withMillisOfDay(0).minusMillis(1).getMillis()
    long millis = RandomState.between(start, end)
    new DateTime(millis)
}

def ConcreteEvent[] generateProjectEvents(ProjectDesc[] projects) {
    ConcreteEvent[] concreteEvents = []

    for (project in projects) {
        for (event in project.events) {
            DateTime firstOccurrence
            String recurrence = null
            int eventLength = RandomState.choose(event.frequency.getEventLength())
            String summary = "${project.name} ${event.name}"

            int startQuarterHour = RandomState.between(36, 66)
            LocalTime startTime = new LocalTime((int)(startQuarterHour / 4), 15 * (startQuarterHour % 4))
            LocalTime endTime = startTime.plusMinutes(eventLength)

            switch (event.frequency) {
                case EventFrequency.START:
                    firstOccurrence = project.start
                    break
                case EventFrequency.END:
                    firstOccurrence = project.end
                    break
                case EventFrequency.ONCE:
                    firstOccurrence = randomDate(project.start, project.end)
                    break
                case EventFrequency.DAILY:
                    firstOccurrence = project.start
                    int count = Days.daysBetween(project.start, project.end).days
                    recurrence = "RRULE:FREQ=DAILY;COUNT=${count};BYDAY=MO,TU,WE,TH,FR"
                    break
                case EventFrequency.WEEKLY:
                    firstOccurrence = project.start.plusDays(RandomState.random.nextInt(7))
                    int count = Weeks.weeksBetween(project.start, project.end).weeks
                    recurrence = "RRULE:FREQ=WEEKLY;COUNT=${count}"
                    break
                case EventFrequency.MONTHLY:
                    firstOccurrence = project.start.plusDays(RandomState.random.nextInt(15))
                    int count = Months.monthsBetween(project.start, project.end).months
                    recurrence = "RRULE:FREQ=MONTHLY;COUNT=${count}"
                    break
                default:
                    firstOccurrence = project.start
                    break
            }

            DateTime startDateTime = startTime.toDateTime(firstOccurrence)
            DateTime endDateTime = endTime.toDateTime(firstOccurrence)
            concreteEvents += new ConcreteEvent(
                startTime: startDateTime,
                endTime: endDateTime,
                summary: summary,
                recurrence: recurrence,
                users: event.takeUsers(project.users)
            )
        }
    }

    concreteEvents
}

class PersonalEventGenerator {
    static String[] EVENT_TYPES = [
         "Coffee with %s",
         "Meeting with %s",
         "Meeting %s",
         "Get-together %s",
         "%s",
         "Catch-up with %s"
    ]

    static String[] FIRST_NAMES = [
            "James",
            "John",
            "Robert",
            "Michael",
            "Mary",
            "William",
            "David",
            "Richard",
            "Charles",
            "Joseph",
            "Thomas",
            "Patricia",
            "Christopher",
            "Linda",
            "Barbara",
            "Daniel",
            "Paul",
            "Mark",
            "Elizabeth",
            "Donald",
            "Jennifer",
            "George",
            "Maria",
            "Kenneth",
            "Susan",
            "Steven",
            "Edward",
            "Margaret",
            "Brian",
            "Ronald",
            "Dorothy",
            "Anthony",
            "Lisa"
    ]

    static String[] LAST_NAMES = [
            "Smith",
            "Johnson",
            "Williams",
            "Jones",
            "Brown",
            "Davis",
            "Miller",
            "Wilson",
            "Moore",
            "Taylor",
            "Anderson",
            "Thomas",
            "Jackson",
            "White",
            "Harris",
            "Martin",
            "Thompson",
            "Garcia",
            "Martinez",
            "Robinson",
            "Clark",
            "Rodriguez",
            "Lewis",
            "Lee",
            "Walker",
            "Hall",
            "Allen",
            "Young",
            "Hernandez",
            "King",
            "Wright",
            "Lopez",
            "Hill",
            "Scott",
            "Green",
            "Adams",
            "Baker",
            "Gonzalez",
    ]

    static ConcreteEvent[] generate(DateTime start, DateTime end, String[] users) {
        ConcreteEvent[] concreteEvents = []

        for (user in users) {
            DateTime cursor = start
            while (cursor.compareTo(end) < 0) {
                int count

                if (RandomState.random.nextInt(10) < 3) {
                    count = RandomState.between(1, 3)
                } else {
                    count = 0
                }

                for (_ in 0..count) {
                    String eventType = RandomState.choose(EVENT_TYPES)
                    String firstName = RandomState.choose(FIRST_NAMES)
                    String lastName = RandomState.choose(LAST_NAMES)
                    String summary = String.format(eventType, "${firstName} ${lastName}")

                    int startQuarterHour = RandomState.between(28, 84)
                    LocalTime startTime = new LocalTime((int)(startQuarterHour / 4), 15 * (startQuarterHour % 4))
                    LocalTime endTime = startTime.plusMinutes((int)(RandomState.between(2, 10) * 15))
                    DateTime startDateTime = startTime.toDateTime(cursor)
                    DateTime endDateTime = endTime.toDateTime(cursor)
                    concreteEvents += new ConcreteEvent(
                        startTime: startDateTime,
                        endTime: endDateTime,
                        summary: summary,
                        users: [user],
                    )
                }

                cursor = cursor.plusDays(1)
            }
        }

        concreteEvents
    }
}

ConcreteEvent[] generateVacationEvents(DateTime start, DateTime end, String[] users) {
    ConcreteEvent[] concreteEvents = []

    for (user in users) {
        DateTime cursor = start
        while (cursor.compareTo(end) < 0) {
            if (RandomState.random.nextInt(20) < 1) {
                int length = RandomState.between(1, 8)

                concreteEvents += new ConcreteEvent(
                    summary: "${user}",
                    startTime: cursor.withMillisOfDay(0),
                    endTime: cursor.plusDays(length).withMillisOfDay(0),
                    users: [user],
                    isAllDay: true,
                )

                cursor = cursor.plusDays(length + 1)
            } else {
                cursor = cursor.plusDays(1)
            }
        }
    }

    concreteEvents
}

class CalendarManager {
    final Map<String, Calendar> calendars = [:]
    final String admin = "admin@test3.sheepdoginc.ca"
    CalendarClient calendarClient

    CalendarManager(String propertiesFileName, String[] users) {
        calendarClient = new CalendarClient(new File(propertiesFileName))

        for (user in users) {
            calendars[user] = calendarClient.getCalendar(user)
        }

        calendars[admin] = calendarClient.getCalendar(admin)
    }

    void clearEvents() {
        print("clearing events: ")
        for (e in calendars) {
            e.value.calendars().clear(e.key).execute()
            print(".")
        }
        println()
    }

    private static EventDateTime getDateTime(DateTime dateTime) {
        def wrappedDateTime = new com.google.api.client.util.DateTime(dateTime.getMillis())
        new EventDateTime().setDateTime(wrappedDateTime).setTimeZone("-0700")
    }

    void deleteCalendars() {
        CalendarList list = calendars[admin].calendarList().list().execute()
        for (item in list.getItems()) {
            if (item.getAccessRole() == "owner" && !item.getPrimary()) {
                calendars[admin].calendars().delete(item.getId()).execute()
            }
        }
    }

    String createCalendar(String summary, String[] users) {
        com.google.api.services.calendar.model.Calendar calendar = new com.google.api.services.calendar.model.Calendar()
        calendar.setSummary(summary)
        calendar = calendars[admin].calendars().insert(calendar).execute()

        AclRule aclRule = new AclRule()
                .setScope(new AclRule.Scope()
                        .setType("domain")
                        .setValue("test3.sheepdoginc.ca"))
                .setRole("writer")

        calendars[admin].acl().insert(calendar.getId(), aclRule).execute()

        for (user in users) {
            CalendarListEntry listEntry = new CalendarListEntry().setId(calendar.getId())
            calendars[user].calendarList().insert(listEntry).execute()
        }

        calendar.getId()
    }

    void postEvent(String creator, String calendarId, ConcreteEvent concreteEvent) {

        Event event = new Event()
            .setSummary(concreteEvent.summary)
            .setStart(getDateTime(concreteEvent.startTime))
            .setEnd(getDateTime(concreteEvent.endTime))
            .setCreator(new Event.Creator().setSelf(true))
            .setAttendees(concreteEvent.users.collect {
                new EventAttendee()
                    .setEmail(it)
                    .setResponseStatus("accepted")
            })

        if (concreteEvent.recurrence != null)
            event.setRecurrence([concreteEvent.recurrence])

        calendars[creator].events().insert(calendarId, event).execute()
    }

    void postEvents(ConcreteEvent[] concreteEvents) {
        print("creating events: ")
        for (concreteEvent in concreteEvents) {
            String creator = concreteEvent.users[0]
            postEvent(creator, creator, concreteEvent)
            print(".")
        }
        println()
    }

    void postEventsTo(String calendarId, ConcreteEvent[] concreteEvents) {
        print("creating events: ")
        for (concreteEvent in concreteEvents) {
            String creator = concreteEvent.users[0]
            postEvent(creator, calendarId, concreteEvent)
            print(".")
        }
        println()
    }
}

ProjectDesc[] projects = [
        new ProjectDesc(
                name: "Project Alpha",
                users: makeUsers(30, 50),
                start: DateTime.parse("2013-06-01"),
                end: DateTime.parse("2013-11-01"),
                events: [
                        new EventDesc(name: "kickoff", frequency: EventFrequency.START, attendance: 40),
                        new EventDesc(name: "meeting", frequency: EventFrequency.DAILY, attendance: 100),
                        new EventDesc(name: "review", frequency: EventFrequency.WEEKLY, attendance: 70),
                        new EventDesc(name: "closeout", frequency: EventFrequency.END, attendance: 100),
                ]
        ),
        new ProjectDesc(
                name: "Project Omega",
                users: makeUsers(24, 50),
                start: DateTime.parse("2013-06-05"),
                end: DateTime.parse("2013-11-10"),
                events: [
                        new EventDesc(name: "kickoff", frequency: EventFrequency.START, attendance: 40),
                        new EventDesc(name: "meeting", frequency: EventFrequency.DAILY, attendance: 100),
                        new EventDesc(name: "review", frequency: EventFrequency.WEEKLY, attendance: 70),
                        new EventDesc(name: "closeout", frequency: EventFrequency.END, attendance: 100),
                ]
        ),
        new ProjectDesc(
                name: "Delta endeavour",
                users: makeUsers(20, 45),
                start: DateTime.parse("2013-05-19"),
                end: DateTime.parse("2013-06-13"),
                events: [
                        new EventDesc(name: "kickoff", frequency: EventFrequency.START, attendance: 100),
                        new EventDesc(name: "meeting", frequency: EventFrequency.DAILY, attendance: 80),
                        new EventDesc(name: "review", frequency: EventFrequency.WEEKLY, attendance: 70),
                        new EventDesc(name: "closeout", frequency: EventFrequency.END, attendance: 100),
                ]
        ),
        new ProjectDesc(
                name: "Lambda undertaking",
                users: makeUsers(30, 40),
                start: DateTime.parse("2013-06-09"),
                end: DateTime.parse("2013-06-13"),
                events: [
                        new EventDesc(name: "standup", frequency: EventFrequency.DAILY, attendance: 80),
                        new EventDesc(name: "retrospective", frequency: EventFrequency.WEEKLY, attendance: 70),
                        new EventDesc(name: "budget meeting", frequency: EventFrequency.MONTHLY, attendance: 30),
                        new EventDesc(name: "planning", frequency: EventFrequency.WEEKLY, attendance: 100),
                        new EventDesc(name: "closeout", frequency: EventFrequency.END, attendance: 100),
                ]
        ),
        new ProjectDesc(
                name: "All hands",
                users: makeUsers(20, 50),
                start: DateTime.parse("2013-06-03"),
                end: DateTime.parse("2013-10-29"),
                events: [
                        new EventDesc(name: "meeting", frequency: EventFrequency.DAILY, attendance: 100),
                        new EventDesc(name: "training", frequency: EventFrequency.WEEKLY, attendance: 80),
                        new EventDesc(name: "team building", frequency: EventFrequency.WEEKLY, attendance: 80),
                ]
        ),
        new ProjectDesc(
                name: "Health and safety",
                users: makeUsers(23, 43),
                start: DateTime.parse("2013-06-07"),
                end: DateTime.parse("2013-11-11"),
                events: [
                        new EventDesc(name: "inspection", frequency: EventFrequency.WEEKLY, attendance: 100),
                        new EventDesc(name: "committee meeting", frequency: EventFrequency.MONTHLY, attendance: 10),
                ]
        )
]

CalendarManager calendarManager = new CalendarManager("oauth.properties", makeUsers(20, 50))
calendarManager.clearEvents()
calendarManager.deleteCalendars()

String vacationCalendarId = calendarManager.createCalendar("Vacation Calendar", makeUsers(20, 50))

ConcreteEvent[] concreteEvents = generateProjectEvents(projects)

// Restrict the period that we generate personal events in to cut down the runtime
DateTime periodStart = DateTime.parse("2013-06-09")
DateTime periodEnd = DateTime.parse("2013-06-15")

concreteEvents += PersonalEventGenerator.generate(periodStart, periodEnd, makeUsers(20, 50))
calendarManager.postEvents(concreteEvents)

ConcreteEvent[] vacationEvents = generateVacationEvents(periodStart, periodEnd, makeUsers(20, 50))
calendarManager.postEventsTo(vacationCalendarId, vacationEvents)

