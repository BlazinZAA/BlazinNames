package net.viedantmc.BlazinNames;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftInventoryPlayer;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
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
    //ArrayList of IDs for loading from HeadDatabase API.
    final public ArrayList<Material> altMiscEffectItems = new ArrayList<Material>(Arrays.asList(
            Material.SUNFLOWER, //OBFUSCATE
            Material.STICK, //BOLD
            Material.NETHER_BRICK_FENCE, //STRIKETHROUGH
            Material.FIREWORK_ROCKET, //UNDERLINE
            Material.STRING, //ITALIC
            Material.SNOWBALL, //RESET
            Material.REDSTONE //CLOSE
    ));

    /**
     * Wait for plugin to be enabled (server start).
     * Fetch contents of YAML and put into customNames and actualNames HashMaps for use elsewhere.
     */
    @Override
    public void onEnable() {
        data = new DataManager(this);
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
        instantiateAPIS();
    }

    /**
     * Creates instances of all required APIs.
     */
    public void instantiateAPIS() {
        lpAPI = LuckPermsProvider.get();
        if (lpAPI == null) {
            getLogger().severe("Failed to load LuckPerms API. Check LuckPerms has loaded correctly.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        lpListener();
    }

    /**
     * Wait for HeadDatabase to load before initialising API.
     * Triggers rest of config / setup.
     *
     * @param e The database load event.
     */
    @EventHandler
    public void onDatabaseLoad(DatabaseLoadEvent e) {
        hdbAPI = new HeadDatabaseAPI();
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

        if (hdbAPI == null) {
            getLogger().warning("Failed to load HeadDatabase API. Reverting to non-HDB icons.");
        }

        //Iterate through colours (0 - 16 in ChatColour.values()). Correspond directly to miscEffectsItems.
        for (int x = 16; x < 20; x++) {
            ItemStack item = new ItemStack(altMiscEffectItems.get(x - 16));
            if (hdbAPI != null) { //HDB loaded?
                item = hdbAPI.getItemHead(miscEffectItems.get(x - 16));
            }
            ItemMeta itemMeta = (ItemMeta) item.getItemMeta();
            itemMeta.setDisplayName(ChatColor.values()[x] + ChatColor.values()[x].getName().toUpperCase());
            item.setItemMeta(itemMeta);
            inv.setItem(x + 11, item);
        }
        ItemStack resetItem = new ItemStack(altMiscEffectItems.get(5));
        if (hdbAPI != null) { //HDB loaded?
            resetItem = hdbAPI.getItemHead("32823");
        }
        ItemMeta resetItemMeta = (ItemMeta) resetItem.getItemMeta();
        resetItemMeta.setDisplayName("* RESET *");
        resetItem.setItemMeta(resetItemMeta);
        inv.setItem(33, resetItem);

        ItemStack closeItem = new ItemStack(altMiscEffectItems.get(6));
        if (hdbAPI != null) { //HDB loaded?
            closeItem = hdbAPI.getItemHead("26417");
        }
        ItemMeta closeItemMeta = (ItemMeta) closeItem.getItemMeta();
        closeItemMeta.setDisplayName("CLOSE");
        closeItem.setItemMeta(closeItemMeta);
        inv.setItem(45, closeItem);
    }

    /**
     * Subscribe to the event listeners so that nameplates change properly on rank change.
     */
    public void lpListener() {
        // Set the LuckPerms event bus
        EventBus eventBus = lpAPI.getEventBus();
        // Subscribe to an event using a lambda
        eventBus.subscribe(LogPublishEvent.class, e -> e.setCancelled(true));
        eventBus.subscribe(UserLoadEvent.class, e -> {
            System.out.println("User " + e.getUser().getUsername() + " was loaded!");
        });
        // Subscribe to an event using a method reference.
        eventBus.subscribe(UserPromoteEvent.class, this::onUserPromote);
        eventBus.subscribe(UserDemoteEvent.class, this::onUserDemote);
    }

    /**
     * Sets the players scoreboard prefix to the correct rank after promotion.
     *
     * @param event The promote event.
     */
    private void onUserPromote(UserPromoteEvent event) {
        Plugin plugin = this;
        Bukkit.getScheduler().runTask(plugin, () -> {
            Player player = Bukkit.getPlayer(event.getUser().getUniqueId());
            setPrefixToRank(player);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tablistupdate");
        });
    }

    /**
     * Sets the players scoreboard prefix to the correct rank after demotion.
     *
     * @param event The demote event.
     */
    private void onUserDemote(UserDemoteEvent event) {
        Plugin plugin = this;
        Bukkit.getScheduler().runTask(plugin, () -> {
            Player player = Bukkit.getPlayer(event.getUser().getUniqueId());
            setPrefixToRank(player);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tablistupdate");
        });
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
        applyFormatting(player, formattedName);
    }

    /**
     * Applies formatting to a players nameplate, tablist, and chat names.
     *
     * @param player        The player to apply formatting to.
     * @param formattedName The formatted name.
     */
    public void applyFormatting(Player player, String formattedName) {
        if (customNames.get(player.getUniqueId()) != null) {
            formattedName = ChatColor.translateAlternateColorCodes('&', customNames.get(player.getUniqueId())).replaceAll("§r", "");
        }
        player.setDisplayName(formattedName);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tablistupdate");
        splitName(player, formattedName);
    }

    /**
     * Splits up a name if > 16 characters to flow over into suffix. Bypasses 16 character nameplate restriction.
     *
     * @param player        The player whose name is being split.
     * @param formattedName The formatted name being applied.
     */
    public void splitName(Player player, String formattedName) {
        int formattedNameLength = formattedName.length();

        if (formattedNameLength > 16) {
            String nameEntry = formattedName.substring(0, 16);
            String suffixEntry = formattedName.substring(16, formattedNameLength);
            if (formattedName.charAt(16) == '§') {
                nameEntry = formattedName.substring(0, 15);
                suffixEntry = formattedName.substring(15, formattedNameLength);
            }
            setNameplate(player, nameEntry);
            setSuffix(player, suffixEntry);
        } else {
            setNameplate(player, formattedName);
        }
        setPrefixToRank(player);
    }

    /**
     * Sets a players nameplate and tablist prefix for a player.
     *
     * @param player The player to apply the prefix to.
     */
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

    /**
     * ets a players nameplate and tablist suffix for a player.
     *
     * @param player The player to apply the suffix to.
     * @param suffix The suffix to apply to the player.
     */
    public void setSuffix(Player player, String suffix) {
        String executingUser = actualNames.get(player.getUniqueId());
        if (executingUser == null) {
            executingUser = player.getDisplayName();
        }
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cmi nameplate " + executingUser + " -suf:" + suffix);
    }

    /**
     * Uses NMS packets to set a users nameplate.
     *
     * @param player The player whos nameplate to set.
     * @param name   The name formatted name to set the nameplate to.
     */
    public void setNameplate(Player player, String name) {
        int playerEntityId = player.getEntityId();
        for (Player onlinePlayer : getServer().getOnlinePlayers()) {
            if (onlinePlayer == player) continue;
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

                //Destroy and recreate entity for all users to reflect nameplate changes.
                ((CraftPlayer) onlinePlayer).getHandle().playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, ((CraftPlayer) player).getHandle()));
                ((CraftPlayer) onlinePlayer).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(playerEntityId));
                ((CraftPlayer) onlinePlayer).getHandle().playerConnection.sendPacket(new PacketPlayOutNamedEntitySpawn(((CraftPlayer) player).getHandle()));
                //Ensure that player's rotation and armour remains the same as before entity destroy.
                ((CraftPlayer) onlinePlayer).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityHeadRotation(((CraftPlayer) player).getHandle(), (byte) (((CraftPlayer) player).getHandle().getHeadRotation() * 256F / 360)));
                ((CraftPlayer) onlinePlayer).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityMetadata(player.getEntityId(), ((CraftPlayer) player).getHandle().getDataWatcher(), true));
                ((CraftPlayer) onlinePlayer).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityEquipment(playerEntityId, EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(player.getInventory().getHelmet())));
                ((CraftPlayer) onlinePlayer).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityEquipment(playerEntityId, EnumItemSlot.CHEST, CraftItemStack.asNMSCopy(player.getInventory().getChestplate())));
                ((CraftPlayer) onlinePlayer).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityEquipment(playerEntityId, EnumItemSlot.LEGS, CraftItemStack.asNMSCopy(player.getInventory().getLeggings())));
                ((CraftPlayer) onlinePlayer).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityEquipment(playerEntityId, EnumItemSlot.FEET, CraftItemStack.asNMSCopy(player.getInventory().getBoots())));
                ((CraftPlayer) onlinePlayer).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityEquipment(playerEntityId, EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(player.getInventory().getItemInMainHand())));
                ((CraftPlayer) onlinePlayer).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityEquipment(playerEntityId, EnumItemSlot.OFFHAND, CraftItemStack.asNMSCopy(player.getInventory().getItemInOffHand())));

            } catch (IllegalAccessException | NoSuchFieldException ex) {
                throw new IllegalStateException(ex);
            }
        }
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
        if (inv == null) {
            createInv();
        }
        Player player = (Player) sender;
        //Get users real name from actualNames HashMap.
        String actualName = actualNames.get(player.getUniqueId());
        //Get unformatted current player name.
        String playerName = player.getDisplayName();
        String currentName = playerName.replaceAll("§.", "");
        if (label.equalsIgnoreCase("NameColor") || label.equalsIgnoreCase("Color")) { //color, namecolor
            player.openInventory(inv);
        } else if (label.equalsIgnoreCase("Nickname") || label.equalsIgnoreCase("Nick")) { //nickname, nick
            //Load new nickname from args.
            String nickName;
            if (args.length == 0) {
                nickName = "Reset";
            } else if (args.length == 1) {
                nickName = args[0];
            } else {
                player.sendMessage(chatOutputPrefix + ChatColor.RED + "Invalid command syntax. The correct syntax is /" + label + " §onickname§r.");
                return true;
            }
            if (nickName.equalsIgnoreCase("Reset")) {
                if (actualName != null) {
                    //Apply formatting to real name.
                    String actualNameWithFormatting = playerName.replaceAll(currentName, actualName).replaceAll("§o", "");
                    //Reset the nickname in the chat and tablist.
                    customNames.put(player.getUniqueId(), actualNameWithFormatting);
                    applyFormatting(player, actualNameWithFormatting);
                    setSuffix(player, "");
                    actualNames.remove(player.getUniqueId());
                    player.sendMessage(chatOutputPrefix + "Nickname reset!");
                } else {
                    player.sendMessage(chatOutputPrefix + "No nickname set. Type /" + label + " §onickname§r to set a nickname.");
                }
                return true;
            } else if (nickName.matches("([A-Za-z0-9]|_){3,16}")) { //Regex to check if name entered matches minecraft naming conventions.
                //If not already exists, add users un-nickedname to the actualNames HashMap for future reference.
                actualNames.putIfAbsent(player.getUniqueId(), currentName);
                //Load that name into a var.
                String nickNameWithFormatting = ChatColor.ITALIC + playerName.replace(currentName, nickName);
                if (playerName.substring(0, 2).matches("§([0-9]|[a-f])")) {
                    nickNameWithFormatting = playerName.substring(0, playerName.lastIndexOf("§") + 2) + ChatColor.ITALIC + nickName;
                }
                //Set the new nickname in the chat and tablist.
                player.sendMessage(chatOutputPrefix + "Nickname set to " + nickNameWithFormatting + ChatColor.RESET + "! Type /" + label + " §rreset to reset it at any time.");
                //Update in the customNames HashMap.
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
        Player player = (Player) event.getWhoClicked();
        //Get player name and replace all reset markers to prevent long chain.
        String playerName = player.getDisplayName().replaceAll("§r", "");
        //Get slot # of item clicked.
        int eventSlot = event.getSlot();
        String itemDisplayName = event.getCurrentItem().getItemMeta().getDisplayName();
        if (eventSlot <= 16) { //Colours
            //Get colour to apply to name.
            ChatColor color = ChatColor.values()[eventSlot];
            String reformattedName = color + player.getDisplayName().replace(player.getDisplayName(), color + player.getDisplayName());
            reformattedName = color + playerName.replaceAll("§([0-9]|[a-f])", "");
            customNames.put(player.getUniqueId(), reformattedName);
            applyFormatting(player, reformattedName);
            player.sendMessage(chatOutputPrefix + "Color applied: " + reformattedName);
        } else if (eventSlot >= 27 && eventSlot <= 31) { //Effects
            String effect = itemDisplayName.substring(0, 2);
            String reformattedName = playerName;
            if (playerName.substring(0, 2).matches("§([0-9]|[a-f])")) { //Does name already have colour applied?
                //Put effect after colour (needed to work).
                reformattedName = playerName.substring(0, 2) + effect + playerName.substring(2);
            } else if (!(playerName.contains(effect))) {
                //Otherwise, no effect applied so just add to the front of the name.
                reformattedName = effect + playerName;
            }
            customNames.put(player.getUniqueId(), reformattedName);
            applyFormatting(player, reformattedName);
            player.sendMessage(chatOutputPrefix + "Effect applied: " + reformattedName);
        } else if (eventSlot == 33) { //Reset
            String resetName = playerName.replaceAll("§([0-9]|[a-f]|[k-n])", "");
            customNames.put(player.getUniqueId(), resetName);
            applyFormatting(player, resetName);
            player.sendMessage(chatOutputPrefix + "Name color and effects reset!");
        } else if (eventSlot == 45) { //Close
            player.closeInventory();
        }
        player.closeInventory();
    }
}