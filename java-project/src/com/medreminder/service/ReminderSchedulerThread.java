package com.medreminder.service;

import com.medreminder.dao.MedicineDAO;
import com.medreminder.dao.ReminderDAO;
import com.medreminder.exception.DatabaseOperationException;
import com.medreminder.interfaces.ReminderListener;
import com.medreminder.model.Medicine;
import com.medreminder.model.Reminder;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.SwingUtilities;

/**
 * Background daemon thread that periodically checks the user's active reminders
 * against the system clock and dispatches GUI notifications safely via SwingUtilities.
 *
 * Demonstrates Multithreading, Synchronization/volatile control, HashMap caching,
 * and Interface callbacks (ReminderListener).
 */
public class ReminderSchedulerThread extends Thread {

    private static final long CHECK_INTERVAL_MS = 15_000L; // Check every 15 seconds
    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

    private final int userId;
    private final ReminderDAO reminderDAO;
    private final MedicineDAO medicineDAO;
    private final ReminderListener listener;
    private final HashMap<Integer, String> lastTriggeredMinuteMap = new HashMap<>();

    private volatile boolean running = true;

    public ReminderSchedulerThread(int userId, ReminderListener listener) {
        super("Medicine-Reminder-Thread");
        this.userId = userId;
        this.listener = listener;
        this.reminderDAO = new ReminderDAO();
        this.medicineDAO = new MedicineDAO();
        setDaemon(true);
    }

    public void stopScheduler() {
        this.running = false;
        this.interrupt();
    }

    /**
     * Checks due reminders immediately (useful for manual testing or on thread tick).
     */
    public void checkDueRemindersNow() {
        try {
            ArrayList<Reminder> activeReminders = reminderDAO.getActiveRemindersForToday(userId);
            HashMap<Integer, Medicine> medicineMap = medicineDAO.getMedicineMapByUser(userId);
            String currentMinute = LocalTime.now().format(HH_MM);

            for (Reminder reminder : activeReminders) {
                String scheduledMinute = reminder.getFormattedTime();
                if (currentMinute.equals(scheduledMinute)) {
                    String alreadyFired = lastTriggeredMinuteMap.get(reminder.getId());
                    if (!currentMinute.equals(alreadyFired)) {
                        lastTriggeredMinuteMap.put(reminder.getId(), currentMinute);
                        Medicine med = medicineMap.get(reminder.getMedicineId());
                        SwingUtilities.invokeLater(() -> {
                            if (listener != null) {
                                listener.onReminderTriggered(reminder, med);
                            }
                        });
                    }
                }
            }
        } catch (DatabaseOperationException e) {
            System.err.println("Background reminder check failed: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        while (running) {
            checkDueRemindersNow();
            try {
                Thread.sleep(CHECK_INTERVAL_MS);
            } catch (InterruptedException e) {
                if (!running) {
                    break;
                }
            }
        }
    }
}
