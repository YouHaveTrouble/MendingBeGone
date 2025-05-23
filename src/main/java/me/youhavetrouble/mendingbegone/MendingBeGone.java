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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class MendingBeGone extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getLogger().info("Mending enchantment will be replaced with unbreaking 3");
        getServer().getPluginManager().registerEvents(this, this);
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
    public void onOpenVillagerInventory(InventoryOpenEvent event) {
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
        if (!itemStack.hasItemMeta()) return;
        if (itemStack.containsEnchantment(Enchantment.MENDING)) {
            itemStack.removeEnchantment(Enchantment.MENDING);
            if (itemStack.containsEnchantment(Enchantment.DURABILITY))
                itemStack.removeEnchantment(Enchantment.DURABILITY);
            itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
        }
        if (!Material.ENCHANTED_BOOK.equals(itemStack.getType())) return;
        ItemMeta meta = itemStack.getItemMeta();
        if (!(meta instanceof EnchantmentStorageMeta)) return;
        EnchantmentStorageMeta storage = (EnchantmentStorageMeta) meta;
        if (storage.hasStoredEnchant(Enchantment.MENDING)) {
            storage.removeStoredEnchant(Enchantment.MENDING);
            if (storage.hasStoredEnchant(Enchantment.DURABILITY))
                storage.removeStoredEnchant(Enchantment.DURABILITY);
            storage.addStoredEnchant(Enchantment.DURABILITY, 3, true);
            itemStack.setItemMeta(storage);
        }
    }

    private void removeMendingTrade(Merchant merchant) {
        List<MerchantRecipe> trades = new ArrayList<>(merchant.getRecipes());
        List<Integer> toReplace = new ArrayList<>();

        for (int i = 0; i < trades.size(); i++) {
            MerchantRecipe recipe = trades.get(i);
            if (!recipe.getResult().getType().equals(Material.ENCHANTED_BOOK)) continue;
            if (!(recipe.getResult().getItemMeta() instanceof EnchantmentStorageMeta)) continue;
            EnchantmentStorageMeta storage = (EnchantmentStorageMeta) recipe.getResult().getItemMeta();
            if (!storage.hasStoredEnchant(Enchantment.MENDING)) continue;
            toReplace.add(i);
        }

        if (toReplace.isEmpty()) return;

        for (int index : toReplace) {
            MerchantRecipe oldTrade = trades.get(index);
            ItemStack result = oldTrade.getResult();
            replaceMendingOnItem(result);
            MerchantRecipe newTrade = new MerchantRecipe(
                    result,
                    oldTrade.getUses(),
                    oldTrade.getMaxUses(),
                    oldTrade.hasExperienceReward(),
                    oldTrade.getVillagerExperience(),
                    oldTrade.getPriceMultiplier()
            );
            for (ItemStack ingredient : oldTrade.getIngredients()) {
                newTrade.addIngredient(ingredient);
            }
            trades.set(index, newTrade);
        }
        merchant.setRecipes(trades);
    }
}
