package com.yurijware.bukkit.deadlyplates;

import java.net.URI;
import java.net.URISyntaxException;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.getspout.spoutapi.SpoutManager;
import org.getspout.spoutapi.player.SpoutPlayer;
import org.getspout.spoutapi.sound.SoundManager;

/**
 * Class for sending messages
 * @author Yurij
 */
public class Messaging {
	private final DeadlyPlates plugin;
	private static SoundManager sound = null;
	
	public Messaging(DeadlyPlates plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * The cause to use when a message is sent
	 */
	public static enum CAUSE {
		CREATED, CHANGED, REMOVED, DESTROYED, LIST, RELOADED, ERROR_EXISTS, ERROR_LIMIT, ERROR_NOPLATES,
		ERROR_NOTDEADLY, ERROR_NUMBER, ERROR_PERMISSION, ERROR_SYNTAX, ERROR_TARGET
	}
	
	/**
	 * Sends a message with either spout notifications or chat messages
	 * @param sender The receiver of the message
	 * @param cause The cause
	 * @see CAUSE
	 */
	public void send(CommandSender sender, CAUSE cause) {
		Config conf = plugin.getSettings();
		
		Material m = Material.AIR;
		short d = 0;
		int sec = 2;
		String spoutHeader = "DeadlyPlates";
		String spoutMessage = "";
		String chatMessage = "";
		String soundUrl = conf.getSound(cause);
		
		if (!isValidUrl(soundUrl)) { soundUrl = "";  }
		
		switch (cause) {
		case CREATED:
			m = Material.TNT;
			sec = 3;
			spoutMessage = "Plate created";
			chatMessage = "§7Plate created";
			break;
		case CHANGED:
			m = Material.TNT;
			sec = 3;
			spoutMessage = "Plate changed";
			chatMessage = "§7Plate changed";
			break;
		case REMOVED:
			m = Material.STONE_PLATE;
			sec = 3;
			spoutMessage = "Plate removed";
			chatMessage = "§7Plate removed";
			break;
		case RELOADED:
			m = Material.WATER_BUCKET;
			sec = 3;
			spoutMessage = "Config reloaded";
			chatMessage = "§9Deadly plates config reloaded";
			break;
		case DESTROYED:
			m = Material.LAVA_BUCKET;
			spoutMessage = "Plate destroyed";
			chatMessage = "§cPlate destroyed";
		case ERROR_EXISTS:
			m = Material.LAVA_BUCKET;
			spoutMessage = "Already deadly";
			chatMessage = "§cThat plate is already deadly";
		case ERROR_LIMIT:
			m = Material.LAVA_BUCKET;
			spoutMessage = "Limit reached";
			chatMessage = "§cYou have reached your limit";
		case ERROR_NOTDEADLY:
			m = Material.LAVA_BUCKET;
			spoutMessage = "That plate isnt deadly";
			chatMessage = "§cThat plate isnt deadly";
		case ERROR_NUMBER:
			m = Material.LAVA_BUCKET;
			spoutMessage = "Invalid number";
			chatMessage = "§cInvalid number";
		case ERROR_PERMISSION:
			m = Material.LAVA_BUCKET;
			spoutMessage = "You need permission";
			chatMessage = "§cYou need permission";
		case ERROR_SYNTAX:
			m = Material.LAVA_BUCKET;
			spoutMessage = "Command syntax error";
			chatMessage = "§cCommand syntax error";
		case ERROR_TARGET:
			m = Material.LAVA_BUCKET;
			spoutMessage = "Must be wood/stone plate";
			chatMessage = "§cYou must target a stone or wood plate";
			break;
		}
		
		if(!(sender instanceof Player)) {
			sender.sendMessage(chatMessage);
			return;
		}
		
		Player player = (Player) sender;
		boolean spoutEnabledPlayer = conf.getSpoutEnabled() &&
				(((SpoutPlayer) player).isSpoutCraftEnabled());
		
		if (spoutEnabledPlayer && soundUrl.length() > 0) {
			sound = SpoutManager.getSoundManager();
			sound.playCustomSoundEffect(plugin, (SpoutPlayer) player, soundUrl, false);
		}
		
		if (spoutEnabledPlayer && spoutMessage.length() > 0) {
			((SpoutPlayer) player).sendNotification(spoutHeader, spoutMessage, m, d, sec * 1000);
		} else if (chatMessage.length() > 0) {
			player.sendMessage(chatMessage);
		}
	}
	
	/**
	 * Check if a string is a valid url
	 * @param url The url to validate
	 * @return true if valid, otherwise false
	 */
	private static boolean isValidUrl(String url) {
		try {
			new URI(url);
		    return true;
		} catch (URISyntaxException e) {
			return false;
		}
	}
	
}
