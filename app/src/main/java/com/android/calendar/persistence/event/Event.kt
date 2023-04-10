package com.android.calendar.persistence.event

data class Event(

    /**
     * Unique event id.
     */
    val id: Long?,

    /**
     * The id of the calendar the event belongs to.
     */
    val calendarId: Long,

    /**
     * Email of the organizer (owner) of the event.
     */
    val organizer: String,

    /**
     * The title of the event.
     */
    val title: String,

    /**
     * The description of the event.
     */
    val description: String,

    /**
     * Where the event takes place.
     */
    val location: String,

    /**
     * A secondary color for the individual event. This should only be
     * updated by the sync adapter for a given account.
     */
    val eventColor: Int,

    /**
     * A secondary color key for the individual event. NULL or an empty
     * string are reserved for indicating that the event does not use a key
     * for looking up the color. The provider will update
     * {@link #EVENT_COLOR} automatically when a valid key is written to
     * this column. The key must reference an existing row of the
     * {@link Colors} table.
     */
    val eventColorKey: String,

    /**
     * This will be [eventColor] if it is not null; otherwise, this will be
     * [android.provider.CalendarContract.CalendarColumns.CALENDAR_COLOR].
     *
     * Read-only value. To modify, write to [eventColor] or
     * [android.provider.CalendarContract.CalendarColumns.CALENDAR_COLOR] directly.
     */
    val displayColor: Int,

    /**
     * The event status.
     */
    val eventStatus: Status,

    /**
     * This is a copy of the attendee status for the owner of this event.
     * This field is copied here so that we can efficiently filter out
     * events that are declined without having to look in the Attendees
     * table.
     */
    val selfAttendeeStatus: Status,

    /**
     * The time the event starts in UTC millis since epoch.
     */
    val startInMillis: Long,

    /**
     * The time the event ends in UTC millis since epoch.
     */
    val endInMillis: Long,

    /**
     * The duration of the event in RFC5545 format. For example, a value of "PT1H" states that the
     * event should last one hour, and a value of "P2W" indicates a duration of 2 weeks.
     */
    val duration: String,

    /**
     * The timezone for the event.
     */
    val eventTimezone: String,

    /**
     * The timezone for the end time of the event.
     */
    val eventEndTimezone: String,

    /**
     * Whether this is an event that occupies the entire day, as defined by the local time zone, or
     * if this is a regular event that may start and end at any time during a day.
     */
    val allDay: Boolean,

    /**
     * Defines how the event shows up for others when the calendar is shared.
     */
    val accessLevel: Access,

    /**
     * If this event counts as busy time or is free time that can be scheduled over.
     */
    val availability: Availability,

    /**
     * Whether the event has an alarm or not.
     */
    val hasAlarm: Boolean,

    /**
     * Whether the event has attendee information. True if the event
     * has full attendee data, false if the event has information about
     * self only.
     */
    val hasAttendeeData: Boolean,

    /**
     * Whether the event has extended properties or not.
     */
    val hasExtendedProperties: Boolean,

    /**
     * Whether guests can invite other guests.
     */
    val guestsCanInviteOthers: Boolean,

    /**
     * Whether guests can see the list of attendees.
     */
    val guestsCanSeeGuests: Boolean,

    /**
     * Whether guests can modify the event.
     */
    val guestsCanModify: Boolean,

    /**
     * Are we the organizer of this event. If this column is not explicitly set, the provider will
     * return 1 if [organizer] is equal to [android.provider.CalendarContract.CalendarColumns.OWNER_ACCOUNT].
     */
    val isOrganizer: Boolean,

    /**
     * The recurrence rule for the event.
     */
    val rrule: String,

    /**
     * The recurrence dates for the event. You typically use RDATE in conjunction with RRULE to
     * define an aggregate set of repeating occurrences. For more discussion, see the RFC5545 spec.
     */
    val rdate: String,

    /**
     * The recurrence exception rule for the event.
     */
    val exrule: String,

    /**
     * The recurrence exception dates for the event.
     */
    val exdate: String,

    /**
     * The [id] of the original recurring event for which this
     * event is an exception.
     */
    val originalId: String,

    /**
     * The _sync_id of the original recurring event for which this event is
     * an exception. The provider should keep the original_id in sync when
     * this is updated.
     */
    val originalSyncId: String,

    /**
     * The original instance time of the recurring event for which this event is an exception.
     * In millis since epoch.
     */
    val originalInstanceTimeInMillis: Long,

    /**
     * The allDay status (true or false) of the original recurring event
     * for which this event is an exception.
     */
    val originalAllDay: Boolean,

    /**
     * The last date this event repeats on, or NULL if it never ends.
     * In millis since epoch.
     */
    val lastDateInMillis: Long,

    /**
     * The UID for events added from the RFC 2445 iCalendar format.
     */
    val uid2445: String,

    /**
     * The URI used by the custom app for the event.
     */
    val customAppUri: String,

    /**
     * The package name of the custom app that can provide a richer
     * experience for the event. See the ACTION TYPE
     * {@link CalendarContract#ACTION_HANDLE_CUSTOM_EVENT} for details.
     */
    val customAppPackage: String,

    ) {

    enum class Status(val value: Int) {
        STATUS_TENTATIVE(0),
        STATUS_CONFIRMED(1),
        STATUS_CANCELED(2);

        companion object {
            fun fromInt(value: Int) = values().firstOrNull { it.value == value } ?: STATUS_TENTATIVE
        }
    }

    enum class Availability(val value: Int) {

        /**
         * Indicates that this event takes up time and will conflict with other
         * events.
         */
        BUSY(0),

        /**
         * Indicates that this event is free time and will not conflict with
         * other events.
         */
        FREE(1),

        /**
         * Indicates that the owner's availability may change, but should be
         * considered busy time that will conflict.
         */
        TENTATIVE(2);

        companion object {
            fun fromInt(value: Int) = values().firstOrNull() { it.value == value } ?: BUSY
        }
    }

    enum class Access(val value: Int) {
        /**
         * Default access is controlled by the server and will be treated as
         * public on the device.
         */
        ACCESS_DEFAULT(0),

        /**
         * Confidential is not used by the app.
         */
        ACCESS_CONFIDENTIAL(1),

        /**
         * Private shares the event as a free/busy slot with no details.
         */
        ACCESS_PRIVATE(2),

        /**
         * Public makes the contents visible to anyone with access to the
         * calendar.
         */
        ACCESS_PUBLIC(3);

        companion object {
            fun fromInt(value: Int) = values().firstOrNull { it.value == value } ?: ACCESS_DEFAULT
        }
    }
}
