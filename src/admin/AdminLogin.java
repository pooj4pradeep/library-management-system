package admin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

import db.DBConnection;

public class AdminLogin extends JFrame {

    public AdminLogin() {
        setTitle("Admin Login - Library Management");
        setSize(450, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel userLabel = new JLabel("Username:");
        JTextField userField = new JTextField(15);
        JLabel passLabel = new JLabel("Password:");
        JPasswordField passField = new JPasswordField(15);
        JButton loginButton = new JButton("Login");

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(userLabel, gbc);
        gbc.gridx = 1;
        add(userField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(passLabel, gbc);
        gbc.gridx = 1;
        add(passField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(loginButton, gbc);

        loginButton.addActionListener(e -> {
            String username = userField.getText();
            String password = String.valueOf(passField.getPassword());

            if(username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please fill all fields", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection con = DBConnection.getConnection()) {
                String query = "SELECT * FROM admin WHERE username=? AND password=?";
                PreparedStatement ps = con.prepareStatement(query);
                ps.setString(1, username);
                ps.setString(2, password);
                ResultSet rs = ps.executeQuery();

                if(rs.next()) {
                    JOptionPane.showMessageDialog(null, "Login Successful!");
                    dispose();
                    new AdminDashboard();
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid username or password", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch(Exception ex) {
                JOptionPane.showMessageDialog(null, "Database error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AdminLogin::new);
    }
}
