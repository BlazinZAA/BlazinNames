// This version is obsolete, the next version will contain much more efficient code

package net.viedantmc.BlazinNames;


import java.lang.reflect.Array;
import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;
import net.viedantmc.Files.DataManager;

public class Main extends JavaPlugin implements Listener {
    public DataManager data;
    public Inventory inv;
    public ArrayList<Color> colorsRGB = new ArrayList<Color>(Arrays.asList(
            Color.fromRGB(0, 0, 0), //BLACK
            Color.fromRGB(0, 0, 170), //DARK_BLUE
            Color.fromRGB(0, 170, 0), //DARK_GREEN
            Color.fromRGB(0, 170, 170), //DARK_AQUA
            Color.fromRGB(170, 0, 0), //DARK_RED
            Color.fromRGB(170, 0, 170), //DARK_PURPLE
            Color.fromRGB(255, 170, 0), //GOLD
            Color.fromRGB(170, 170, 170), //GRAY
            Color.fromRGB(85, 85, 85), //DARK_GRAY
            Color.fromRGB(85, 85, 255), //BLUE
            Color.fromRGB(85, 255, 85), //GREEN
            Color.fromRGB(85, 255, 255), //AQUA
            Color.fromRGB(255, 85, 85), //RED
            Color.fromRGB(255, 85, 255), //LIGHT_PURPLE
            Color.fromRGB(255, 255, 85), //YELLOW
            Color.fromRGB(255, 255, 255) //WHITE
    ));

    @Override
    public void onEnable() {
        this.data = new DataManager(this);
        this.getServer().getPluginManager().registerEvents(this, this);
        createInv();
        this.saveDefaultConfig();
        if (this.getConfig().contains("customNames")) {
            this.getConfig().getConfigurationSection("customNames").getKeys(false).forEach(key ->
                    customNames.put(UUID.fromString(key), this.getConfig().getString("customNames." + key))
            );
        }
    }

    public void onDisable() {
        if (!customNames.isEmpty()) {
            for (Map.Entry<UUID, String> entry : customNames.entrySet()) {
                this.getConfig().set("customNames." + entry.getKey().toString(), entry.getValue());
            }
            this.saveConfig();
        }
    }

    public static Map<UUID, String> customNames = new HashMap<>();

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (customNames.containsKey(e.getPlayer().getUniqueId())) {
            e.getPlayer().setDisplayName(customNames.get(e.getPlayer().getUniqueId()));
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        //Changing the color
        if (label.equalsIgnoreCase("NameColor") || label.equalsIgnoreCase("Color")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Sure lets color the console! Because that makes sense!... Idiot.");
                return true;
            }
            Player player = (Player) sender;
            player.openInventory(inv);
            return true;
        }
        Player player = (Player) sender;
        String playerName = player.getDisplayName();
        //String nickColors = playerName.substring(0, 2); // Storing the Color code
        String nickColors = "";
        String nickColors2 = "";
        if (playerName.length() > 4) {
            nickColors2 = playerName.substring(2, 4);
        }
        String BoldEffect = "";
        if (!(playerName.contains("~"))) {
            if (playerName.contains("§")) {
                nickColors = playerName.substring(0, 2);
                if (nickColors.contains("§l")) {
                    BoldEffect = "§l";
                }
            }
        }
        if (playerName.contains("~")) {
            if (playerName.contains("§")) {
                nickColors = playerName.substring(0, 2);
                if (nickColors.contains("§l")) {
                    BoldEffect = "§l";
                } else if (nickColors2.contains("§l")) {
                    BoldEffect = "§l";
                }
            }
        }

        String nick = "";
        for (String arg : args) {
            nick += arg + " ";
        }


        if (label.equalsIgnoreCase("nick") || label.equalsIgnoreCase("nickname")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Console wants a nickname now? Too bad.");
                return true;
            }
            if (!(args.length == 0)) {
                if ((nickColors.contains("§"))) {
                    // Nickname + Set color
                    player.setDisplayName(nickColors + BoldEffect + "~" + nick + ChatColor.RESET);
                    sender.sendMessage(ChatColor.GOLD + "Your nickname has been set to " + player.getDisplayName());
                    customNames.put(player.getUniqueId(), player.getDisplayName());
                    return true;
                }
                // No color :(
                player.setDisplayName(nickColors + BoldEffect + "~" + nick);
                sender.sendMessage(ChatColor.GOLD + "Your nickname has been set to " + player.getDisplayName());
                customNames.put(player.getUniqueId(), player.getDisplayName());
                return true;
            }
            // No args
            player.setDisplayName(player.getName());
            sender.sendMessage(ChatColor.GOLD + "Your nickname has been reset.");
            customNames.put(player.getUniqueId(), player.getDisplayName());
            return false;
        }

        return false;

    }


    @EventHandler()
    public void onClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null) {
            return;
        }

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        String playerName = player.getDisplayName();
        String nick = ChatColor.stripColor(player.getDisplayName());

        int eventSlot = event.getSlot();
        String itemDisplayName = event.getCurrentItem().getItemMeta().getDisplayName();


        if (eventSlot <= 16) {
            ChatColor color = ChatColor.values()[eventSlot];
            player.setDisplayName(ChatColor.translateAlternateColorCodes('&', color + player.getDisplayName().replaceAll("§.", "") + ChatColor.RESET));
            player.setPlayerListName(ChatColor.translateAlternateColorCodes('&', color + player.getDisplayName().replaceAll("§.", "") + ChatColor.RESET));
            player.sendMessage("[Blazin's Names] You set your name color to " + color + color.getName().replaceAll("_", " ") + ChatColor.RESET + "!");
        } else if (itemDisplayName.matches("§.(obfuscated|bold|strikethrough|underline|italic)")) {
            String effect = itemDisplayName.substring(0, 2);
            player.setDisplayName(ChatColor.translateAlternateColorCodes('&', effect + player.getDisplayName().replaceAll("§.", "") + ChatColor.RESET));
            player.setPlayerListName(ChatColor.translateAlternateColorCodes('&', effect + player.getDisplayName().replaceAll("§.", "") + ChatColor.RESET));
            player.sendMessage("[Blazin's Names] You applied " + effect + event.getCurrentItem().getItemMeta().getDisplayName() + ChatColor.RESET + " to your name!");
        } else if (eventSlot == 45) {
            player.closeInventory();
        }
        customNames.put(player.getUniqueId(), player.getDisplayName());
        player.closeInventory();

    }


    public void createInv() {
        inv = Bukkit.createInventory(null, 54, ChatColor.DARK_GRAY + "" + "Select Name Color");

        for (int x = 0; x < 16; x++) {
            ChatColor color = ChatColor.values()[x];
            ItemStack leatherChest = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
            LeatherArmorMeta leatherChestMeta = (LeatherArmorMeta) leatherChest.getItemMeta();
            leatherChestMeta.setColor(colorsRGB.get(x));
            leatherChestMeta.setDisplayName(color + color.getName());
            leatherChestMeta.setLore(null);
            leatherChest.setItemMeta(leatherChestMeta);
            inv.setItem(x, leatherChest);
        }

        ArrayList<Material> miscEffectItems = new ArrayList<Material>(Arrays.asList(
                Material.SUNFLOWER,
                Material.STICK,
                Material.DRAGON_EGG,
                Material.FIREWORK_ROCKET,
                Material.STRING
        ));

        for (int x = 16; x < 21; x++) {
            ItemStack item = new ItemStack(miscEffectItems.get(x - 16), 1);
            ItemMeta itemMeta = (ItemMeta) item.getItemMeta();
            itemMeta.setDisplayName(ChatColor.values()[x] + ChatColor.values()[x].getName());
            item.setItemMeta(itemMeta);
            inv.setItem(x + 11, item);
        }

        ItemStack closeItem = new ItemStack(Material.HOPPER, 1);
        ItemMeta closeItemMeta = (ItemMeta) closeItem.getItemMeta();
        closeItemMeta.setDisplayName("Close Color Menu");
        closeItem.setItemMeta(closeItemMeta);
        inv.setItem(45, closeItem);
    }
}
