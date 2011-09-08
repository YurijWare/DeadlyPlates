package com.yurijware.bukkit.deadlyplates;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

import com.yurijware.bukkit.deadlyplates.Messaging.CAUSE;

/**
 * Listens on block events
 * @author Yurij
 */
public class BlockListener extends org.bukkit.event.block.BlockListener {
	
	@Override
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled()) { return; }
		
		//Check if it is a wodden or stone plate
		Material mat = event.getBlock().getType();
		if (mat != Material.STONE_PLATE && mat != Material.WOOD_PLATE) { return; }
		
		//Check if it is a deadly plate
		Plate plate = Plate.getPlateIfDeadly(event.getBlock());
		if (plate == null) { return; }
		
		//Remove the plate frome the database
		DeadlyPlates.getInstance().getDatabase().delete(plate);
		
		//Log with hawkeye
		Config.logHawkEye(event.getPlayer(), plate, CAUSE.DESTROYED);
		
		//Send message to owner if online
		Player owner = Bukkit.getServer().getPlayer(plate.getPlayer());
		if (owner != null && owner.isOnline()) {
			Messaging.sendMessage(owner, CAUSE.DESTROYED);
		}
	}
	
}
