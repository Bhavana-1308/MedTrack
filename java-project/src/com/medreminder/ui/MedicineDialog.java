package com.medreminder.ui;

import com.medreminder.exception.InvalidInputException;
import com.medreminder.model.Medicine;
import com.medreminder.model.OTCMedicine;
import com.medreminder.model.PrescriptionMedicine;
import com.medreminder.service.InputValidator;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Modal dialog for adding or editing a Medicine.
 * Dynamically instantiates PrescriptionMedicine or OTCMedicine (Polymorphism).
 */
public class MedicineDialog extends JDialog {

    private final int userId;
    private final Medicine existingMedicine;
    private Medicine resultMedicine;

    private final JTextField nameField = new JTextField(22);
    private final JComboBox<String> typeCombo = new JComboBox<>(new String[]{"PRESCRIPTION", "OTC"});
    private final JTextField dosageField = new JTextField(22);
    private final JComboBox<String> frequencyCombo = new JComboBox<>(new String[]{
            "Once Daily", "Twice Daily", "Three Times Daily", "Every 8 Hours", "Before Sleep", "As Needed"
    });
    private final JTextField startDateField = new JTextField(LocalDate.now().toString(), 22);
    private final JTextField endDateField = new JTextField(LocalDate.now().plusDays(7).toString(), 22);
    private final JLabel subDetailLabel = new JLabel("Doctor Name");
    private final JTextField subDetailField = new JTextField(22);
    private final JTextField notesField = new JTextField(22);

    public MedicineDialog(Frame owner, int userId, Medicine existingMedicine) {
        super(owner, existingMedicine == null ? "Add New Medicine" : "Edit Medicine", true);
        this.userId = userId;
        this.existingMedicine = existingMedicine;

        setSize(470, 480);
        setLocationRelativeTo(owner);
        setResizable(false);
        initComponents();
        populateIfEditing();
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        root.setBackground(Color.WHITE);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addRow(form, gbc, row++, "Medicine Name *", nameField);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.35;
        form.add(boldLabel("Medicine Type *"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.65;
        form.add(typeCombo, gbc);
        row++;

        addRow(form, gbc, row++, "Dosage (e.g., 500 mg) *", dosageField);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.35;
        form.add(boldLabel("Frequency *"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.65;
        frequencyCombo.setEditable(true);
        form.add(frequencyCombo, gbc);
        row++;

        addRow(form, gbc, row++, "Start Date (YYYY-MM-DD) *", startDateField);
        addRow(form, gbc, row++, "End Date (YYYY-MM-DD) *", endDateField);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.35;
        subDetailLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        form.add(subDetailLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.65;
        form.add(subDetailField, gbc);
        row++;

        addRow(form, gbc, row, "Notes / Instructions", notesField);

        typeCombo.addActionListener(e -> updateSubDetailLabel());
        updateSubDetailLabel();

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> {
            resultMedicine = null;
            dispose();
        });

        JButton saveBtn = new JButton(existingMedicine == null ? "Save Medicine" : "Update Medicine");
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

    private void addRow(JPanel form, GridBagConstraints gbc, int row, String label, JTextField field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.35;
        form.add(boldLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        field.setMargin(new Insets(4, 6, 4, 6));
        form.add(field, gbc);
    }

    private JLabel boldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        return lbl;
    }

    private void updateSubDetailLabel() {
        String selected = (String) typeCombo.getSelectedItem();
        if ("PRESCRIPTION".equals(selected)) {
            subDetailLabel.setText("Doctor Name");
        } else {
            subDetailLabel.setText("OTC Category");
        }
    }

    private void populateIfEditing() {
        if (existingMedicine == null) {
            return;
        }
        nameField.setText(existingMedicine.getMedicineName());
        typeCombo.setSelectedItem(existingMedicine.getMedicineType());
        dosageField.setText(existingMedicine.getDosage());
        frequencyCombo.setSelectedItem(existingMedicine.getFrequency());
        startDateField.setText(existingMedicine.getStartDate().toString());
        endDateField.setText(existingMedicine.getEndDate().toString());
        notesField.setText(existingMedicine.getNotes() != null ? existingMedicine.getNotes() : "");

        if (existingMedicine instanceof PrescriptionMedicine) {
            subDetailField.setText(((PrescriptionMedicine) existingMedicine).getDoctorName());
        } else if (existingMedicine instanceof OTCMedicine) {
            subDetailField.setText(((OTCMedicine) existingMedicine).getPurposeCategory());
        }
        updateSubDetailLabel();
    }

    private void handleSave() {
        try {
            String medName = InputValidator.requireNonEmpty(nameField.getText(), "Medicine Name");
            String dosage = InputValidator.requireNonEmpty(dosageField.getText(), "Dosage");
            Object freqObj = frequencyCombo.getSelectedItem();
            String frequency = InputValidator.requireNonEmpty(freqObj != null ? freqObj.toString() : "", "Frequency");
            LocalDate startDate = InputValidator.parseDate(startDateField.getText(), "Start Date");
            LocalDate endDate = InputValidator.parseDate(endDateField.getText(), "End Date");
            InputValidator.validateDateRange(startDate, endDate);

            String type = (String) typeCombo.getSelectedItem();
            String detail = subDetailField.getText().trim();
            String notes = notesField.getText().trim();
            int id = existingMedicine != null ? existingMedicine.getId() : 0;

            if ("PRESCRIPTION".equals(type)) {
                resultMedicine = new PrescriptionMedicine(
                        id, userId, medName, dosage, frequency, startDate, endDate, detail, notes, null
                );
            } else {
                resultMedicine = new OTCMedicine(
                        id, userId, medName, dosage, frequency, startDate, endDate, detail, notes, null
                );
            }
            dispose();
        } catch (InvalidInputException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    public Medicine getResultMedicine() {
        return resultMedicine;
    }
}
