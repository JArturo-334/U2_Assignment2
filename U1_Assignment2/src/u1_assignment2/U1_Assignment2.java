package u1_assignment2;

import java.sql.*;
import java.util.Scanner;
import java.util.concurrent.locks.*;
import java.util.logging.*;
import javax.swing.*;

public class U1_Assignment2 {
    String bd = "server";
    String url = "jdbc:mysql://localhost:3306/server";
    String username = "root";
    String password = "";
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
                url = "jdbc:mysql://localhost:3306/server";
                cx=DriverManager.getConnection(url, username, password);
            
                stmt = cx.createStatement();
                stmt.executeUpdate("DROP TABLE IF EXISTS prueba"); //borrar tabla si exixte
                String sql2 = "CREATE TABLE prueba " +
                    "(id INT PRIMARY KEY NOT NULL AUTO_INCREMENT, "+
                    "name VARCHAR(255) NOT NULL, " +
                    "age INT)";
                stmt.executeUpdate(sql2); 
           
                // Insert data into the database
                stmt.executeUpdate("INSERT INTO prueba (name, age) VALUES ('John', 30)");
                
                String[] options = {"Consult", "Update"};
                int op = JOptionPane.showOptionDialog(null, "What you want to do:", "Select one!", 0, 3, null, options, options[0]);
                if (op == 0){
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
                String selectSql = "SELECT name, age, id FROM prueba";
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
            try {
                lock.writeLock().lock();
                cx=DriverManager.getConnection(url, username, password);
                stmt = cx.createStatement();
                Scanner input = new Scanner(System.in);
                
                //  UPDATE DE DATOS
                System.out.print("Update name: ");
                String firstValue = input.nextLine();
                System.out.print("Update age: ");
                int secondValue = input.nextInt();
                System.out.print("Update id: ");
                int thirdValue = input.nextInt();
                stmt2 = cx.prepareStatement("UPDATE prueba SET name = ?, age = ?, id = ?");
                stmt2.setString(1, firstValue);
                stmt2.setInt(2, secondValue);
                stmt2.setInt(3, thirdValue);

                // EJECUTAR STATEMENT
                int rowsAffected = stmt2.executeUpdate();
                System.out.println("Number of rows updated: " + rowsAffected);
                System.out.println("Data inserted successfully");
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