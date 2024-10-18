import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class ManagerApp extends JFrame {
    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;
    private JPanel panel;
    private JTextArea textArea;

    public ManagerApp(Connection connection) {
        super("Manager Application");
        this.connection = connection;

        panel = new JPanel();
        textArea = new JTextArea(10, 30);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);

        panel.setLayout(new BorderLayout());
        panel.add(new JLabel("Contest Results:"), BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        add(panel);

        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        fetchResults();
    }

    private void fetchResults() {
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT c.description AS contest_description, p.name AS candidate_name, COALESCE(SUM(r.tally), 0) AS vote_count " +
                    "FROM contest c " +
                    "JOIN runs_in r ON c.contest_id = r.contest_id " +
                    "JOIN candidate ca ON r.cand_id = ca.cand_id " +
                    "JOIN person p ON ca.cand_id = p.ID " +
                    "GROUP BY c.contest_id, ca.cand_id " +
                    "ORDER BY c.contest_id, vote_count DESC");

            StringBuilder resultText = new StringBuilder();
            String currentContest = "";
            while (resultSet.next()) {
                String contestDescription = resultSet.getString("contest_description");
                String candidateName = resultSet.getString("candidate_name");
                int voteCount = resultSet.getInt("vote_count");

                if (!currentContest.equals(contestDescription)) {
                    if (!currentContest.isEmpty()) {
                        resultText.append("\n");
                    }
                    resultText.append("Contest: ").append(contestDescription).append("\n");
                    currentContest = contestDescription;
                }

                resultText.append("- ").append(candidateName).append(": ").append(voteCount).append(" votes\n");
            }

            textArea.setText(resultText.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Create database connection
        Connection connection = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:../univ.db");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create and display the manager application
        ManagerApp managerApp = new ManagerApp(connection);
    }
}







