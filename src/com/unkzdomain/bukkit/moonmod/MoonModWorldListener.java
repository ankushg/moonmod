package com.unkzdomain.bukkit.moonmod;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.MobSpawner;
import org.bukkit.entity.MobType;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldListener;

public class MoonModWorldListener extends WorldListener {
	public static MoonMod plugin;
	
	public MoonModWorldListener(MoonMod instance) {
		plugin = instance;
	}
	
	// Check if String representing a Chunk is in the list of processed Chunks
	private boolean checkChunk(String chunkString) {
		return plugin.chunkList.contains(chunkString);
	}
	
	@Override
	public void onChunkLoaded(ChunkLoadEvent event) {
		// Only do stuff to the Chunk if you are on the Moon
		if (event.getWorld().getName().equals(plugin.moonWorldName)) {
			Chunk chunk = event.getChunk();
			// For some reason, the event occasionally fails to pass and then
			// the Chunk ends up being null...
			if (chunk != null) {
				String chunkString = "[" + chunk.getWorld().getId() + " "
						+ chunk.getWorld().getName() + "] (" + chunk.getX()
						+ ", " + chunk.getZ() + ")";
				if (!this.checkChunk(chunkString)) {
					System.out.println("New chunk found: " + chunkString);
					this.processChunk(chunk);
					this.recordChunk(chunkString);
				}
			} else {
				System.out.println("Chunk was null.");
			}
		}
	}
	
	// Process Blocks in a Chunk to "Moon-ify" it
	private void processChunk(Chunk chunk) {
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				for (int y = 127; y >= 0; y--) {
					Block block = chunk.getBlock(x, y, z);
					if (block.getType().equals(Material.DIRT)
							|| block.getType().equals(Material.GRASS)
							|| block.getType().equals(Material.SAND)) {
						block.setType(Material.SPONGE);
					} else if (block.getType().equals(Material.CACTUS)
							|| block.getType().equals(Material.WOOD)) {
						block.setType(Material.GLOWSTONE);
					} else if (y > 32
							&& (block.getType().equals(Material.WATER) || block
									.getType().equals(Material.LEAVES))) {
						block.setType(Material.AIR);
					} else if (y > 32 && block.getType().equals(Material.LAVA)) {
						block.setType(Material.OBSIDIAN);
					} else if (block.getType().equals(Material.MOB_SPAWNER)) {
						// Who needs Skeletons when you can have Ghasts?!
						((MobSpawner) block.getState())
								.setMobType(MobType.GHAST);
					} else if (block.getType().equals(Material.GOLD_ORE)
							|| block.getType().equals(Material.LAPIS_ORE)) {
						// At least give SOME incentive to visit the Moon
						block.setType(Material.DIAMOND_ORE);
					}
				}
			}
		}
	}
	
	// Add String representing a Chunk to list of processed Chunks
	private void recordChunk(String chunkString) {
		plugin.chunkList.add(chunkString);
		System.out.println("Recorded " + chunkString);
	}
}
