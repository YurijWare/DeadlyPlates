package com.yurijware.bukkit.deadlyplates;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import net.sacredlabyrinth.Phaed.PreciousStones.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.ForceFieldManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;

import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import uk.co.oliwali.HawkEye.util.HawkEyeAPI;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.yurijware.bukkit.deadlyplates.Messaging.CAUSE;

/**
 * Holds the configuration
 * @author Yurij
 */
public class Config {
	private static PermissionManager permissionsExHandler;
	private static PermissionHandler permissionsHandler;
	private static WorldGuardPlugin worldguardHandler;
	private static ForceFieldManager preciousstonesHandler;
	
	private static boolean spoutEnabled = false;
	private static boolean hawkEyeEnabled = false;
	private static boolean worldguardEnabled = false;
	private static boolean preciousstonesEnabled = false;
	
	private static HashMap<CAUSE,String> sounds = new HashMap<CAUSE, String>();
	private static HashMap<CAUSE,String> localeChat = new HashMap<CAUSE, String>();
	private static HashMap<CAUSE,String> localeSpout = new HashMap<CAUSE, String>();
	
	private static String lang = "eng";
	private static String langName = "English (Hardcoded)";
	private static String spoutHeader = DeadlyPlates.getInstance().getDescription().getName();
	
	private static int defaultDamage = 5;
	private static int maxDamage = 10;
	private static int plateLimit = 5;
	private static boolean ownerImmune = true;
	private static boolean damageMobs = true;
	private static boolean redstoneDisable = true;
	
	/**
	 * Loads the config file
	 */
	protected static void load() {
		try {
			createDefaultConfiguration("config.yml", "config.yml");
			File configFile = new File(DeadlyPlates.getInstance().getDataFolder(), "config.yml");
			
			Configuration conf = new Configuration(configFile);
			conf.load();
			
			if (!conf.getBoolean("Spout-support", true) && spoutEnabled) { spoutEnabled = false; }
			lang = conf.getString("Language", lang);
			plateLimit = conf.getInt("Plate-limit", plateLimit);
			defaultDamage = conf.getInt("Default-damage", defaultDamage);
			maxDamage = conf.getInt("Max-damage", maxDamage);
			ownerImmune = conf.getBoolean("Owner-is-immune", ownerImmune);
			damageMobs = conf.getBoolean("Damage-animals", damageMobs);
			redstoneDisable = conf.getBoolean("Disable-with-redstone-power", redstoneDisable);
			
			sounds.put(CAUSE.CREATED, conf.getString("Spout.Sounds.Created", ""));
			sounds.put(CAUSE.DESTROYED, conf.getString("Spout.Sounds.Destroyed", ""));
			sounds.put(CAUSE.REMOVED, conf.getString("Spout.Sounds.Destroyed", ""));
			sounds.put(CAUSE.ERROR_LIMIT, conf.getString("Spout.Sounds.Limit-reached", ""));
			sounds.put(CAUSE.ERROR_PERMISSION, conf.getString("Spout.Sounds.Need-permission", ""));
			
			if (defaultDamage > maxDamage) {
				defaultDamage = maxDamage;
				conf.setProperty("Default-damage", defaultDamage);
			}
			
		} catch (Exception e) {
			DeadlyPlates.LogSevere("Failed to read configuration! Reason: " + e.getCause());
		}
		
		loadLang();
	}
	
	/**
	 * Check plugin support
	 */
	protected static void checkPluginSupport() {
		PluginManager pm = Bukkit.getServer().getPluginManager();
		
		Plugin spout = pm.getPlugin("Spout");
		if (spout != null && spout.isEnabled()) {
			spoutEnabled = true;
			String v = spout.getDescription().getVersion();
			DeadlyPlates.LogInfo("Spout detected! Using version " + v);
		}
		
		Plugin worldguard = pm.getPlugin("WorldGuard");
		if (worldguard != null && worldguard.isEnabled()) {
			worldguardEnabled = true;
			String v = worldguard.getDescription().getVersion();
			DeadlyPlates.LogInfo("WorldGuard detected! Using version " + v);
			worldguardHandler = (WorldGuardPlugin) worldguard;
		}
		
		Plugin preciousstones = pm.getPlugin("PreciousStones");
		if (preciousstones != null && preciousstones.isEnabled()) {
			preciousstonesEnabled = true;
			String v = preciousstones.getDescription().getVersion();
			DeadlyPlates.LogInfo("PreciousStones detected! Using version " + v);
			preciousstonesHandler =((PreciousStones) preciousstones).getForceFieldManager();
		}
		
		Plugin hawkeye = pm.getPlugin("HawkEye");
		if (hawkeye != null && hawkeye.isEnabled()) {
			hawkEyeEnabled = true;
			String v = hawkeye.getDescription().getVersion();
			DeadlyPlates.LogInfo("HawkEye detected! Using version " + v);
		}
		
		
		Plugin permissionsEx = pm.getPlugin("PermissionsEx");
		Plugin permissions = pm.getPlugin("Permissions");
		if (permissionsEx != null && permissionsEx.isEnabled()) {
			permissionsExHandler = PermissionsEx.getPermissionManager();
			String v = permissionsEx.getDescription().getVersion();
			DeadlyPlates.LogInfo("PermissionsEx detected! Using version " + v);
			
		} else if (permissions != null && permissions.isEnabled()) {
			permissionsHandler = ((Permissions) permissions).getHandler();
			String v = permissions.getDescription().getVersion();
			DeadlyPlates.LogInfo("Permissions detected! Using version " + v);
		}
		
	}
	
	/**
	 * Loads the selected language
	 */
	private static void loadLang() {
		createDefaultConfiguration("lang/eng.yml", "lang/eng.yml");
		createDefaultConfiguration("lang/swe.yml", "lang/swe.yml");
		
		localeSpout.put(CAUSE.CREATED, "Plate created");
		localeChat.put(CAUSE.CREATED, "§7Plate created");
		
		localeSpout.put(CAUSE.CHANGED, "Plate changed");
		localeChat.put(CAUSE.CHANGED, "§7Plate changed");
		
		localeSpout.put(CAUSE.DESTROYED, "Plate destroyed");
		localeChat.put(CAUSE.DESTROYED, ChatColor.RED + "Plate destroyed");
		
		localeSpout.put(CAUSE.REMOVED, "Plate removed");
		localeChat.put(CAUSE.REMOVED, "§7Plate removed");
		
		localeSpout.put(CAUSE.ERROR_EXISTS, "Already deadly");
		localeChat.put(CAUSE.ERROR_EXISTS, "§cThat plate is already deadly");
		
		localeSpout.put(CAUSE.ERROR_LIMIT, "Limit reached");
		localeChat.put(CAUSE.ERROR_LIMIT, ChatColor.RED + "You have reached your limit");
		
		localeSpout.put(CAUSE.ERROR_NOTDEADLY, "That plate isnt deadly");
		localeChat.put(CAUSE.ERROR_NOTDEADLY, ChatColor.RED + "That plate isnt deadly");
		
		localeSpout.put(CAUSE.ERROR_NUMBER, "Invalid number");
		localeChat.put(CAUSE.ERROR_NUMBER, ChatColor.RED + "Invalid number");
		
		localeSpout.put(CAUSE.ERROR_PERMISSION, "You need permission");
		localeChat.put(CAUSE.ERROR_PERMISSION, ChatColor.RED + "You need permission");
		
		localeSpout.put(CAUSE.ERROR_SYNTAX, "Command syntax error");
		localeChat.put(CAUSE.ERROR_SYNTAX, ChatColor.RED + "Command syntax error");
		
		localeSpout.put(CAUSE.ERROR_TARGET, "Must be wood/stone plate");
		localeChat.put(CAUSE.ERROR_TARGET, ChatColor.RED + "You must target a stone or wood plate");
		
		try {
			File directory = new File(DeadlyPlates.getInstance().getDataFolder(), "lang");
			if (!directory.exists()) {
				directory.mkdirs();
			}
			
			Configuration conf = new Configuration(new File(directory, lang + ".yml"));
			conf.load();
			
			ConfigurationNode lang = conf.getNode("lang");
			if (lang == null) { throw new NullPointerException("No lang node"); }
			
			langName = lang.getString("language");
			spoutHeader = lang.getString("spoutHeader");
			
			localeSpout.put(CAUSE.CREATED, lang.getString("create.spout"));
			localeChat.put(CAUSE.CREATED, lang.getString("create.chat"));
			
			localeSpout.put(CAUSE.CHANGED, lang.getString("change.spout"));
			localeChat.put(CAUSE.CHANGED, lang.getString("change.chat"));
			
			localeSpout.put(CAUSE.DESTROYED, lang.getString("destroy.spout"));
			localeChat.put(CAUSE.DESTROYED, lang.getString("destroy.chat"));
			
			localeSpout.put(CAUSE.REMOVED, lang.getString("remove.spout"));
			localeChat.put(CAUSE.REMOVED, lang.getString("remove.spout"));
			
			localeChat.put(CAUSE.LIST, conf.getString("listTitle.chat"));
			
			localeSpout.put(CAUSE.RELOADED, lang.getString("reloaded.spout"));
			localeChat.put(CAUSE.RELOADED, lang.getString("reloaded.spout"));
			
			ConfigurationNode error = lang.getNode("error");
			if (error == null) { throw new NullPointerException("No error node"); }
			
			localeSpout.put(CAUSE.ERROR_EXISTS, error.getString("exists.spout"));
			localeChat.put(CAUSE.ERROR_PERMISSION, error.getString("exists.chat"));
			
			localeSpout.put(CAUSE.ERROR_LIMIT, error.getString("limit.spout"));
			localeChat.put(CAUSE.ERROR_LIMIT, error.getString("limit.chat"));
			
			localeChat.put(CAUSE.ERROR_NOPLATES, error.getString("noplates.chat"));
			
			localeSpout.put(CAUSE.ERROR_NOTDEADLY, error.getString("notdeadly.spout"));
			localeChat.put(CAUSE.ERROR_NOTDEADLY, error.getString("notdeadly.chat"));
			
			localeSpout.put(CAUSE.ERROR_NUMBER, error.getString("number.spout"));
			localeChat.put(CAUSE.ERROR_NUMBER, error.getString("number.chat"));
			
			localeSpout.put(CAUSE.ERROR_PERMISSION, error.getString("permission.spout"));
			localeChat.put(CAUSE.ERROR_PERMISSION, error.getString("permission.chat"));
			
			localeSpout.put(CAUSE.ERROR_SYNTAX, error.getString("syntax.spout"));
			localeChat.put(CAUSE.ERROR_SYNTAX, error.getString("syntax.chat"));

			localeSpout.put(CAUSE.ERROR_TARGET, error.getString("target.spout"));
			localeChat.put(CAUSE.ERROR_TARGET, error.getString("target.chat"));
			
		} catch (Exception e) {
			DeadlyPlates.LogSevere("Failed to load language! Defaulting to english");
		}
		
		DeadlyPlates.LogInfo("Language: " + langName);
	}
	
	/**
	 * Extract the specified resource from the jar file
	 * @param name Path to the file relative to the resources dir
	 * @param filename Path to the file relative to the plugins datafolder
	 */
	private static void createDefaultConfiguration(String name, String filename) {
		File configFile = new File(DeadlyPlates.getInstance().getDataFolder(), filename);
		File configFolder = new File(configFile.getParent());
		
		if (configFile.exists()) { return; }
		if (!configFolder.exists()) {
			configFolder.mkdir();
		}
		
		InputStream input = DeadlyPlates.class.getResourceAsStream("/resources/" + name);
		if (input != null) {
			FileOutputStream output = null;
			try {
				output = new FileOutputStream(configFile);
				byte[] buf = new byte[8192];
				int length = 0;
				while ((length = input.read(buf)) > 0) {
					output.write(buf, 0, length);
				}
				
				DeadlyPlates.LogInfo("Created configuration file: " + name);
			} catch (IOException e) {
				e.printStackTrace();
				try {
					if (input != null)
						input.close();
				} catch (IOException localIOException) { }
				try {
					if (output != null)
						output.close();
				} catch (IOException localIOException) { }
			} finally {
				try {
					if (input != null)
						input.close();
				} catch (IOException localIOException) { }
				try {
					if (output != null)
						output.close();
				} catch (IOException localIOException) { }
			}
		}
	}
	
	/**
	 * Check permissions
	 * @param sender
	 * @param node The permission node to check for
	 */
	protected static boolean checkPermissions(CommandSender sender, String node) {
		if (permissionsExHandler != null && sender instanceof Player) {
			return permissionsExHandler.has((Player) sender, node);
		} else if (permissionsHandler != null && sender instanceof Player) {
			return permissionsHandler.has((Player) sender, node);
		} else {
			return sender.hasPermission(node);
		}
	}
	
	/**
	 * Log stuff with HawkEye
	 * @param player The player doing the stuff
	 * @param plate The plate regarding the log entry
	 * @param cause The casue of the log entry.
	 * Can only be CREATED, CHANGED, REMOVED or DESTROYED
	 * @param loc The location where the stuff happend
	 * @see CAUSE
	 */
	protected static void logHawkEye(Player player, Plate plate, CAUSE cause) {
		if (!hawkEyeEnabled) { return; }
		DeadlyPlates p = DeadlyPlates.getInstance();
		
		String action = "";
		String msg = "";
		Location loc = plate.getLocation();
		
		switch(cause) {
		case CREATED:
			action = "Created";
			msg = "Deadly plate created";
			break;
		case CHANGED:
			action = "Changed";
			msg = "Deadly plate damage changed";
			break;
		case REMOVED:
			action = "Removed";
			msg = "Deadly plate removed";
			break;
		case DESTROYED:
			action = "Destroyed";
			String owner = plate.getPlayer();
			
			if (owner.equals(player.getName())) {
				msg = "Deadly plate destroyed";
			} else {
				msg = "Deadly plate destroyed. Owner: " + owner;
			}
			break;
		}
		
		if (!action.equals("")) { HawkEyeAPI.addCustomEntry(p, action, player, loc, msg); }
		
	}
	
	/**
	 * Check with protection plugins if the player can build on the block
	 * @param player The player doing something with the block
	 * @param block The block to check
	 */
	protected static boolean isProtected(Player player, Block block) {
		boolean wg = true;
		boolean ps = true;
		
		if (preciousstonesEnabled) {
			ps = preciousstonesHandler.getSourceField(block.getLocation(),
					FieldFlag.PREVENT_DESTROY) == null;
		}
		
		if (worldguardEnabled) {
			wg = worldguardHandler.canBuild(player, block);
		}
		
		return wg && ps;
	}
	
	
	/**
	 * Check if spout is loaded and enabled
	 */
	protected static boolean getSpoutEnabled() {
		return spoutEnabled;
	}
	
	
	/**
	 * Get the limit of plates
	 */
	public static int getPlateLimit() {
		return plateLimit;
	}
	
	
	
	/**
	 * Get the default damage
	 */
	public static int getDefaultDamage() {
		return defaultDamage;
	}
	
	
	/**
	 * Get the max damage
	 */
	public static int getMaxDamage() {
		return maxDamage;
	}
	
	
	/**
	 * Check if the owner of a plate should be immune
	 */
	public static boolean getOwnerImmune() {
		return ownerImmune;
	}
	
	
	/**
	 * Check if mobs should be damaged by plates
	 */
	public static boolean getDamageMobs() {
		return damageMobs;
	}
	
	
	/**
	 * Check if redstone power should disable plates
	 */
	public static boolean getRedstoneDisable() {
		return redstoneDisable;
	}
	
	protected static String getSpoutHeader() {
		return spoutHeader;
	}
	
	protected static String getLangChat(CAUSE cause) {
		if (localeChat.containsKey(cause)) {
			return localeChat.get(cause);
		}
		return "";
	}
	
	protected static String getLangSpout(CAUSE cause) {
		if (localeSpout.containsKey(cause)) {
			return localeSpout.get(cause);
		}
		return "";
	}
	
	protected static String getSound(CAUSE cause) {
		if (sounds.containsKey(cause)) {
			return sounds.get(cause);
		}
		return "";
	}
	
}
