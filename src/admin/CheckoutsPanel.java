package admin;

import db.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class CheckoutsPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JButton returnBtn, refreshBtn, addBtn;

    public CheckoutsPanel() {
        setLayout(new BorderLayout(10, 10));

        // Table model
        model = new DefaultTableModel(
                new String[]{"Checkout ID", "Book ID", "Borrower ID", "Checkout Date", "Return Date", "Fine"}, 0
        );
        table = new JTable(model);
        table.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        addBtn = new JButton("Add Checkout");
        returnBtn = new JButton("Return Book");
        refreshBtn = new JButton("Refresh");
        btnPanel.add(addBtn);
        btnPanel.add(returnBtn);
        btnPanel.add(refreshBtn);
        add(btnPanel, BorderLayout.SOUTH);

        // Load checkouts initially
        SwingUtilities.invokeLater(this::loadCheckouts);

        // Button actions
        addBtn.addActionListener(e -> addCheckout());
        returnBtn.addActionListener(e -> returnSelectedBook());
        refreshBtn.addActionListener(e -> loadCheckouts());
    }

    private void loadCheckouts() {
        model.setRowCount(0);
        try (Connection con = DBConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM checkout")) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("checkout_id"),
                        rs.getInt("book_id"),
                        rs.getInt("borrow_id"),
                        rs.getDate("checkout_date"),
                        rs.getDate("return_date"),
                        rs.getInt("fine")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading checkouts: " + ex.getMessage());
        }
    }

    private void returnSelectedBook() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a checkout to return.");
            return;
        }

        int checkoutId = (int) model.getValueAt(selectedRow, 0);
        int bookId = (int) model.getValueAt(selectedRow, 1);
        int borrowerId = (int) model.getValueAt(selectedRow, 2);

        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement psCheckout = con.prepareStatement("SELECT checkout_date FROM checkout WHERE checkout_id=?");
            psCheckout.setInt(1, checkoutId);
            ResultSet rs = psCheckout.executeQuery();

            boolean overdue = false;
            if (rs.next()) {
                java.sql.Date checkoutDate = rs.getDate("checkout_date");
                java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
                long diff = today.getTime() - checkoutDate.getTime();
                long daysDiff = diff / (1000 * 60 * 60 * 24);

                if (daysDiff > 30) {
                    overdue = true;
                    JOptionPane.showMessageDialog(this, "Book is overdue! Returned after " + daysDiff + " days.");
                }
            }

            PreparedStatement psReturn = con.prepareStatement("UPDATE checkout SET return_date=CURDATE() WHERE checkout_id=?");
            psReturn.setInt(1, checkoutId);
            psReturn.executeUpdate();

            PreparedStatement psUpdateBook = con.prepareStatement("UPDATE books SET available_copies = available_copies + 1 WHERE book_id=?");
            psUpdateBook.setInt(1, bookId);
            psUpdateBook.executeUpdate();

            if (overdue) {
                int fineAmount = 50; 
                PreparedStatement psFine = con.prepareStatement("UPDATE borrower SET current_fine = current_fine + ? WHERE borrow_id=?");
                psFine.setInt(1, fineAmount);
                psFine.setInt(2, borrowerId);
                psFine.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Book returned successfully.");
            loadCheckouts();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error returning book: " + ex.getMessage());
        }
    }

    private void addCheckout() {
        try (Connection con = DBConnection.getConnection()) {
            // Get all borrowers
            PreparedStatement psBorrowers = con.prepareStatement("SELECT borrow_id, name FROM borrower");
            ResultSet rsB = psBorrowers.executeQuery();
            Vector<String> borrowers = new Vector<>();
            Vector<Integer> borrowerIds = new Vector<>();
            while (rsB.next()) {
                borrowerIds.add(rsB.getInt("borrow_id"));
                borrowers.add(rsB.getInt("borrow_id") + " - " + rsB.getString("name"));
            }

            // Get all books with available copies
            PreparedStatement psBooks = con.prepareStatement("SELECT book_id, title, available_copies FROM books WHERE available_copies>0");
            ResultSet rsBooks = psBooks.executeQuery();
            Vector<String> books = new Vector<>();
            Vector<Integer> bookIds = new Vector<>();
            while (rsBooks.next()) {
                bookIds.add(rsBooks.getInt("book_id"));
                books.add(rsBooks.getInt("book_id") + " - " + rsBooks.getString("title") + " (" + rsBooks.getInt("available_copies") + " available)");
            }

            if (borrowers.isEmpty() || books.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No borrowers or books with available copies.");
                return;
            }

            JComboBox<String> borrowerBox = new JComboBox<>(borrowers);
            JComboBox<String> bookBox = new JComboBox<>(books);
            Object[] fields = {"Select Borrower:", borrowerBox, "Select Book:", bookBox};

            int option = JOptionPane.showConfirmDialog(this, fields, "Add Checkout", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                int selectedBorrower = borrowerIds.get(borrowerBox.getSelectedIndex());
                int selectedBook = bookIds.get(bookBox.getSelectedIndex());

                // Insert into checkout
                PreparedStatement psInsert = con.prepareStatement(
                        "INSERT INTO checkout(book_id, borrow_id, checkout_date, return_date, fine) VALUES(?,?,CURDATE(),NULL,0)"
                );
                psInsert.setInt(1, selectedBook);
                psInsert.setInt(2, selectedBorrower);
                psInsert.executeUpdate();

                // Reduce available copies
                PreparedStatement psUpdateBook = con.prepareStatement("UPDATE books SET available_copies = available_copies - 1 WHERE book_id=?");
                psUpdateBook.setInt(1, selectedBook);
                psUpdateBook.executeUpdate();

                JOptionPane.showMessageDialog(this, "Checkout added successfully!");
                loadCheckouts();
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error adding checkout: " + ex.getMessage());
        }
    }

    // Test panel standalone
    public static void main(String[] args) {
        JFrame frame = new JFrame("Checkouts Management");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400);
        frame.add(new CheckoutsPanel());
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
