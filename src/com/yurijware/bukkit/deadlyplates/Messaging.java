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
	private static SoundManager sound = null;
	
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
	 * @param casue The cause
	 * @see CAUSE
	 */
	protected static void sendMessage(CommandSender sender, CAUSE cause) {
		Material m = Material.AIR;
		short d = 0;
		int sec = 2;
		String spoutHeader = Config.getSpoutHeader();
		String spoutMessage = "";
		String chatMessage = "";
		String soundUrl = Config.getSound(cause);
		
		if (!isValidUrl(soundUrl)) { soundUrl = "";  }
		
		spoutMessage = Config.getLangSpout(cause);
		chatMessage = Config.getLangChat(cause);
		
		switch (cause) {
		case CREATED:
		case CHANGED:
			m = Material.TNT;
			sec = 3;
			break;
		case REMOVED:
			m = Material.STONE_PLATE;
			sec = 3;
			break;
		case RELOADED:
			m = Material.WATER_BUCKET;
			sec = 3;
			break;
		case DESTROYED:
		case ERROR_EXISTS:
		case ERROR_LIMIT:
		case ERROR_NOTDEADLY:
		case ERROR_NUMBER:
		case ERROR_PERMISSION:
		case ERROR_SYNTAX:
		case ERROR_TARGET:
			m = Material.LAVA_BUCKET;
			break;
		}
		
		if(!(sender instanceof Player)) {
			sender.sendMessage(chatMessage);
			return;
		}
		
		Player player = (Player) sender;
		boolean spoutEnabledPlayer = Config.getSpoutEnabled() && (((SpoutPlayer) player).isSpoutCraftEnabled());
		
		if (spoutEnabledPlayer && soundUrl.length() > 0) {
			sound = SpoutManager.getSoundManager();
			sound.playCustomSoundEffect(DeadlyPlates.getInstance(), (SpoutPlayer) player, soundUrl, false);
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
