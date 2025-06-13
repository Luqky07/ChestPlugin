package pch.luqky.models;

import org.bukkit.block.TileState;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

//Class to manage chest TileState to recover profileId
public class ChestInvetoryHolder implements InventoryHolder {
    private final TileState tileState;
    public ChestInvetoryHolder(TileState tileState) {
        this.tileState = tileState;
    }

    public TileState getTileState() {
        return tileState;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
