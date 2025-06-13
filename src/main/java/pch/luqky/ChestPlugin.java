package pch.luqky;

import java.util.List;
import java.util.Objects;

import org.bukkit.plugin.java.JavaPlugin;

import pch.luqky.commands.ChestCommandTabCompleter;
import pch.luqky.commands.ChestCommands;
import pch.luqky.configuration.DatabaseConfig;
import pch.luqky.connection.DatabaseConnection;
import pch.luqky.listners.ChestListener;
import pch.luqky.models.SecureProfile;
import pch.luqky.querys.Querys;

public class ChestPlugin extends JavaPlugin {
    private Querys querys;
    private List<SecureProfile> profiles;

    //Execution when server starts
    public void onEnable() {
        //Get database config info
        DatabaseConfig dbConfig = new DatabaseConfig(this);
        dbConfig.loadConfig();

        //Enable database connection
        DatabaseConnection dbConnection = new DatabaseConnection(dbConfig.getDatabaseInfo());

        //Create class to manage database queries
        querys = new Querys(dbConnection.getConnection());

        //Validate basic tables
        querys.validateTables();

        //Get initial secure profiles
        profiles = querys.getSecureProfiles();

        //Register plugin actions
        registerCommands();
        registerEvents();
        getCommand("chest").setTabCompleter(new ChestCommandTabCompleter(this));
    }

    @Override
    public void onDisable() {
    }

    public void registerCommands() {
        //Register command to interact with chests
        Objects.requireNonNull(this.getCommand("chest")).setExecutor(new ChestCommands(this));
    }

    public void registerEvents() {
        //Register event handlers
        getServer().getPluginManager().registerEvents(new ChestListener(this), this);
    }

    //Get query manager
    public Querys getQuerys(){
        return querys;
    }

    //Get secure profiles
    public List<SecureProfile> getProfiles(){
        return profiles;
    }

    //Reload secure profiles
    public void reloadProfiles(){
        profiles = querys.getSecureProfiles();
    }
}
