import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BankManagementSystemGUI {
    private static final String FILE_NAME = "bank_accounts.txt";
    private static int currentAccountNumber = -1; // Track currently logged in account number
    private static JTextArea outputArea;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Bank Management System");
        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        frame.add(panel);
        placeLoginComponents(panel);

        frame.setVisible(true);
    }

    private static void placeLoginComponents(JPanel panel) {
        panel.setLayout(null);

        JLabel accountNumberLabel = new JLabel("Account Number:");
        accountNumberLabel.setBounds(10, 20, 150, 25);
        panel.add(accountNumberLabel);

        JTextField accountNumberText = new JTextField(20);
        accountNumberText.setBounds(150, 20, 165, 25);
        panel.add(accountNumberText);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(10, 50, 150, 25);
        panel.add(passwordLabel);

        JPasswordField passwordField = new JPasswordField(20);
        passwordField.setBounds(150, 50, 165, 25);
        panel.add(passwordField);

        JButton loginButton = new JButton("Login");
        loginButton.setBounds(100, 80, 150, 25);
        panel.add(loginButton);

        JButton createAccountButton = new JButton("Create Account");
        createAccountButton.setBounds(100, 110, 150, 25);
        panel.add(createAccountButton);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int accountNumber = Integer.parseInt(accountNumberText.getText());
                String password = new String(passwordField.getPassword());

                if (validateLogin(accountNumber, password)) {
                    currentAccountNumber = accountNumber;
                    panel.removeAll();
                    placeTransactionComponents(panel);
                    panel.revalidate();
                    panel.repaint();
                } else {
                    JOptionPane.showMessageDialog(panel, "Invalid account number or password. Please try again.");
                }
            }
        });

        createAccountButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = JOptionPane.showInputDialog(panel, "Enter your name:");
                String password = JOptionPane.showInputDialog(panel, "Choose a password:");
                double initialBalance;
                while (true) {
                    String balanceInput = JOptionPane.showInputDialog(panel, "Enter initial balance:");
                    try {
                        initialBalance = Double.parseDouble(balanceInput);
                        Object[] options = {"Urban", "Rural"};
                        int locationChoice = JOptionPane.showOptionDialog(null, "Select your location:", "Location",
                                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                        boolean isUrban = (locationChoice == 0); // true if Urban, false if Rural
        
                        double minBalance = isUrban ? 5000 : 500;
                        if (initialBalance < minBalance) {
                            int option = JOptionPane.showConfirmDialog(null, "Error: Initial balance must be at least " + minBalance +
                                    " for " + (isUrban ? "urban" : "rural") + " accounts.\nDo you want to enter a new balance?", "Error", JOptionPane.YES_NO_OPTION);
                            if (option == JOptionPane.YES_OPTION) {
                                continue; // Prompt user to enter balance again
                            } else {
                                return; // Exit method if user chooses not to enter new balance
                            }
                        } else {
                            // Valid balance entered, proceed to create account
                            int newAccountNumber = generateAccountNumber();
                            createAccount(name, newAccountNumber, initialBalance, password, isUrban);
                            JOptionPane.showMessageDialog(null, "Account created successfully!\nYour account number is: " + newAccountNumber);
                            break; // Exit loop
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, "Error: Please enter a valid numeric balance.");
                    }
                }
            }
            
        });
    }

    private static void placeTransactionComponents(JPanel panel) {
        panel.setLayout(null);

        JLabel welcomeLabel = new JLabel("Welcome! What would you like to do?");
        welcomeLabel.setBounds(50, 20, 300, 25);
        panel.add(welcomeLabel);

        JButton depositButton = new JButton("Deposit");
        depositButton.setBounds(100, 60, 200, 25);
        panel.add(depositButton);

        JButton withdrawButton = new JButton("Withdraw");
        withdrawButton.setBounds(100, 100, 200, 25);
        panel.add(withdrawButton);

        JButton checkBalanceButton = new JButton("Check Balance");
        checkBalanceButton.setBounds(100, 140, 200, 25);
        panel.add(checkBalanceButton);

        JButton transferButton = new JButton("Transfer Money");
        transferButton.setBounds(100, 180, 200, 25);
        panel.add(transferButton);

        JButton exitButton = new JButton("Exit");
        exitButton.setBounds(100, 220, 200, 25);
        panel.add(exitButton);

        outputArea = new JTextArea();
        outputArea.setBounds(50, 260, 300, 100);
        panel.add(outputArea);

        depositButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                double amount = Double.parseDouble(JOptionPane.showInputDialog(panel, "Enter the amount to deposit:"));
                double updatedBalance = deposit(currentAccountNumber, amount);
                if (updatedBalance >= 0) {
                    outputArea.setText("Amount deposited successfully!\n\n" + "Your balance is: " + updatedBalance);
                } else {
                    outputArea.setText("Error: Unable to deposit amount.");
                }
            }
        });

        withdrawButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                double amount = Double.parseDouble(JOptionPane.showInputDialog(panel, "Enter the amount to withdraw:"));
                double updatedBalance = withdraw(currentAccountNumber, amount);
                if (updatedBalance >= 0) {
                    outputArea.setText("Amount withdrawn successfully!\n\n" + "Your balance is: " + updatedBalance);
                } else {
                    outputArea.setText("Error: Insufficient funds or unable to withdraw amount.");
                }
            }
        });

        checkBalanceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                double balance = getAccountBalance(currentAccountNumber);
                if (balance >= 0) {
                    outputArea.setText("Your balance is: " + balance);
                } else {
                    outputArea.setText("Error: Unable to retrieve balance.");
                }
            }
        });

        transferButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int receiverAccountNumber = Integer.parseInt(JOptionPane.showInputDialog(panel, "Enter the receiver's account number:"));
                double amount = Double.parseDouble(JOptionPane.showInputDialog(panel, "Enter the amount to transfer:"));
                transferMoney(currentAccountNumber, receiverAccountNumber, amount);
                outputArea.setText("Money transferred successfully!");
            }
        });

        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
    }

    private static boolean validateLogin(int accountNumber, String password) {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 4) {
                    System.out.println("Error: Invalid format in the file: " + line);
                    continue;
                }
                int accNumber;
                try {
                    accNumber = Integer.parseInt(parts[0].trim());
                } catch (NumberFormatException e) {
                    System.out.println("Error: Invalid account number format in the file: " + parts[0]);
                    continue;
                }
                if (accNumber == accountNumber) {
                    String storedPassword = parts[3].trim();
                    return storedPassword.equals(password);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false; // Account not found
    }

    private static int generateAccountNumber() {
        // Logic to generate a unique account number
        // For simplicity, you can implement a random number generator or use a counter
        return (int) (Math.random() * 10000);
    }

    private static

 void createAccount(String name, int accountNumber, double balance, String password,boolean isUrban) {
    double minBalance = isUrban ? 5000 : 500; // Determine minimum balance based on location

    if (balance < minBalance) {
        JOptionPane.showMessageDialog(null, "Error: Initial balance must be at least " + minBalance +
                " for " + (isUrban ? "urban" : "rural") + " accounts.");
        return; // Exit method if balance requirement not met
    }

        Account account = new Account(name, accountNumber, balance, password);
        writeAccountToFile(account);
    }

    private static double getAccountBalance(int accountNumber) {
        Account account = getAccountByAccountNumber(accountNumber);
        if (account != null) {
            return account.getBalance();
        } else {
            return -1; // Error: Account not found
        }
    }

    private static double deposit(int accountNumber, double amount) {
        Account account = getAccountByAccountNumber(accountNumber);
        if (account != null) {
            double updatedBalance = account.deposit(amount);
            updateAccountBalanceInFile(account);
            return updatedBalance;
        } else {
            return -1; // Error: Account not found
        }
    }

    private static double withdraw(int accountNumber, double amount) {
        Account account = getAccountByAccountNumber(accountNumber);
        if (account != null) {
            double updatedBalance = account.withdraw(amount);
            if (updatedBalance >= 0) {
                updateAccountBalanceInFile(account);
            }
            return updatedBalance;
        } else {
            return -1; // Error: Account not found
        }
    }

    private static void transferMoney(int senderAccountNumber, int receiverAccountNumber, double amount) {
        Account senderAccount = getAccountByAccountNumber(senderAccountNumber);
        Account receiverAccount = getAccountByAccountNumber(receiverAccountNumber);
        if (senderAccount != null && receiverAccount != null) {
            if (amount <= senderAccount.getBalance()) {
                senderAccount.withdraw(amount);
                receiverAccount.deposit(amount);
                updateAccountBalanceInFile(senderAccount);
                updateAccountBalanceInFile(receiverAccount);
            }
        }
    }

    private static void writeAccountToFile(Account account) {
        try (FileWriter fileWriter = new FileWriter(FILE_NAME, true)) {
            fileWriter.write(account.toString() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void updateAccountBalanceInFile(Account account) {
        String tempFileName = "temp.txt";
        File inputFile = new File(FILE_NAME);
        File tempFile = new File(tempFileName);

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    int accNumber = Integer.parseInt(parts[0].trim());
                    if (accNumber == account.getAccountNumber()) {
                        writer.write(account.toString() + "\n");
                    } else {
                        writer.write(line + "\n");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Delete the original file and rename the temp file
        if (inputFile.delete()) {
            if (!tempFile.renameTo(inputFile)) {
                System.out.println("Error: Failed to update account balance in file.");
            }
        }
    }

    private static String getTimestamp() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        return formatter.format(date);
    }

    private static Account getAccountByAccountNumber(int accountNumber) {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 4) {
                    System.out.println("Error: Invalid format in the file: " + line);
                    continue;
                }
                int accNumber;
                try {
                    accNumber = Integer.parseInt(parts[0].trim());
                } catch (NumberFormatException e) {
                    System.out.println("Error: Invalid account number format in the file: " + parts[0]);
                    continue;
                }
                if (accNumber == accountNumber) {
                    String name = parts[1].trim();
                    double balance;
                    try {
                        balance = Double.parseDouble(parts[2].trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Error: Invalid balance format in the file: " + parts[2]);
                        continue;
                    }
                    String password = parts[3].trim();
                    return new Account(name, accNumber, balance, password);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; // Account not found
    }
}

class Account {
    private String name;
    private int accountNumber;
    private double balance;
    private String password;

    public Account(String name, int accountNumber, double balance, String password) {
        this.name = name;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.password = password;
    }

    public double deposit(double amount) {
        this.balance += amount;
        return this.balance;
    }

    public double withdraw(double amount) {
        if (amount <= this.balance) {
            this.balance -= amount;
            return this.balance;
        } else {
            return -1; // Error: Insufficient funds
        }
    }

    public double getBalance() {
        return this.balance;
    }

    public int getAccountNumber() {
        return this.accountNumber;
    }

    @Override
    public String toString() {
        return accountNumber + "," + name + "," + balance + "," + password;
    }
}
