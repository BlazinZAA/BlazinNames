// This version is obsolete, the next version will contain much more efficient code

package net.viedantmc.BlazinNames;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

public class Main extends JavaPlugin implements Listener{
	public DataManager data;
	public Inventory inv;

	@Override
	public void onEnable() {
		this.data = new DataManager(this);
		this.getServer().getPluginManager().registerEvents(this, this);
		createInv();
		this.saveDefaultConfig();
		  if (this.getConfig().contains("customNames")) {
		    this.getConfig().getConfigurationSection("customNames").getKeys(false).forEach(key->
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
		if(label.equalsIgnoreCase("NameColor") || label.equalsIgnoreCase("Color")) {
			if(!(sender instanceof Player)) {
				sender.sendMessage("Sure lets color the console! Because that makes sense!... Idiot.");
			return true;
			}
			Player player = (Player) sender;
			player.openInventory(inv);
			return true;
		}
		Player player = (Player) sender;
		String Playername = player.getDisplayName();
		//String nickColors = Playername.substring(0, 2); // Storing the Color code
		String nickColors = "";
		String nickColors2 = "";
		if (Playername.length() > 4) {
		nickColors2 = Playername.substring(2,4);
		}
		String BoldEffect = "";
		if(!(Playername.contains("~"))) {
			if (Playername.contains("§")) {
				nickColors = Playername.substring(0,2);
				if (nickColors.contains("§l"))  {
					BoldEffect = "§l";
				}
			}
		}
			if (Playername.contains("~")) {
				if (Playername.contains("§")) {
					nickColors = Playername.substring(0,2);
					if (nickColors.contains("§l"))  {
						BoldEffect = "§l";
						}
					else if (nickColors2.contains("§l")) {
						BoldEffect = "§l";
					}
				}
			}
		
		String nick = "";
		for (String arg : args) {
			nick += arg + " ";
		}


		if(label.equalsIgnoreCase("nick") || label.equalsIgnoreCase("nickname")) {
			if(!(sender instanceof Player)) {
				sender.sendMessage("Console wants a nickname now? Too bad.");
				return true;
			}
            if(!(args.length == 0)){
            	if ((nickColors.contains("§"))) { 
            		// Nickname + Set color
            		player.setDisplayName(nickColors + BoldEffect+ "~" + nick+ ChatColor.RESET);
            		sender.sendMessage(ChatColor.GOLD + "Your nickname has been set to " + player.getDisplayName());
            		customNames.put(player.getUniqueId(), player.getDisplayName());
            		return true;
            	}
            	// No color :(
			player.setDisplayName(nickColors+BoldEffect+ "~" + nick);
			sender.sendMessage(ChatColor.GOLD+ "Your nickname has been set to " + player.getDisplayName());
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
		
		if (!event.getClickedInventory().equals(inv)) {
		
			return;
			
		}
		if(event.getCurrentItem() == null) return;
		if(event.getCurrentItem().getItemMeta() == null) return;
		if(event.getCurrentItem().getItemMeta().getDisplayName() == null) return;
		//if(event.getCurrentItem().getItemMeta().getLore() == null) return;
		
		event.setCancelled(true);		
		Player player = (Player) event.getWhoClicked();
		String Playername = player.getDisplayName();
		String nickColors = "";
		if (Playername.substring(0,2).contains("§")) {
			nickColors = Playername.substring(0,2);
		}
		String nickColors2 = Playername.substring(2,4);
		String BoldEffect = "";
		if (nickColors.contains("§l")) {
			BoldEffect = "&l";
		}
		else if (nickColors2.contains("§l")) {
			BoldEffect = "&l";
		}
		
		String nick = ChatColor.stripColor(player.getDisplayName());
		

		
		if(event.getSlot() == 0) {
			//blue team
			player.setDisplayName(player.getDisplayName().replaceAll("&", "2"));
			player.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&1" + BoldEffect + nick + "&r"));
			player.setPlayerListName(ChatColor.translateAlternateColorCodes('&', "&1" + BoldEffect + nick + "&r"));
			player.closeInventory();
			player.sendMessage(ChatColor.BOLD + "" + ChatColor.DARK_BLUE + "You set your name color to dark blue!");
			nick.replace("~", "");
			if (!(player.getDisplayName().contains("~")))
				if(!nick.equals(player.getName()))
					player.setDisplayName("~" + player.getDisplayName());	
			customNames.put(player.getUniqueId(), player.getDisplayName());
		}
		if(event.getSlot() == 1) {
			//blue team
			player.setDisplayName(player.getDisplayName().replaceAll("&", "2"));
			player.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&9" + BoldEffect +nick + "&r"));
			player.setPlayerListName(ChatColor.translateAlternateColorCodes('&', "&9" + BoldEffect + nick + "&r"));
			player.closeInventory();
			player.sendMessage(ChatColor.BOLD + "" + ChatColor.BLUE + "You set your name color to blue!");
			nick.replace("~", "");
			if (!(player.getDisplayName().contains("~")))
				if(!nick.equals(player.getName()))
					player.setDisplayName("~" + player.getDisplayName());		
			customNames.put(player.getUniqueId(), player.getDisplayName());
		}
		if(event.getSlot() == 2) {
			//blue team
			player.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&4" +BoldEffect + nick + "&r"));
			player.setPlayerListName(ChatColor.translateAlternateColorCodes('&', "&4" + BoldEffect + nick + "&r"));
			player.closeInventory();
			player.sendMessage(ChatColor.BOLD + "" + ChatColor.DARK_RED + "You set your name color to Dark Red");
			nick.replace("~", "");
			if (!(player.getDisplayName().contains("~")))
				if(!nick.equals(player.getName()))
					player.setDisplayName("~" + player.getDisplayName());	
			customNames.put(player.getUniqueId(), player.getDisplayName());
		}
		if(event.getSlot() == 3) {
			player.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c" +BoldEffect + nick + "&r"));
			player.setPlayerListName(ChatColor.translateAlternateColorCodes('&', "&c" + BoldEffect + nick + "&r"));
			player.closeInventory();
			player.sendMessage(ChatColor.BOLD + "" + ChatColor.RED + "You set your name color to Red!");
			nick.replace("~", "");
			if (!(player.getDisplayName().contains("~")))
				if(!nick.equals(player.getName()))
					player.setDisplayName("~" + player.getDisplayName());	
			customNames.put(player.getUniqueId(), player.getDisplayName());
		}
		if(event.getSlot() == 4) {
			player.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&d" +BoldEffect + nick + "&r"));
			player.setPlayerListName(ChatColor.translateAlternateColorCodes('&', "&d" + BoldEffect + nick + "&r"));
			player.closeInventory();
			player.sendMessage(ChatColor.BOLD + "" + ChatColor.LIGHT_PURPLE + "You set your name color to Pink!");
			nick.replace("~", "");
			if (!(player.getDisplayName().contains("~")))
				if(!nick.equals(player.getName()))
					player.setDisplayName("~" + player.getDisplayName());	
			customNames.put(player.getUniqueId(), player.getDisplayName());
		}
		if(event.getSlot() == 5) {
			player.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&5" +BoldEffect + nick + "&r"));
			player.setPlayerListName(ChatColor.translateAlternateColorCodes('&', "&5" + BoldEffect + nick + "&r"));
			player.closeInventory();
			player.sendMessage(ChatColor.BOLD + "" + ChatColor.DARK_PURPLE + "You set your name color to Purple!");
			nick.replace("~", "");
			if (!(player.getDisplayName().contains("~")))
				if(!nick.equals(player.getName()))
					player.setDisplayName("~" + player.getDisplayName());	
			customNames.put(player.getUniqueId(), player.getDisplayName());
		}
		if(event.getSlot() == 6) {
			player.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&2" +BoldEffect + nick + "&r"));
			player.setPlayerListName(ChatColor.translateAlternateColorCodes('&', "&2" + BoldEffect + nick + "&r"));
			player.closeInventory();
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2"+ "You set your name color to Light Green!"));
			nick.replace("~", "");
			if (!(player.getDisplayName().contains("~")))
				if(!nick.equals(player.getName()))
					player.setDisplayName("~" + player.getDisplayName());	
			customNames.put(player.getUniqueId(), player.getDisplayName());
		}
		if(event.getSlot() == 7) {
			player.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&2" + BoldEffect +nick + "&r"));
			player.setPlayerListName(ChatColor.translateAlternateColorCodes('&', "&2" + BoldEffect + nick + "&r"));
			player.closeInventory();
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2"+ "You set your name color to Dark Green!"));
			nick.replace("~", "");
			if (!(player.getDisplayName().contains("~")))
				if(!nick.equals(player.getName()))
					player.setDisplayName("~" + player.getDisplayName());	
			customNames.put(player.getUniqueId(), player.getDisplayName());
		}
		if(event.getSlot() == 8) {
			player.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&3" +BoldEffect + nick + "&r"));
			player.setPlayerListName(ChatColor.translateAlternateColorCodes('&', "&3" + BoldEffect + nick + "&r"));
			player.closeInventory();
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3"+ "You set your name color to Cyan!"));
			nick.replace("~", "");
			if (!(player.getDisplayName().contains("~")))
				if(!nick.equals(player.getName()))
					player.setDisplayName("~" + player.getDisplayName());	
			customNames.put(player.getUniqueId(), player.getDisplayName());
		}
		if(event.getSlot() == 9) {
			player.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b" +BoldEffect + nick + "&r"));
			player.setPlayerListName(ChatColor.translateAlternateColorCodes('&', "&b" + BoldEffect + nick + "&r"));
			player.closeInventory();
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b"+ "You set your name color to Light Blue!"));
			nick.replace("~", "");
			if (!(player.getDisplayName().contains("~")))
				if(!nick.equals(player.getName()))
					player.setDisplayName("~" + player.getDisplayName());	
			customNames.put(player.getUniqueId(), player.getDisplayName());
		}
		if(event.getSlot() == 10) {
			player.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6" + BoldEffect +nick + "&r"));
			player.setPlayerListName(ChatColor.translateAlternateColorCodes('&', "&6" + BoldEffect + nick + "&r"));
			player.closeInventory();
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6"+ "You set your name color to Orange!"));
			nick.replace("~", "");
			if (!(player.getDisplayName().contains("~")))
				if(!nick.equals(player.getName()))
					player.setDisplayName("~" + player.getDisplayName());	
			customNames.put(player.getUniqueId(), player.getDisplayName());
		}
		if(event.getSlot()== 11) {
			player.setDisplayName(org.bukkit.ChatColor.translateAlternateColorCodes('&',"&e" +BoldEffect + nick + "&r" ));
			player.setPlayerListName(ChatColor.translateAlternateColorCodes('&', "&e" + BoldEffect + nick + "&r"));
			player.closeInventory();
			player.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&',"&e" + "You set your name color to Yellow!"));
			nick.replace("~", "");
			if (!(player.getDisplayName().contains("~")))
				if(!nick.equals(player.getName()))
					player.setDisplayName("~" + player.getDisplayName());	
			customNames.put(player.getUniqueId(), player.getDisplayName());
		}
		if(event.getSlot()== 12) {
			player.setDisplayName(org.bukkit.ChatColor.translateAlternateColorCodes('&',"&0" + BoldEffect +nick + "&r" ));
			player.setPlayerListName(ChatColor.translateAlternateColorCodes('&', "&0" + BoldEffect + nick + "&r"));
			player.closeInventory();
			player.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&',"&0" + "You set your name color to Black!"));
			nick.replace("~", "");
			if (!(player.getDisplayName().contains("~")))
				if(!nick.equals(player.getName()))
					player.setDisplayName("~" + player.getDisplayName());	
			customNames.put(player.getUniqueId(), player.getDisplayName());
		}
		if(event.getSlot()== 13) {
			player.setDisplayName(org.bukkit.ChatColor.translateAlternateColorCodes('&',"&8" + BoldEffect +nick + "&r" ));
			player.setPlayerListName(ChatColor.translateAlternateColorCodes('&', "&8" + BoldEffect + nick + "&r"));
			player.closeInventory();
			player.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&',"&8" + "You set your name color to Gray!"));
			nick.replace("~", "");
			if (!(player.getDisplayName().contains("~")))
				if(!nick.equals(player.getName()))
					player.setDisplayName("~" + player.getDisplayName());	
			customNames.put(player.getUniqueId(), player.getDisplayName());
		}
		if(event.getSlot()== 14) {
			player.setDisplayName(org.bukkit.ChatColor.translateAlternateColorCodes('&',"&7" +BoldEffect + nick + "&r" ));
			player.setPlayerListName(ChatColor.translateAlternateColorCodes('&', "&7" + BoldEffect + nick + "&r"));
			player.closeInventory();
			player.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&',"&7" + "You set your name color to Light Gray"));
			nick.replace("~", "");
			if (!(player.getDisplayName().contains("~")))
				if(!nick.equals(player.getName()))
					player.setDisplayName("~" + player.getDisplayName());	
			customNames.put(player.getUniqueId(), player.getDisplayName());
		}
		
		if(event.getSlot()== 15) {
			if ((nickColors.contains("§"))) {
			player.setDisplayName(org.bukkit.ChatColor.translateAlternateColorCodes('&', nickColors + "&l" + nick + "&r" ));
			player.setPlayerListName(ChatColor.translateAlternateColorCodes('&', BoldEffect + nick + "&r"));
			player.closeInventory();
			player.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', nickColors + "&l"+ "Your name is now Bold!"));
			nick.replace("~", "");
			if (!(player.getDisplayName().contains("~")))
				if(!nick.equals(player.getName()))
					player.setDisplayName("~" + player.getDisplayName());		
			customNames.put(player.getUniqueId(), player.getDisplayName());
		}
			else {
			player.setDisplayName(org.bukkit.ChatColor.translateAlternateColorCodes('&', "&l" + nick + "&r"));
			player.setPlayerListName(ChatColor.translateAlternateColorCodes('&', "&l" + BoldEffect + nick + "&r"));
			player.closeInventory();
			player.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', "&l"+ "Your name is now Bold!"));
			nick.replace("~", "");
			if (!(player.getDisplayName().contains("~")))
				if(!nick.equals(player.getName()))
					player.setDisplayName("~" + player.getDisplayName());	
			customNames.put(player.getUniqueId(), player.getDisplayName());
			}
		}
		if(event.getSlot()== 17) {
			player.setDisplayName(org.bukkit.ChatColor.translateAlternateColorCodes('&',player.getDisplayName()));
			player.setPlayerListName(ChatColor.translateAlternateColorCodes('&', BoldEffect + nick + "&r"));
			player.closeInventory();
			nick.replace("~", "");
			if (!(player.getDisplayName().contains("~")))
				if(!nick.equals(player.getName()))
					player.setDisplayName("~" + player.getDisplayName());	
			customNames.put(player.getUniqueId(), player.getDisplayName());
		}
		
	}
	
	
	
	
	public void createInv() {
		inv = Bukkit.createInventory(null, 18, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Select Name Color");
		ItemStack item = new ItemStack(Material.BLUE_CONCRETE);
		ItemMeta meta = item.getItemMeta();
		
        meta.setDisplayName(ChatColor.DARK_BLUE +"" + ChatColor.BOLD +"Dark Blue");
        List<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GRAY +"Set your name color!");
        meta.setLore(lore);
        item.setItemMeta(meta);
        inv.setItem(0, item);
        
        item.setType(Material.LIGHT_BLUE_CONCRETE);
        meta.setDisplayName(ChatColor.BLUE + "" +ChatColor.BOLD +"Blue");
        item.setItemMeta(meta);
        inv.setItem(1, item);
        
        item.setType(Material.RED_CONCRETE);
        meta.setDisplayName(ChatColor.DARK_RED+ "" + ChatColor.BOLD+ "DARK RED");
        item.setItemMeta(meta);
        inv.setItem(2, item);
        
        item.setType(Material.RED_CONCRETE_POWDER);
        meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "RED");
        item.setItemMeta(meta);
        inv.setItem(3, item);
        
        item.setType(Material.PINK_CONCRETE);
        meta.setDisplayName(ChatColor.LIGHT_PURPLE+ "" + ChatColor.BOLD+  "Pink");
        item.setItemMeta(meta);
        inv.setItem(4, item);
        
        item.setType(Material.PURPLE_CONCRETE);
        meta.setDisplayName(ChatColor.DARK_PURPLE+"" + ChatColor.BOLD + "Purple");
        item.setItemMeta(meta);
        inv.setItem(5, item);
        
        item.setType(Material.LIME_CONCRETE);
        meta.setDisplayName(ChatColor.GREEN+"" + ChatColor.BOLD+ "Light Green");
        item.setItemMeta(meta);
        inv.setItem(6, item);
        
        item.setType(Material.GREEN_CONCRETE);
        meta.setDisplayName(ChatColor.DARK_GREEN+ ""+ ChatColor.BOLD + "Dark Green");
        item.setItemMeta(meta);
        inv.setItem(7, item);
        
        item.setType(Material.CYAN_CONCRETE);
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&3"+ "&l" + "Cyan"));
        item.setItemMeta(meta);
        inv.setItem(8, item);
        
        item.setType(Material.LIGHT_BLUE_CONCRETE_POWDER);
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b" + "&l"+"Light Blue"));
        item.setItemMeta(meta);
        inv.setItem(9, item);
        
        item.setType(Material.ORANGE_CONCRETE);
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6"+ "&l"+ "Orange"));
        item.setItemMeta(meta);
        inv.setItem(10, item);
        
        item.setType(Material.YELLOW_CONCRETE);
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e"+ "&l"+ "Yellow"));
        item.setItemMeta(meta);
        inv.setItem(11, item);
        
        item.setType(Material.BLACK_CONCRETE);
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&0"+ "&l"+ "Black"));
        item.setItemMeta(meta);
        inv.setItem(12, item);
        
        item.setType(Material.GRAY_CONCRETE);
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&8"+ "&l"+ "Gray"));
        item.setItemMeta(meta);
        inv.setItem(13, item);
        
        item.setType(Material.LIGHT_GRAY_CONCRETE);
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&7"+ "&l"+ "Light gray"));
        item.setItemMeta(meta);
        inv.setItem(14, item);
        
        item.setType(Material.ENDER_CHEST);
        meta.setDisplayName(ChatColor.BOLD + "BOLD");
        lore.clear();
        lore.add("Makes your name Bold!");
        meta.setLore(lore);
        item.setItemMeta(meta);
        inv.setItem(15, item);
         
        item.setType(Material.BARRIER);
        meta.setDisplayName(ChatColor.DARK_GRAY + "Close Menu");
        lore.clear();
        meta.setLore(lore);
        item.setItemMeta(meta);
        inv.setItem(17, item);
	}
}
