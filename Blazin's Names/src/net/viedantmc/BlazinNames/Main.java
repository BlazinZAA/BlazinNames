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
    final public String chatOutputPrefix = ChatColor.GRAY + "[" + ChatColor.GOLD + "Blazin" + ChatColor.RED + "Names" + ChatColor.GRAY + "] " + ChatColor.RESET;
    public ArrayList<Material> headColorIDArr = new ArrayList<Material>(Arrays.asList(
            Material.BLACK_CONCRETE, //BLACK
            Material.BLUE_CONCRETE, //DARK_BLUE
            Material.GREEN_CONCRETE, //DARK_GREEN
            Material.CYAN_CONCRETE, //DARK_AQUA
            Material.RED_CONCRETE, //DARK_RED
            Material.MAGENTA_CONCRETE, //DARK_PURPLE
            Material.YELLOW_CONCRETE, //GOLD
            Material.LIGHT_GRAY_CONCRETE, //GRAY
            Material.GRAY_CONCRETE, //DARK_GRAY
            Material.BLUE_CONCRETE_POWDER, //BLUE
            Material.LIME_CONCRETE_POWDER, //GREEN
            Material.CYAN_CONCRETE_POWDER, //AQUA
            Material.RED_CONCRETE_POWDER, //RED
            Material.MAGENTA_CONCRETE_POWDER, //LIGHT_PURPLE
            Material.YELLOW_CONCRETE_POWDER, //YELLOW
            Material.WHITE_CONCRETE_POWDER //WHITE
    ));
    public ArrayList<String> miscEffectItems = new ArrayList<String>(Arrays.asList(
            "27530",
            "32822",
            "32819",
            "32820",
            "32821"
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
        if (label.equalsIgnoreCase("NameColor") || label.equalsIgnoreCase("Color")) {
            player.openInventory(inv);
            return false;
        } else if (label.equalsIgnoreCase("Nickname") || label.equalsIgnoreCase("Nick")) {
            if (args.length != 1) {
                player.sendMessage(chatOutputPrefix + ChatColor.RED + "Invalid command syntax. The correct syntax is /" + label + " §onickname§r.");
                return true;
            }
            String actualName = actualNames.get(player.getUniqueId());
            String currentName = player.getDisplayName().replaceAll("§.", "");
            String lpPrefix = lpAPI.getUserManager().getUser(player.getUniqueId()).getCachedData().getMetaData().getPrefix();
            if (lpPrefix == null) {
                lpPrefix = "";
            }
            String nickName = args[0];
            if (nickName.equalsIgnoreCase("Reset")) {
                if (actualName != null) {
                    String actualNameWithFormatting = player.getDisplayName().replace(currentName, actualName);
                    player.setDisplayName(ChatColor.translateAlternateColorCodes('&', actualNameWithFormatting));
                    player.setPlayerListName(ChatColor.translateAlternateColorCodes('&', lpPrefix + actualNameWithFormatting));
                    player.sendMessage(chatOutputPrefix + "Your nickname was reset!");
                } else {
                    player.sendMessage(chatOutputPrefix + "You do not have a nickname set.");
                }
                return true;
            } else if (nickName.matches("([A-Za-z0-9]|_){3,16}")) {
                actualNames.putIfAbsent(player.getUniqueId(), currentName);
                actualName = actualNames.get(player.getUniqueId());
                String nickNameWithFormatting = actualName;
                if (!(actualName.equals(nickName))) {
                    nickNameWithFormatting = "~" + player.getDisplayName().replace(currentName, nickName);
                }
                player.setDisplayName(ChatColor.translateAlternateColorCodes('&', nickNameWithFormatting));
                player.setPlayerListName(ChatColor.translateAlternateColorCodes('&', lpPrefix + nickNameWithFormatting));
                player.sendMessage(chatOutputPrefix +"Your nickname was set to " + nickNameWithFormatting + "! Type /" + label + " §rreset to reset it at any time.");
                customNames.put(player.getUniqueId(), player.getDisplayName());
            } else {
                sender.sendMessage(chatOutputPrefix + ChatColor.RED + "Nicknames must be alphanumeric and between 3-16 characters. The only special symbol allowed is _.");
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
            player.sendMessage(chatOutputPrefix + "You changed the color of your name. Your new name is " + reformattedName);
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
            player.sendMessage(chatOutputPrefix + "You applied an effect to your name. Your new name is " + reformattedName);
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
            ItemStack colorBlock = new ItemStack(headColorIDArr.get(x), 1);
            ItemMeta colorBlockMeta = (ItemMeta) colorBlock.getItemMeta();
            colorBlockMeta.setDisplayName(ChatColor.values()[x] + ChatColor.values()[x].getName().replaceAll("_", " ").toUpperCase());
            colorBlock.setItemMeta(colorBlockMeta);
            inv.setItem(x, colorBlock);
        }

        for (int x = 16; x < 21; x++) {
            ItemStack item = hdbAPI.getItemHead(miscEffectItems.get(x-16));
            ItemMeta itemMeta = (ItemMeta) item.getItemMeta();
            itemMeta.setDisplayName(ChatColor.values()[x] + ChatColor.values()[x].getName().toUpperCase());
            item.setItemMeta(itemMeta);
            inv.setItem(x + 11, item);
        }

        ItemStack resetItem = hdbAPI.getItemHead("32823");
        ItemMeta resetItemMeta = (ItemMeta) resetItem.getItemMeta();
        resetItemMeta.setDisplayName("* RESET *");
        resetItem.setItemMeta(resetItemMeta);
        inv.setItem(33, resetItem);

        ItemStack closeItem = hdbAPI.getItemHead("26417");
        ItemMeta closeItemMeta = (ItemMeta) closeItem.getItemMeta();
        closeItemMeta.setDisplayName("CLOSE");
        closeItem.setItemMeta(closeItemMeta);
        inv.setItem(45, closeItem);
    }
}