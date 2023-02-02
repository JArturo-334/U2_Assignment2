package u1_assignment2;

import java.sql.*;
import java.util.concurrent.locks.*;
import javax.swing.*;

public class U1_Assignment2 {

    private ReentrantLock lock = new ReentrantLock();
    private Connection conn;

    public U1_Assignment2() {
        try {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (Exception e) {
                e.printStackTrace();
            }
            conn = DriverManager.getConnection("localhost:3306/server", "root", "");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateRecord(int id, String newValue) {
        lock.lock();
        try {
            PreparedStatement statement = conn.prepareStatement("UPDATE records SET tel = ? WHERE id = ?");
            statement.setString(1, newValue);
            statement.setInt(2, id);
            statement.executeUpdate();
            System.out.println("Record updated successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public int consultRecord(int id) {
        synchronized (this) {
            try {
                PreparedStatement statement = conn.prepareStatement("SELECT name FROM records WHERE id = ?");
                statement.setInt(1, id);
                ResultSet result = statement.executeQuery();
                if (result.next()) {
                    System.out.println("Record consulted successfully.");
                    return result.getInt("value");
                } else {
                    return -1;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return -1;
            }
        }
    }

    public static void main(String[] args) {
        U1_Assignment2 server = new U1_Assignment2();
        String value = "";

        Thread updateThread = new Thread(() -> {
            JOptionPane.showInputDialog("New number", value);
            server.updateRecord(1, value);
        });
        updateThread.start();

        Thread consultThread = new Thread(() -> {
            int record = server.consultRecord(1);
            System.out.println("Consulted record value: " + record);
        });
        consultThread.start();
    }

}
