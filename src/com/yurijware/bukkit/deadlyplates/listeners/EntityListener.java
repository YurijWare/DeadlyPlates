package com.yurijware.bukkit.deadlyplates.listeners;

import java.util.logging.Level;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityInteractEvent;

import com.yurijware.bukkit.deadlyplates.Config;
import com.yurijware.bukkit.deadlyplates.DeadlyPlates;
import com.yurijware.bukkit.deadlyplates.Plate;

/**
 * Listens on entity events
 * @author Yurij
 */
public class EntityListener extends org.bukkit.event.entity.EntityListener {
	private final DeadlyPlates plugin;
	
	public EntityListener(DeadlyPlates plugin) {
		this.plugin = plugin;
	}

	@Override
	public void onEntityInteract(EntityInteractEvent event) {
		Config conf = plugin.getSettings();
		
		if (!conf.getDamageMobs()) { return; }
		
		Entity entity = event.getEntity();
		
		//Check if entity is alive
		if (!(entity instanceof LivingEntity)) { return; }
		
		Block block = event.getBlock();
		
		//Check if plate is deadly
		Plate plate = Plate.getPlateIfDeadly(block);
		if (plate == null) { return; }
		
		plugin.Log(Level.INFO, "Living entity stepped on a deadly plate");
		
		event.setCancelled(true);
		
		//Check for redstone power
		Block under = block.getRelative(BlockFace.DOWN, 2);
		if (conf.getRedstoneDisable() && (block.isBlockIndirectlyPowered() || under.isBlockPowered())) {
			return;
		}
		
		//Damage entity
		int damage = plate.getDamage();
		LivingEntity livingEntity = (LivingEntity) entity;
		livingEntity.damage(damage);
	}
	
}
