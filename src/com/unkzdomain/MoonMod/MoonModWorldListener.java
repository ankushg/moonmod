package com.unkzdomain.MoonMod;

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

	public void onChunkLoaded(ChunkLoadEvent event) {
		if (event.getWorld().getName().equals(plugin.moonWorldName)) {
			Chunk chunk = event.getChunk();
			String chunkString = "( " + chunk.getWorld().getId() + " "
					+ chunk.getWorld().getName() + " " + chunk.getX() + " "
					+ chunk.getZ() + " )";
			if (!checkChunk(chunkString)) {
				processChunk(chunk);
				recordChunk(chunkString);
			}
		}
	}

	private void processChunk(Chunk chunk) {
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				for (int y = 0; y < 128; y++) {
					Block block = chunk.getBlock(x, y, z);
					if (y == 127){
						if(Math.random() < plugin.starFreq){
							block.setType(Material.GLOWSTONE);
						} else {
							block.setType(Material.OBSIDIAN);
						}
					} else if (block.getType().equals(Material.DIRT)
							|| block.getType().equals(Material.GRASS)
							|| block.getType().equals(Material.SAND)) {
						block.setType(Material.SPONGE);
					} else if(block.getType().equals(Material.CACTUS)
							|| block.getType().equals(Material.WOOD)) {
						block.setType(Material.GLOWSTONE);
					} else if (y > 32
							&& (block.getType().equals(Material.WATER)
									|| block.getType().equals(Material.LAVA)
									|| block.getType().equals(Material.LEAVES))) {
						block.setType(Material.AIR);
					} else if (block.getType().equals(Material.MOB_SPAWNER)) {
						((MobSpawner) block.getState()).setMobType(MobType.GHAST);
					}
				}
			}
		}
	}

	private boolean checkChunk(String chunkString) {
		return plugin.chunkList.contains(chunkString);
	}

	private void recordChunk(String chunkString) {
		plugin.chunkList.add(chunkString);
	}
}
