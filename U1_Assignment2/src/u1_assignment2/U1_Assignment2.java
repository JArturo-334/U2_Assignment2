package u1_assignment2;

import java.sql.*;
import java.util.concurrent.locks.*;
import java.util.logging.*;
import javax.swing.*;

public class U1_Assignment2 {
    String bd = "server";
    String url = "jdbc:mysql://25.71.84.174/server";
    String username = "artur";
    String password = "123";
    String driver = "com.mysql.cj.jdbc.Driver";
    Connection cx = null;
    Statement stmt = null;
    boolean flag = false;
    ResultSet rs = null;
    static ReentrantReadWriteLock lock = new ReentrantReadWriteLock(); 
  
    public U1_Assignment2() {
        
    }
    
    public Connection conectar(){
        try {
            Class.forName(driver);
            cx=DriverManager.getConnection(url, username, password);
            System.out.println("Connection successful");
            stmt = cx.createStatement();
            String checkSql = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = 'server'";
            stmt = cx.createStatement();
            rs = stmt.executeQuery(checkSql);
            stmt = cx.createStatement();
            flag = true;
            if (rs.next()) { //si existe la base de datos
                Statement stmt = cx.createStatement();

                // borrar la base de datos si existe
                stmt.executeUpdate("DROP DATABASE IF EXISTS server");
                String sql = "CREATE DATABASE server";
                stmt.executeUpdate(sql);
                url = "jdbc:mysql://25.71.84.174/server";
                cx=DriverManager.getConnection(url, username, password);
            
                stmt = cx.createStatement();
                stmt.executeUpdate("DROP TABLE IF EXISTS prueba"); //borrar tabla si exixte
                String sql2 = "CREATE TABLE prueba " +
                    "(id INT PRIMARY KEY NOT NULL AUTO_INCREMENT, "+
                    "name VARCHAR(255) NOT NULL, " +
                    "age INT)";
                stmt.executeUpdate(sql2); 
           
                // Insert data into the database
                stmt.executeUpdate("INSERT INTO prueba (name, age) VALUES ('Juan', 20)");
                stmt.executeUpdate("INSERT INTO prueba (name, age) VALUES ('Carlos', 20)");
                stmt.executeUpdate("INSERT INTO prueba (name, age) VALUES ('Jacky', 20)");
                stmt.executeUpdate("INSERT INTO prueba (name, age) VALUES ('Cesar', 20)");
                stmt.executeUpdate("INSERT INTO prueba (name, age) VALUES ('Alonso', 20)");
                stmt.executeUpdate("INSERT INTO prueba (name, age) VALUES ('Arturo', 20)");
                stmt.executeUpdate("INSERT INTO prueba (name, age) VALUES ('Paul', 20)");
                
                String[] options = {"Consult", "Update"};
                int op = JOptionPane.showOptionDialog(null, "What you want to do:", "Select one!", 0, 3, null, options, options[0]);
                if (op == 0){ //si selecciona consult
                    Thread Reader = new Thread(new Reader());
                    Reader.start();
                }
                if (op == 1) { //si selecciona update
                    Thread Writter = new Thread(new Writter());
                    Writter.start();
                }
            }
        } catch (ClassNotFoundException |SQLException ex) {
            System.out.println("Connection Failed"+bd);
            Logger.getLogger(U1_Assignment2.class.getName()).log(Level.SEVERE, null, ex);
        }
        return cx;
    }
    
    class Reader implements Runnable{
        public void run(){
            try{
                lock.writeLock().lock();
                cx=DriverManager.getConnection(url, username, password);
                stmt = cx.createStatement();
                String selectSql = "SELECT * FROM prueba";
                stmt = cx.prepareStatement(selectSql);
                ResultSet rs = stmt.executeQuery(selectSql);
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    int age = rs.getInt("age");
                    System.out.println("id: " + id + " name: " + name + " age: " + age);
                }
                rs.close();
            } catch (SQLException ex) {
                System.out.println("Connection Failed"+bd);
                Logger.getLogger(U1_Assignment2.class.getName()).log(Level.SEVERE, null, ex);
            } finally{
                lock.writeLock().unlock();
            }
        }
    }
    
    class Writter implements Runnable{
        public void run(){
            PreparedStatement stmt2 = null;
            JTextField id = new JTextField();
            JTextField name = new JTextField();
            JTextField age = new JTextField();
            Object[] message = {
                "ID to update:", id,
                "New name:", name,
                "New age:", age
            };
            try {
                lock.writeLock().lock();
                cx=DriverManager.getConnection(url, username, password);
                stmt = cx.createStatement();
                int option = JOptionPane.showConfirmDialog(null, message, "Update", JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    // Update
                    String firstValue = name.getText();
                    int secondValue = Integer.parseInt(age.getText());
                    int thirdValue = Integer.parseInt(id.getText());
                    stmt2 = cx.prepareStatement("UPDATE prueba SET name = ?, age = ? WHERE id = ?");
                    stmt2.setString(1, firstValue);
                    stmt2.setInt(2, secondValue);
                    stmt2.setInt(3, thirdValue);

                    // Execute statement
                    int rowsAffected = stmt2.executeUpdate();
                    System.out.println("Number of rows updated: " + rowsAffected);
                    System.out.println("Data inserted successfully");
                    
                    // Create consult query
                    Thread Reader = new Thread(new Reader());
                    Reader.start();
                } else {
                    System.out.println("Proccess canceled");
                }
            } catch (SQLException ex) {
                Logger.getLogger(U1_Assignment2.class.getName()).log(Level.SEVERE, null, ex);
            } finally{
                lock.writeLock().unlock();
            }
        }
    }
    
    public void desconectar(){
        try {   
            cx.close();
        } catch (SQLException ex) {
            Logger.getLogger(U1_Assignment2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String[] args){
        U1_Assignment2 dr= new U1_Assignment2();
        dr.conectar();
    }
}