package com.medreminder.ui;

import com.medreminder.dao.HistoryDAO;
import com.medreminder.dao.MedicineDAO;
import com.medreminder.dao.ReminderDAO;
import com.medreminder.exception.DatabaseOperationException;
import com.medreminder.interfaces.ReminderListener;
import com.medreminder.model.Medicine;
import com.medreminder.model.MedicineHistory;
import com.medreminder.model.Reminder;
import com.medreminder.model.User;
import com.medreminder.service.FileReportService;
import com.medreminder.service.ReminderSchedulerThread;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/**
 * Main Swing Dashboard frame managing medicines, reminders, dose tracking,
 * background multithreaded alerts, and CSV/log file handling.
 */
public class DashboardFrame extends JFrame implements ReminderListener {

    private final User currentUser;
    private final MedicineDAO medicineDAO = new MedicineDAO();
    private final ReminderDAO reminderDAO = new ReminderDAO();
    private final HistoryDAO historyDAO = new HistoryDAO();
    private final FileReportService fileService = new FileReportService();

    private ReminderSchedulerThread schedulerThread;

    // Cached collections for fast table actions
    private ArrayList<Medicine> currentMedicines = new ArrayList<>();
    private HashMap<Integer, Medicine> medicineMap = new HashMap<>();
    private ArrayList<Reminder> currentReminders = new ArrayList<>();
    private ArrayList<MedicineHistory> currentHistory = new ArrayList<>();

    // Dashboard KPI Labels
    private final JLabel totalMedValueLabel = new JLabel("0");
    private final JLabel activeRemValueLabel = new JLabel("0");
    private final JLabel takenValueLabel = new JLabel("0");
    private final JLabel missedValueLabel = new JLabel("0");

    // Search & filter controls
    private final JTextField searchField = new JTextField(18);
    private final JComboBox<String> typeFilterCombo = new JComboBox<>(new String[]{"ALL", "PRESCRIPTION", "OTC"});

    // Tables & Models
    private DefaultTableModel todayScheduleModel;
    private JTable todayScheduleTable;

    private DefaultTableModel medicinesTableModel;
    private JTable medicinesTable;

    private DefaultTableModel remindersTableModel;
    private JTable remindersTable;

    private DefaultTableModel historyTableModel;
    private JTable historyTable;

    private final JTextArea logTextArea = new JTextArea(6, 40);

    public DashboardFrame(User user) {
        this.currentUser = user;
        setTitle("MedTrack Dashboard - " + user.getSummary());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1040, 680);
        setMinimumSize(new Dimension(920, 600));
        setLocationRelativeTo(null);

        initComponents();
        refreshAllData();
        startReminderThread();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopReminderThread();
            }
        });
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(new Color(245, 247, 250));
        root.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Top Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        JPanel titleGroup = new JPanel(new BorderLayout(0, 2));
        titleGroup.setOpaque(false);
        JLabel appTitle = new JLabel("Medicine Reminder & Management System");
        appTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        appTitle.setForeground(new Color(15, 23, 42));
        JLabel userSubtitle = new JLabel("Signed in as " + currentUser.getFullName() + " (" + currentUser.getEmail() + ")");
        userSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        userSubtitle.setForeground(new Color(100, 116, 139));
        titleGroup.add(appTitle, BorderLayout.NORTH);
        titleGroup.add(userSubtitle, BorderLayout.SOUTH);

        JPanel topActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topActions.setOpaque(false);
        JButton exportBtn = new JButton("Export History CSV");
        exportBtn.addActionListener(e -> handleExportHistory());

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshAllData());

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setBackground(new Color(239, 68, 68));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.addActionListener(e -> handleLogout());

        topActions.add(exportBtn);
        topActions.add(refreshBtn);
        topActions.add(logoutBtn);

        topBar.add(titleGroup, BorderLayout.WEST);
        topBar.add(topActions, BorderLayout.EAST);

        // KPI Summary Row
        JPanel kpiGrid = new JPanel(new GridLayout(1, 4, 12, 0));
        kpiGrid.setOpaque(false);
        kpiGrid.add(createKpiCard("Total Medicines", totalMedValueLabel, new Color(15, 118, 110)));
        kpiGrid.add(createKpiCard("Upcoming Reminders", activeRemValueLabel, new Color(37, 99, 235)));
        kpiGrid.add(createKpiCard("Doses Taken", takenValueLabel, new Color(22, 163, 74)));
        kpiGrid.add(createKpiCard("Doses Missed", missedValueLabel, new Color(220, 38, 38)));

        JPanel northContainer = new JPanel(new BorderLayout(0, 12));
        northContainer.setOpaque(false);
        northContainer.add(topBar, BorderLayout.NORTH);
        northContainer.add(kpiGrid, BorderLayout.SOUTH);

        // Main Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.BOLD, 13));
        tabs.addTab("Today's Schedule", buildTodayPanel());
        tabs.addTab("Medicines", buildMedicinesPanel());
        tabs.addTab("Reminders", buildRemindersPanel());
        tabs.addTab("History & Logs", buildHistoryPanel());

        root.add(northContainer, BorderLayout.NORTH);
        root.add(tabs, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel createKpiCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        titleLbl.setForeground(new Color(100, 116, 139));

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        valueLabel.setForeground(accentColor);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildTodayPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        JLabel info = new JLabel("Active reminders scheduled for today (" + LocalDate.now() + ")");
        info.setFont(new Font("SansSerif", Font.BOLD, 13));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setOpaque(false);
        JButton takenBtn = new JButton("Mark Selected Taken");
        takenBtn.setBackground(new Color(22, 163, 74));
        takenBtn.setForeground(Color.WHITE);
        takenBtn.addActionListener(e -> markSelectedTodayDose("TAKEN"));

        JButton missedBtn = new JButton("Mark Selected Missed");
        missedBtn.setBackground(new Color(217, 119, 6));
        missedBtn.setForeground(Color.WHITE);
        missedBtn.addActionListener(e -> markSelectedTodayDose("MISSED"));

        JButton guideBtn = new JButton("View Dosage Guide");
        guideBtn.addActionListener(e -> showSelectedTodayInstructions());

        btns.add(guideBtn);
        btns.add(takenBtn);
        btns.add(missedBtn);
        toolbar.add(info, BorderLayout.WEST);
        toolbar.add(btns, BorderLayout.EAST);

        todayScheduleModel = nonEditableModel(new String[]{"Reminder ID", "Time", "Medicine", "Dosage", "Label", "Status"});
        todayScheduleTable = new JTable(todayScheduleModel);
        styleTable(todayScheduleTable);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(todayScheduleTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildMedicinesPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(typeFilterCombo);
        JButton searchBtn = new JButton("Filter");
        searchBtn.addActionListener(e -> filterMedicines());
        JButton clearBtn = new JButton("Reset");
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            typeFilterCombo.setSelectedIndex(0);
            refreshMedicinesTable(currentMedicines);
        });
        searchPanel.add(searchBtn);
        searchPanel.add(clearBtn);

        JPanel crudBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        crudBtns.setOpaque(false);
        JButton addBtn = new JButton("+ Add Medicine");
        addBtn.setBackground(new Color(15, 118, 110));
        addBtn.setForeground(Color.WHITE);
        addBtn.addActionListener(e -> handleAddMedicine());

        JButton editBtn = new JButton("Edit");
        editBtn.addActionListener(e -> handleEditMedicine());

        JButton deleteBtn = new JButton("Delete");
        deleteBtn.setForeground(new Color(185, 28, 28));
        deleteBtn.addActionListener(e -> handleDeleteMedicine());

        crudBtns.add(addBtn);
        crudBtns.add(editBtn);
        crudBtns.add(deleteBtn);

        toolbar.add(searchPanel, BorderLayout.WEST);
        toolbar.add(crudBtns, BorderLayout.EAST);

        medicinesTableModel = nonEditableModel(new String[]{
                "ID", "Medicine Name", "Type", "Dosage", "Frequency", "Start Date", "End Date", "Doctor / Category"
        });
        medicinesTable = new JTable(medicinesTableModel);
        styleTable(medicinesTable);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(medicinesTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildRemindersPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        toolbar.setOpaque(false);

        JButton testTriggerBtn = new JButton("Simulate Alert Popup");
        testTriggerBtn.addActionListener(e -> simulateSelectedReminder());

        JButton addRemBtn = new JButton("+ Set Reminder");
        addRemBtn.setBackground(new Color(15, 118, 110));
        addRemBtn.setForeground(Color.WHITE);
        addRemBtn.addActionListener(e -> handleAddReminder());

        JButton toggleBtn = new JButton("Toggle Active/Paused");
        toggleBtn.addActionListener(e -> handleToggleReminder());

        JButton delRemBtn = new JButton("Delete");
        delRemBtn.setForeground(new Color(185, 28, 28));
        delRemBtn.addActionListener(e -> handleDeleteReminder());

        toolbar.add(testTriggerBtn);
        toolbar.add(addRemBtn);
        toolbar.add(toggleBtn);
        toolbar.add(delRemBtn);

        remindersTableModel = nonEditableModel(new String[]{"ID", "Time", "Medicine", "Label", "Status"});
        remindersTable = new JTable(remindersTableModel);
        styleTable(remindersTable);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(remindersTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        historyTableModel = nonEditableModel(new String[]{
                "ID", "Date", "Scheduled Time", "Medicine", "Dosage", "Status", "Remarks"
        });
        historyTable = new JTable(historyTableModel);
        styleTable(historyTable);

        JPanel logSection = new JPanel(new BorderLayout(0, 4));
        logSection.setOpaque(false);
        JLabel logLabel = new JLabel("Recent Activity File Log (logs/reminder_activity.log)");
        logLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        logTextArea.setEditable(false);
        logTextArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        logSection.add(logLabel, BorderLayout.NORTH);
        logSection.add(new JScrollPane(logTextArea), BorderLayout.CENTER);

        panel.add(new JScrollPane(historyTable), BorderLayout.CENTER);
        panel.add(logSection, BorderLayout.SOUTH);
        return panel;
    }

    private DefaultTableModel nonEditableModel(String[] columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private void styleTable(JTable table) {
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
    }

    private void startReminderThread() {
        schedulerThread = new ReminderSchedulerThread(currentUser.getId(), this);
        schedulerThread.start();
    }

    private void stopReminderThread() {
        if (schedulerThread != null) {
            schedulerThread.stopScheduler();
        }
    }

    private void refreshAllData() {
        try {
            currentMedicines = medicineDAO.getMedicinesByUser(currentUser.getId());
            medicineMap = medicineDAO.getMedicineMapByUser(currentUser.getId());
            currentReminders = reminderDAO.getRemindersByUser(currentUser.getId());
            currentHistory = historyDAO.getHistoryByUser(currentUser.getId());
            HashMap<String, Integer> stats = historyDAO.getDashboardStats(currentUser.getId());

            totalMedValueLabel.setText(String.valueOf(stats.get("TOTAL_MEDICINES")));
            activeRemValueLabel.setText(String.valueOf(stats.get("ACTIVE_REMINDERS")));
            takenValueLabel.setText(String.valueOf(stats.get("TAKEN_COUNT")));
            missedValueLabel.setText(String.valueOf(stats.get("MISSED_COUNT")));

            refreshMedicinesTable(currentMedicines);
            refreshRemindersTables();
            refreshHistoryTable();
            refreshLogViewer();
        } catch (DatabaseOperationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshMedicinesTable(List<Medicine> medicines) {
        medicinesTableModel.setRowCount(0);
        for (Medicine m : medicines) {
            medicinesTableModel.addRow(new Object[]{
                    m.getId(),
                    m.getMedicineName(),
                    m.getMedicineType(),
                    m.getDosage(),
                    m.getFrequency(),
                    m.getStartDate(),
                    m.getEndDate(),
                    m.getSubTypeDetail()
            });
        }
    }

    private void refreshRemindersTables() {
        remindersTableModel.setRowCount(0);
        todayScheduleModel.setRowCount(0);
        LocalDate today = LocalDate.now();

        for (Reminder r : currentReminders) {
            Medicine med = medicineMap.get(r.getMedicineId());
            String medName = med != null ? med.getMedicineName() : r.getMedicineName();
            String dosage = med != null ? med.getDosage() : "-";

            remindersTableModel.addRow(new Object[]{
                    r.getId(),
                    r.getFormattedTime(),
                    medName,
                    r.getReminderLabel(),
                    r.isActive() ? "ACTIVE" : "PAUSED"
            });

            if (r.isActive() && med != null && med.isActiveOn(today)) {
                todayScheduleModel.addRow(new Object[]{
                        r.getId(),
                        r.getFormattedTime(),
                        medName,
                        dosage,
                        r.getReminderLabel(),
                        "SCHEDULED"
                });
            }
        }
    }

    private void refreshHistoryTable() {
        historyTableModel.setRowCount(0);
        for (MedicineHistory h : currentHistory) {
            historyTableModel.addRow(new Object[]{
                    h.getId(),
                    h.getDoseDate(),
                    h.getScheduledTime(),
                    h.getMedicineName(),
                    h.getDosage(),
                    h.getStatus(),
                    h.getRemarks() != null ? h.getRemarks() : ""
            });
        }
    }

    private void refreshLogViewer() {
        List<String> lines = fileService.readRecentActivityLogs(20);
        logTextArea.setText(String.join("\n", lines));
    }

    private void filterMedicines() {
        try {
            String keyword = searchField.getText();
            String type = (String) typeFilterCombo.getSelectedItem();
            ArrayList<Medicine> filtered = "ALL".equals(type)
                    ? medicineDAO.searchMedicines(currentUser.getId(), keyword)
                    : medicineDAO.searchMedicines(currentUser.getId(), keyword, type);
            refreshMedicinesTable(filtered);
        } catch (DatabaseOperationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Search Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleAddMedicine() {
        MedicineDialog dialog = new MedicineDialog(this, currentUser.getId(), null);
        dialog.setVisible(true);
        Medicine newMed = dialog.getResultMedicine();
        if (newMed != null) {
            try {
                medicineDAO.addMedicine(newMed);
                fileService.appendActivityLog(currentUser.getUsername(), "Added medicine: " + newMed.getSummary());
                refreshAllData();
            } catch (DatabaseOperationException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleEditMedicine() {
        int row = medicinesTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a medicine row to edit.");
            return;
        }
        int medId = (int) medicinesTableModel.getValueAt(row, 0);
        Medicine existing = medicineMap.get(medId);
        if (existing == null) {
            return;
        }
        MedicineDialog dialog = new MedicineDialog(this, currentUser.getId(), existing);
        dialog.setVisible(true);
        Medicine updated = dialog.getResultMedicine();
        if (updated != null) {
            try {
                medicineDAO.updateMedicine(updated);
                fileService.appendActivityLog(currentUser.getUsername(), "Updated medicine: " + updated.getSummary());
                refreshAllData();
            } catch (DatabaseOperationException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleDeleteMedicine() {
        int row = medicinesTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a medicine row to delete.");
            return;
        }
        int medId = (int) medicinesTableModel.getValueAt(row, 0);
        String medName = String.valueOf(medicinesTableModel.getValueAt(row, 1));
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete '" + medName + "' and all associated reminders/history?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                medicineDAO.deleteMedicine(medId, currentUser.getId());
                fileService.appendActivityLog(currentUser.getUsername(), "Deleted medicine: " + medName);
                refreshAllData();
            } catch (DatabaseOperationException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleAddReminder() {
        if (currentMedicines.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please add at least one medicine first.");
            return;
        }
        ReminderDialog dialog = new ReminderDialog(this, currentUser.getId(), currentMedicines);
        dialog.setVisible(true);
        Reminder newRem = dialog.getResultReminder();
        if (newRem != null) {
            try {
                reminderDAO.addReminder(newRem);
                fileService.appendActivityLog(currentUser.getUsername(), "Created reminder: " + newRem.getSummary());
                refreshAllData();
            } catch (DatabaseOperationException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleToggleReminder() {
        int row = remindersTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a reminder row first.");
            return;
        }
        int remId = (int) remindersTableModel.getValueAt(row, 0);
        String status = String.valueOf(remindersTableModel.getValueAt(row, 4));
        boolean nextActive = !"ACTIVE".equals(status);
        try {
            reminderDAO.toggleReminderStatus(remId, currentUser.getId(), nextActive);
            refreshAllData();
        } catch (DatabaseOperationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeleteReminder() {
        int row = remindersTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a reminder row to delete.");
            return;
        }
        int remId = (int) remindersTableModel.getValueAt(row, 0);
        try {
            reminderDAO.deleteReminder(remId, currentUser.getId());
            refreshAllData();
        } catch (DatabaseOperationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void markSelectedTodayDose(String status) {
        int row = todayScheduleTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a scheduled reminder row first.");
            return;
        }
        int remId = (int) todayScheduleModel.getValueAt(row, 0);
        Reminder target = findReminderById(remId);
        if (target == null) {
            return;
        }
        logDoseAction(target, status, "Marked from Today's Schedule");
    }

    private void showSelectedTodayInstructions() {
        int row = todayScheduleTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a row in Today's Schedule to view instructions.");
            return;
        }
        int remId = (int) todayScheduleModel.getValueAt(row, 0);
        Reminder target = findReminderById(remId);
        if (target != null) {
            Medicine med = medicineMap.get(target.getMedicineId());
            if (med != null) {
                JOptionPane.showMessageDialog(this,
                        med.getDosageInstructions(),
                        med.getMedicineName() + " - Dosage Instructions",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void simulateSelectedReminder() {
        int row = remindersTable.getSelectedRow();
        if (row < 0 && !currentReminders.isEmpty()) {
            Reminder first = currentReminders.get(0);
            onReminderTriggered(first, medicineMap.get(first.getMedicineId()));
            return;
        }
        if (row >= 0) {
            int remId = (int) remindersTableModel.getValueAt(row, 0);
            Reminder target = findReminderById(remId);
            if (target != null) {
                onReminderTriggered(target, medicineMap.get(target.getMedicineId()));
            }
        } else {
            JOptionPane.showMessageDialog(this, "Add a reminder first to test the alert popup.");
        }
    }

    private Reminder findReminderById(int id) {
        for (Reminder r : currentReminders) {
            if (r.getId() == id) {
                return r;
            }
        }
        return null;
    }

    private void logDoseAction(Reminder reminder, String status, String remarks) {
        MedicineHistory history = new MedicineHistory();
        history.setMedicineId(reminder.getMedicineId());
        history.setReminderId(reminder.getId());
        history.setUserId(currentUser.getId());
        history.setDoseDate(LocalDate.now());
        history.setScheduledTime(reminder.getReminderTime() != null ? reminder.getReminderTime() : LocalTime.now());
        history.setTakenAt("TAKEN".equals(status) ? LocalDateTime.now() : null);
        history.setStatus(status);
        history.setRemarks(remarks);

        try {
            historyDAO.logDose(history);
            fileService.appendActivityLog(currentUser.getUsername(),
                    "Marked " + reminder.getMedicineName() + " (" + reminder.getFormattedTime() + ") as " + status);
            refreshAllData();
        } catch (DatabaseOperationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleExportHistory() {
        try {
            String fileName = "medicine_history_" + currentUser.getUsername() + ".csv";
            File exported = fileService.exportToCsv(fileName, currentHistory);
            fileService.appendActivityLog(currentUser.getUsername(), "Exported CSV report to " + exported.getPath());
            refreshLogViewer();
            JOptionPane.showMessageDialog(this,
                    "Exported successfully to:\n" + exported.getAbsolutePath(),
                    "CSV Export Complete",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleLogout() {
        stopReminderThread();
        fileService.appendActivityLog(currentUser.getUsername(), "User logged out.");
        new LoginFrame().setVisible(true);
        dispose();
    }

    @Override
    public void onReminderTriggered(Reminder reminder, Medicine medicine) {
        Toolkit.getDefaultToolkit().beep();
        String medName = medicine != null ? medicine.getMedicineName() : reminder.getMedicineName();
        String dosage = medicine != null ? medicine.getDosage() : "";
        String instructions = medicine != null ? medicine.getDosageInstructions() : "";

        fileService.appendActivityLog(currentUser.getUsername(),
                "Reminder triggered for " + medName + " at " + reminder.getFormattedTime());

        String message = String.format(
                "Time to take your medicine!\n\nMedicine: %s (%s)\nScheduled Time: %s\nNote: %s\n\n%s",
                medName, dosage, reminder.getFormattedTime(), reminder.getReminderLabel(), instructions
        );

        Object[] options = {"Mark as Taken", "Mark as Missed", "Dismiss"};
        int choice = JOptionPane.showOptionDialog(
                this,
                message,
                "Medicine Reminder Alert",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) {
            logDoseAction(reminder, "TAKEN", "Confirmed via reminder popup");
        } else if (choice == 1) {
            logDoseAction(reminder, "MISSED", "Marked missed via reminder popup");
        } else {
            refreshLogViewer();
        }
    }
}
