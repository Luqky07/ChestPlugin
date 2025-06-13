package pch.luqky.configuration;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import pch.luqky.ChestPlugin;

import java.io.File;
import java.io.IOException;

public class CustomConfig {
    private ChestPlugin plugin;
    private String fileName;
    private FileConfiguration fileConfiguration = null;
    private File file = null;
    private String folderName;
    public CustomConfig(String fileName, String folderName, ChestPlugin plugin) {
        this.fileName = fileName;
        this.folderName = folderName;
        this.plugin = plugin;
    }

    //Register the config file
    public void registerConfig(){
        //Get the file or create it if it doesn't exist
        if(folderName != null){
            file = new File(plugin.getDataFolder() + File.separator + folderName, fileName);
        }else {
            file = new File(plugin.getDataFolder(), fileName);
        }

        if(!file.exists()){
            if(folderName != null){
                plugin.saveResource(folderName + File.separator + fileName, false);
            }else {
                plugin.saveResource(fileName, false);
            }
        }

        // Load the yaml file
        fileConfiguration = new YamlConfiguration();
        try {
            fileConfiguration.load(file);
        }catch (IOException e){
            e.printStackTrace();
        }catch (InvalidConfigurationException e){
            e.printStackTrace();
        }
    }

    public FileConfiguration getConfig(){
        if(fileConfiguration == null){
            registerConfig();
        }
        return fileConfiguration;
    }

    public void reloadConfig(){
        if(fileConfiguration == null){
            if(folderName != null){
                file = new File(plugin.getDataFolder() + File.separator + folderName, fileName);
            }else {
                file = new File(plugin.getDataFolder(), fileName);
            }
        }

        fileConfiguration = YamlConfiguration.loadConfiguration(file);

        if(file != null){
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(file);
            fileConfiguration.setDefaults(defConfig);
        }

    }
}
