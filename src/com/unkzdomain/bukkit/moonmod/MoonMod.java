package com.unkzdomain.bukkit.moonmod;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijiko.Messaging;
import com.nijiko.permissions.PermissionHandler;

public class MoonMod extends JavaPlugin {
	
	World earthWorld = null;
	World moonWorld = null;
	public ArrayList<String> chunkList;
	private Timer tick = null;
	private int rate = 1000;
	private long dayStart;
	public static PermissionHandler perms = null;
	
	// Configuration settings (loaded from config file)
	String earthWorldName, moonWorldName, earthWorldText, moonWorldText,
			chunkListFileName, dayMode;
	Double starFreq;
	
	public MoonMod(PluginLoader pluginLoader, Server instance,
			PluginDescriptionFile desc, File folder, File plugin,
			ClassLoader cLoader) {
		super(pluginLoader, instance, desc, folder, plugin, cLoader);
	}
	
	@Override
	public void onDisable() {
		this.stopTimer();
		saveChunkList();
		saveConfig();
		
		PluginDescriptionFile pdfFile = getDescription();
		System.out.println(pdfFile.getName() + " version "
				+ pdfFile.getVersion() + " is disabled!");
	}
	
	// Start timer that keeps Moon world at a specific time
	private void startTimer() {
		MoonModTimerTask timerTask = new MoonModTimerTask();
		timerTask.server = this.getServer();
		timerTask.worldName = this.moonWorldName;
		timerTask.dayStart = this.dayStart;
		
		tick = new Timer();
		tick.schedule(timerTask, 0, rate);
	}
	
	// Stop the timer's ticking
	private void stopTimer() {
		if (tick != null) {
			tick.cancel();
			tick = null;
		}
	}
	
	@Override
	public void onEnable() {
		setupPermissions();
		loadConfig();
		setWorlds();
		loadChunkList();
		
		this.startTimer();
		
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.CHUNK_LOADED,
				new MoonModWorldListener(this), Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_COMMAND, new MoonModPlayerListener(
				this), Event.Priority.Normal, this);
		
		PluginDescriptionFile pdfFile = getDescription();
		System.out.println(pdfFile.getName() + " version "
				+ pdfFile.getVersion() + " is enabled!");
	}
	
	// Uses Ninjiko's Permissions system until Bukkit has its own
	public void setupPermissions() {
		Plugin test = this.getServer().getPluginManager()
				.getPlugin("Permissions");
		
		if (MoonMod.perms == null) {
			if (test != null) {
				MoonMod.perms = ((Permissions) test).getHandler();
			} else {
				PluginDescriptionFile pdfFile = getDescription();
				System.out.println(Messaging.bracketize(pdfFile.getName())
						+ " Permission system not enabled. Disabling plugin.");
				this.getServer().getPluginManager().disablePlugin(this);
			}
		}
	}
	
	// Load settings from Config file. If file does not exist, use default
	// settings provided
	private void loadConfig() {
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
		
		dayMode = config.getString("moonmod.day-mode", "night");
		if (dayMode.equalsIgnoreCase("day")) {
			this.dayStart = 0;
		} else if (dayMode.equalsIgnoreCase("sunset")) {
			this.dayStart = 12000;
		} else if (dayMode.equalsIgnoreCase("night")) {
			this.dayStart = 13800;
		} else if (dayMode.equalsIgnoreCase("sunrise")) {
			this.dayStart = 22200;
		} else {
			this.dayStart = 0;
		}
	}
	
	// Save current settings to Config file. If Config file doesn't already
	// exist, create it with current settings (which would most likely be the
	// defaults)
	private void saveConfig() {
		Configuration config = this.getConfiguration();
		config.setProperty("moonmod.earth-world-name", this.earthWorldName);
		config.setProperty("moonmod.moon-world-name", this.moonWorldName);
		config.setProperty("moonmod.earth-world-text", this.earthWorldText);
		config.setProperty("moonmod.moon-world-text", this.moonWorldText);
		config.setProperty("moonmod.chunk-list-filename",
				this.chunkListFileName);
		config.setProperty("moonmod.star-frequency", this.starFreq);
		config.setProperty("moonmod.day-mode", this.dayMode);
		config.save();
	}
	
	// Set Earth and Moon worlds based on world names in Config
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
			System.out.println("Creating new " + this.earthWorldText
					+ " world " + this.earthWorldName + "...");
			earthWorld = getServer().createWorld(earthWorldName,
					World.Environment.NORMAL);
		}
		if (moonWorld == null) {
			System.out.println("Creating new " + this.moonWorldText + " world "
					+ this.moonWorldName + "...");
			moonWorld = getServer().createWorld(moonWorldName,
					World.Environment.NORMAL);
		}
		
	}
	
	// Save list of chunks that have been processed to a file
	boolean saveChunkList() {
		try {
			FileOutputStream fileOut = new FileOutputStream(
					this.getDataFolder() + "/" + chunkListFileName);
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
	
	// Load list of processed chunks from file. If not possible, create new list
	// of chunks.
	@SuppressWarnings("unchecked")
	boolean loadChunkList() {
		try {
			FileInputStream fileIn = new FileInputStream(this.getDataFolder()
					+ "/" + chunkListFileName);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			chunkList = (ArrayList<String>) in.readObject();
			in.close();
			fileIn.close();
			return true;
		} catch (IOException i) {
			i.printStackTrace();
		} catch (ClassNotFoundException c) {
			c.printStackTrace();
		}
		chunkList = new ArrayList<String>();
		return true;
	}
}
