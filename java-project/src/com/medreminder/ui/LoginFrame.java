package com.medreminder.ui;

import com.medreminder.dao.UserDAO;
import com.medreminder.exception.DatabaseOperationException;
import com.medreminder.exception.InvalidInputException;
import com.medreminder.model.User;
import com.medreminder.service.FileReportService;
import com.medreminder.service.InputValidator;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
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
 * Login window for the Medicine Reminder and Management System.
 */
public class LoginFrame extends JFrame {

    private final JTextField usernameField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final UserDAO userDAO = new UserDAO();
    private final FileReportService fileService = new FileReportService();

    public LoginFrame() {
        setTitle("MedTrack - Medicine Reminder & Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 410);
        setLocationRelativeTo(null);
        setResizable(false);
        initComponents();
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(new Color(245, 247, 250));
        root.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        // Header
        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.setOpaque(false);
        JLabel title = new JLabel("Medicine Reminder System", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(new Color(15, 23, 42));

        JLabel subtitle = new JLabel("Sign in to manage prescriptions, schedules & dose logs", SwingConstants.CENTER);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(new Color(100, 116, 139));
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);

        // Form card
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(22, 24, 22, 24)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.gridx = 0;

        gbc.gridy = 0;
        JLabel userLabel = new JLabel("Username or Email");
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        card.add(userLabel, gbc);

        gbc.gridy = 1;
        usernameField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        usernameField.setMargin(new Insets(6, 8, 6, 8));
        card.add(usernameField, gbc);

        gbc.gridy = 2;
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        card.add(passLabel, gbc);

        gbc.gridy = 3;
        passwordField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        passwordField.setMargin(new Insets(6, 8, 6, 8));
        card.add(passwordField, gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(14, 4, 6, 4);
        JButton loginBtn = new JButton("Sign In");
        loginBtn.setBackground(new Color(15, 118, 110));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        loginBtn.setFocusPainted(false);
        loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginBtn.addActionListener(e -> handleLogin());
        card.add(loginBtn, gbc);

        gbc.gridy = 5;
        gbc.insets = new Insets(4, 4, 2, 4);
        JButton registerBtn = new JButton("New User? Create an Account");
        registerBtn.setBackground(new Color(241, 245, 249));
        registerBtn.setForeground(new Color(30, 41, 59));
        registerBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        registerBtn.setFocusPainted(false);
        registerBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerBtn.addActionListener(e -> {
            new RegisterFrame(this).setVisible(true);
            setVisible(false);
        });
        card.add(registerBtn, gbc);

        getRootPane().setDefaultButton(loginBtn);

        // Footer hint
        JLabel hint = new JLabel("Demo user (after running schema.sql): arjun_med / MedTrack@123", SwingConstants.CENTER);
        hint.setFont(new Font("SansSerif", Font.ITALIC, 11));
        hint.setForeground(new Color(100, 116, 139));

        root.add(header, BorderLayout.NORTH);
        root.add(card, BorderLayout.CENTER);
        root.add(hint, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void handleLogin() {
        try {
            String identifier = InputValidator.requireNonEmpty(usernameField.getText(), "Username or Email");
            String password = new String(passwordField.getPassword());
            InputValidator.requireNonEmpty(password, "Password");

            User user = userDAO.authenticate(identifier, password);
            if (user == null) {
                JOptionPane.showMessageDialog(this,
                        "Invalid username/email or password. Please try again.",
                        "Authentication Failed",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            fileService.appendActivityLog(user.getUsername(), "User signed in.");
            DashboardFrame dashboard = new DashboardFrame(user);
            dashboard.setVisible(true);
            dispose();
        } catch (InvalidInputException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (DatabaseOperationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
