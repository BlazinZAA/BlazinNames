package net.viedantmc.BlazinNames;

import me.neznamy.tab.api.EnumProperty;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.neznamy.tab.api.TABAPI.*;

//LuckPerms API import

public class Main extends JavaPlugin implements Listener {
    //HashMap of all custom Names.
    public static Map<UUID, String> customNames = new HashMap<>();
    //HashMap of all UUID->Original Names. Used when resetting nickname.
    public static Map<UUID, String> actualNames = new HashMap<>();
    public Inventory inv;
    final public String chatOutputPrefix = ChatColor.GRAY + "[" + ChatColor.GOLD + "B" + ChatColor.RED + "N" + ChatColor.GRAY + "] " + ChatColor.RESET;

    /**
     * Wait for plugin to be enabled (server start).
     * Fetch contents of YAML and put into customNames and actualNames HashMaps for use elsewhere.
     */
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        this.saveDefaultConfig();
        if (getConfig().contains("customNames")) {
            getConfig().getConfigurationSection("customNames").getKeys(false).forEach(key ->
                    customNames.put(UUID.fromString(key), getConfig().getString("customNames." + key))
            );
        }
        if (getConfig().contains("actualNames")) {
            getConfig().getConfigurationSection("actualNames").getKeys(false).forEach(key ->
                    actualNames.put(UUID.fromString(key), getConfig().getString("actualNames." + key))
            );
        }
    }


    /**
     * Wait for plugin to be disabled (server stop).
     * Dump HashMap into YAML config. Ensures changes not lost on server restart.
     */
    public void onDisable() {
        if (!customNames.isEmpty()) {
            for (Map.Entry<UUID, String> entry : customNames.entrySet()) {
                getConfig().set("customNames." + entry.getKey().toString(), entry.getValue());
            }
            saveConfig();
        }
        if (!actualNames.isEmpty()) {
            for (Map.Entry<UUID, String> entry : actualNames.entrySet()) {
                getConfig().set("actualNames." + entry.getKey().toString(), entry.getValue());
            }
            saveConfig();
        }
    }

    /**
     * Generate the inventory containing colours and effects.
     * Called onEnable.
     *
     * @param player The player creating the inventory for.
     */
    public void createInv(Player player) {
        inv = Bukkit.createInventory(null, 54, ChatColor.DARK_GRAY + "" + "Select Name Color or Effect");

        //Handle allowed colours.

        LinkedHashMap<Material, ChatColor> colorsAllowed = new LinkedHashMap<>();

        if (player.hasPermission("bn.namecolor.darkred")) {
            colorsAllowed.put(Material.RED_CONCRETE, ChatColor.DARK_RED);
        }
        if (player.hasPermission("bn.namecolor.red")) {
            colorsAllowed.put(Material.RED_CONCRETE_POWDER, ChatColor.RED);
        }
        if (player.hasPermission("bn.namecolor.gold")) {
            colorsAllowed.put(Material.YELLOW_CONCRETE, ChatColor.GOLD);
        }
        if (player.hasPermission("bn.namecolor.yellow")) {
            colorsAllowed.put(Material.YELLOW_CONCRETE_POWDER, ChatColor.YELLOW);
        }
        if (player.hasPermission("bn.namecolor.darkgreen")) {
            colorsAllowed.put(Material.GREEN_CONCRETE, ChatColor.DARK_GREEN);
        }
        if (player.hasPermission("bn.namecolor.green")) {
            colorsAllowed.put(Material.LIME_CONCRETE_POWDER, ChatColor.GREEN);
        }
        if (player.hasPermission("bn.namecolor.darkaqua")) {
            colorsAllowed.put(Material.CYAN_CONCRETE, ChatColor.DARK_AQUA);
        }
        if (player.hasPermission("bn.namecolor.aqua")) {
            colorsAllowed.put(Material.CYAN_CONCRETE_POWDER, ChatColor.AQUA);
        }
        if (player.hasPermission("bn.namecolor.darkblue")) {
            colorsAllowed.put(Material.BLUE_CONCRETE, ChatColor.DARK_BLUE);
        }
        if (player.hasPermission("bn.namecolor.blue")) {
            colorsAllowed.put(Material.BLUE_CONCRETE_POWDER, ChatColor.BLUE);
        }
        if (player.hasPermission("bn.namecolor.darkpurple")) {
            colorsAllowed.put(Material.MAGENTA_CONCRETE, ChatColor.DARK_PURPLE);
        }
        if (player.hasPermission("bn.namecolor.lightpurple")) {
            colorsAllowed.put(Material.MAGENTA_CONCRETE_POWDER, ChatColor.LIGHT_PURPLE);
        }
        if (player.hasPermission("bn.namecolor.black")) {
            colorsAllowed.put(Material.BLACK_CONCRETE, ChatColor.BLACK);
        }
        if (player.hasPermission("bn.namecolor.darkgray")) {
            colorsAllowed.put(Material.GRAY_CONCRETE, ChatColor.DARK_GRAY);
        }
        if (player.hasPermission("bn.namecolor.gray")) {
            colorsAllowed.put(Material.LIGHT_GRAY_CONCRETE, ChatColor.GRAY);
        }
        if (player.hasPermission("bn.namecolor.white")) {
            colorsAllowed.put(Material.WHITE_CONCRETE_POWDER, ChatColor.WHITE);
        }

        int colorPos = 0;


        for (Map.Entry<Material, ChatColor> entry : colorsAllowed.entrySet()) {
            String playerName = player.getDisplayName();
            Material mat = entry.getKey();
            ChatColor color = entry.getValue();
            ItemStack colorBlock = new ItemStack(mat, 1);
            ItemMeta colorBlockMeta = (ItemMeta) colorBlock.getItemMeta();
            colorBlockMeta.setDisplayName(color + color.getName().replaceAll("_", " ").toUpperCase());
            String primaryColor = "§f";
            if (playerName.substring(0, 2).matches("§[0-9a-f]")) {
                primaryColor = playerName.substring(0, 2);
            }
            String rightClickName = getMultiColorName(primaryColor, color.toString(), playerName);
            if (player.hasPermission("bn.namecolor.multi")) {
                colorBlockMeta.setLore(Arrays.asList(ChatColor.GREEN + "Left-click " + ChatColor.WHITE + "to set your name to " + color + player.getDisplayName().replaceAll("§[0-9a-f]", "") + ChatColor.WHITE + ".",
                        ChatColor.RED + "Right-click " + ChatColor.WHITE + "to set your name to " + rightClickName + ChatColor.WHITE + "."));
            } else {
                colorBlockMeta.setLore(Arrays.asList(ChatColor.GREEN + "Left-click " + ChatColor.WHITE + "to set your name to " + color + player.getDisplayName().replaceAll("§[0-9a-f]", "") + ChatColor.WHITE + "."));
            }

            colorBlock.setItemMeta(colorBlockMeta);
            inv.setItem(colorPos, colorBlock);
            colorPos++;
        }

        //Handle allowed effects.

        LinkedHashMap<Material, ChatColor> effectsAllowed = new LinkedHashMap<>();

        if (player.hasPermission("bn.namecolor.magic")) {
            effectsAllowed.put(Material.STRING, ChatColor.MAGIC);
        }
        if (player.hasPermission("bn.namecolor.bold")) {
            effectsAllowed.put(Material.STICK, ChatColor.BOLD);
        }
        if (player.hasPermission("bn.namecolor.strikethrough")) {
            effectsAllowed.put(Material.NETHER_BRICK_FENCE, ChatColor.STRIKETHROUGH);
        }
        if (player.hasPermission("bn.namecolor.underline")) {
            effectsAllowed.put(Material.FIREWORK_ROCKET, ChatColor.UNDERLINE);
        }

        int effectPos = 27;

        for (Map.Entry<Material, ChatColor> entry : effectsAllowed.entrySet()) {
            Material mat = entry.getKey();
            ChatColor effect = entry.getValue();
            ItemStack effectBlock = new ItemStack(mat, 1);
            ItemMeta effectBlockMeta = (ItemMeta) effectBlock.getItemMeta();
            effectBlockMeta.setDisplayName(effect + effect.getName().replaceAll("_", " ").toUpperCase());
            String playerName = player.getDisplayName().replaceAll("§r", "");
            effectBlockMeta.setLore(Arrays.asList(ChatColor.GREEN + "Left-click " + ChatColor.WHITE + "to set your name to " + getNameWithEffect(playerName, effect.toString(), player) + ChatColor.WHITE + "."));
            effectBlock.setItemMeta(effectBlockMeta);
            inv.setItem(effectPos, effectBlock);
            effectPos++;
        }

        //Add reset and close buttons.

        ItemStack resetItem = new ItemStack(Material.SNOWBALL, 1);
        ItemMeta resetItemMeta = (ItemMeta) resetItem.getItemMeta();
        resetItemMeta.setDisplayName("RESET");
        resetItemMeta.setLore(Arrays.asList(ChatColor.GREEN + "Left-click " + ChatColor.WHITE + "to reset your name to " + player.getDisplayName().replaceAll("§.", "") + "."));
        resetItem.setItemMeta(resetItemMeta);
        if (effectsAllowed.isEmpty()) {
            inv.setItem(27, resetItem);
        } else {
            inv.setItem(33, resetItem);
        }

        ItemStack closeItem = new ItemStack(Material.REDSTONE, 1);
        ItemMeta closeItemMeta = (ItemMeta) closeItem.getItemMeta();
        closeItemMeta.setDisplayName("CLOSE");
        closeItemMeta.setLore(Arrays.asList(ChatColor.GREEN + "Left-click " + ChatColor.WHITE + "to " + ChatColor.RED + "close " + ChatColor.WHITE + "this menu."));
        closeItem.setItemMeta(closeItemMeta);
        inv.setItem(45, closeItem);
    }

    public String getMultiColorName(String primaryColor, String altColor, String playerName) {
        String rightClickName = "";
        int ctr = 0;

        Set<String> allMatches = new HashSet<String>();
        Matcher m = Pattern.compile("§[k-o]").matcher(playerName);
        while (m.find()) {
            allMatches.add(m.group());
        }
        StringJoiner joiner = new StringJoiner("");
        for (String item : allMatches) {
            joiner.add(item);
        }
        String effects = joiner.toString();

        playerName = playerName.replaceAll("§.", "");
        while (ctr < playerName.length()) {
            String useColor;
            if (ctr % 2 == 0) {
                useColor = primaryColor;
            } else {
                useColor = altColor;
            }
            rightClickName += useColor + effects + playerName.substring(ctr, ctr + 1);
            ctr++;
        }
        return rightClickName;
    }


    /**
     * Waits for a user to join the server, looks up their nickname / colouration in the YAML file and applies them.
     * Ensures changes aren't lost on relog.
     *
     * @param e The event generated by each player when they join the server.
     */
    @EventHandler
    public void onLogin(PlayerLoginEvent e) {
        Player player = e.getPlayer();
        //Fetch the formatted name.
        String formattedName = player.getDisplayName();
        if (customNames.get(player.getUniqueId()) != null) {
            formattedName = ChatColor.translateAlternateColorCodes('&', customNames.get(player.getUniqueId()));
        }
        applyFormatting(player, formattedName);
    }

    /**
     * Applies formatting to a player.
     *
     * @param player        The player to apply formatting to.
     * @param formattedName The formatted name.
     */
    public void applyFormatting(Player player, String formattedName) {
        if (customNames.get(player.getUniqueId()) != null) {
            formattedName = ChatColor.translateAlternateColorCodes('&', customNames.get(player.getUniqueId())).replaceAll("§r", "");
        }
        player.setDisplayName(formattedName);
    }

    /**
     * Disable the tab suggestions for all BlazinNames commands.
     *
     * @param sender  The sender fo the command.
     * @param command The command executed.
     * @param alias   The command typed by the user.
     * @param args    Arguments of the command.
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> list = new ArrayList<>();
        return list;
    }

    /**
     * Waits for a new command.
     * /color, /namecolor - Open the colour change menu.
     * /nickname player_nick, /nick player_nick - Change the players name to player_nick.
     *
     * @param sender The user sending the command.
     * @param cmd    The command object containing the command executed.
     * @param label  The actual command typed by the user.
     * @param args   Array of arguments included in command.
     * @return boolean. True - Do nothing. False - Revert to "usage:" in plugin.yml.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You can't colour the console!");
            return true;
        }
        Player player = (Player) sender;
        String actualName = actualNames.get(player.getUniqueId());
        String playerName = player.getDisplayName();
        String currentName = playerName.replaceAll("§.", "");
        if (label.equalsIgnoreCase("NameColor") || label.equalsIgnoreCase("Color")) { //color, namecolor
            createInv(player);
            player.openInventory(inv);
        } else if (label.equalsIgnoreCase("Nickname") || label.equalsIgnoreCase("Nick")) { //nickname, nick
            //Load new nickname from args.
            String nickName;
            if (args.length == 0) {
                nickName = "Reset";
            } else if (args.length == 1) {
                nickName = args[0];
            } else {
                player.sendMessage(chatOutputPrefix + ChatColor.RED + "Invalid command syntax. The correct syntax is /" + label + " §onickname.§r");
                return true;
            }
            if (nickName.equalsIgnoreCase("Reset")) {
                if (actualName != null) {
                    customNames.put(player.getUniqueId(), actualName);
                    applyFormatting(player, actualName);
                    actualNames.remove(player.getUniqueId());
                    player.sendMessage(chatOutputPrefix + "Nickname reset!");
                } else {
                    player.sendMessage(chatOutputPrefix + "No nickname set. Type /" + label + " §onickname§r to set a nickname.");
                }
                return true;
            } else if (nickName.matches("([A-Za-z0-9]|_){3,16}")) { //Regex to check if name entered matches minecraft naming conventions.
                actualNames.putIfAbsent(player.getUniqueId(), currentName);
                String nickNameWithFormatting = ChatColor.ITALIC + nickName;
                player.sendMessage(chatOutputPrefix + "Nickname set to " + nickNameWithFormatting + ChatColor.RESET + "! You will need to reapply any colors/effects you previously had applied.");
                customNames.put(player.getUniqueId(), nickNameWithFormatting);
                applyFormatting(player, nickNameWithFormatting);
            } else { //Does not conform to regex - error out.
                sender.sendMessage(chatOutputPrefix + ChatColor.RED + "Nicknames must be alphanumeric, between 3-16 characters, and may contain _.");
            }
        }
        return true;
    }

    /**
     * Wait for a user to click one of the colours / effects in the menu. Apply the effect to the player name.
     *
     * @param event Event containing information about the click.
     */
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        ItemStack currentItem = event.getCurrentItem();
        if (currentItem == null) { //Clicked an empty space?
            return;
        } else if (event.getClickedInventory() != inv) { //Not the correct inventory clicked?
            return;
        }
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        String playerName = player.getDisplayName().replaceAll("§r", "");
        int eventSlot = event.getSlot();
        String itemDisplayName = event.getCurrentItem().getItemMeta().getDisplayName();
        if (event.isLeftClick()) {
            if (eventSlot <= 16) {
                leftClick(itemDisplayName, player);
            } else if (eventSlot >= 27 && eventSlot != 45) { //Effects
                String effect = itemDisplayName.substring(0, 2);
                if (effect.equals("RE")) {
                    String resetName = playerName.replaceAll("§[0-9a-fk-n]", "");
                    customNames.put(player.getUniqueId(), resetName);
                    applyFormatting(player, resetName);
                    player.sendMessage(chatOutputPrefix + "Name color and effects reset!");
                } else {
                    String reformattedName = getNameWithEffect(playerName, effect, player);
                    customNames.put(player.getUniqueId(), reformattedName);
                    applyFormatting(player, reformattedName);
                    player.sendMessage(chatOutputPrefix + "Effect applied: " + reformattedName);
                }
            } else if (eventSlot == 33) { //Reset
                String resetName = playerName.replaceAll("§[0-9a-fk-n]", "");
                customNames.put(player.getUniqueId(), resetName);
                applyFormatting(player, resetName);
                player.sendMessage(chatOutputPrefix + "Name color and effects reset!");
            } else if (eventSlot == 45) { //Close
                player.closeInventory();
            }
        } else if (event.isRightClick() && player.hasPermission("bn.namecolor.multi")) {
            if (eventSlot <= 16) {
                rightClick(itemDisplayName, player);
            }
        }
        if (eventSlot != 45) {
            createInv(player);
            player.openInventory(inv);
        }
    }

    /**
     * Gets a player name with effects applied.
     *
     * @param playerName The player's current display name.
     * @param effect     The effect to be applied to the player name.
     * @param player     The player object.
     * @return String. The formatted name.
     */
    public String getNameWithEffect(String playerName, String effect, Player player) {
        String reformattedName = playerName;
        Set<String> allMatches = new HashSet<String>();
        Matcher m = Pattern.compile("§[k-n]").matcher(reformattedName);
        while (m.find()) {
            allMatches.add(m.group());
        }
        StringJoiner joiner = new StringJoiner("");
        for (String item : allMatches) {
            joiner.add(item);
        }
        String effects = joiner.toString();
        effect += effects;
        if (actualNames.get(player.getUniqueId()) != null) {
            effect += ChatColor.ITALIC;
        }
        reformattedName = reformattedName.replaceAll("§[k-o]", "");
        if (reformattedName.substring(0, 2).matches("§[0-9a-f]")) { //Does name already have colour applied?
            String primaryColor = reformattedName.substring(0, 2);
            if (reformattedName.substring(3, 5).matches("§[0-9a-f]")) {
                String altColor = reformattedName.substring(3, 5);
                reformattedName = "";
                int i = 0;
                for (Character c : player.getDisplayName().replaceAll("§.", "").toCharArray()) {
                    if (i % 2 == 0) {
                        reformattedName += primaryColor + effect + c.toString();
                    } else {
                        reformattedName += altColor + effect + c.toString();
                    }
                    i++;
                }
            } else {
                reformattedName = reformattedName.substring(0, 2) + effect + reformattedName.substring(2);
            }
        } else {
            reformattedName = ChatColor.WHITE + effect + playerName;
        }
        return reformattedName;
    }

    @EventHandler
    public void onAsyncChat(AsyncPlayerChatEvent e) {
        new Thread(new Runnable() {
            public void run() {
                Player player = e.getPlayer();
                UUID playerUUID = player.getUniqueId();
                String message = e.getMessage();
                if (message.length() > 35) {
                    message = message.substring(0, 33) + "...";
                }
                setValueTemporarily(playerUUID, EnumProperty.BELOWNAME, message);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                if (hasTemporaryValue(playerUUID, EnumProperty.BELOWNAME)) {
                    removeTemporaryValue(playerUUID, EnumProperty.BELOWNAME);
                }
            }
        }).start();
    }

    /**
     * Applies single colour to name on left-click on color.
     *
     * @param itemDisplayName The display name of the item clicked.
     * @param player          The player object to apply the color to.
     */
    public void leftClick(String itemDisplayName, Player player) {
        //Get colour to apply to name.
        String color = itemDisplayName.substring(0, 2);
        String reformattedName = color + player.getDisplayName().replaceAll("§[0-9a-f]", "");
        customNames.put(player.getUniqueId(), reformattedName);
        player.sendMessage(chatOutputPrefix + "Color applied: " + reformattedName);
        applyFormatting(player, reformattedName);
    }

    /**
     * Applies a multi-colour to a player on right-click.
     *
     * @param itemDisplayName The display name of the item clicked.
     * @param player          The player object to apply the color to.
     */
    public void rightClick(String itemDisplayName, Player player) {
        String color = itemDisplayName.substring(0, 2);
        String playerName = player.getDisplayName();
        String primaryColor = "§f";
        if (playerName.substring(0, 2).matches("§[0-9a-f]")) {
            primaryColor = playerName.substring(0, 2);
        }
        String rightClickName = getMultiColorName(primaryColor, color, playerName);
        customNames.put(player.getUniqueId(), rightClickName);
        player.sendMessage(chatOutputPrefix + "Color applied: " + rightClickName);
        applyFormatting(player, rightClickName);
    }

}