package com.android.calendar.event

import android.app.Dialog
import android.content.ContentUris
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.provider.CalendarContract.Events
import android.text.TextUtils
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.android.calendar.CalendarEventModel
import com.android.calendar.event.DeleteEventHelper.RecurrenceDeletion
import ws.xsoh.etar.R

interface OnEventDeletedListener {
    fun onEventDeleted()
}

class DeleteEventDialogFragment : DialogFragment() {

    private lateinit var deleteEventHelper: DeleteEventHelper
    private lateinit var onEventDeletedListener: OnEventDeletedListener

    companion object {
        private const val KEY_EVENT_MODEL = "EXTRA_EVENT_MODEL"
        private const val KEY_EVENT_ID = "EXTRA_EVENT_ID"
        private const val KEY_EVENT_START = "EXTRA_EVENT_START"
        private const val KEY_EVENT_END = "EXTRA_EVENT_END"

        fun newInstance(model: CalendarEventModel, start: Long, end: Long) = DeleteEventDialogFragment().apply {
            arguments = bundleOf(
                KEY_EVENT_MODEL to model,
                KEY_EVENT_START to start,
                KEY_EVENT_END to end
            )
        }

        fun newInstance(id: Long, start: Long, end: Long) = DeleteEventDialogFragment().apply {
            arguments = bundleOf(
                KEY_EVENT_ID to id,
                KEY_EVENT_START to start,
                KEY_EVENT_END to end
            )
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (activity !is OnEventDeletedListener) {
            throw IllegalStateException("Caller activity must implement OnEventDeletedListener!")
        }
        onEventDeletedListener = activity as OnEventDeletedListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val model = getModel() ?: return super.onCreateDialog(savedInstanceState)
        deleteEventHelper = DeleteEventHelper(requireContext())

        return if (isRecurring(model)) {
            buildDialogForRecurringEvent(model)
        } else {
            buildDialogForNonRecurringEvent(model)
        }
    }

    private fun getModel(): CalendarEventModel? {
        var model = requireArguments().getSerializable(KEY_EVENT_MODEL) as CalendarEventModel?
        if (model != null) {
            return model
        }

        val id = requireArguments().getLong(KEY_EVENT_ID)
        if (id == 0L) return null

        requireContext().contentResolver.query(
            ContentUris.withAppendedId(Events.CONTENT_URI, id),
            EditEventHelper.EVENT_PROJECTION, null, null, null
        ).use { cursor ->
            if (cursor == null) {
                return null
            }
            cursor.moveToFirst()
            model = CalendarEventModel()
            EditEventHelper.setModelFromCursor(model, cursor, requireContext())
            return model
        }
    }

    private fun buildDialogForNonRecurringEvent(model: CalendarEventModel): AlertDialog {
        return AlertDialog.Builder(requireContext())
            .setMessage(R.string.delete_this_event_title)
            .setIconAttribute(android.R.attr.alertDialogIcon)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                if (isRecurrenceException(model)) {
                    deleteEventHelper.deleteRecurrenceExceptionEvent(model)
                    onEventDeletedListener.onEventDeleted()
                } else {
                    deleteEventHelper.deleteEvent(model)
                    onEventDeletedListener.onEventDeleted()
                }
            }
            .create()
    }

    private fun buildDialogForRecurringEvent(model: CalendarEventModel): AlertDialog {
        val recurrenceDeletion = RecurrenceDeletion.values().toMutableList()

        if (model.mSyncId == null) {
            // Until the event is synced, SYNC_ID will return null and no exception for the
            // recurring event can be added

            // remove 'Only this event' item
            recurrenceDeletion.remove(RecurrenceDeletion.SELECTED)
        }

        if (!model.mIsOrganizer) {
            // Only organizer can edit recurring event, remove 'This and future events' item
            recurrenceDeletion.remove(RecurrenceDeletion.ALL_FOLLOWING)
        }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_single_choice,
            recurrenceDeletion.map { requireContext().getString(it.stringRes) }
        )

        return AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_recurring_event_title, model.mTitle))
            .setIconAttribute(android.R.attr.alertDialogIcon)
            .setSingleChoiceItems(adapter, -1) { _, _ ->
                // enable confirm button after we got a selection
                (dialog as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = true
            }
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                val listView = (dialog as AlertDialog).listView

                deleteEventHelper.deleteRecurringEvent(
                    model,
                    recurrenceDeletion[listView.checkedItemPosition],
                    requireArguments().getLong(KEY_EVENT_START)
                )
                onEventDeletedListener.onEventDeleted()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create().apply {
                this.setOnShowListener {
                    // disable confirm button until we got a selection
                    this.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = false
                }
            }
    }

    private fun isRecurring(model: CalendarEventModel): Boolean {
        return !TextUtils.isEmpty(model.mRrule)
    }

    private fun isRecurrenceException(model: CalendarEventModel): Boolean {
        // `ORIGINAL_SYNC_ID` is only set for events that are a exception for recurring events
        return model.mOriginalSyncId != null
    }
}
