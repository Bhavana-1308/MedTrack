package com.medreminder.ui;

import com.medreminder.exception.InvalidInputException;
import com.medreminder.model.Medicine;
import com.medreminder.model.Reminder;
import com.medreminder.service.InputValidator;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalTime;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Modal dialog to schedule a new daily reminder for an existing medicine.
 */
public class ReminderDialog extends JDialog {

    private final int userId;
    private final List<Medicine> medicines;
    private Reminder resultReminder;

    private final JComboBox<MedicineItem> medicineCombo = new JComboBox<>();
    private final JTextField timeField = new JTextField("08:30", 16);
    private final JTextField labelField = new JTextField("After Breakfast", 16);
    private final JCheckBox activeCheck = new JCheckBox("Enable reminder immediately", true);

    public ReminderDialog(Frame owner, int userId, List<Medicine> medicines) {
        super(owner, "Set Medicine Reminder", true);
        this.userId = userId;
        this.medicines = medicines;

        setSize(420, 290);
        setLocationRelativeTo(owner);
        setResizable(false);
        initComponents();
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        root.setBackground(Color.WHITE);

        for (Medicine m : medicines) {
            medicineCombo.addItem(new MedicineItem(m));
        }

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.38;
        form.add(boldLabel("Select Medicine *"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.62;
        form.add(medicineCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.38;
        form.add(boldLabel("Time (24h HH:mm) *"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.62;
        timeField.setMargin(new Insets(4, 6, 4, 6));
        form.add(timeField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.38;
        form.add(boldLabel("Reminder Note"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.62;
        labelField.setMargin(new Insets(4, 6, 4, 6));
        form.add(labelField, gbc);

        gbc.gridx = 1; gbc.gridy = 3;
        activeCheck.setOpaque(false);
        form.add(activeCheck, gbc);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> {
            resultReminder = null;
            dispose();
        });

        JButton saveBtn = new JButton("Save Reminder");
        saveBtn.setBackground(new Color(15, 118, 110));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        saveBtn.addActionListener(e -> handleSave());

        actions.add(cancelBtn);
        actions.add(saveBtn);

        root.add(form, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private JLabel boldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        return lbl;
    }

    private void handleSave() {
        try {
            MedicineItem selected = (MedicineItem) medicineCombo.getSelectedItem();
            if (selected == null) {
                throw new InvalidInputException("Medicine", "Please add a medicine before creating a reminder.");
            }
            LocalTime time = InputValidator.parseTime(timeField.getText());
            String label = labelField.getText().trim();
            if (label.isEmpty()) {
                label = "Scheduled Dose";
            }

            Reminder r = new Reminder();
            r.setMedicineId(selected.medicine.getId());
            r.setUserId(userId);
            r.setReminderTime(time);
            r.setReminderLabel(label);
            r.setActive(activeCheck.isSelected());
            r.setMedicineName(selected.medicine.getMedicineName());

            resultReminder = r;
            dispose();
        } catch (InvalidInputException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    public Reminder getResultReminder() {
        return resultReminder;
    }

    private static class MedicineItem {
        private final Medicine medicine;

        MedicineItem(Medicine medicine) {
            this.medicine = medicine;
        }

        @Override
        public String toString() {
            return medicine.getMedicineName() + " (" + medicine.getDosage() + ")";
        }
    }
}
