package admin;

import db.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class BorrowersPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JButton addBtn, removeBtn, editBtn, refreshBtn;

    public BorrowersPanel() {
        setLayout(new BorderLayout(10, 10));

        // Table model
        model = new DefaultTableModel(
                new String[]{"Borrower ID", "Name", "Current Fine", "Contact"}, 0
        );
        table = new JTable(model);
        table.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel();
        addBtn = new JButton("Add Borrower");
        removeBtn = new JButton("Remove Borrower");
        editBtn = new JButton("Edit Borrower");
        refreshBtn = new JButton("Refresh");
        btnPanel.add(addBtn);
        btnPanel.add(removeBtn);
        btnPanel.add(editBtn);
        btnPanel.add(refreshBtn);
        add(btnPanel, BorderLayout.SOUTH);

        // Load table
        SwingUtilities.invokeLater(this::loadBorrowers);

        // Button actions
        addBtn.addActionListener(e -> addBorrower());
        removeBtn.addActionListener(e -> removeBorrower());
        editBtn.addActionListener(e -> editBorrower());
        refreshBtn.addActionListener(e -> loadBorrowers());
    }

    private void loadBorrowers() {
        model.setRowCount(0);
        try (Connection con = DBConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT borrow_id, name, current_fine, contact FROM borrower")) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("borrow_id"),
                        rs.getString("name"),
                        rs.getFloat("current_fine"),
                        rs.getLong("contact")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading borrowers: " + ex.getMessage());
        }
    }

    private void addBorrower() {
        JTextField nameField = new JTextField();
        JTextField fineField = new JTextField("0");
        JTextField contactField = new JTextField();

        Object[] fields = {
                "Name:", nameField,
                "Current Fine:", fineField,
                "Contact:", contactField
        };

        int option = JOptionPane.showConfirmDialog(this, fields, "Add Borrower", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(
                         "INSERT INTO borrower(name, current_fine, contact) VALUES(?,?,?)"
                 )) {
                ps.setString(1, nameField.getText());
                ps.setFloat(2, Float.parseFloat(fineField.getText()));
                ps.setLong(3, Long.parseLong(contactField.getText()));
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Borrower added successfully!");
                loadBorrowers();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error adding borrower: " + ex.getMessage());
            }
        }
    }

    private void removeBorrower() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int borrowId = (int) model.getValueAt(selectedRow, 0);
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement("DELETE FROM borrower WHERE borrow_id=?")) {
                ps.setInt(1, borrowId);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Borrower removed successfully!");
                loadBorrowers();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error removing borrower: " + ex.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a borrower to remove.");
        }
    }

    private void editBorrower() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int borrowId = (int) model.getValueAt(selectedRow, 0);
            String currentName = (String) model.getValueAt(selectedRow, 1);
            float currentFine = (float) model.getValueAt(selectedRow, 2);
            long currentContact = (long) model.getValueAt(selectedRow, 3);

            JTextField nameField = new JTextField(currentName);
            JTextField fineField = new JTextField(String.valueOf(currentFine));
            JTextField contactField = new JTextField(String.valueOf(currentContact));

            Object[] fields = {
                    "Name:", nameField,
                    "Current Fine:", fineField,
                    "Contact:", contactField
            };

            int option = JOptionPane.showConfirmDialog(this, fields, "Edit Borrower", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                try (Connection con = DBConnection.getConnection();
                     PreparedStatement ps = con.prepareStatement(
                             "UPDATE borrower SET name=?, current_fine=?, contact=? WHERE borrow_id=?"
                     )) {
                    ps.setString(1, nameField.getText());
                    ps.setFloat(2, Float.parseFloat(fineField.getText()));
                    ps.setLong(3, Long.parseLong(contactField.getText()));
                    ps.setInt(4, borrowId);
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Borrower updated successfully!");
                    loadBorrowers();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error updating borrower: " + ex.getMessage());
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a borrower to edit.");
        }
    }

    // Test standalone
    public static void main(String[] args) {
        JFrame frame = new JFrame("Borrowers Management");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400);
        frame.add(new BorrowersPanel());
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
