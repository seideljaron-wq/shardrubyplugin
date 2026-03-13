package de.shardplugin.store;

import de.shardplugin.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RubyStore {

    private final Main plugin;
    private final List<ItemStack> items = new ArrayList<>();
    private File file;
    private FileConfiguration cfg;

    public static final String MAIN_TITLE    = ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Ruby Shop";
    public static final String CONFIRM_TITLE = ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Ruby Shop " + ChatColor.DARK_GRAY + "| " + ChatColor.WHITE + "Confirm";

    public RubyStore(Main plugin) {
        this.plugin = plugin;
        load();
    }

    // ─── Persistence ────────────────────────────────────────────────────────────

    private void load() {
        file = new File(plugin.getDataFolder(), "rubystore.yml");
        if (!file.exists()) return;
        cfg = YamlConfiguration.loadConfiguration(file);
        int count = cfg.getInt("count", 0);
        for (int i = 0; i < count; i++) {
            ItemStack it = cfg.getItemStack("item." + i);
            if (it != null) items.add(it);
        }
    }

    public void setItems(List<ItemStack> newItems) {
        items.clear();
        items.addAll(newItems);
        if (file == null) file = new File(plugin.getDataFolder(), "rubystore.yml");
        cfg = new YamlConfiguration();
        cfg.set("count", items.size());
        for (int i = 0; i < items.size(); i++) cfg.set("item." + i, items.get(i));
        try { cfg.save(file); } catch (IOException e) { e.printStackTrace(); }
    }

    public boolean hasItems() { return !items.isEmpty(); }
    public List<ItemStack> getItems() { return items; }
    public int getPrice() { return plugin.getConfig().getInt("settings.store-price", 200); }

    // ─── Open main shop GUI ──────────────────────────────────────────────────────

    public void openShop(Player player) {
        Inventory inv = Bukkit.createInventory(null, 18, MAIN_TITLE);

        // Fill all with pink glass
        ItemStack fill = glass(Material.PINK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 18; i++) inv.setItem(i, fill);

        // Center items in row 1
        int start = Math.max(0, (9 - items.size()) / 2);
        for (int i = 0; i < items.size() && i < 9; i++) {
            ItemStack display = items.get(i).clone();
            ItemMeta meta = display.getItemMeta();
            if (meta == null) meta = Bukkit.getItemFactory().getItemMeta(display.getType());
            List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.RED + "Price: " + getPrice() + " Shards");
            meta.setLore(lore);
            display.setItemMeta(meta);
            inv.setItem(start + i, display);
        }
        player.openInventory(inv);
    }

    // ─── Open confirm GUI ────────────────────────────────────────────────────────

    public void openConfirm(Player player, ItemStack item) {
        Inventory inv = Bukkit.createInventory(null, 9, CONFIRM_TITLE);

        // Gray filler
        ItemStack gray = glass(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 9; i++) inv.setItem(i, gray);

        // Slot 0 = cancel (red)
        ItemStack no = glass(Material.RED_STAINED_GLASS_PANE,
                ChatColor.RED + "" + ChatColor.BOLD + "✗ Cancel");
        inv.setItem(0, no);

        // Slot 4 = item preview (strip price lore)
        inv.setItem(4, stripPriceLore(item));

        // Slot 8 = confirm (green)
        ItemStack yes = glass(Material.LIME_STAINED_GLASS_PANE,
                ChatColor.GREEN + "" + ChatColor.BOLD + "✔ Buy for " + getPrice() + " Shards");
        inv.setItem(8, yes);

        player.openInventory(inv);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────────

    private ItemStack glass(Material mat, String name) {
        ItemStack it = new ItemStack(mat);
        ItemMeta m = it.getItemMeta();
        m.setDisplayName(name);
        it.setItemMeta(m);
        return it;
    }

    public ItemStack stripPriceLore(ItemStack item) {
        ItemStack copy = item.clone();
        ItemMeta meta = copy.getItemMeta();
        if (meta != null && meta.hasLore()) {
            List<String> lore = new ArrayList<>(meta.getLore());
            // Remove last 2 lines (blank + "Price: X Shards")
            if (lore.size() >= 2) {
                lore.remove(lore.size() - 1);
                lore.remove(lore.size() - 1);
            }
            meta.setLore(lore.isEmpty() ? null : lore);
            copy.setItemMeta(meta);
        }
        return copy;
    }

    public String getItemName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName())
            return item.getItemMeta().getDisplayName();
        String n = item.getType().name().toLowerCase().replace("_", " ");
        StringBuilder sb = new StringBuilder();
        for (String w : n.split(" "))
            sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
        return sb.toString().trim();
    }
}
