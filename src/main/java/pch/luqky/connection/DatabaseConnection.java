package pch.luqky.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.bukkit.Bukkit;

import net.md_5.bungee.api.ChatColor;
import pch.luqky.models.DatabaseInfo;

public class DatabaseConnection {
    private Connection connection;
    private DatabaseInfo databaseInfo;
    public DatabaseConnection(DatabaseInfo databaseInfo) {
        this.databaseInfo = databaseInfo;
        connect();
    }

    public Connection getConnection() {
        try{
            if (connection == null || connection.isClosed() || !connection.isValid(2)){
                connect();
            }
            return connection;
        }catch(SQLException e){
            return null;
        }
    }

    private void connect() {
        try{
            //Try to connect to database
            synchronized(this){
                if(connection != null && !connection.isClosed()){
                    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[ChestPlugin] Database connection failed");
                    return;
                }

                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection(this.databaseInfo.getConnectionString(), this.databaseInfo.getUser(), this.databaseInfo.getPassword());
                
                Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[ChestPlugin] Database connection established");
            }
        } catch(SQLException e){

        } catch (ClassNotFoundException e) {
            
        }
    }
}
