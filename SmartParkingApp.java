
package smartparking;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SmartParkingApp extends JFrame {

    private static final int TOTAL_SLOTS = 10;
    private static final double RATE_PER_HOUR = 20.0;
    private int selectedSlot = -1;

    private final Map<Integer, Vehicle> parkedVehicles =
            new HashMap<>();

    private final Map<Integer, JButton> slotButtons =
            new HashMap<>();

    private final ArrayList<Integer> slotNumbers =
            new ArrayList<>();

    private final JTextField vehicleField = new JTextField(15);
    private final JLabel availableLabel = new JLabel();
    private final JLabel occupiedLabel = new JLabel();
    private final JLabel revenueLabel = new JLabel();
    private final JTextArea logArea = new JTextArea(7, 35);

    private double totalRevenue = 0;

    private final DateTimeFormatter timeFormat =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    static class Vehicle {
        String number;
        LocalDateTime entryTime;

        Vehicle(String number) {
            this.number = number;
            this.entryTime = LocalDateTime.now();
        }
    }

    public SmartParkingApp() {
        setTitle("Smart Parking Management System");
        setSize(850, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        createInterface();
        updateDisplay();
    }

    private void createInterface() {
        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(240, 244, 248));
        setContentPane(mainPanel);

        JLabel heading = new JLabel(
                "SMART PARKING MANAGEMENT SYSTEM",
                SwingConstants.CENTER);
        heading.setFont(new Font("Arial", Font.BOLD, 23));
        heading.setForeground(new Color(30, 60, 100));
        mainPanel.add(heading, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setOpaque(false);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        JPanel slotsPanel = new JPanel(new GridLayout(2, 5, 10, 10));
        slotsPanel.setBorder(BorderFactory.createTitledBorder(
                "Parking Slots"));
        slotsPanel.setBackground(Color.WHITE);

        for (int i = 1; i <= TOTAL_SLOTS; i++) {
            final int slot = i;
            slotNumbers.add(i);

            JButton button = new JButton();
            button.setFont(new Font("Arial", Font.BOLD, 14));
            button.setFocusPainted(false);
            button.setOpaque(true);
            button.setBorderPainted(false);

            button.addActionListener(e -> {
                if (parkedVehicles.containsKey(slot)) {
                    showSlotDetails(slot);
                } else {
                    selectedSlot = slot;

                    addLog("Slot " + slot + " selected.");

                    JOptionPane.showMessageDialog(this,
                            "You selected Slot " + slot
                            + ".\nEnter the vehicle number and click Park Vehicle.");

                    updateDisplay();
                }
            });

            slotButtons.put(slot, button);
            slotsPanel.add(button);
        }

        centerPanel.add(slotsPanel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout(8, 8));
        rightPanel.setPreferredSize(new Dimension(280, 350));
        rightPanel.setOpaque(false);

        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.setBorder(BorderFactory.createTitledBorder(
                "Vehicle Management"));
        controls.setBackground(Color.WHITE);

        controls.add(new JLabel("Vehicle Registration Number"));
        controls.add(Box.createVerticalStrut(5));
        controls.add(vehicleField);
        controls.add(Box.createVerticalStrut(12));

        JButton parkButton = new JButton("Park Vehicle");
        JButton removeButton = new JButton("Remove Vehicle");
        JButton summaryButton = new JButton("View Summary");

        parkButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        removeButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        summaryButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        controls.add(parkButton);
        controls.add(Box.createVerticalStrut(8));
        controls.add(removeButton);
        controls.add(Box.createVerticalStrut(8));
        controls.add(summaryButton);

        rightPanel.add(controls, BorderLayout.NORTH);

        JPanel stats = new JPanel();
        stats.setLayout(new BoxLayout(stats, BoxLayout.Y_AXIS));
        stats.setBorder(BorderFactory.createTitledBorder(
                "Live Parking Status"));
        stats.setBackground(Color.WHITE);

        stats.add(availableLabel);
        stats.add(Box.createVerticalStrut(10));
        stats.add(occupiedLabel);
        stats.add(Box.createVerticalStrut(10));
        stats.add(revenueLabel);

        rightPanel.add(stats, BorderLayout.CENTER);
        centerPanel.add(rightPanel, BorderLayout.EAST);

        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder(
                "Activity Log"));
        logScroll.setPreferredSize(new Dimension(500, 150));
        mainPanel.add(logScroll, BorderLayout.SOUTH);

        parkButton.addActionListener(e -> parkVehicle());
        removeButton.addActionListener(e -> removeVehicle());
        summaryButton.addActionListener(e -> showSummary());

        addLog("Welcome! Smart Parking System is ready.");
        addLog("Hourly parking rate: Rs. " + RATE_PER_HOUR);
    }

    private void parkVehicle() {
        String number = vehicleField.getText()
                .trim().toUpperCase();

        if (number.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a vehicle number.");
            return;
        }

        for (Vehicle vehicle : parkedVehicles.values()) {
            if (vehicle.number.equals(number)) {
                JOptionPane.showMessageDialog(this,
                        "This vehicle is already parked.");
                return;
            }
        }

        if (selectedSlot == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a parking slot first!");
            return;
        }

        if (parkedVehicles.containsKey(selectedSlot)) {
            JOptionPane.showMessageDialog(this,
                    "This slot is already occupied. Please select another slot.");
            selectedSlot = -1;
            updateDisplay();
            return;
        }

        int freeSlot = selectedSlot;

        Vehicle vehicle = new Vehicle(number);
        parkedVehicles.put(freeSlot, vehicle);

        selectedSlot = -1;

        addLog("Vehicle " + number + " parked in Slot "
                + freeSlot + " at "
                + vehicle.entryTime.format(timeFormat));

        vehicleField.setText("");
        updateDisplay();

        JOptionPane.showMessageDialog(this,
                "Vehicle parked successfully!\nAssigned Slot: "
                        + freeSlot);
    }

    private void removeVehicle() {
        String number = vehicleField.getText()
                .trim().toUpperCase();

        if (number.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Enter the vehicle number to remove.");
            return;
        }

        int foundSlot = -1;
        Vehicle foundVehicle = null;

        for (Map.Entry<Integer, Vehicle> entry
                : parkedVehicles.entrySet()) {

            if (entry.getValue().number.equals(number)) {
                foundSlot = entry.getKey();
                foundVehicle = entry.getValue();
                break;
            }
        }

        if (foundVehicle == null) {
            JOptionPane.showMessageDialog(this,
                    "Vehicle not found in the parking lot.");
            return;
        }

        LocalDateTime exitTime = LocalDateTime.now();

        long minutes = Math.max(0,
                Duration.between(foundVehicle.entryTime,
                        exitTime).toMinutes());

        long hours = Math.max(1, (minutes + 59) / 60);
        double fee = hours * RATE_PER_HOUR;

        parkedVehicles.remove(foundSlot);
        totalRevenue += fee;

        addLog("Vehicle " + number + " left Slot "
                + foundSlot + ". Duration: " + minutes
                + " minutes. Fee: Rs. "
                + String.format("%.2f", fee));

        vehicleField.setText("");
        updateDisplay();

        JOptionPane.showMessageDialog(this,
                "Vehicle Removed Successfully!\n"
                + "Vehicle: " + number + "\n"
                + "Parking duration: " + minutes + " minutes\n"
                + "Billable hours: " + hours + "\n"
                + "Total fee: Rs. "
                + String.format("%.2f", fee));
    }

    private void showSlotDetails(int slot) {
        Vehicle vehicle = parkedVehicles.get(slot);

        if (vehicle == null) {
            JOptionPane.showMessageDialog(this,
                    "Slot " + slot + " is AVAILABLE.");
        } else {
            JOptionPane.showMessageDialog(this,
                    "Slot: " + slot + "\n"
                    + "Status: OCCUPIED\n"
                    + "Vehicle: " + vehicle.number + "\n"
                    + "Entry Time: "
                    + vehicle.entryTime.format(timeFormat));
        }
    }

    private void updateDisplay() {
        int occupied = parkedVehicles.size();
        int available = TOTAL_SLOTS - occupied;

        availableLabel.setText("Available Slots: " + available);
        occupiedLabel.setText("Occupied Slots: " + occupied);
        revenueLabel.setText("Revenue Collected: Rs. "
                + String.format("%.2f", totalRevenue));

        for (int slot : slotNumbers) {
            JButton button = slotButtons.get(slot);
            Vehicle vehicle = parkedVehicles.get(slot);

            if (vehicle == null) {
                if (selectedSlot == slot) {
                    button.setText("<html>Slot " + slot
                            + "<br>SELECTED</html>");
                    button.setBackground(new Color(45, 115, 220));
                    button.setForeground(Color.WHITE);
                    button.setToolTipText("Selected parking slot");
                } else {
                    button.setText("<html>Slot " + slot
                            + "<br>AVAILABLE</html>");
                    button.setBackground(new Color(46, 160, 90));
                    button.setForeground(Color.WHITE);
                    button.setToolTipText("Click to select this slot");
                }
            } else {
                button.setText("<html>Slot " + slot
                        + "<br>" + vehicle.number
                        + "<br>OCCUPIED</html>");
                button.setBackground(new Color(205, 65, 65));
                button.setForeground(Color.WHITE);
                button.setToolTipText("Click to view slot");
            }
        }
    }

    private void showSummary() {
        int occupied = parkedVehicles.size();
        int available = TOTAL_SLOTS - occupied;

        StringBuilder details = new StringBuilder();

        details.append("SMART PARKING SUMMARY\n\n");
        details.append("Total Slots: ").append(TOTAL_SLOTS);
        details.append("\nAvailable: ").append(available);
        details.append("\nOccupied: ").append(occupied);
        details.append("\nRevenue Collected: Rs. ")
                .append(String.format("%.2f", totalRevenue));

        details.append("\n\nCurrently Parked Vehicles:\n");

        if (parkedVehicles.isEmpty()) {
            details.append("No vehicles currently parked.");
        } else {
            for (Map.Entry<Integer, Vehicle> entry
                    : parkedVehicles.entrySet()) {
                details.append("\nSlot ").append(entry.getKey())
                        .append(" - ")
                        .append(entry.getValue().number);
            }
        }

        JOptionPane.showMessageDialog(this,
                details.toString(),
                "Parking Summary",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void addLog(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SmartParkingApp app = new SmartParkingApp();
            app.setVisible(true);
        });
    }
}
