import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class VoterApp extends JFrame {
    private JTextField voterIDField;
    private JButton seeContestsButton;
    private JPanel contestPanel;
    private Connection connection;

    // Store selected contest and candidate ID
    private String selectedContestId;
    private String selectedCandidateId;
    private ResultSet contestResultSet;

    public VoterApp() {
        super("Voter Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel inputPanel = new JPanel();
        JPanel buttonPanel = new JPanel();
        
        voterIDField = new JTextField(10);
        JLabel voterIDLabel = new JLabel("Enter Voter ID:");
        inputPanel.add(voterIDLabel);
        inputPanel.add(voterIDField);

        seeContestsButton = new JButton("See Contests");
        seeContestsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String voterID = voterIDField.getText().trim();
                if (!voterID.isEmpty()) {
                    fetchContests(voterID);
                } else {
                    JOptionPane.showMessageDialog(VoterApp.this, "Please enter your voter ID.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        buttonPanel.add(seeContestsButton);

        contestPanel = new JPanel();
        contestPanel.setLayout(new BoxLayout(contestPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(contestPanel);

        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);

        try {
            // Establish connection to SQLite database
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:../univ.db");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void fetchContests(String voterID) {
        try {
            // Fetch voter's county_id from the person table
            PreparedStatement countyStatement = connection.prepareStatement(
                "SELECT county_id FROM person WHERE ID = ?"
            );
            countyStatement.setString(1, voterID);
            ResultSet countyResultSet = countyStatement.executeQuery();

            // Check if the voter exists and fetch their county_id
            if (countyResultSet.next()) {
                String countyID = countyResultSet.getString("county_id");

                // Fetch the corresponding state_id using county_id
                PreparedStatement stateStatement = connection.prepareStatement(
                    "SELECT state_id FROM county WHERE county_id = ?"
                );
                stateStatement.setString(1, countyID);
                ResultSet stateResultSet = stateStatement.executeQuery();

                // Check if state_id is found
                if (stateResultSet.next()) {
                    String stateID = stateResultSet.getString("state_id");

                    // Fetch contests based on state_id and county_id
                    PreparedStatement contestStatement = connection.prepareStatement(
                        "SELECT c.contest_id, c.description " +
                        "FROM (SELECT lc.contest_id, c.description " +
                        "      FROM local_contest lc " +
                        "      JOIN contest c ON lc.contest_id = c.contest_id " +
                        "      WHERE lc.county_id = ? " +
                        "      UNION " +
                        "      SELECT sc.contest_id, c.description " +
                        "      FROM state_contest sc " +
                        "      JOIN contest c ON sc.contest_id = c.contest_id " +
                        "      WHERE sc.state_id = ? " +
                        "      UNION " +
                        "      SELECT nc.contest_id, c.description " +
                        "      FROM national_contest nc " +
                        "      JOIN contest c ON nc.contest_id = c.contest_id " +
                        "      UNION " +
                        "      SELECT stc.contest_id, c.description " +
                        "      FROM state_contest stc " +
                        "      JOIN contest c ON stc.contest_id = c.contest_id " +
                        "      WHERE stc.state_id = ?) c"
                    );
                    contestStatement.setString(1, countyID);
                    contestStatement.setString(2, stateID);
                    contestStatement.setString(3, stateID); // Use state_id for state-level contests
                    contestResultSet = contestStatement.executeQuery();

                    if (!contestResultSet.isBeforeFirst()) {
                        JOptionPane.showMessageDialog(this, "No contests available for this voter.", "Information", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        showContests();
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "State ID not found for the county.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Voter not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occurred while fetching contests: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showContests() {
        try {
            while (contestResultSet.next()) {
                String contestId = contestResultSet.getString("contest_id");
                String contestDescription = contestResultSet.getString("description");

                JButton contestButton = new JButton(contestDescription);
                contestButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        selectedContestId = contestId;
                        fetchCandidates(selectedContestId);
                    }
                });
                contestPanel.add(contestButton);
            }

            revalidate();
            repaint();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void fetchCandidates(String contestID) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                "SELECT p.ID, p.name FROM candidate cd " +
                "JOIN person p ON cd.cand_id = p.ID " +
                "JOIN runs_in r ON cd.cand_id = r.cand_id " +
                "WHERE r.contest_id = ? " +
                "ORDER BY p.name"
            );
            statement.setString(1, contestID);
            ResultSet resultSet = statement.executeQuery();

            JFrame candidateFrame = new JFrame("Select Candidate");
            JPanel candidatePanel = new JPanel();
            candidatePanel.setLayout(new BoxLayout(candidatePanel, BoxLayout.Y_AXIS));

            while (resultSet.next()) {
                String candidateID = resultSet.getString("ID");
                String candidateName = resultSet.getString("name");

                JButton candidateButton = new JButton(candidateName);
                candidateButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        selectedCandidateId = candidateID;
                        showConfirmVotesDialog();
                        candidateFrame.dispose();
                    }
                });
                candidatePanel.add(candidateButton);
            }
            resultSet.close();

            JScrollPane scrollPane = new JScrollPane(candidatePanel);
            candidateFrame.add(scrollPane);
            candidateFrame.setSize(300, 200);
            candidateFrame.setLocationRelativeTo(this);
            candidateFrame.setVisible(true);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void showConfirmVotesDialog() {
        JFrame confirmFrame = new JFrame("Confirm Votes");
        JPanel confirmPanel = new JPanel(new BorderLayout());

        JButton confirmButton = new JButton("Confirm Votes");
        confirmButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                confirmVotesAndExit();
                confirmFrame.dispose();
            }
        });

        confirmPanel.add(new JLabel("Are you sure you want to confirm your vote?"), BorderLayout.CENTER);
        confirmPanel.add(confirmButton, BorderLayout.SOUTH);

        confirmFrame.add(confirmPanel);
        confirmFrame.setSize(300, 150);
        confirmFrame.setLocationRelativeTo(this);
        confirmFrame.setVisible(true);
    }

    private void confirmVotesAndExit() {
        try {
            String voterID = voterIDField.getText().trim();
            if (voterID.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter your voter ID.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Check if the combination of candidate and contest already exists
            PreparedStatement conflictCheckStatement = connection.prepareStatement(
                "SELECT * FROM runs_in WHERE cand_id = ? AND contest_id = ?"
            );
            conflictCheckStatement.setString(1, selectedCandidateId);
            conflictCheckStatement.setString(2, selectedContestId);
            ResultSet conflictResultSet = conflictCheckStatement.executeQuery();

            if (conflictResultSet.next()) {
                // Update the tally for existing candidate in the contest
                PreparedStatement updateStatement = connection.prepareStatement(
                    "UPDATE runs_in SET tally = tally + 1 WHERE cand_id = ? AND contest_id = ?"
                );
                updateStatement.setString(1, selectedCandidateId);
                updateStatement.setString(2, selectedContestId);
                updateStatement.executeUpdate();
            } else {
                // Insert a new row
                PreparedStatement insertStatement = connection.prepareStatement(
                    "INSERT INTO runs_in (cand_id, contest_id, tally) VALUES (?, ?, 1)"
                );
                insertStatement.setString(1, selectedCandidateId);
                insertStatement.setString(2, selectedContestId);
                insertStatement.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Vote recorded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occurred while confirming votes: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new VoterApp().setVisible(true);
            }
        });
    }
}



