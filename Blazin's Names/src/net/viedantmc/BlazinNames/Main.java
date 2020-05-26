package net.viedantmc.BlazinNames;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import com.mojang.authlib.GameProfile;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.luckperms.api.model.user.User;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;
import net.viedantmc.Files.DataManager;

//HeadDB API import.
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import me.arcaniax.hdb.api.DatabaseLoadEvent;

//LuckPerms API import
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.log.LogPublishEvent;
import net.luckperms.api.event.user.UserLoadEvent;
import net.luckperms.api.event.user.track.UserPromoteEvent;
import net.luckperms.api.event.user.track.UserDemoteEvent;

public class Main extends JavaPlugin implements Listener {
    //HashMap of all custom Names.
    public static Map<UUID, String> customNames = new HashMap<>();
    //HashMap of all UUID->Original Names. Used when resetting nickname.
    public static Map<UUID, String> actualNames = new HashMap<>();

    public DataManager data;
    public Inventory inv;
    public HeadDatabaseAPI hdbAPI;
    public LuckPerms lpAPI;
    final public String chatOutputPrefix = ChatColor.GRAY + "[" + ChatColor.GOLD + "B" + ChatColor.RED + "N" + ChatColor.GRAY + "] " + ChatColor.RESET;
    //ArrayList of all concrete colours to be used for colour selection.
    final public ArrayList<Material> colouredConcreteArr = new ArrayList<Material>(Arrays.asList(
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
    //ArrayList of IDs for loading from HeadDatabase API.
    final public ArrayList<String> miscEffectItems = new ArrayList<String>(Arrays.asList(
            "27530", //OBFUSCATE
            "32822", //BOLD
            "32819", //STRIKETHROUGH
            "32820", //UNDERLINE
            "32821" //ITALIC
    ));

    /**
     * Wait for plugin to be enabled (server start).
     * Fetch contents of YAML and put into customNames and actualNames HashMaps for use elsewhere.
     */
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

    /**
     * Wait for plugin to be disabled (server stop).
     * Dump HashMap into YAML config. Ensures changes not lost on server restart.
     */
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

    /**
     * Waits for a user to join the server, looks up their nickname / colouration in the YAML file and applies them.
     * Ensures changes aren't lost on relog.
     *
     * @param e The event generated by each player when they join the server.
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        //Fetch the formatted name.
        String formattedName = player.getDisplayName();
        if (customNames.get(player.getUniqueId()) != null) {
            formattedName = ChatColor.translateAlternateColorCodes('&', customNames.get(player.getUniqueId()));
        }
        String prefix = lpAPI.getUserManager().getUser(player.getUniqueId()).getCachedData().getMetaData().getPrefix();
        if (prefix == null) {
            prefix = "";
        }

        applyFormatting(player, formattedName, prefix);
        setPrefixToRank(player);
    }


    public void setPrefixToRank(Player player) {
        String executingUser = actualNames.get(player.getUniqueId());
        if (executingUser == null) {
            executingUser = player.getDisplayName();
        }
        String prefix = lpAPI.getUserManager().getUser(player.getUniqueId()).getCachedData().getMetaData().getPrefix();
        if (prefix == null) {
            prefix = "";
        }
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cmi nameplate " + executingUser + " -pref:" + prefix);
    }

    public void setSuffix(Player player, String suffix) {
        String executingUser = actualNames.get(player.getUniqueId());
        if (executingUser == null) {
            executingUser = player.getDisplayName();
        }
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cmi nameplate " + executingUser + " -suf:" + suffix);
    }

    public void setNameplate(Player player, String name) {
        for (Player onlinePlayer : getServer().getOnlinePlayers()) {
            if (onlinePlayer == player) continue;

            String prefix = lpAPI.getUserManager().getUser(onlinePlayer.getUniqueId()).getCachedData().getMetaData().getPrefix();
            if (prefix == null) {
                prefix = "";
            }

            //REMOVES THE PLAYER
            ((CraftPlayer) onlinePlayer).getHandle().playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, ((CraftPlayer) player).getHandle()));
            //CHANGES THE PLAYER'S GAME PROFILE
            GameProfile gp = ((CraftPlayer) player).getProfile();
            try {
                Field nameField = GameProfile.class.getDeclaredField("name");
                nameField.setAccessible(true);

                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(nameField, nameField.getModifiers() & ~Modifier.FINAL);
                nameField.set(gp, name);

                ((CraftPlayer) onlinePlayer).getHandle().playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, ((CraftPlayer) player).getHandle()));
                ((CraftPlayer) onlinePlayer).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(player.getEntityId()));
                ((CraftPlayer) onlinePlayer).getHandle().playerConnection.sendPacket(new PacketPlayOutNamedEntitySpawn(((CraftPlayer) player).getHandle()));

            } catch (IllegalAccessException | NoSuchFieldException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }


    /**
     * Wait for SQL database to load completely, then initialise session with HeadDatabase API, and LuckPerms API.
     * Create Inventory containing colours and effects.
     */
    @EventHandler
    public void onDatabaseLoad(DatabaseLoadEvent e) {
        lpAPI = LuckPermsProvider.get();
        hdbAPI = new HeadDatabaseAPI();
        createInv();
        lpListener();
    }


    /**
     * Generate the inventory containing colours and effects.
     * Called onDatabaseLoad.
     */
    public void createInv() {
        inv = Bukkit.createInventory(null, 54, ChatColor.DARK_GRAY + "" + "Select Name Color");
        //Iterate through colours (0 - 16 in ChatColour.values()). Correspond directly to colouredConcreteArr.
        for (int x = 0; x < 16; x++) {
            ItemStack colorBlock = new ItemStack(colouredConcreteArr.get(x), 1);
            ItemMeta colorBlockMeta = (ItemMeta) colorBlock.getItemMeta();
            //Set the display name of the item.
            colorBlockMeta.setDisplayName(ChatColor.values()[x] + ChatColor.values()[x].getName().replaceAll("_", " ").toUpperCase());
            colorBlock.setItemMeta(colorBlockMeta);
            //Add to pos x in inventory.
            inv.setItem(x, colorBlock);
        }

        //Iterate through colours (0 - 16 in ChatColour.values()). Correspond directly to miscEffectsItems.
        for (int x = 16; x < 21; x++) {
            ItemStack item = hdbAPI.getItemHead(miscEffectItems.get(x - 16));
            ItemMeta itemMeta = (ItemMeta) item.getItemMeta();
            itemMeta.setDisplayName(ChatColor.values()[x] + ChatColor.values()[x].getName().toUpperCase());
            item.setItemMeta(itemMeta);
            inv.setItem(x + 11, item);
        }

        //Use the HeadDatabase API to get reset item head.
        ItemStack resetItem = hdbAPI.getItemHead("32823");
        ItemMeta resetItemMeta = (ItemMeta) resetItem.getItemMeta();
        resetItemMeta.setDisplayName("* RESET *");
        resetItem.setItemMeta(resetItemMeta);
        inv.setItem(33, resetItem);

        //Same as above but for close item head.
        ItemStack closeItem = hdbAPI.getItemHead("26417");
        ItemMeta closeItemMeta = (ItemMeta) closeItem.getItemMeta();
        closeItemMeta.setDisplayName("CLOSE");
        closeItem.setItemMeta(closeItemMeta);
        inv.setItem(45, closeItem);
    }


    public void lpListener() {
        // get the LuckPerms event bus
        EventBus eventBus = lpAPI.getEventBus();
        // subscribe to an event using a lambda
        eventBus.subscribe(LogPublishEvent.class, e -> e.setCancelled(true));
        eventBus.subscribe(UserLoadEvent.class, e -> {
            System.out.println("User " + e.getUser().getUsername() + " was loaded!");
            // TODO: do something else...
        });
        // subscribe to an event using a method reference
        eventBus.subscribe(UserPromoteEvent.class, this::onUserPromote);
        eventBus.subscribe(UserDemoteEvent.class, this::onUserDemote);
    }


    private void onUserPromote(UserPromoteEvent event) {
        Plugin plugin = this;
        Bukkit.getScheduler().runTask(plugin, () -> {
            Player player = Bukkit.getPlayer(event.getUser().getUniqueId());
            setPrefixToRank(player);
        });
    }
    private void onUserDemote(UserDemoteEvent event) {
        Plugin plugin = this;
        Bukkit.getScheduler().runTask(plugin, () -> {
            Player player = Bukkit.getPlayer(event.getUser().getUniqueId());
            setPrefixToRank(player);
        });
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
        Player player = (Player) sender;
        //Get users real name from actualNames HashMap.
        String actualName = actualNames.get(player.getUniqueId());
        //Get unformatted current player name.
        String currentName = player.getDisplayName().replaceAll("§.|~", "");
        //Get prefix from luckperms API
        String prefix = lpAPI.getUserManager().getUser(player.getUniqueId()).getCachedData().getMetaData().getPrefix();
        if (prefix == null) {
            prefix = "";
        }
        if (label.equalsIgnoreCase("NameColor") || label.equalsIgnoreCase("Color")) { //color, namecolor
            player.openInventory(inv);
        } else if (label.equalsIgnoreCase("Nickname") || label.equalsIgnoreCase("Nick")) { //nickname, nick
            if (args.length == 0) {
                if (actualName != null) {
                    //Apply formatting to real name.
                    String actualNameWithFormatting = player.getDisplayName().replaceAll(currentName, actualName).replaceAll("~", "");
                    //Reset the nickname in the chat and tablist.
                    customNames.put(player.getUniqueId(), actualNameWithFormatting);
                    applyFormatting(player, actualNameWithFormatting, prefix);
                    setSuffix(player, "");
                    player.sendMessage(chatOutputPrefix + "Your nickname was reset!");
                } else {
                    player.sendMessage(chatOutputPrefix + "You do not have a nickname set. Type /" + label + " §onickname§r to set a nickname.");
                }
                return true;
            }
            if (args.length > 1) { //Invalid number of args? Show error.
                player.sendMessage(chatOutputPrefix + ChatColor.RED + "Invalid command syntax. The correct syntax is /" + label + " §onickname§r.");
            }
            //Load new nickname from args.
            String nickName = args[0];
            if (nickName.equalsIgnoreCase("Reset")) { //Resetting nickname.
                if (actualName != null) {
                    //Apply formatting to real name.
                    String actualNameWithFormatting = player.getDisplayName().replaceAll(currentName, actualName).replaceAll("~", "");
                    //Reset the nickname in the chat and tablist.
                    customNames.put(player.getUniqueId(), actualNameWithFormatting);
                    applyFormatting(player, actualNameWithFormatting + ChatColor.RESET, prefix);
                    setSuffix(player, "");
                    player.sendMessage(chatOutputPrefix + "Your nickname was reset!");
                } else {
                    player.sendMessage(chatOutputPrefix + "You do not have a nickname set.");
                }
            } else if (nickName.matches("([A-Za-z0-9]|_){3,16}")) { //Regex to check if name entered matches minecraft naming conventions.
                //If not already exists, add users un-nickedname to the actualNames HashMap for future reference.
                actualNames.putIfAbsent(player.getUniqueId(), currentName);
                //Load that name into a var.
                String nickNameWithFormatting = "~" + player.getDisplayName().replace(currentName, nickName).replaceAll("~", "");
                //Set the new nickname in the chat and tablist.
                player.sendMessage(chatOutputPrefix + "Your nickname was set to " + nickNameWithFormatting + "! Type /" + label + " §rreset to reset it at any time.");
                //Update in the customNames HashMap.
                customNames.put(player.getUniqueId(), nickNameWithFormatting);
                applyFormatting(player, nickNameWithFormatting + ChatColor.RESET, prefix);
            } else { //Does not conform to regex - error out.
                sender.sendMessage(chatOutputPrefix + ChatColor.RED + "Nicknames must be alphanumeric and between 3-16 characters. The only special symbol allowed is _.");
            }
        }
        return true;
    }

    public void applyFormatting(Player player, String formattedName, String prefix) {
        if (customNames.get(player.getUniqueId()) != null) {
            formattedName = ChatColor.translateAlternateColorCodes('&', customNames.get(player.getUniqueId()));
        }
        player.setDisplayName(ChatColor.translateAlternateColorCodes('&', formattedName));
        player.setPlayerListName(ChatColor.translateAlternateColorCodes('&', prefix + formattedName));
        splitName(player, formattedName, prefix);
    }

    public void splitName(Player player, String formattedName, String prefix) {
        int formattedNameLength = formattedName.length();
        if (formattedNameLength > 16) {
            String nameEntry = formattedName.substring(0, 16);
            String suffixEntry = formattedName.substring(16, formattedNameLength);
            if (formattedName.charAt(16) == '§') {
                nameEntry = formattedName.substring(0, 15);
                suffixEntry = formattedName.substring(15, formattedNameLength);
            }
            setSuffix(player, suffixEntry);
            getLogger().info(nameEntry + "|" + suffixEntry);
            setNameplate(player, nameEntry);
        } else {
            setNameplate(player, formattedName);
        }
        setPrefixToRank(player);
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
        Player player = (Player) event.getWhoClicked();
        //Get player name and replace all reset markers to prevent long chain.
        String playerName = player.getDisplayName().replaceAll("§r", "");
        //Get slot # of item clicked.
        int eventSlot = event.getSlot();
        //Get prefix from luckperms API
        String prefix = lpAPI.getUserManager().getUser(player.getUniqueId()).getCachedData().getMetaData().getPrefix();
        if (prefix == null) {
            prefix = "";
        }
        String itemDisplayName = event.getCurrentItem().getItemMeta().getDisplayName();
        int offset = 0;
        if (playerName.charAt(0) == '~') {
            offset = 1;
        }
        if (eventSlot <= 16) { //Colours
            //Get colour to apply to name.
            ChatColor color = ChatColor.values()[eventSlot];
            String reformattedName = color + player.getDisplayName().replace(player.getDisplayName(), color + player.getDisplayName());
            reformattedName = color + playerName.replaceAll("§([0-9]|[a-f])", "");
            //Set that new coloured name in the chat and tablist.
            customNames.put(player.getUniqueId(), reformattedName);
            applyFormatting(player, reformattedName + ChatColor.RESET, prefix);
            player.sendMessage(chatOutputPrefix + "You changed the color of your name. Your new name is " + reformattedName);
        } else if (eventSlot >= 27 && eventSlot <= 32) { //Effects
            String effect = itemDisplayName.substring(0, 2);
            String reformattedName = "";
            if (playerName.substring(offset, 2 + offset).matches("§([0-9]|[a-f])")) { //Does name already have colour applied?
                //Put effect after colour (needed to work).
                reformattedName = playerName.substring(0, 2 + offset) + effect + playerName.substring(2 + offset);
            } else {
                //Otherwise, no effect applied so just add to the front of the name.
                reformattedName = effect + playerName.replaceAll("§.", "");
            }
            //Set that name with effects in the chat and tablist.
            customNames.put(player.getUniqueId(), reformattedName);
            applyFormatting(player, reformattedName + ChatColor.RESET, prefix);
            player.sendMessage(chatOutputPrefix + "You applied an effect to your name. Your new name is " + reformattedName);
        } else if (eventSlot == 33) { //Reset
            //Remove all formatting and apply to chat and tablist.
            String resetName = playerName.replaceAll("§.", "");
            customNames.put(player.getUniqueId(), resetName);
            applyFormatting(player, resetName + ChatColor.RESET, prefix);
            player.sendMessage(chatOutputPrefix + "Your name's colour and effects have been reset!");
        } else if (eventSlot == 45) { //Close
            player.closeInventory();
        }
        //Update customNames HashMap with new name and close the inventory.
        player.closeInventory();

    }
}