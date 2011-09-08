package com.yurijware.bukkit.deadlyplates;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.PersistenceException;

import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class
 * @author Yurij
 */
public class DeadlyPlates extends JavaPlugin {
	private static final Logger log = Logger.getLogger("Minecraft");
	private static PluginDescriptionFile pdfFile = null;
	private static String logPrefix = null;
	private static DeadlyPlates plugin = null;
	
	protected static DeadlyPlates getInstance() {
		return plugin;
	}
	
	public void onDisable() {
		LogInfo("Plugin disabled!");
	}
	
	public void onEnable() {
		plugin = this;
		pdfFile = this.getDescription();
		logPrefix = "[" + pdfFile.getName() + "] ";
		
		setupDatabase();
		Config.checkPluginSupport();
		Config.load();
		
		this.getCommand("DeadlyPlates").setExecutor(new Commands(this));
		
		PluginManager pm = getServer().getPluginManager();
		
		pm.registerEvent(Event.Type.BLOCK_BREAK, new BlockListener(), Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, new PlayerListener(), Priority.Normal, this);
		
		if (Config.getDamageMobs()) {
			pm.registerEvent(Event.Type.ENTITY_INTERACT, new EntityListener(), Priority.Normal, this);
		}
		
		LogInfo("Version " + pdfFile.getVersion() + " is enabled!");
	}
	
	private void setupDatabase() {
		try {
			getDatabase().find(Plate.class).findRowCount();
		} catch (PersistenceException ex) {
			LogInfo("Setting up database");
			installDDL();
        }
    }
	
	protected static void LogInfo(String msg) {
		log.info(logPrefix + msg);
	}
	
	protected static void LogWarning(String msg) {
		log.warning(logPrefix + msg);
	}
	
	protected static void LogSevere(String msg) {
		log.severe(logPrefix + msg);
	}
	
	@Override
    public List<Class<?>> getDatabaseClasses() {
        List<Class<?>> list = new ArrayList<Class<?>>();
        list.add(Plate.class);
        return list;
    }
	
}
