package pch.luqky.listners;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.TileState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.ChatColor;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;

import pch.luqky.ChestPlugin;
import pch.luqky.models.ChestInvetoryHolder;
import pch.luqky.models.SecureProfile;

public class ChestListener implements Listener {

    //List of unprotected blocks
    private static final Set<Material> UNPROTECTED_BLOCKS = Set.of(
        Material.ENDER_CHEST
    );

    private ChestPlugin plugin;
    public ChestListener(ChestPlugin plugin) {
        this.plugin = plugin;
    }

    //Event to manage chest placement
    @EventHandler
    public void onChestPlace(BlockPlaceEvent event){
        Block block = event.getBlockPlaced();
        //Run task later for evaluate double chests placement
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            BlockState state = block.getState();
            
            if(!(state instanceof TileState) || UNPROTECTED_BLOCKS.contains(block.getType()))
                return;

            //Namespace key to manage profile in PersistentDataContainer
            NamespacedKey key = new NamespacedKey(plugin, "profile");

            Player player = event.getPlayer();

            //Get default profile
            Optional<SecureProfile> profile = plugin.getProfiles().stream()
                   .filter(p -> p.getOwnerName().equalsIgnoreCase(player.getName()) && p.getIsDefault())
                   .findFirst();
            
            if(!profile.isPresent())
                return;

            //If chest is a double chest
            if(state instanceof Chest chest){
                InventoryHolder holder = chest.getInventory().getHolder();
                if(holder instanceof DoubleChest doubleChest){
                    //Get left and right chests profileId if exists
                    Chest left = (Chest) doubleChest.getLeftSide();
                    Chest right = (Chest) doubleChest.getRightSide();

                    TileState leftState = (TileState) left.getBlock().getState();
                    TileState rightState = (TileState) right.getBlock().getState();

                    PersistentDataContainer leftContainer = leftState.getPersistentDataContainer();
                    PersistentDataContainer rightContainer = rightState.getPersistentDataContainer();

                    Integer leftProfile = leftContainer.get(key, PersistentDataType.INTEGER);
                    Integer rightProfile = rightContainer.get(key, PersistentDataType.INTEGER);

                    if (leftProfile != null) {
                        //If have profile on left side, set profileId on right side
                        rightContainer.set(key, PersistentDataType.INTEGER, leftProfile);
                        rightState.update();
                        player.sendMessage(ChatColor.GREEN + "Profile copied from left side.");
                    } else if (rightProfile != null) {
                        //If have profile on right side, set profileId on left side
                        leftContainer.set(key, PersistentDataType.INTEGER, rightProfile);
                        leftState.update();
                        player.sendMessage(ChatColor.GREEN + "Profile copied from right side.");
                    } else {
                        //If don't have profile, set default profile on both sides
                        leftContainer.set(key, PersistentDataType.INTEGER, profile.get().getProfileId());
                        leftState.update();
                    
                        rightContainer.set(key, PersistentDataType.INTEGER, profile.get().getProfileId());
                        rightState.update();
                    
                        player.sendMessage(ChatColor.GREEN + profile.get().getName() + " profile set to both sides.");
                    }
                    return;
                }
            }

            //If chest is a single chest or tile set player default profile
            if(state instanceof TileState tileState){
                PersistentDataContainer container = tileState.getPersistentDataContainer();
                container.set(key, PersistentDataType.INTEGER, profile.get().getProfileId());
                tileState.update();

                player.sendMessage(ChatColor.GREEN + profile.get().getName() + " profile set");
            }
        }, 1L);
    }

    //Event to manage chest actions
    @EventHandler
    public void onChestAction(PlayerInteractEvent event){
        Action action = event.getAction();

        //If action is not right or left click, return
        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.LEFT_CLICK_BLOCK) return;
        
        //Get clicked block and evaluate if isn't air
        Block block = event.getClickedBlock();
        if (block == null) return;

        BlockState state = block.getState();
        if(!(state instanceof TileState) || UNPROTECTED_BLOCKS.contains(block.getType()))
            return;

        Player player = event.getPlayer();

        //If state is TileState, get profileId from PersistentDataContainer
        if (state instanceof TileState tileState) {
            PersistentDataContainer container = tileState.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(plugin, "profile");
            Integer chestProfile = container.get(key, PersistentDataType.INTEGER);

            //If profileId is null, set default profile
            if(chestProfile == null) chestProfile = 0;

            //Evaluate if player has access to this chest
            SecureProfile profile = hasAccess(player, chestProfile);

            //If player doesn't have access to this chest, cancel event
            if(profile == null){
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You don't have access to this chest");
                return;
            }

            //If player is not the owner and there isn't a profileId, return
            if (!profile.getOwnerName().equalsIgnoreCase(player.getName()) && profile.getProfileId() != 0) return;
            //If player isn't sneaking return
            if (!player.isSneaking()) return;
            //If player is holding an item, return
            if (player.getInventory().getItemInMainHand().getType() != Material.AIR) return;

            //Load al player secure profiles
            List<SecureProfile> profiles = plugin.getProfiles().stream()
                .filter(p -> p.getOwnerName().equalsIgnoreCase(player.getName()))
                .collect(Collectors.toList());
            //Calculate size of inventory to show
            int size = ((profiles.size() - 1) / 9 + 1) * 9;
            
            //Create inventory
            Inventory inv;
            if(state instanceof Chest chest){
                //If is a double chest, create inventory using de InventoryHolder of double chest
                //Else, create inventory using InventoryHolder of the single block
                InventoryHolder holder = chest.getInventory().getHolder();
                if(holder instanceof DoubleChest doubleChest){
                    inv = Bukkit.createInventory(doubleChest, size, Component.text("Secure Profiles").color(NamedTextColor.GREEN));
                }else{
                    inv = Bukkit.createInventory(new ChestInvetoryHolder(tileState), size, Component.text("Secure Profiles").color(NamedTextColor.GREEN));
                }

            }else{
                inv = Bukkit.createInventory(new ChestInvetoryHolder(tileState), size, Component.text("Secure Profiles").color(NamedTextColor.GREEN));
            }

            //Iterate over profiles
            for (SecureProfile p : profiles) {
                //Create item with profile name
                ItemStack item = new ItemStack(Material.SHIELD);
                ItemMeta meta = item.getItemMeta();
                meta.displayName(Component.text(p.getName()).color(NamedTextColor.AQUA));
                if(p.getProfileId() == profile.getProfileId()){
                    //If profile is the block profile, add enchantment to identify it
                    meta.addEnchant(Enchantment.LURE, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }
                item.setItemMeta(meta);
                inv.addItem(item);
            }
            ItemStack openIntem = new ItemStack(Material.PAPER);
            ItemMeta openMeta = openIntem.getItemMeta();
            openMeta.displayName(Component.text("Free").color(NamedTextColor.AQUA));
            if(profile.getProfileId() == 0){
                openMeta.addEnchant(Enchantment.LURE, 1, true);
                openMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            openIntem.setItemMeta(openMeta);
            inv.addItem(openIntem);

            player.openInventory(inv);
            event.setCancelled(true);
            return;
        }
    }

    //Event to manage inventory clicks
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        Component inventoryTitle = event.getView().title();
        String plainTitle = PlainTextComponentSerializer.plainText().serialize(inventoryTitle);

        //If title isn't "Secure Profiles", return
        if (!plainTitle.equals("Secure Profiles"))
            return;
        
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        //If clicked item is null is air or doesn't have a display name, return
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;
        Component displayComponent = meta.displayName();
        String name = PlainTextComponentSerializer.plainText().serialize(displayComponent);
        //Cancel event for prevent get the item
        event.setCancelled(true);

        //Get player secure profile whit the name of the clicked item
        Optional<SecureProfile> searchProfile = plugin.getProfiles().stream()
                    .filter(p -> p.getOwnerName().equalsIgnoreCase(player.getName()) && p.getName().equalsIgnoreCase(name))
                    .findFirst();
        //If profile doesn't exist, send message and return
        if(!searchProfile.isPresent() && !name.equalsIgnoreCase("Free")) {
            player.sendMessage(ChatColor.RED + "This profile does not exist");
            return;
        }

        SecureProfile profile = name.equalsIgnoreCase("Free") ? 
            new SecureProfile(0, "", "", false, null)
            : searchProfile.get();

        //Namespace key to manage profile in PersistentDataContainer
        NamespacedKey key = new NamespacedKey(plugin, "profile");

        //Get holder of inventory
        InventoryHolder holder = event.getView().getTopInventory().getHolder();
        
        //If holder is a double chest change profileId on both sides
        if(holder instanceof DoubleChest doubleChest){
            Chest left = (Chest) doubleChest.getLeftSide();
            Chest right = (Chest) doubleChest.getRightSide();

            TileState leftState = (TileState) left.getBlock().getState();
            TileState rightState = (TileState) right.getBlock().getState();

            PersistentDataContainer leftContainer = leftState.getPersistentDataContainer();
            PersistentDataContainer rightContainer = rightState.getPersistentDataContainer();

            Integer leftProfile = leftContainer.get(key, PersistentDataType.INTEGER);
            Integer rightProfile = rightContainer.get(key, PersistentDataType.INTEGER);

            if(leftProfile == null || leftProfile != profile.getProfileId()){
                leftContainer.set(key, PersistentDataType.INTEGER, profile.getProfileId());
                leftState.update();
            }
            if(rightProfile == null || rightProfile != profile.getProfileId()){
                rightContainer.set(key, PersistentDataType.INTEGER, profile.getProfileId());
                rightState.update();
            }

            player.sendMessage(ChatColor.GREEN + "Double chest profile updated to " + name);
            player.closeInventory();
            return;
        }

        //If holder is a single chest or tile, change profileId on single chest
        if(holder instanceof ChestInvetoryHolder state){
            PersistentDataContainer container = state.getTileState().getPersistentDataContainer();
                container.set(key, PersistentDataType.INTEGER, profile.getProfileId());
                state.getTileState().update();

                player.sendMessage(ChatColor.GREEN + "Profile updated to " + name);
            player.closeInventory();
        }
    }

    private SecureProfile hasAccess(Player player, int chestProfile){

        //If profileId is 0, return empty profile
        if(chestProfile == 0)
            return new SecureProfile(0, "", "", false, null);

        //Get profile info with profileId
         Optional<SecureProfile> profile = plugin.getProfiles().stream()
                    .filter(p -> p.getProfileId() == chestProfile)
                    .findFirst();
        
        //If profile don't exist return empty profile
        if (!profile.isPresent())
            return new SecureProfile(0, "", "", false, null);

        //If is the owener or a member of the profile, return profile
        if(
            profile.get().getOwnerName().equals(player.getName()) ||
            profile.get().getMembers().contains(player.getName())
        ) return profile.get();
        
        return null;
    }
}
