package com.android.calendar.event

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.CalendarContract
import android.provider.CalendarContract.Events
import androidx.annotation.StringRes
import com.android.calendar.CalendarEventModel
import com.android.calendar.persistence.CalendarRepository.Companion.asLocalCalendarSyncAdapter
import com.android.calendarcommon2.EventRecurrence
import com.android.calendarcommon2.Time
import ws.xsoh.etar.R

class DeleteEventHelper(
    val context: Context,
) {

    /**
     * Delete an event. If the event is a recurring event, the whole series is deleted.
     */
    fun deleteEvent(model: CalendarEventModel) {
        context.contentResolver.delete(buildDeleteUri(model), null, null)
    }

    /**
     * Delete the exception of a recurring event.
     */
    fun deleteRecurrenceExceptionEvent(model: CalendarEventModel) {
        // TODO: we should also update EXDATE property here

        // deletes the recurrence exception by settings its status to "canceled"
        val values = ContentValues().apply { put(Events.STATUS, Events.STATUS_CANCELED) }

        val uri = ContentUris.withAppendedId(Events.CONTENT_URI, model.mId)
        context.contentResolver.update(uri, values, null, null)
    }

    /**
     * Delete a recurring event, or parts of it.
     *
     * Because recurring events only store the start of the whole series, [eventStartInMillis] has
     * to be supplied additionally.
     */
    fun deleteRecurringEvent(
        model: CalendarEventModel,
        which: RecurrenceDeletion,
        eventStartInMillis: Long
    ) {
        when (which) {
            RecurrenceDeletion.SELECTED -> {
                deleteSelectedRecurring(model, eventStartInMillis)
            }
            RecurrenceDeletion.ALL -> {
                deleteEvent(model)
            }
            RecurrenceDeletion.ALL_FOLLOWING -> {
                deleteAllFollowing(model, eventStartInMillis)
            }
        }
    }

    private fun deleteSelectedRecurring(
        model: CalendarEventModel,
        eventStartInMillis: Long,
    ) {
        // TODO: if we are deleting the first event in a series, then, instead of creating
        //  a recurrence exception, we could change the start time of the recurrence.
        //  if (model.mStart == startMillis) { ... }

        // create a recurrence exception by creating a new event with the status "canceled"
        val values = ContentValues().apply {
            put(Events.STATUS, Events.STATUS_CANCELED)
            put(Events.ORIGINAL_INSTANCE_TIME, eventStartInMillis)
        }

        val exceptionUriBuilder = Events.CONTENT_EXCEPTION_URI.buildUpon()
        ContentUris.appendId(exceptionUriBuilder, model.mId)

        context.contentResolver.insert(exceptionUriBuilder.build(), values)
    }

    private fun deleteAllFollowing(
        model: CalendarEventModel,
        eventStartInMillis: Long,
    ) {
        if (model.mStart == eventStartInMillis) {
            // delete all events if this is the first
            deleteEvent(model)
        } else {
            // modify the repeating event to end just before this event time
            val date = Time().apply {
                if (model.mAllDay) timezone = Time.TIMEZONE_UTC
                set(eventStartInMillis)
                second -= 1
                normalize()

                // Google calendar seems to require the UNTIL string to be in UTC
                switchTimezone(Time.TIMEZONE_UTC)
            }
            val eventRecurrence = EventRecurrence().apply {
                parse(model.mRrule)
                until = date.format2445()
            }
            val values = ContentValues().apply {
                put(Events.DTSTART, model.mStart)
                put(Events.RRULE, eventRecurrence.toString())
            }

            val uri = ContentUris.withAppendedId(Events.CONTENT_URI, model.mId)
            context.contentResolver.update(uri, values, null, null)
        }
    }

    private fun isLocalEvent(model: CalendarEventModel): Boolean {
        return model.mSyncAccountType == CalendarContract.ACCOUNT_TYPE_LOCAL
    }

    private fun buildDeleteUri(model: CalendarEventModel): Uri {
        // If this event is part of a local calendar, really remove it from the database
        //
        // "There are two versions of delete: as an app and as a sync adapter.
        // An app delete will set the deleted column on an event and remove all instances of that event.
        // A sync adapter delete will remove the event from the database and all associated data."
        // from https://developer.android.com/reference/android/provider/CalendarContract.Events

        val uri = if (isLocalEvent(model)) {
            asLocalCalendarSyncAdapter(model.mSyncAccountName, Events.CONTENT_URI)
        } else {
            Events.CONTENT_URI
        }
        return ContentUris.withAppendedId(uri, model.mId)
    }

    enum class RecurrenceDeletion(@StringRes val stringRes: Int) {
        SELECTED(R.string.delete_selected),
        ALL(R.string.delete_all),
        ALL_FOLLOWING(R.string.delete_all_following);
    }
}
