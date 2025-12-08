package admin;

import db.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class BooksPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JButton addBtn, removeBtn, refreshBtn, editBtn;

    public BooksPanel() {
        setLayout(new BorderLayout(10, 10));

        // Table model
        model = new DefaultTableModel(new String[]{"Book ID", "Title", "Author", "Total Copies", "Available Copies", "Category"}, 0);
        table = new JTable(model);
        table.setFillsViewportHeight(true);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel();
        addBtn = new JButton("Add Book");
        removeBtn = new JButton("Remove Book");
        refreshBtn = new JButton("Refresh");
        editBtn = new JButton("Edit Book");

        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(removeBtn);
        btnPanel.add(refreshBtn);
        add(btnPanel, BorderLayout.SOUTH);

        // Load data
        SwingUtilities.invokeLater(this::loadBooks);

        // Button actions
        addBtn.addActionListener(e -> addBook());
        removeBtn.addActionListener(e -> removeBook());
        refreshBtn.addActionListener(e -> loadBooks());
        editBtn.addActionListener(e -> editBook());
    }

    private void loadBooks() {
        model.setRowCount(0);
        try (Connection con = DBConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT b.book_id, b.title, b.author, b.total_copies, b.available_copies, c.c_name FROM books b JOIN category c ON b.cid=c.cid")) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getInt("total_copies"),
                        rs.getInt("available_copies"),
                        rs.getString("c_name")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading books: " + ex.getMessage());
        }
    }

    private void addBook() {
        JTextField titleField = new JTextField();
        JTextField authorField = new JTextField();
        JTextField totalField = new JTextField();
        JTextField availField = new JTextField();
        JTextField cidField = new JTextField();

        Object[] fields = {
                "Title:", titleField,
                "Author:", authorField,
                "Total Copies:", totalField,
                "Available Copies:", availField,
                "Category ID:", cidField
        };

        int option = JOptionPane.showConfirmDialog(this, fields, "Add Book", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement("INSERT INTO books(title, author, total_copies, available_copies, cid) VALUES(?,?,?,?,?)")) {
                ps.setString(1, titleField.getText());
                ps.setString(2, authorField.getText());
                ps.setInt(3, Integer.parseInt(totalField.getText()));
                ps.setInt(4, Integer.parseInt(availField.getText()));
                ps.setInt(5, Integer.parseInt(cidField.getText()));
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Book added successfully!");
                loadBooks();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error adding book: " + ex.getMessage());
            }
        }
    }

    private void removeBook() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int bookId = (int) model.getValueAt(selectedRow, 0);
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement("DELETE FROM books WHERE book_id=?")) {
                ps.setInt(1, bookId);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Book removed successfully!");
                loadBooks();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error removing book: " + ex.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a book to remove.");
        }
    }

    private void editBook() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a book to edit.");
            return;
        }

        int bookId = (int) model.getValueAt(selectedRow, 0);
        String title = (String) model.getValueAt(selectedRow, 1);
        String author = (String) model.getValueAt(selectedRow, 2);
        int totalCopies = (int) model.getValueAt(selectedRow, 3);
        int availableCopies = (int) model.getValueAt(selectedRow, 4);

        JTextField titleField = new JTextField(title);
        JTextField authorField = new JTextField(author);
        JTextField totalField = new JTextField(String.valueOf(totalCopies));
        JTextField availField = new JTextField(String.valueOf(availableCopies));

        Object[] fields = {
                "Title:", titleField,
                "Author:", authorField,
                "Total Copies:", totalField,
                "Available Copies:", availField
        };

        int option = JOptionPane.showConfirmDialog(this, fields, "Edit Book", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement("UPDATE books SET title=?, author=?, total_copies=?, available_copies=? WHERE book_id=?")) {
                ps.setString(1, titleField.getText());
                ps.setString(2, authorField.getText());
                ps.setInt(3, Integer.parseInt(totalField.getText()));
                ps.setInt(4, Integer.parseInt(availField.getText()));
                ps.setInt(5, bookId);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Book details updated successfully!");
                loadBooks();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error updating book: " + ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Books Management");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400);
        frame.add(new BooksPanel());
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
