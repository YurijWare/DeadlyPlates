package com.yurijware.bukkit.deadlyplates;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.avaje.ebean.QueryIterator;
import com.yurijware.bukkit.deadlyplates.Messaging.CAUSE;

/**
 * Performs commands
 * @author Yurij
 */
public class Commands implements CommandExecutor {
	private final DeadlyPlates plugin;
	
	public Commands(DeadlyPlates plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Config conf = plugin.getSettings();
		
		if (args.length == 0) { return false; }
		
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}
		
		if (args[0].equalsIgnoreCase("reload")) {
			if (!conf.checkPermissions(sender, "DeadlyPlates.reload") && (sender instanceof Player)) {
				plugin.getMessaging().send(sender, CAUSE.ERROR_PERMISSION);
				return true;
			}
			
			conf.load();
			plugin.getMessaging().send(sender, CAUSE.RELOADED);
			return true;
			
		} else if (args[0].equalsIgnoreCase("create") && sender instanceof Player) {
			createPlate(player, args);
			return true;
			
		} else if (args[0].equalsIgnoreCase("remove") && sender instanceof Player) {
			removePlate(player);
			return true;
			
		}  else if (args[0].equalsIgnoreCase("change") && args.length > 1 && sender instanceof Player) {
			changePlate(player, args);
			return true;
			
		} else if (args[0].equalsIgnoreCase("list") && sender instanceof Player) {
			listPlates(player);
			return true;
			
		} else if (!(sender instanceof Player)) {
			sender.sendMessage("You must be a player");
			return true;
		}
		
		plugin.getMessaging().send(sender, CAUSE.ERROR_SYNTAX);
		return false;
	}
	
	private void createPlate(Player player, String[] args) {
		Config conf = plugin.getSettings();
		Block block = player.getTargetBlock(null, 10);
		
		//Check for permission and if player is permitted to
		//build by protection plugins
		if (!conf.checkPermissions(player, "DeadlyPlates.create") || !conf.isProtected(player, block)) {
			plugin.getMessaging().send(player, CAUSE.ERROR_PERMISSION);
			return;
		}
		
		if (block.getType() != Material.STONE_PLATE && block.getType() != Material.WOOD_PLATE) {
			plugin.getMessaging().send(player, CAUSE.ERROR_TARGET);
			return;
		}
		
		Plate p = Plate.getPlateIfDeadly(block);
		if (p != null) {
			plugin.getMessaging().send(player, CAUSE.ERROR_EXISTS);
			return;
		}
		
		int limit = conf.getPlateLimit();
		int count = plugin.getDatabase().find(Plate.class)
				.where().ieq("player", player.getName()).findRowCount();
		
		if (count >= limit) {
			plugin.getMessaging().send(player, CAUSE.ERROR_LIMIT);
			return;
		}
		
		int damage = conf.getDefaultDamage();
		if (args.length > 1) {
			try {
				damage = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				plugin.getMessaging().send(player, CAUSE.ERROR_NUMBER);
				return;
			}
			
			if (damage > conf.getMaxDamage()) {
				damage = conf.getMaxDamage();
			}
		}
		
		//Save plate
		Plate plate = new Plate(block, player.getName(), damage);
		plugin.getDatabase().save(plate);
		
		conf.logHawkEye(player, plate, CAUSE.CREATED); //Log with hawkeye
		plugin.getMessaging().send(player, CAUSE.CREATED); //Send message
	}
	
	private void removePlate(Player player) {
		Config conf = plugin.getSettings();
		Block block = player.getTargetBlock(null, 10); //Get targeted block
		
		//Check if block is a plate
		if (block.getType() != Material.STONE_PLATE && block.getType() != Material.WOOD_PLATE) {
			plugin.getMessaging().send(player, CAUSE.ERROR_TARGET);
			return;
		}
		
		//Check if plate is deadly
		Plate plate = Plate.getPlateIfDeadly(block);
		if (plate == null) {
			plugin.getMessaging().send(player, CAUSE.ERROR_NOTDEADLY);
			return;
		}
		
		String owner = plate.getPlayer(); //Get owner
		
		//Must be owner or have permission and is permitted to
		//build by protection plugins
		if ((!owner.equals(player.getName()) &&
				!conf.checkPermissions(player, "DeadlyPlates.admin")) ||
				!conf.isProtected(player, block)) {
			plugin.getMessaging().send(player, CAUSE.ERROR_PERMISSION);
			return;
		}
		
		plugin.getDatabase().delete(plate); //Remove plate
		conf.logHawkEye(player, plate, CAUSE.REMOVED); //Log with hawkeye
		plugin.getMessaging().send(player, CAUSE.REMOVED); //Send message
	}
	
	private void changePlate(Player player, String[] args) {
		Config conf = plugin.getSettings();
		Block block = player.getTargetBlock(null, 10);
		
		int damage = 1;
		try {
			damage = Integer.parseInt(args[1]);
			if (damage > conf.getMaxDamage()) { damage = conf.getMaxDamage(); }
		} catch (NumberFormatException e) {
			plugin.getMessaging().send(player, CAUSE.ERROR_NUMBER);
			return;
		}
		
		Plate plate = Plate.getPlateIfDeadly(block);
		if (plate == null) {
			plugin.getMessaging().send(player, CAUSE.ERROR_NOTDEADLY);
			return;
		}
		
		if ((!player.getName().equals(plate.getPlayer()) &&
				!conf.checkPermissions(player, "DeadlyPlates.admin")) ||
				!conf.isProtected(player, block)) {
			plugin.getMessaging().send(player, CAUSE.ERROR_PERMISSION);
			return;
		}
		
		plate.setDamage(damage);
		plugin.getDatabase().save(plate);
		
		conf.logHawkEye(player, plate, CAUSE.CHANGED);
		plugin.getMessaging().send(player, CAUSE.CHANGED);
	}
	
	private void listPlates(Player player) {
		player.sendMessage("§9DeadlyPlates list");
		QueryIterator<Plate> itr = plugin.getDatabase().find(Plate.class)
				.where().ieq("player", player.getName()).findIterate();
		int count = 0;
		while (itr.hasNext()) {
			Plate p = itr.next();
			player.sendMessage("§bId=" + p.getId() + " X=" + p.getX() + " Y="
					+ p.getY() + " Z=" + p.getZ() + " World="
					+ p.getWorld() + " Damage=" + p.getDamage());
			count++;
		}
		itr.close();
		if (count == 0) {
			player.sendMessage("§cYou dont have any deadly plates");
		}
	}
	
}
