package pch.luqky.configuration;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import pch.luqky.ChestPlugin;
import pch.luqky.models.DatabaseInfo;

public class DatabaseConfig {
    private CustomConfig customConfig;
    protected ChestPlugin plugin;
    private DatabaseInfo connectionInfo;

    public DatabaseConfig(ChestPlugin plugin) {
        this.plugin = plugin;
        //Initialize the custom config for the database
        this.customConfig = new CustomConfig("config_chest_database.yml", null, plugin);

        customConfig.registerConfig();
        loadConfig();
    }

    //Load the config
    public void loadConfig() {
        FileConfiguration config = customConfig.getConfig();
        ConfigurationSection section = config.getConfigurationSection("database");

        connectionInfo = new DatabaseInfo(
            section.getString("host"),
            section.getInt("port"),
            section.getString("database"),
            section.getString("user"),
            section.getString("password")
        );
    }

    //Reload the config
    public void reloadConfig() {
        customConfig.reloadConfig();
        loadConfig();
    }

    //Get the database info
    public DatabaseInfo getDatabaseInfo() {
        return connectionInfo;
    }
}