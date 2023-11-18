import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
//Initial Credentials: Username: user1, password: 1234
public class User {
    private String userId;
    private String pin;
    private double balance;
    private List<String> transactions;

    public User(String userId, String pin, double balance) {
        this.userId = userId;
        this.pin = pin;
        this.balance = balance;
        this.transactions = new ArrayList<>();
    }

    public String getUserId() {
        return userId;
    }

    public String getPin() {
        return pin;
    }

    public double getBalance() {
        return balance;
    }

    public List<String> getTransactions() {
        return transactions;
    }

    public void deposit(double amount) {
        balance += amount;
        transactions.add("Deposit: +" + amount);
    }

    public void withdraw(double amount) {
        if (amount <= balance) {
            balance -= amount;
            transactions.add("Withdraw: -" + amount);
        } else {
            transactions.add("Withdraw failed. Insufficient balance.");
        }
    }

    public void transfer(User recipient, double amount) {
        if (amount <= balance) {
            balance -= amount;
            recipient.deposit(amount);
            transactions.add("Transfer to " + recipient.getUserId() + ": -" + amount);
        } else {
            transactions.add("Transfer failed. Insufficient balance.");
        }
    }
}

class UserManager {
    public static List<User> createDummyUsers() {
        List<User> users = new ArrayList<>();
        users.add(new User("user1", "1234", 1000));
        users.add(new User("user2", "5678", 500));
        users.add(new User("user3", "0000", 1500)); // Additional user
        return users;
    }
}

class ATMApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}

class LoginFrame extends JFrame {
    private JTextField userIdField;
    private JPasswordField pinField;

    public LoginFrame() {
        setTitle("ATM Login");
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        userIdField = new JTextField();
        pinField = new JPasswordField();

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> onLogin());

        panel.add(new JLabel("User ID:"));
        panel.add(userIdField);
        panel.add(new JLabel("PIN:"));
        panel.add(pinField);
        panel.add(loginButton);

        add(panel);
    }

    private void onLogin() {
        String userId = userIdField.getText();
        String pin = new String(pinField.getPassword());
        User user = authenticateUser(userId, pin);

        if (user != null) {
            ATMFrame atmFrame = new ATMFrame(user);
            atmFrame.setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid user credentials", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private User authenticateUser(String userId, String pin) {
        List<User> users = UserManager.createDummyUsers();
        for (User user : users) {
            if (user.getUserId().equals(userId) && user.getPin().equals(pin)) {
                return user;
            }
        }
        return null;
    }
}

class ATMFrame extends JFrame {
    private User user;
    private TransactionHistoryPanel transactionHistoryPanel;

    public ATMFrame(User user) {
        this.user = user;
        setTitle("ATM Operations - User: " + user.getUserId());
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        transactionHistoryPanel = new TransactionHistoryPanel(user);

        BalancePanel balancePanel = new BalancePanel(user);
        WithdrawPanel withdrawPanel = new WithdrawPanel(user, balancePanel, transactionHistoryPanel);
        DepositPanel depositPanel = new DepositPanel(user, balancePanel, transactionHistoryPanel);
        TransferPanel transferPanel = new TransferPanel(user, balancePanel, transactionHistoryPanel);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Balance", balancePanel);
        tabbedPane.addTab("Transaction History", transactionHistoryPanel);
        tabbedPane.addTab("Withdraw", withdrawPanel);
        tabbedPane.addTab("Deposit", depositPanel);
        tabbedPane.addTab("Transfer", transferPanel);

        add(tabbedPane);
    }
}

class BalancePanel extends JPanel {
    private User user;
    private JLabel balanceLabel;

    public BalancePanel(User user) {
        this.user = user;
        balanceLabel = new JLabel("Balance: $" + user.getBalance());
        add(balanceLabel);
    }

    public void updateBalance() {
        balanceLabel.setText("Balance: $" + user.getBalance());
    }
}

class TransactionHistoryPanel extends JPanel {
    private User user;
    private JTextArea textArea;

    public TransactionHistoryPanel(User user) {
        this.user = user;
        setLayout(new BorderLayout());

        textArea = new JTextArea();
        textArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);

        updateTransactionHistory();
    }

    public void updateTransactionHistory() {
        List<String> transactions = user.getTransactions();
        for (String transaction : transactions) {
            textArea.append(transaction + "\n");
        }
    }
}

class WithdrawPanel extends JPanel {
    private User user;
    private JTextField amountField;
    private BalancePanel balancePanel;
    private TransactionHistoryPanel transactionHistoryPanel;

    public WithdrawPanel(User user, BalancePanel balancePanel, TransactionHistoryPanel transactionHistoryPanel) {
        this.user = user;
        this.balancePanel = balancePanel;
        this.transactionHistoryPanel = transactionHistoryPanel;

        setLayout(new FlowLayout());

        amountField = new JTextField(10);
        JButton withdrawButton = new JButton("Withdraw");
        withdrawButton.addActionListener(e -> onWithdraw());

        add(new JLabel("Amount: "));
        add(amountField);
        add(withdrawButton);
    }

    private void onWithdraw() {
        try {
            double amount = Double.parseDouble(amountField.getText());
            user.withdraw(amount);
            balancePanel.updateBalance();
            transactionHistoryPanel.updateTransactionHistory();
            JOptionPane.showMessageDialog(this, "Withdrawal successful", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid amount", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

class DepositPanel extends JPanel {
    private User user;
    private JTextField amountField;
    private BalancePanel balancePanel;
    private TransactionHistoryPanel transactionHistoryPanel;

    public DepositPanel(User user, BalancePanel balancePanel, TransactionHistoryPanel transactionHistoryPanel) {
        this.user = user;
        this.balancePanel = balancePanel;
        this.transactionHistoryPanel = transactionHistoryPanel;

        setLayout(new FlowLayout());

        amountField = new JTextField(10);
        JButton depositButton = new JButton("Deposit");
        depositButton.addActionListener(e -> onDeposit());

        add(new JLabel("Amount: "));
        add(amountField);
        add(depositButton);
    }

    private void onDeposit() {
        try {
            double amount = Double.parseDouble(amountField.getText());
            user.deposit(amount);
            balancePanel.updateBalance();
            transactionHistoryPanel.updateTransactionHistory();
            JOptionPane.showMessageDialog(this, "Deposit successful", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid amount", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

class TransferPanel extends JPanel {
    private User user;
    private JTextField beneficiaryField;
    private JTextField amountField;
    private BalancePanel balancePanel;
    private TransactionHistoryPanel transactionHistoryPanel;

    public TransferPanel(User user, BalancePanel balancePanel, TransactionHistoryPanel transactionHistoryPanel) {
        this.user = user;
        this.balancePanel = balancePanel;
        this.transactionHistoryPanel = transactionHistoryPanel;

        setLayout(new FlowLayout());

        beneficiaryField = new JTextField(10);
        amountField = new JTextField(10);
        JButton transferButton = new JButton("Transfer");
        JButton addBeneficiaryButton = new JButton("Add Beneficiary");

        transferButton.addActionListener(e -> onTransfer());
        addBeneficiaryButton.addActionListener(e -> onAddBeneficiary());

        add(new JLabel("Beneficiary: "));
        add(beneficiaryField);
        add(new JLabel("Amount: "));
        add(amountField);
        add(transferButton);
        add(addBeneficiaryButton);
    }

    private void onTransfer() {
        try {
            String recipientId = beneficiaryField.getText();
            double amount = Double.parseDouble(amountField.getText());

            User recipient = findRecipientUser(recipientId);

            if (recipient != null) {
                user.transfer(recipient, amount);
                balancePanel.updateBalance();
                transactionHistoryPanel.updateTransactionHistory();
                JOptionPane.showMessageDialog(this, "Transfer successful", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Recipient not found", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid amount", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onAddBeneficiary() {
        String newBeneficiary = JOptionPane.showInputDialog(this, "Enter new beneficiary's User ID:");
        if (newBeneficiary != null && !newBeneficiary.trim().isEmpty()) {
            beneficiaryField.setText(newBeneficiary);
            JOptionPane.showMessageDialog(this, "Beneficiary added successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private User findRecipientUser(String recipientId) {
        List<User> users = UserManager.createDummyUsers();
        for (User u : users) {
            if (u.getUserId().equals(recipientId)) {
                return u;
            }
        }
        return null;
    }
}