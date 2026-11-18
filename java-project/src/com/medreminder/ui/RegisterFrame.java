package com.medreminder.ui;

import com.medreminder.dao.UserDAO;
import com.medreminder.exception.DatabaseOperationException;
import com.medreminder.exception.InvalidInputException;
import com.medreminder.model.User;
import com.medreminder.service.FileReportService;
import com.medreminder.service.InputValidator;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * Registration window for creating new user accounts with validation.
 */
public class RegisterFrame extends JFrame {

    private final LoginFrame parentFrame;
    private final JTextField fullNameField = new JTextField(20);
    private final JTextField usernameField = new JTextField(20);
    private final JTextField emailField = new JTextField(20);
    private final JTextField phoneField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JPasswordField confirmPasswordField = new JPasswordField(20);

    private final UserDAO userDAO = new UserDAO();
    private final FileReportService fileService = new FileReportService();

    public RegisterFrame(LoginFrame parentFrame) {
        this.parentFrame = parentFrame;
        setTitle("Create Account - Medicine Reminder System");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(480, 540);
        setLocationRelativeTo(parentFrame);
        setResizable(false);
        initComponents();
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(new Color(245, 247, 250));
        root.setBorder(BorderFactory.createEmptyBorder(20, 26, 20, 26));

        JLabel header = new JLabel("Create Your MedTrack Account", SwingConstants.CENTER);
        header.setFont(new Font("SansSerif", Font.BOLD, 20));
        header.setForeground(new Color(15, 23, 42));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(18, 22, 18, 22)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.weightx = 1.0;

        int row = 0;
        addField(form, gbc, row++, "Full Name *", fullNameField);
        addField(form, gbc, row++, "Username *", usernameField);
        addField(form, gbc, row++, "Email Address *", emailField);
        addField(form, gbc, row++, "Phone Number", phoneField);
        addField(form, gbc, row++, "Password (min 6 chars) *", passwordField);
        addField(form, gbc, row++, "Confirm Password *", confirmPasswordField);

        JPanel buttons = new JPanel(new BorderLayout(10, 0));
        buttons.setOpaque(false);

        JButton backBtn = new JButton("Back to Login");
        backBtn.addActionListener(e -> goBackToLogin());

        JButton registerBtn = new JButton("Register Account");
        registerBtn.setBackground(new Color(15, 118, 110));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        registerBtn.addActionListener(e -> handleRegister());

        buttons.add(backBtn, BorderLayout.WEST);
        buttons.add(registerBtn, BorderLayout.CENTER);

        root.add(header, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, String labelText, JTextField field) {
        gbc.gridx = 0;
        gbc.gridy = row * 2;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        panel.add(label, gbc);

        gbc.gridy = row * 2 + 1;
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
        field.setMargin(new Insets(5, 7, 5, 7));
        panel.add(field, gbc);
    }

    private void handleRegister() {
        try {
            String fullName = InputValidator.requireNonEmpty(fullNameField.getText(), "Full Name");
            String username = InputValidator.requireNonEmpty(usernameField.getText(), "Username");
            String email = InputValidator.validateEmail(emailField.getText());
            String phone = phoneField.getText().trim();
            String password = InputValidator.validatePassword(new String(passwordField.getPassword()));
            String confirm = new String(confirmPasswordField.getPassword());

            if (!password.equals(confirm)) {
                throw new InvalidInputException("Confirm Password", "Passwords do not match.");
            }
            if (userDAO.usernameExists(username)) {
                throw new InvalidInputException("Username", "Username '" + username + "' is already taken.");
            }
            if (userDAO.emailExists(email)) {
                throw new InvalidInputException("Email", "An account with this email already exists.");
            }

            User newUser = new User();
            newUser.setFullName(fullName);
            newUser.setUsername(username);
            newUser.setEmail(email);
            newUser.setPhone(phone);

            userDAO.registerUser(newUser, password);
            fileService.appendActivityLog(username, "New user account registered.");

            JOptionPane.showMessageDialog(this,
                    "Account created successfully! You can now sign in.",
                    "Registration Complete",
                    JOptionPane.INFORMATION_MESSAGE);
            goBackToLogin();
        } catch (InvalidInputException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (DatabaseOperationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void goBackToLogin() {
        if (parentFrame != null) {
            parentFrame.setVisible(true);
        }
        dispose();
    }
}
