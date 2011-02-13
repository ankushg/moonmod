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
	
	private boolean checkChunk(String chunkString) {
		return plugin.chunkList.contains(chunkString);
	}
	
	@Override
	public void onChunkLoaded(ChunkLoadEvent event) {
		if (event.getWorld().getName().equals(plugin.moonWorldName)) {
			Chunk chunk = event.getChunk();
			String chunkString = "( " + chunk.getWorld().getId() + " "
					+ chunk.getWorld().getName() + " " + chunk.getX() + " "
					+ chunk.getZ() + " )";
			if (!this.checkChunk(chunkString)) {
				this.processChunk(chunk);
				this.recordChunk(chunkString);
			}
		}
	}
	
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
						block.setType(Material.AIR);
					} else if (block.getType().equals(Material.MOB_SPAWNER)) {
						((MobSpawner) block.getState())
								.setMobType(MobType.GHAST);
					} else if (block.getType().equals(Material.GOLD_ORE)
							|| block.getType().equals(Material.LAPIS_ORE)) {
						block.setType(Material.DIAMOND_ORE);
					}
				}
			}
		}
	}
	
	private void recordChunk(String chunkString) {
		plugin.chunkList.add(chunkString);
		System.out.println("Scanned and recorded " + chunkString);
	}
}
