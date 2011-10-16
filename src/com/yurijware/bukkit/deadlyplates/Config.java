package com.yurijware.bukkit.deadlyplates;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.logging.Level;

import net.sacredlabyrinth.Phaed.PreciousStones.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.ForceFieldManager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

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
	private final DeadlyPlates plugin;
	
	private FileConfiguration conf;
	
	private PermissionManager permissionsExHandler;
	private PermissionHandler permissionsHandler;
	private WorldGuardPlugin worldguardHandler;
	private ForceFieldManager preciousstonesHandler;
	
	private boolean spoutEnabled = false;
	private boolean hawkEyeEnabled = false;
	private boolean worldguardEnabled = false;
	private boolean preciousstonesEnabled = false;
	
	private HashMap<CAUSE,String> sounds = new HashMap<CAUSE, String>();
	
	private int defaultDamage = 5;
	private int maxDamage = 10;
	private int plateLimit = 5;
	private boolean ownerImmune = true;
	private boolean damageMobs = true;
	private boolean redstoneDisable = true;
	
	public Config(DeadlyPlates plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * Loads the config file
	 */
	protected void load() {
		plugin.getConfig().options().copyDefaults(true);
		plugin.saveConfig();
		
		conf = plugin.getConfig();
		conf.options().header("DeadlyPlates config");
		
		checkPluginSupport();
		
		try {
			createDefaultConfiguration("config.yml", "config.yml");
			
			if (!conf.getBoolean("Spout-support", true) && spoutEnabled) { spoutEnabled = false; }
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
				conf.set("Default-damage", defaultDamage);
			}
			
			plugin.saveConfig();
			
		} catch (Exception e) {
			plugin.Log(Level.SEVERE, "Failed to read configuration! Reason: " + e.getCause());
		}
	}
	
	/**
	 * Check plugin support
	 */
	private void checkPluginSupport() {
		PluginManager pm = Bukkit.getServer().getPluginManager();
		
		Plugin spout = pm.getPlugin("Spout");
		if (spout != null && spout.isEnabled()) {
			spoutEnabled = true;
			String v = spout.getDescription().getVersion();
			plugin.Log(Level.INFO, "Spout detected! Using version " + v);
		}
		
		Plugin worldguard = pm.getPlugin("WorldGuard");
		if (worldguard != null && worldguard.isEnabled()) {
			worldguardEnabled = true;
			worldguardHandler = (WorldGuardPlugin) worldguard;
			String v = worldguard.getDescription().getVersion();
			plugin.Log(Level.INFO, "WorldGuard detected! Using version " + v);
		}
		
		Plugin preciousstones = pm.getPlugin("PreciousStones");
		if (preciousstones != null && preciousstones.isEnabled()) {
			preciousstonesEnabled = true;
			preciousstonesHandler =((PreciousStones) preciousstones).getForceFieldManager();
			String v = preciousstones.getDescription().getVersion();
			plugin.Log(Level.INFO, "PreciousStones detected! Using version " + v);
		}
		
		Plugin hawkeye = pm.getPlugin("HawkEye");
		if (hawkeye != null && hawkeye.isEnabled()) {
			hawkEyeEnabled = true;
			String v = hawkeye.getDescription().getVersion();
			plugin.Log(Level.INFO, "HawkEye detected! Using version " + v);
		}
		
		
		Plugin permissionsEx = pm.getPlugin("PermissionsEx");
		Plugin permissions = pm.getPlugin("Permissions");
		if (permissionsEx != null && permissionsEx.isEnabled()) {
			permissionsExHandler = PermissionsEx.getPermissionManager();
			String v = permissionsEx.getDescription().getVersion();
			plugin.Log(Level.INFO, "PermissionsEx detected! Using version " + v);
			
		} else if (permissions != null && permissions.isEnabled()) {
			permissionsHandler = ((Permissions) permissions).getHandler();
			String v = permissions.getDescription().getVersion();
			plugin.Log(Level.INFO, "Permissions detected! Using version " + v);
		}
		
	}
	
	/**
	 * Extract the specified resource from the jar file
	 * @param name Path to the file relative to the resources dir
	 * @param filename Path to the file relative to the plugins datafolder
	 */
	private void createDefaultConfiguration(String name, String filename) {
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
				
				plugin.Log(Level.INFO, "Created configuration file: " + name);
			} catch (IOException e) {
				e.printStackTrace();
				try {
						input.close();
				} catch (IOException localIOException) { }
				try {
					if (output != null)
						output.close();
				} catch (IOException localIOException) { }
			} finally {
				try {
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
	public boolean checkPermissions(CommandSender sender, String node) {
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
	 * @param cause The cause of the log entry.
	 * Can only be CREATED, CHANGED, REMOVED or DESTROYED
	 * @see CAUSE
	 */
	public void logHawkEye(Player player, Plate plate, CAUSE cause) {
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
	public boolean isProtected(Player player, Block block) {
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
	public boolean getSpoutEnabled() {
		return spoutEnabled;
	}
	
	/**
	 * Get the limit of plates
	 */
	public int getPlateLimit() {
		return plateLimit;
	}
	
	/**
	 * Get the default damage
	 */
	public int getDefaultDamage() {
		return defaultDamage;
	}
	
	/**
	 * Get the max damage
	 */
	public int getMaxDamage() {
		return maxDamage;
	}
	
	/**
	 * Check if the owner of a plate should be immune
	 */
	public boolean getOwnerImmune() {
		return ownerImmune;
	}
	
	/**
	 * Check if mobs should be damaged by plates
	 */
	public boolean getDamageMobs() {
		return damageMobs;
	}
	
	/**
	 * Check if redstone power should disable plates
	 */
	public boolean getRedstoneDisable() {
		return redstoneDisable;
	}
	
	protected String getSound(CAUSE cause) {
		if (sounds.containsKey(cause)) {
			return sounds.get(cause);
		}
		return "";
	}
	
}
