package com.android.calendar.persistence.event

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract.Events
import androidx.annotation.WorkerThread

class EventDataSource(val context: Context, val calendarId: Long) {

    @WorkerThread
    fun getAllEvents(): List<Event> {
        val events: MutableList<Event> = mutableListOf()

        context.contentResolver.query(
            eventsUri,
            projection,
            WHERE_CALENDAR_ID,
            arrayOf(calendarId.toString()),
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                events.add(getEventFromCursor(cursor))
            }
        }

        return events
    }

    @WorkerThread
    fun getEvent(eventId: Long): Event? {
        context.contentResolver.query(
            eventUri(eventId),
            projection,
            WHERE_CALENDAR_ID,
            arrayOf(calendarId.toString()),
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                return getEventFromCursor(cursor)
            }
        }
        return null
    }

    @WorkerThread
    fun save(event: Event) {
        val contentValues = ContentValues().apply {
            put(Events.CALENDAR_ID, event.calendarId)
            put(Events.ORGANIZER, event.organizer)
            put(Events.TITLE, event.title)
            put(Events.DESCRIPTION, event.description)
            put(Events.EVENT_LOCATION, event.location)
            put(Events.EVENT_COLOR_KEY, event.eventColorKey)
            put(Events.STATUS, event.eventStatus.value)
            put(Events.SELF_ATTENDEE_STATUS, event.selfAttendeeStatus.value)
            put(Events.DTSTART, event.startInMillis)
            put(Events.DTEND, event.endInMillis)
            put(Events.DURATION, event.duration)
            put(Events.EVENT_TIMEZONE, event.eventTimezone)
            put(Events.EVENT_END_TIMEZONE, event.eventEndTimezone)
            put(Events.ALL_DAY, event.allDay)
            put(Events.ACCESS_LEVEL, event.accessLevel.value)
            put(Events.AVAILABILITY, event.availability.value)
            put(Events.HAS_ALARM, event.hasAlarm)
            put(Events.HAS_ATTENDEE_DATA, event.hasAttendeeData)
            put(Events.HAS_EXTENDED_PROPERTIES, event.hasExtendedProperties)
            put(Events.GUESTS_CAN_INVITE_OTHERS, event.guestsCanInviteOthers)
            put(Events.GUESTS_CAN_SEE_GUESTS, event.guestsCanSeeGuests)
            put(Events.GUESTS_CAN_MODIFY, event.guestsCanModify)
            put(Events.IS_ORGANIZER, event.isOrganizer)
            put(Events.RRULE, event.rrule)
            put(Events.RDATE, event.rdate)
            put(Events.EXRULE, event.exrule)
            put(Events.EXDATE, event.exdate)
            put(Events.ORIGINAL_ID, event.originalId)
            put(Events.ORIGINAL_SYNC_ID, event.originalSyncId)
            put(Events.ORIGINAL_INSTANCE_TIME, event.originalInstanceTimeInMillis)
            put(Events.ORIGINAL_ALL_DAY, event.originalAllDay)
            put(Events.LAST_DATE, event.lastDateInMillis)
            put(Events.UID_2445, event.uid2445)
            put(Events.CUSTOM_APP_URI, event.customAppUri)
            put(Events.CUSTOM_APP_PACKAGE, event.customAppPackage)
        }

        if (event.id == null) {
            context.contentResolver.insert(eventsUri, contentValues)
        } else {
            context.contentResolver.update(eventUri(event.id), contentValues, null, null)
        }
    }

    private fun getEventFromCursor(cursor: Cursor): Event = Event(
        id = cursor.getLongValue(Events._ID),
        calendarId = cursor.getLongValue(Events.CALENDAR_ID),
        organizer = cursor.getStringValue(Events.ORGANIZER),
        title = cursor.getStringValue(Events.TITLE),
        description = cursor.getStringValue(Events.DESCRIPTION),
        location = cursor.getStringValue(Events.EVENT_LOCATION),
        eventColor = cursor.getIntValue(Events.EVENT_COLOR),
        eventColorKey = cursor.getStringValue(Events.EVENT_COLOR_KEY),
        displayColor = cursor.getIntValue(Events.DISPLAY_COLOR),
        eventStatus = Event.Status.fromInt(cursor.getIntValue(Events.STATUS)),
        selfAttendeeStatus = Event.Status.fromInt(cursor.getIntValue(Events.SELF_ATTENDEE_STATUS)),
        startInMillis = cursor.getLongValue(Events.DTSTART),
        endInMillis = cursor.getLongValue(Events.DTEND),
        duration = cursor.getStringValue(Events.DURATION),
        eventTimezone = cursor.getStringValue(Events.EVENT_TIMEZONE),
        eventEndTimezone = cursor.getStringValue(Events.EVENT_END_TIMEZONE),
        allDay = cursor.getIntValue(Events.ALL_DAY) == 1,
        accessLevel = Event.Access.fromInt(cursor.getIntValue(Events.ACCESS_LEVEL)),
        availability = Event.Availability.fromInt(cursor.getIntValue(Events.AVAILABILITY)),
        hasAlarm = cursor.getIntValue(Events.HAS_ALARM) == 1,
        hasAttendeeData = cursor.getIntValue(Events.HAS_ATTENDEE_DATA) == 1,
        hasExtendedProperties = cursor.getIntValue(Events.HAS_EXTENDED_PROPERTIES) == 1,
        guestsCanInviteOthers = cursor.getIntValue(Events.GUESTS_CAN_INVITE_OTHERS) == 1,
        guestsCanSeeGuests = cursor.getIntValue(Events.GUESTS_CAN_SEE_GUESTS) == 1,
        guestsCanModify = cursor.getIntValue(Events.GUESTS_CAN_MODIFY) == 1,
        isOrganizer = cursor.getIntValue(Events.IS_ORGANIZER) == 1,
        rrule = cursor.getStringValue(Events.RRULE),
        rdate = cursor.getStringValue(Events.RDATE),
        exrule = cursor.getStringValue(Events.EXRULE),
        exdate = cursor.getStringValue(Events.EXDATE),
        originalId = cursor.getStringValue(Events.ORIGINAL_ID),
        originalSyncId = cursor.getStringValue(Events.ORIGINAL_SYNC_ID),
        originalInstanceTimeInMillis = cursor.getLongValue(Events.ORIGINAL_INSTANCE_TIME),
        originalAllDay = cursor.getIntValue(Events.ORIGINAL_ALL_DAY) == 1,
        lastDateInMillis = cursor.getLongValue(Events.LAST_DATE),
        uid2445 = cursor.getStringValue(Events.UID_2445),
        customAppUri = cursor.getStringValue(Events.CUSTOM_APP_URI),
        customAppPackage = cursor.getStringValue(Events.CUSTOM_APP_PACKAGE),
    )

    companion object {
        private val eventsUri = Events.CONTENT_URI
        private val eventUri: (Long) -> Uri =
            { eventId -> ContentUris.withAppendedId(eventsUri, eventId) }
        private const val WHERE_CALENDAR_ID = "${Events.CALENDAR_ID} = ?"

        private val projection = arrayOf(
            Events._ID,
            Events.CALENDAR_ID,
            Events.ORGANIZER,
            Events.TITLE,
            Events.DESCRIPTION,
            Events.EVENT_LOCATION,
            Events.EVENT_COLOR,
            Events.EVENT_COLOR_KEY,
            Events.DISPLAY_COLOR,
            Events.STATUS,
            Events.SELF_ATTENDEE_STATUS,
            Events.DTSTART,
            Events.DTEND,
            Events.DURATION,
            Events.EVENT_TIMEZONE,
            Events.EVENT_END_TIMEZONE,
            Events.ALL_DAY,
            Events.ACCESS_LEVEL,
            Events.AVAILABILITY,
            Events.HAS_ALARM,
            Events.HAS_ATTENDEE_DATA,
            Events.HAS_EXTENDED_PROPERTIES,
            Events.GUESTS_CAN_INVITE_OTHERS,
            Events.GUESTS_CAN_SEE_GUESTS,
            Events.GUESTS_CAN_MODIFY,
            Events.IS_ORGANIZER,
            Events.RRULE,
            Events.RDATE,
            Events.EXRULE,
            Events.EXDATE,
            Events.ORIGINAL_ID,
            Events.ORIGINAL_SYNC_ID,
            Events.ORIGINAL_INSTANCE_TIME,
            Events.ORIGINAL_ALL_DAY,
            Events.LAST_DATE,
            Events.UID_2445,
            Events.CUSTOM_APP_URI,
            Events.CUSTOM_APP_PACKAGE,
        )
    }
}

@SuppressLint("Range")
fun Cursor.getStringValue(key: String) = getString(getColumnIndex(key))

@SuppressLint("Range")
fun Cursor.getIntValue(key: String) = getInt(getColumnIndex(key))

@SuppressLint("Range")
fun Cursor.getLongValue(key: String) = getLong(getColumnIndex(key))
