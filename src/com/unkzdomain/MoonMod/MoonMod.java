package com.unkzdomain.MoonMod;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

public class MoonMod extends JavaPlugin {

	private World earthWorld = null;
	private World moonWorld = null;
	public ArrayList<String> chunkList;
	private MoonModWorldListener mmwl;

	String earthWorldName, moonWorldName, earthWorldText, moonWorldText,
			chunkListFileName;
	Double starFreq;

	public MoonMod(PluginLoader pluginLoader, Server instance,
			PluginDescriptionFile desc, File folder, File plugin,
			ClassLoader cLoader) {
		super(pluginLoader, instance, desc, folder, plugin, cLoader);
	}

	@Override
	public void onDisable() {
		saveChunkList();
		Configuration config = this.getConfiguration();
		config.setProperty("moonmod.earth-world-name", this.earthWorldName);
		config.setProperty("moonmod.moon-world-name", this.moonWorldName);
		config.setProperty("moonmod.earth-world-text", this.earthWorldText);
		config.setProperty("moonmod.moon-world-text", this.moonWorldText);
		config.setProperty("moonmod.chunk-list-filename", this.chunkListFileName);
		config.setProperty("moonmod.star-frequency", this.starFreq);

		
		PluginDescriptionFile pdfFile = getDescription();
		System.out.println(pdfFile.getName() + " version "
				+ pdfFile.getVersion() + " is disabled!");
	}

	@Override
	public void onEnable() {
		Configuration config = this.getConfiguration();

		this.earthWorldName = config.getString("moonmod.earth-world-name",
				"world");
		this.moonWorldName = config
				.getString("moonmod.moon-world-name", "moon");
		this.earthWorldText = config.getString("moonmod.earth-world-text",
				"Earth");
		this.moonWorldText = config
				.getString("moonmod.moon-world-text", "Moon");
		this.chunkListFileName = config.getString(
				"moonmod.chunk-list-filename", "chunks.file");
		this.starFreq = config.getDouble("moonmod.star-frequency", 0.01);

		mmwl = new MoonModWorldListener(this);
		loadChunkList();
		setWorlds();
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.CHUNK_LOADED, mmwl, Event.Priority.High,
				this);
		PluginDescriptionFile pdfFile = getDescription();
		System.out.println(pdfFile.getName() + " version "
				+ pdfFile.getVersion() + " is enabled!");
	}

	private void setWorlds() {
		List<World> worlds = getServer().getWorlds();

		for (World world : worlds) {
			if (world.getName().equals(earthWorldName)) {
				earthWorld = world;
			} else if (world.getName().equals(moonWorldName)) {
				moonWorld = world;
			}
		}

		if (earthWorld == null) {
			earthWorld = getServer().createWorld(earthWorldName,
					World.Environment.NORMAL);
		}

		if (moonWorld == null) {
			moonWorld = getServer().createWorld(moonWorldName,
					World.Environment.NORMAL);
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String commandLabel, String[] args) {
		String[] trimmedArgs = args;
		String commandName = command.getName().toLowerCase();

		// Command Syntax: "/launch [player (defaults to self)] planet"
		if (commandName.equals("launch")) {
			return performGoToCommand(sender, trimmedArgs);
		}
		return false;
	}

	private boolean performGoToCommand(CommandSender sender, String[] args) {
		if (args.length > 2) {
			return false;
		}

		if (args.length == 1) {
			if (anonymousCheck(sender)) {
				return false;
			}

			Player player = (Player) sender;

			if (args[0].equalsIgnoreCase(moonWorldText)) {
				Location loc = moonWorld.getSpawnLocation();
				player.teleportTo(loc);
				player.setCompassTarget(loc);
			} else if (args[0].equalsIgnoreCase(earthWorldText)) {
				Location loc = earthWorld.getSpawnLocation();
				player.teleportTo(loc);
				player.setCompassTarget(loc);
			} else {
				return false;
			}
		} else if (args.length == 2) {
			Player player = this.getServer().getPlayer(args[0]);

			if (args[1].equalsIgnoreCase(moonWorldText)) {
				Location loc = moonWorld.getSpawnLocation();
				player.teleportTo(loc);
				player.setCompassTarget(loc);
			} else if (args[1].equalsIgnoreCase(earthWorldText)) {
				Location loc = earthWorld.getSpawnLocation();
				player.teleportTo(loc);
				player.setCompassTarget(loc);
			} else {
				return false;
			}
		}

		return true;
	}

	private boolean anonymousCheck(CommandSender sender) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Cannot execute that command, I don't know who you are!");
			return true;
		} else {
			return false;
		}
	}

	boolean saveChunkList() {
		try {
			FileOutputStream fileOut = new FileOutputStream(
					this.getDataFolder() + chunkListFileName);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(chunkList);
			out.close();
			fileOut.close();
			return true;
		} catch (IOException i) {
			i.printStackTrace();
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	boolean loadChunkList() {
		try {
			FileInputStream fileIn = new FileInputStream(this.getDataFolder()
					+ chunkListFileName);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			chunkList = (ArrayList<String>) in.readObject();
			in.close();
			fileIn.close();
			return true;
		} catch (IOException i) {
			i.printStackTrace();
			return false;
		} catch (ClassNotFoundException c) {
			c.printStackTrace();
			return false;
		}
	}
}
