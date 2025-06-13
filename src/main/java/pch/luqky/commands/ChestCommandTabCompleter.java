package pch.luqky.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import pch.luqky.ChestPlugin;
import pch.luqky.models.SecureProfile;

public class ChestCommandTabCompleter implements org.bukkit.command.TabCompleter {
    private static final Set<String> COMMANDS_USE_PROFILE = Set.of("members", "delete", "add", "remove", "default");

    private ChestPlugin plugin;

    public ChestCommandTabCompleter(ChestPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("chest")) {
            List<String> completions = new ArrayList<>();

            //If args length is 1, show all commands
            if (args.length == 1) {
                completions.add("help");
                completions.add("show");
                completions.add("members");
                completions.add("create");
                completions.add("delete");
                completions.add("add");
                completions.add("remove");
                completions.add("default");
            }

            //If args length is 2, show all user secure profiles
            if (args.length == 2 && COMMANDS_USE_PROFILE.contains(args[0])) {
                for(SecureProfile p : plugin.getProfiles().stream()
                    .filter(p -> p.getOwnerName().equalsIgnoreCase(sender.getName()))
                    .collect(Collectors.toList())
                ){
                    completions.add(p.getName());
                }
            }

            //If args length is 3, show all members of a profile
            if (args.length == 3) {
                switch (args[0]) {
                    case "remove":
                        for(SecureProfile p : plugin.getProfiles().stream()
                            .filter(p -> p.getOwnerName().equalsIgnoreCase(sender.getName())
                                && p.getName().equalsIgnoreCase(args[1])
                            )
                            .collect(Collectors.toList())
                        ){
                            for(String member : p.getMembers()){
                                    completions.add(member);
                                }
                        }
                        break;
                }
            }

            return completions.stream()
                        .collect(Collectors.toList());
        }
        return null;
    }
}
