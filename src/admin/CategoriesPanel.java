package admin;

import db.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class CategoriesPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JButton addBtn, removeBtn, refreshBtn;

    public CategoriesPanel() {
        setLayout(new BorderLayout(10, 10));

        model = new DefaultTableModel(new String[]{"Category ID", "Category Name"}, 0);
        table = new JTable(model);
        table.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        addBtn = new JButton("Add Category");
        removeBtn = new JButton("Remove Category");
        refreshBtn = new JButton("Refresh");
        btnPanel.add(addBtn);
        btnPanel.add(removeBtn);
        btnPanel.add(refreshBtn);
        add(btnPanel, BorderLayout.SOUTH);

        SwingUtilities.invokeLater(this::loadCategories);

        addBtn.addActionListener(e -> addCategory());
        removeBtn.addActionListener(e -> removeCategory());
        refreshBtn.addActionListener(e -> loadCategories());
    }

    private void loadCategories() {
        model.setRowCount(0);
        try (Connection con = DBConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM category")) {
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("cid"),
                        rs.getString("c_name")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading categories: " + ex.getMessage());
        }
    }

    private void addCategory() {
        String name = JOptionPane.showInputDialog(this, "Enter category name:");
        if (name != null && !name.isEmpty()) {
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement("INSERT INTO category(c_name) VALUES(?)")) {
                ps.setString(1, name);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Category added!");
                loadCategories();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error adding category: " + ex.getMessage());
            }
        }
    }

    private void removeCategory() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            int id = (int) model.getValueAt(row, 0);
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement psCheck = con.prepareStatement("SELECT * FROM books WHERE cid=?");
                 PreparedStatement psDel = con.prepareStatement("DELETE FROM category WHERE cid=?")) {
                psCheck.setInt(1, id);
                ResultSet rs = psCheck.executeQuery();
                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "Cannot remove category with books.");
                    return;
                }
                psDel.setInt(1, id);
                psDel.executeUpdate();
                JOptionPane.showMessageDialog(this, "Category removed!");
                loadCategories();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error removing category: " + ex.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Select a category first.");
        }
    }
}
