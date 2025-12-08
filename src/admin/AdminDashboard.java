package admin;

import javax.swing.*;
import java.awt.*;

public class AdminDashboard extends JFrame {

    public AdminDashboard() {
        setTitle("Admin Dashboard");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 3, 10, 10));

        JButton booksBtn = new JButton("Manage Books");
        JButton borrowersBtn = new JButton("Manage Borrowers");
        JButton categoriesBtn = new JButton("Manage Categories");
        JButton checkoutsBtn = new JButton("Manage Checkouts");

        panel.add(booksBtn);
        panel.add(borrowersBtn);
        panel.add(categoriesBtn);
        panel.add(checkoutsBtn);

        add(panel, BorderLayout.CENTER);

        // Button actions
        booksBtn.addActionListener(e -> showPanel(new BooksPanel()));
        borrowersBtn.addActionListener(e -> showPanel(new BorrowersPanel()));
        categoriesBtn.addActionListener(e -> showPanel(new CategoriesPanel()));
        checkoutsBtn.addActionListener(e -> showPanel(new CheckoutsPanel()));

        setVisible(true);
    }

    private void showPanel(JPanel panel) {
        JFrame frame = new JFrame();
        frame.setTitle(panel.getClass().getSimpleName());
        frame.setSize(900, 500);
        frame.setLocationRelativeTo(null);
        frame.add(panel);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AdminDashboard::new);
    }
}
