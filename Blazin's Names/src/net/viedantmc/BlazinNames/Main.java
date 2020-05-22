// This version is obsolete, the next version will contain much more efficient code

package net.viedantmc.BlazinNames;

import java.util.*;

import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.Bukkit;
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
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;
import net.viedantmc.Files.DataManager;

//HeadDB API import.
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import me.arcaniax.hdb.api.DatabaseLoadEvent;

//LuckPerms API import
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;


public class Main extends JavaPlugin implements Listener {
    public DataManager data;
    public Inventory inv;
    public HeadDatabaseAPI hdbAPI;
    public LuckPerms lpAPI;
    public ArrayList<String> headColorIDArr = new ArrayList<String>(Arrays.asList(
            "6265", //BLACK
            "6263", //DARK_BLUE
            "6247", //DARK_GREEN
            "31697", //DARK_AQUA
            "6241", //DARK_RED
            "6242", //DARK_PURPLE
            "6240", //GOLD
            "6223", //GRAY
            "6232", //DARK_GRAY
            "6149", //BLUE
            "6197", //GREEN
            "6252", //AQUA
            "6141", //RED
            "6172", //LIGHT_PURPLE
            "6135", //YELLOW
            "6137" //WHITE
    ));
    public ArrayList<Material> miscEffectItems = new ArrayList<Material>(Arrays.asList(
            Material.SUNFLOWER,
            Material.STICK,
            Material.NETHER_BRICK_FENCE,
            Material.FIREWORK_ROCKET,
            Material.STRING
    ));

    @Override
    public void onEnable() {
        this.data = new DataManager(this);
        this.getServer().getPluginManager().registerEvents(this, this);
        this.saveDefaultConfig();
        if (this.getConfig().contains("customNames")) {
            this.getConfig().getConfigurationSection("customNames").getKeys(false).forEach(key ->
                    customNames.put(UUID.fromString(key), this.getConfig().getString("customNames." + key))
            );
        }
        if (this.getConfig().contains("actualNames")) {
            this.getConfig().getConfigurationSection("actualNames").getKeys(false).forEach(key ->
                    actualNames.put(UUID.fromString(key), this.getConfig().getString("actualNames." + key))
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
        if (!actualNames.isEmpty()) {
            for (Map.Entry<UUID, String> entry : actualNames.entrySet()) {
                this.getConfig().set("actualNames." + entry.getKey().toString(), entry.getValue());
            }
            this.saveConfig();
        }
    }

    public static Map<UUID, String> customNames = new HashMap<>();
    public static Map<UUID, String> actualNames = new HashMap<>();

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if (customNames.containsKey(player.getUniqueId())) {
            String formattedName = ChatColor.translateAlternateColorCodes('&', customNames.get(player.getUniqueId()));
            String lpPrefix = lpAPI.getUserManager().getUser(player.getUniqueId()).getCachedData().getMetaData().getPrefix();
            if (lpPrefix == null) {
                lpPrefix = "";
            }
            player.setDisplayName(ChatColor.translateAlternateColorCodes('&', formattedName));
            player.setPlayerListName(ChatColor.translateAlternateColorCodes('&', lpPrefix + formattedName));
        }
    }

    @EventHandler
    public void onDatabaseLoad(DatabaseLoadEvent e) {
        hdbAPI = new HeadDatabaseAPI();
        lpAPI = LuckPermsProvider.get();
        createInv();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        String currentName = player.getDisplayName().replaceAll("§.", "");
        String lpPrefix = lpAPI.getUserManager().getUser(player.getUniqueId()).getCachedData().getMetaData().getPrefix();
        if (lpPrefix == null) {
            lpPrefix = "";
        }
        if (label.equalsIgnoreCase("NameColor") || label.equalsIgnoreCase("Color")) {
            player.openInventory(inv);
            return false;
        } else if (label.equalsIgnoreCase("Nickname") || label.equalsIgnoreCase("Nick")) {
            String nickName = args[0];
            if (nickName.equalsIgnoreCase("Reset")) {
                String actualName = actualNames.get(player.getUniqueId());
                if (actualName != null) {
                    String actualNameWithFormatting = player.getDisplayName().replace(currentName, actualName);
                    player.setDisplayName(ChatColor.translateAlternateColorCodes('&', actualNameWithFormatting));
                    player.setPlayerListName(ChatColor.translateAlternateColorCodes('&', lpPrefix + actualNameWithFormatting));
                    player.sendMessage("[BlazinNames] Your nickname was reset!");
                } else {
                    player.sendMessage("[BlazinNames] You do not have a nickname set.");
                }
                return true;
            } else if (nickName.matches("([A-Za-z0-9]|_){3,16}")) {
                actualNames.putIfAbsent(player.getUniqueId(), currentName);
                String nickNameWithFormatting = player.getDisplayName().replace(currentName, "~" + nickName);
                player.setDisplayName(ChatColor.translateAlternateColorCodes('&', nickNameWithFormatting));
                player.setPlayerListName(ChatColor.translateAlternateColorCodes('&', lpPrefix + nickNameWithFormatting));
                player.sendMessage("[BlazinNames] Your nickname was set to " + nickNameWithFormatting + "! Type /nick reset to reset it at any time.");
                customNames.put(player.getUniqueId(), player.getDisplayName());
            } else {
                player.sendMessage("[BlazinNames] Nicknames must be alphanumeric and between 3-16 characters. The only special symbol allowed is _.");
                return true;
            }

        }
        return true;
    }


    @EventHandler()
    public void onClick(InventoryClickEvent event) {
        ItemStack currentItem = event.getCurrentItem();
        if (currentItem == null) {
            return;
        } else if (event.getClickedInventory() != inv) {
            return;
        }

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        String playerName = player.getDisplayName().replaceAll("§r", "");
        String lpPrefix = lpAPI.getUserManager().getUser(player.getUniqueId()).getCachedData().getMetaData().getPrefix();
        if (lpPrefix == null) {
            lpPrefix = "";
        }
        int eventSlot = event.getSlot();
        String itemDisplayName = event.getCurrentItem().getItemMeta().getDisplayName();


        if (eventSlot <= 16) {
            ChatColor color = ChatColor.values()[eventSlot];
            String reformattedName = "";
            if (playerName.substring(0, 2).matches("§([0-9]|[a-f])")) {
                reformattedName = color + playerName.substring(2);
            } else if (playerName.substring(0, 2).matches("§[k-r]")) {
                reformattedName = color + playerName;
            } else {
                reformattedName = color + playerName.replaceAll("§.", "");
            }
            player.setDisplayName(ChatColor.translateAlternateColorCodes('&', reformattedName + ChatColor.RESET));
            player.setPlayerListName(ChatColor.translateAlternateColorCodes('&', lpPrefix + reformattedName + ChatColor.RESET));
            player.sendMessage("[BlazinNames] You set your name color to " + color + color.getName().replaceAll("_", " ").toUpperCase() + ChatColor.RESET + "!");
        } else if (itemDisplayName.matches("§.(OBFUSCATED|BOLD|STRIKETHROUGH|UNDERLINE|ITALIC)")) {
            String effect = itemDisplayName.substring(0, 2);
            String reformattedName = "";
            if (playerName.substring(0, 2).matches("§([0-9]|[a-f])")) {
                reformattedName = playerName.substring(0, 2) + effect + playerName.substring(2);
            } else {
                reformattedName = effect + playerName.replaceAll("§.", "");
            }
            player.setDisplayName(ChatColor.translateAlternateColorCodes('&', reformattedName + ChatColor.RESET));
            player.setPlayerListName(ChatColor.translateAlternateColorCodes('&', lpPrefix + reformattedName + ChatColor.RESET));
            player.sendMessage("[BlazinNames] You applied " + effect + event.getCurrentItem().getItemMeta().getDisplayName() + ChatColor.RESET + " to your name!");
        } else if (eventSlot == 33) {
            player.setDisplayName(ChatColor.translateAlternateColorCodes('&', playerName.replaceAll("§.", "")));
            player.setPlayerListName(ChatColor.translateAlternateColorCodes('&', lpPrefix + playerName.replaceAll("§.", "")));
        } else if (eventSlot == 45) {
            player.closeInventory();
        }
        customNames.put(player.getUniqueId(), player.getDisplayName());
        player.closeInventory();

    }


    public void createInv() {
        inv = Bukkit.createInventory(null, 54, ChatColor.DARK_GRAY + "" + "Select Name Color");

        for (int x = 0; x < 16; x++) {
            ItemStack colorBlock = hdbAPI.getItemHead(headColorIDArr.get(x));
            ItemMeta colorBlockMeta = (ItemMeta) colorBlock.getItemMeta();
            colorBlockMeta.setDisplayName(ChatColor.values()[x] + ChatColor.values()[x].getName().replaceAll("_", " ").toUpperCase());
            colorBlock.setItemMeta(colorBlockMeta);
            inv.setItem(x, colorBlock);
        }

        for (int x = 16; x < 21; x++) {
            ItemStack item = new ItemStack(miscEffectItems.get(x - 16), 1);
            ItemMeta itemMeta = (ItemMeta) item.getItemMeta();
            itemMeta.setDisplayName(ChatColor.values()[x] + ChatColor.values()[x].getName().toUpperCase());
            item.setItemMeta(itemMeta);
            inv.setItem(x + 11, item);
        }

        ItemStack resetItem = hdbAPI.getItemHead("26417");
        ItemMeta resetItemMeta = (ItemMeta) resetItem.getItemMeta();
        resetItemMeta.setDisplayName("* RESET *");
        resetItem.setItemMeta(resetItemMeta);
        inv.setItem(33, resetItem);

        ItemStack closeItem = new ItemStack(Material.HOPPER, 1);
        ItemMeta closeItemMeta = (ItemMeta) closeItem.getItemMeta();
        closeItemMeta.setDisplayName("CLOSE");
        closeItem.setItemMeta(closeItemMeta);
        inv.setItem(45, closeItem);
    }
}
