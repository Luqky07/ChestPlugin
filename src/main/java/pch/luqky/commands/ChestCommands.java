package pch.luqky.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import pch.luqky.ChestPlugin;
import pch.luqky.models.SecureProfile;

public class ChestCommands implements CommandExecutor {
    private ChestPlugin plugin;
    public ChestCommands(ChestPlugin plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        
        if(args.length == 0){
            sender.sendMessage("Please specify a command");
            return true;
        }
        
        switch (args[0]) {
            case "help":
                help(sender);
                break;
            case "show":
                getSecureProfiles(sender);
                break;
            case "members":
                if(args.length == 1){
                    sender.sendMessage(ChatColor.RED + "Please specify a profile id");
                    break;
                }
                getSecureProfileMembers(sender, args[1]);
                break;
            case "create":
                if(args.length < 2){
                    sender.sendMessage(ChatColor.RED + "Please specify a name");
                    break;
                }
                createSecureProfile(sender, args[1]);
                break;
            case "delete":
                if(args.length < 2){
                    sender.sendMessage(ChatColor.RED + "Please specify a profile name");
                    break;
                }
                deleteSecureProfile(sender, args[1]);
                break;
            case "add":
                if(args.length < 3){
                    sender.sendMessage(ChatColor.RED + "Please specify a profile name and a member name");
                    break;
                }
                addSecureProfileMember(sender, args[1], args[2]);
                break;
            case "remove":
                if(args.length < 3){
                    sender.sendMessage(ChatColor.RED + "Please specify a profile name and a member name");
                    break;
                }
                deleteSecureProfileMember(sender, args[1], args[2]);
                break;
            case "default":
                if(args.length < 2){
                    sender.sendMessage(ChatColor.RED + "Please specify a profile name");
                    break;
                }
                setDefaultProfile(sender, args[1]);
                break;
            case "refresh":
                refreshSecureProfiles();
                break;
            default:
                break;
        }

        return true;
    }

    private void help(CommandSender sender){
        sender.sendMessage(ChatColor.GREEN + "ChestPlugin commands:");
        sender.sendMessage(ChatColor.GREEN + "/chest help - Show this help");
        sender.sendMessage(ChatColor.GREEN + "/chest show - Show your secure profiles");
        sender.sendMessage(ChatColor.GREEN + "/chest members <profileName> - Show members of a secure profile");
        sender.sendMessage(ChatColor.GREEN + "/chest create <name> - Add a new secure profile");
        sender.sendMessage(ChatColor.GREEN + "/chest delete <profileName> - Delete a secure profile");
        sender.sendMessage(ChatColor.GREEN + "/chest add <profileName> <memberName> - Add a member to a secure profile");
        sender.sendMessage(ChatColor.GREEN + "/chest remove <profileName> <memberName> - Remove a member from a secure profile");
        sender.sendMessage(ChatColor.GREEN + "/chest default <profileName> - Set a secure profile as default");
    }

    private void getSecureProfiles(CommandSender sender){

        if(!(sender instanceof org.bukkit.entity.Player)){
            sender.sendMessage(ChatColor.RED + "This command can only be used by players");
            return;
        }

        Player player = (Player) sender;
        List<SecureProfile> profiles = plugin.getProfiles().stream()
                    .filter(p -> p.getOwnerName().equalsIgnoreCase(player.getName()))
                    .collect(Collectors.toList());

        List<String> profilesInfo = new ArrayList<String>();

        for(int i = 0; i < profiles.size(); i++){
            profilesInfo.add(( "- " + profiles.get(i).getName() + (profiles.get(i).getIsDefault() ? " (default)" : "")));
        }

        if(profilesInfo.size() == 0){
            player.sendMessage(ChatColor.RED + "You don't have any secure profile");
            return;
        }

        player.sendMessage(ChatColor.GREEN + "Your secure profiles:");
        for(String info : profilesInfo){
            player.sendMessage(ChatColor.GREEN + info);
        }
    }

    private void getSecureProfileMembers(CommandSender sender, String profileName){
        if(!(sender instanceof org.bukkit.entity.Player)){
            sender.sendMessage(ChatColor.RED + "This command can only be used by players");
            return;
        }

        Player player = (Player) sender;
        Optional<SecureProfile> profile = plugin.getProfiles().stream()
                    .filter(p -> p.getOwnerName().equalsIgnoreCase(player.getName())
                        && p.getName().equalsIgnoreCase(profileName)
                    )
                    .findFirst();

        if(!profile.isPresent()){
            sender.sendMessage(ChatColor.RED + "This profile does not exist or you don't own it");
            return;
        }

        if(profile.get().getMembers().isEmpty()){
            sender.sendMessage(ChatColor.GREEN + "This profile has no members");
            return;
        }
        
        sender.sendMessage(ChatColor.GREEN + "Members of " + profileName + ":");
        for(String member : profile.get().getMembers()){
            sender.sendMessage(ChatColor.GREEN + "- " + member);
        }
    }
    
    private void createSecureProfile(CommandSender sender, String name){
        if(!(sender instanceof org.bukkit.entity.Player)){
            sender.sendMessage(ChatColor.RED + "This command can only be used by players");
            return;
        }

        if(name == null || name.isEmpty()){
            sender.sendMessage(ChatColor.RED + "Please specify a name for the secure profile");
            return;
        }

        Player player = (Player) sender;

        List<SecureProfile> profile = plugin.getProfiles().stream()
                    .filter(p -> p.getOwnerName().equalsIgnoreCase(player.getName()))
                    .collect(Collectors.toList());

        Optional<SecureProfile> profileExists = profile.stream()
                    .filter(p -> p.getName().equalsIgnoreCase(name))
                    .findFirst();

        Optional<SecureProfile> defaultProfile = profile.stream()
                    .filter(p -> p.getIsDefault())
                    .findFirst();
        
        if(profileExists.isPresent()){
            sender.sendMessage(ChatColor.RED + "This profile already exists for this owner");
            return;
        }

        boolean success = plugin.getQuerys().addSecureProfile(name, player.getName(), !defaultProfile.isPresent());

        if(!success){
            sender.sendMessage(ChatColor.RED + "Creating profile failed");
            return;
        }

        plugin.reloadProfiles();

        sender.sendMessage(ChatColor.GREEN + "Secure profile created");
    }
    
    private void deleteSecureProfile(CommandSender sender, String profileName){
        if(!(sender instanceof org.bukkit.entity.Player)){
            sender.sendMessage(ChatColor.RED + "This command can only be used by players");
            return;
        }

        Player player = (Player) sender;
        Optional<SecureProfile> profile = plugin.getProfiles().stream()
                    .filter(p -> p.getOwnerName().equalsIgnoreCase(player.getName())
                        && p.getName().equalsIgnoreCase(profileName)
                    )
                    .findFirst();

        if(!profile.isPresent()){
            sender.sendMessage(ChatColor.RED + "This profile does not exist or you don't own it");
            return;
        }

        boolean success = plugin.getQuerys().removeSecureProfile(player.getName(), profile.get().getProfileId());
        if(!success){
            sender.sendMessage(ChatColor.RED + "Removing profile failed");
            return;
        }
        plugin.reloadProfiles();

        sender.sendMessage(ChatColor.GREEN + "Secure profile removed");
    }
    
    private void addSecureProfileMember(CommandSender sender, String profileName, String memberName){
        if(!(sender instanceof org.bukkit.entity.Player)){
            sender.sendMessage(ChatColor.RED + "This command can only be used by players");
            return;
        }

        Player player = (Player) sender;

        Optional<SecureProfile> profile = plugin.getProfiles().stream()
                    .filter(p -> p.getOwnerName().equalsIgnoreCase(player.getName())
                        && p.getName().equalsIgnoreCase(profileName)
                    )
                    .findFirst();

        if(!profile.isPresent()){
            sender.sendMessage(ChatColor.RED + "This profile does not exist or you don't own it");
            return;
        }

        if(profile.get().getMembers().contains(memberName)){
            sender.sendMessage(ChatColor.RED + "This member is already in this profile");
            return;
        }

        boolean success = plugin.getQuerys().addSecureProfileMember(profile.get().getProfileId(), memberName);
        if(!success){
            sender.sendMessage(ChatColor.RED + "Adding member failed");
            return;
        }
        plugin.reloadProfiles();

        sender.sendMessage(ChatColor.GREEN + "Member added");
    }

    private void deleteSecureProfileMember(CommandSender sender, String profileName, String memberName){
        if(!(sender instanceof org.bukkit.entity.Player)){
            sender.sendMessage(ChatColor.RED + "This command can only be used by players");
            return;
        }

        Player player = (Player) sender;

        Optional<SecureProfile> profile = plugin.getProfiles().stream()
                    .filter(p -> p.getOwnerName().equalsIgnoreCase(player.getName())
                        && p.getName().equalsIgnoreCase(profileName)
                    )
                    .findFirst();

        if(!profile.isPresent()){
            sender.sendMessage(ChatColor.RED + "This profile does not exist or you don't own it");
            return;
        }

        if(!profile.get().getMembers().contains(memberName)){
            sender.sendMessage(ChatColor.RED + "This member is not in this profile");
            return;
        }

        boolean success = plugin.getQuerys().removeSecureProfileMember(profile.get().getProfileId(), memberName);
        if(!success){
            sender.sendMessage(ChatColor.RED + "Removing member failed");
            return;
        }
        
        plugin.reloadProfiles();

        sender.sendMessage(ChatColor.GREEN + "Member removed");
    }

    private void setDefaultProfile(CommandSender sender, String profileName){
        if(!(sender instanceof org.bukkit.entity.Player)){
            sender.sendMessage(ChatColor.RED + "This command can only be used by players");
            return;
        }

        Player player = (Player) sender;

        Optional<SecureProfile> profile = plugin.getProfiles().stream()
                    .filter(p -> p.getOwnerName().equalsIgnoreCase(player.getName())
                        && p.getName().equalsIgnoreCase(profileName)
                    )
                    .findFirst();

        if(!profile.isPresent()){
            sender.sendMessage(ChatColor.RED + "This profile does not exist or you don't own it");
            return;
        }

        boolean success = plugin.getQuerys().setDefaultProfile(player.getName(), profile.get().getProfileId());
        if(!success){
            sender.sendMessage(ChatColor.RED + "Setting default profile failed");
            return;
        }
        plugin.reloadProfiles();

        sender.sendMessage(ChatColor.GREEN + profileName + " set as default profile");
    }

    private void refreshSecureProfiles(){
        plugin.reloadProfiles();
    }
}
