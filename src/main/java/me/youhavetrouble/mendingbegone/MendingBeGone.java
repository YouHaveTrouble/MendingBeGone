package me.youhavetrouble.mendingbegone;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class MendingBeGone extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getLogger().info("Mending enchantment will be replaced with unbreaking 3");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMendingItemPickup(EntityPickupItemEvent event) {
        replaceMendingOnItem(event.getItem().getItemStack());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInvOpen(InventoryOpenEvent event) {
        for (ItemStack itemStack : event.getInventory().getContents()) {
            replaceMendingOnItem(itemStack);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLogin(PlayerJoinEvent event) {
        for (ItemStack itemStack : event.getPlayer().getInventory()) {
            replaceMendingOnItem(itemStack);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInvClick(InventoryClickEvent event) {
        replaceMendingOnItem(event.getCurrentItem());
        replaceMendingOnItem(event.getCursor());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onVillagerJoinWorld(InventoryOpenEvent event) {
        if (!(event.getInventory() instanceof MerchantInventory)) return;
        MerchantInventory merchantInventory = (MerchantInventory) event.getInventory();
        removeMendingTrade(merchantInventory.getMerchant());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onVillagerJoinWorld(EntityAddToWorldEvent event) {
        if (!event.getEntityType().equals(EntityType.VILLAGER)) return;
        removeMendingTrade((Merchant) event.getEntity());
    }

    private void replaceMendingOnItem(ItemStack itemStack) {
        if (itemStack == null) return;
        if (itemStack.containsEnchantment(Enchantment.MENDING)) {
            itemStack.removeEnchantment(Enchantment.MENDING);
            if (itemStack.containsEnchantment(Enchantment.DURABILITY))
                itemStack.removeEnchantment(Enchantment.DURABILITY);
            itemStack.addEnchantment(Enchantment.DURABILITY, 3);
        }
    }

    private void removeMendingTrade(Merchant merchant) {
        List<MerchantRecipe> trades = new ArrayList<>(merchant.getRecipes());
        trades.removeIf(recipe -> {
            if (!recipe.getResult().getType().equals(Material.ENCHANTED_BOOK)) return false;
            EnchantmentStorageMeta storage = (EnchantmentStorageMeta) recipe.getResult().getItemMeta();
            return storage.hasStoredEnchant(Enchantment.MENDING);
        });
        merchant.setRecipes(trades);
    }
}
