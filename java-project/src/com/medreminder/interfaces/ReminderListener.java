package com.medreminder.interfaces;

import com.medreminder.model.Medicine;
import com.medreminder.model.Reminder;

/**
 * Callback interface used by the background ReminderSchedulerThread to notify
 * the Swing Dashboard UI when a scheduled dose time arrives.
 */
public interface ReminderListener {

    /**
     * Invoked on the Swing Event Dispatch Thread when a reminder triggers.
     *
     * @param reminder The scheduled reminder that matched the current time.
     * @param medicine The associated medicine object (may be null if not cached).
     */
    void onReminderTriggered(Reminder reminder, Medicine medicine);
}
