package com.yurijware.bukkit.deadlyplates;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.PersistenceException;

import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.yurijware.bukkit.deadlyplates.listeners.BlockListener;
import com.yurijware.bukkit.deadlyplates.listeners.EntityListener;
import com.yurijware.bukkit.deadlyplates.listeners.PlayerListener;

/**
 * Main class
 * @author Yurij
 */
public class DeadlyPlates extends JavaPlugin {
	private static DeadlyPlates plugin = null;
	
	private final Logger log = Logger.getLogger("Minecraft");
	private String logPrefix = "[DeadlyPlates] ";
	private Config conf = new Config(this);
	private Messaging msg = new Messaging(this);
	
	public DeadlyPlates() {
		
	}
	
	public static DeadlyPlates getInstance() {
		return plugin;
	}
	
	@Override
	public void onDisable() {
		Log(Level.INFO, "Plugin disabled!");
	}
	
	@Override
	public void onEnable() {
		plugin = this;
		
		setupDatabase();
		conf.load();
		
		this.getCommand("DeadlyPlates").setExecutor(new Commands(this));
		
		PluginManager pm = getServer().getPluginManager();
		
		pm.registerEvent(Event.Type.BLOCK_BREAK, new BlockListener(this), Priority.Monitor, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, new PlayerListener(this), Priority.Highest, this);
		pm.registerEvent(Event.Type.ENTITY_INTERACT, new EntityListener(this), Priority.Highest, this);
		
		Log(Level.INFO, "Version " + getDescription().getVersion() + " is enabled!");
	}
	
	private void setupDatabase() {
		try {
			getDatabase().find(Plate.class).findRowCount();
		} catch (PersistenceException ex) {
			Log(Level.INFO, "Setting up database");
			installDDL();
		}
	}
	
	public void Log(Level level, String ... messages) {
		for (String message : messages) {
			log.log(level, logPrefix + message);
		}
	}
	
	@Override
	public List<Class<?>> getDatabaseClasses() {
		List<Class<?>> list = new ArrayList<Class<?>>();
		list.add(Plate.class);
		return list;
	}
	
	public Config getSettings() {
		return conf;
	}
	
	public Messaging getMessaging() {
		return msg;
	}
	
}
