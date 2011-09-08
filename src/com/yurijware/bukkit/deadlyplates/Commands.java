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
		
		if (args.length == 0) { return false; }
		
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}
		
		if (args[0].equalsIgnoreCase("reload")) {
			if (!Config.checkPermissions(sender, "DeadlyPlates.reload") && (sender instanceof Player)) {
				Messaging.sendMessage(sender, CAUSE.ERROR_PERMISSION);
				return true;
			}
			
			Config.load();
			Messaging.sendMessage(sender, CAUSE.RELOADED);
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
		
		Messaging.sendMessage(sender, CAUSE.ERROR_SYNTAX);
		return false;
	}
	
	private void createPlate(Player player, String[] args) {
		Block block = player.getTargetBlock(null, 10);
		
		//Check for permission and if player is permitted to
		//build by protection plugins
		if (!Config.checkPermissions(player, "DeadlyPlates.create") || !Config.isProtected(player, block)) {
			Messaging.sendMessage(player, CAUSE.ERROR_PERMISSION);
			return;
		}
		
		if (block.getType() != Material.STONE_PLATE && block.getType() != Material.WOOD_PLATE) {
			Messaging.sendMessage(player, CAUSE.ERROR_TARGET);
			return;
		}
		
		Plate p = Plate.getPlateIfDeadly(block);
		if (p != null) {
			Messaging.sendMessage(player, CAUSE.ERROR_EXISTS);
			return;
		}
		
		int limit = Config.getPlateLimit();
		int count = plugin.getDatabase().find(Plate.class)
				.where().ieq("player", player.getName()).findRowCount();
		
		if (count >= limit) {
			Messaging.sendMessage(player, CAUSE.ERROR_LIMIT);
			return;
		}
		
		int damage = Config.getDefaultDamage();
		if (args.length > 1) {
			try {
				damage = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				Messaging.sendMessage(player, CAUSE.ERROR_NUMBER);
				return;
			}
			
			if (damage > Config.getMaxDamage()) {
				damage = Config.getMaxDamage();
			}
		}
		
		//Save plate
		Plate plate = new Plate(block, player.getName(), damage);
		plugin.getDatabase().save(plate);
		
		Config.logHawkEye(player, plate, CAUSE.CREATED); //Log with hawkeye
		Messaging.sendMessage(player, CAUSE.CREATED); //Send message
	}
	
	private void removePlate(Player player) {
		Block block = player.getTargetBlock(null, 10); //Get targeted block
		
		//Check if block is a plate
		if (block.getType() != Material.STONE_PLATE && block.getType() != Material.WOOD_PLATE) {
			Messaging.sendMessage(player, CAUSE.ERROR_TARGET);
			return;
		}
		
		//Check if plate is deadly
		Plate plate = Plate.getPlateIfDeadly(block);
		if (plate == null) {
			Messaging.sendMessage(player, CAUSE.ERROR_NOTDEADLY);
			return;
		}
		
		String owner = plate.getPlayer(); //Get owner
		
		//Must be owner or have permission and is permitted to
		//build by protection plugins
		if ((!owner.equals(player.getName()) &&
				!Config.checkPermissions(player, "DeadlyPlates.admin")) ||
				!Config.isProtected(player, block)) {
			Messaging.sendMessage(player, CAUSE.ERROR_PERMISSION);
			return;
		}
		
		plugin.getDatabase().delete(plate); //Remove plate
		Config.logHawkEye(player, plate, CAUSE.REMOVED); //Log with hawkey
		Messaging.sendMessage(player, CAUSE.REMOVED); //Send message
	}
	
	private void changePlate(Player player, String[] args) {
		Block block = player.getTargetBlock(null, 10);
		
		int damage = 1;
		try {
			damage = Integer.parseInt(args[1]);
			if (damage > Config.getMaxDamage()) { damage = Config.getMaxDamage(); }
		} catch (NumberFormatException e) {
			Messaging.sendMessage(player, CAUSE.ERROR_NUMBER);
			return;
		}
		
		Plate plate = Plate.getPlateIfDeadly(block);
		if (plate == null) {
			Messaging.sendMessage(player, CAUSE.ERROR_NOTDEADLY);
			return;
		}
		
		if ((!player.getName().equals(plate.getPlayer()) &&
				!Config.checkPermissions(player, "DeadlyPlates.admin")) ||
				!Config.isProtected(player, block)) {
			Messaging.sendMessage(player, CAUSE.ERROR_PERMISSION);
			return;
		}
		
		plate.setDamage(damage);
		plugin.getDatabase().save(plate);
		
		Config.logHawkEye(player, plate, CAUSE.CHANGED);
		Messaging.sendMessage(player, CAUSE.CHANGED);
	}
	
	private void listPlates(Player player) {
		player.sendMessage(Config.getLangChat(CAUSE.LIST));
		QueryIterator<Plate> itr = plugin.getDatabase().find(Plate.class)
				.where().ieq("player", player.getName()).findIterate();
		int count = 0;
		while (itr.hasNext()) {
			Plate p = itr.next();
			player.sendMessage("§bId=" + p.getId() + " X=" + p.getX() + " Y="
					+ p.getY() + " Z=" + p.getZ() + " World="
					+ p.getWorld());
			count++;
		}
		itr.close();
		if (count == 0) {
			player.sendMessage(Config.getLangChat(CAUSE.ERROR_NOPLATES));
		}
	}
	
}
