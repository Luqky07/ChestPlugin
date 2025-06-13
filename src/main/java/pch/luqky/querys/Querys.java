package pch.luqky.querys;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;

import net.md_5.bungee.api.ChatColor;
import pch.luqky.models.SecureProfile;

public class Querys {
    //Database connection
    private Connection connection;
    
    public Querys(Connection connection) {
        this.connection = connection;
    }

    //Create tables if they don't exist
    public boolean validateTables(){
        String secure_profile = """ 
        CREATE TABLE IF NOT EXISTS SECURE_PROFILES(
	        PROFILE_ID INT PRIMARY KEY AUTO_INCREMENT,
	        NAME VARCHAR(20),
	        OWNER_NAME VARCHAR(20),
            IS_DEFAULT BIT
        )
        """;

        String secure_profile_member = """
            CREATE TABLE IF NOT EXISTS SECURE_PROFILE_MEMBER(
            	MEMBER_ID INT PRIMARY KEY AUTO_INCREMENT,
            	PROFILE_ID INT,
            	FOREIGN KEY (PROFILE_ID) REFERENCES SECURE_PROFILES(PROFILE_ID),
            	MEMBER_NAME VARCHAR(50)
            )
        """;

        try(Statement stmt = this.connection.createStatement()){
            stmt.executeUpdate(secure_profile);
            stmt.executeUpdate(secure_profile_member);
        }catch(SQLException e){
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[ChestPlugin] Failed to create tables in mysql\n" + e.getMessage());
            return false;
        }
        return true;
    }

    public List<SecureProfile> getSecureProfiles(){
        //Querys
        String profilesQuery = "SELECT * FROM SECURE_PROFILES";
        String membersQuery = "SELECT MEMBER_NAME FROM SECURE_PROFILE_MEMBER WHERE PROFILE_ID = ?";
        
        //Result list
        List<SecureProfile> profiles = new ArrayList<SecureProfile>();
        
        try(Statement stmt = this.connection.createStatement()){
            //Get all profiles
            ResultSet profilesRes = stmt.executeQuery(profilesQuery);
            //Prepared statement to get all members of a profile
            try(PreparedStatement mStatement = this.connection.prepareStatement(membersQuery)){
                //Iterate over all profiles
                while(profilesRes.next()){
                    //Get profile base data
                    int profileId = profilesRes.getInt("PROFILE_ID");
                    String name = profilesRes.getString("NAME");
                    String ownerName = profilesRes.getString("OWNER_NAME");
                    Boolean isDefault = profilesRes.getBoolean("IS_DEFAULT");

                    List<String> members = new ArrayList<String>();
                    mStatement.setInt(1, profileId);

                    //Get all members of the profile
                    ResultSet membersRes = mStatement.executeQuery();

                    //Iterate over all members
                    while(membersRes.next()){
                        members.add(membersRes.getString("MEMBER_NAME"));
                    }

                    //Add profile to result list
                    profiles.add(new SecureProfile(profileId, name, ownerName, isDefault, members));
                }
            }
        }
        catch(SQLException e){
            //If fails, print error message
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "[CHESTPULIGN] Failed to load profiles\n" + e.getMessage()));
        }
        return profiles;
    }

    public boolean addSecureProfile(String name, String ownerName, boolean isDefault){
        String addProfileQuery = "INSERT INTO SECURE_PROFILES (NAME, OWNER_NAME, IS_DEFAULT) VALUES (?, ?, ?)";
        try(PreparedStatement stmt = this.connection.prepareStatement(addProfileQuery)){
            stmt.setString(1, name);
            stmt.setString(2, ownerName);
            stmt.setBoolean(3, isDefault);
            stmt.executeUpdate();
        }catch(SQLException e){
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "[ChestPlugin] Failed to add new profile\n" + e.getMessage()));
            return false;
        }

        return true;
    }

    public boolean addSecureProfileMember(int profileId, String memberName){
        String addProfileMemberQuery = "INSERT INTO SECURE_PROFILE_MEMBER (PROFILE_ID, MEMBER_NAME) VALUES (?, ?)";
        try(PreparedStatement stmt = this.connection.prepareStatement(addProfileMemberQuery)){
            stmt.setInt(1, profileId);
            stmt.setString(2, memberName);
            stmt.executeUpdate();
        }catch(SQLException e){
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "[ChestPlugin] Failed to add new profile member\n" + e.getMessage()));
            return false;
        }

        return true;
    }

    public boolean removeSecureProfile(String ownerName, int profileId){
        String removeProfileQuery = "DELETE FROM SECURE_PROFILES WHERE OWNER_NAME = ? AND PROFILE_ID = ?";
        try(PreparedStatement stmt = this.connection.prepareStatement(removeProfileQuery)){
            stmt.setString(1, ownerName);
            stmt.setInt(2, profileId);
            stmt.executeUpdate();
        }catch(SQLException e){
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[ChestPlugin] Failed to remove profile\n" + e.getMessage());
            return false;
        }
        return true;
    }

    public boolean removeSecureProfileMember(int profileId, String memberName){
        String removeProfileMemberQuery = "DELETE FROM SECURE_PROFILE_MEMBER WHERE PROFILE_ID = ? AND MEMBER_NAME = ?";
        try(PreparedStatement stmt = this.connection.prepareStatement(removeProfileMemberQuery)){
            stmt.setInt(1, profileId);
            stmt.setString(2, memberName);
            stmt.executeUpdate();
        }catch(SQLException e){
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[ChestPlugin] Failed to remove profile member\n" + e.getMessage());
            return false;
        }
        return true;
    }

    public boolean setDefaultProfile(String ownerName,int profileId){
        String setDefaultProfileQuery = "UPDATE SECURE_PROFILES SET IS_DEFAULT = 1 WHERE OWNER_NAME = ? AND PROFILE_ID = ?";
        String setUndefaultProfileQuery = "UPDATE SECURE_PROFILES SET IS_DEFAULT = 0 WHERE OWNER_NAME = ?";
        
        try(PreparedStatement stmt = this.connection.prepareStatement(setUndefaultProfileQuery)){
            stmt.setString(1, ownerName);
            stmt.executeUpdate();
        }catch(SQLException e){
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[ChestPlugin] Failed to eliminate default profile\n" + e.getMessage());
            return false;
        }
        
        try(PreparedStatement stmt = this.connection.prepareStatement(setDefaultProfileQuery)){
            stmt.setString(1, ownerName);
            stmt.setInt(2, profileId);
            stmt.executeUpdate();
        }catch(SQLException e){
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[ChestPlugin] Failed to set default profile\n" + e.getMessage());
            return false;
        }
        return true;
    }
}
