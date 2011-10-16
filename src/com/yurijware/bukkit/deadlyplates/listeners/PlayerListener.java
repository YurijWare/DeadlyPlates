package com.yurijware.bukkit.deadlyplates.listeners;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.yurijware.bukkit.deadlyplates.Config;
import com.yurijware.bukkit.deadlyplates.DeadlyPlates;
import com.yurijware.bukkit.deadlyplates.Plate;

/**
 * Listens on player events
 * @author Yurij
 */
public class PlayerListener extends org.bukkit.event.player.PlayerListener {
	private final DeadlyPlates plugin;
	
	public PlayerListener(DeadlyPlates plugin) {
		this.plugin = plugin;
	}

	@Override
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction() != Action.PHYSICAL) { return; }
		Config conf = plugin.getSettings();
		
		Block block = event.getClickedBlock();
		
		//Check if plate is deadly
		Plate plate = Plate.getPlateIfDeadly(block);
		if (plate == null) { return; }
		
		event.setCancelled(true);
		
		Player player = event.getPlayer();
		String owner = plate.getPlayer();
		int damage = plate.getDamage();
		
		//Check if damage exceeds max damage
		if (damage > conf.getMaxDamage()) {
			damage = conf.getMaxDamage();
		}
		
		//Check if player is immune
		if (conf.checkPermissions(player, "DeadlyPlates.ignore-damage")) {
			return;
		}
		
		//Check if owner and if owner is immune
		if (owner.equals(player.getName()) && conf.getOwnerImmune()) {
			return;
		}
		
		//Check for redstone power
		Block under = block.getRelative(BlockFace.DOWN, 2);
		if (conf.getRedstoneDisable() && (block.isBlockIndirectlyPowered() || under.isBlockPowered())) {
			return;
		}
		
		//Damage player
		player.damage(damage);
		
	}
	
}
