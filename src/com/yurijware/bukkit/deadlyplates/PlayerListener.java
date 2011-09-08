package com.yurijware.bukkit.deadlyplates;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Listens on player events
 * @author Yurij
 */
public class PlayerListener extends org.bukkit.event.player.PlayerListener {
	
	@Override
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction() != Action.PHYSICAL) { return; }
		Block block = event.getClickedBlock();
		
		//Check if plate is deadly
		Plate plate = Plate.getPlateIfDeadly(block);
		if (plate == null) { return; }
		
		event.setCancelled(true);
		
		Player player = event.getPlayer();
		String owner = plate.getPlayer();
		int damage = plate.getDamage();
		
		//Check if damage exceeds max damage
		if (damage > Config.getMaxDamage()) {
			damage = Config.getMaxDamage();
		}
		
		//Check if player is immune
		if (Config.checkPermissions(player, "DeadlyPlates.ignore-damage")) {
			return;
		}
		
		//Check if owner and if owner is immune
		if (owner.equals(player.getName()) && Config.getOwnerImmune()) {
			return;
		}
		
		//Check for redstone power
		Block under = block.getRelative(BlockFace.DOWN, 2);
		if (Config.getRedstoneDisable() && (block.isBlockIndirectlyPowered() || under.isBlockPowered())) {
			return;
		}
		
		//Damage player
		player.damage(damage);
		
	}
	
}
