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

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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
	
	private World earthWorld = null;
	private World moonWorld = null;
	public ArrayList<String> chunkList;
	private MoonModWorldListener worldListener;
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
		if (tick != null) {
			tick.cancel();
			tick = null;
		}
		saveChunkList();
		saveConfig();
		
		PluginDescriptionFile pdfFile = getDescription();
		System.out.println(pdfFile.getName() + " version "
				+ pdfFile.getVersion() + " is disabled!");
	}
	
	@Override
	public void onEnable() {
		setupPermissions();
		loadConfig();
		setWorlds();
		loadChunkList();
		
		MoonModTimerTask timerTask = new MoonModTimerTask();
		timerTask.server = this.getServer();
		timerTask.worldName = this.moonWorldName;
		timerTask.dayStart = this.dayStart;
		
		tick = new Timer();
		tick.schedule(timerTask, 0, rate);
		
		worldListener = new MoonModWorldListener(this);
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.CHUNK_LOADED, worldListener,
				Event.Priority.High, this);
		
		PluginDescriptionFile pdfFile = getDescription();
		System.out.println(pdfFile.getName() + " version "
				+ pdfFile.getVersion() + " is enabled!");
	}
	
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
	
	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String commandLabel, String[] args) {
		String[] trimmedArgs = args;
		String commandName = command.getName().toLowerCase();
		
		if (commandName.equals("launch") || commandName.equals("l")) {
			if (!anonymousCheck(sender)
					&& !MoonMod.perms.has((Player) sender,
							"moonmod.launch.self")) {
				// TODO: Notify permissions failed
				return false;
			}
			return performLaunchCommand(sender, trimmedArgs);
		} else if (commandName.equals("launchp") || commandName.equals("lp")) {
			if (!anonymousCheck(sender)
					&& !MoonMod.perms.has((Player) sender,
							"moonmod.launch.others")) {
				// TODO: Notify permissions failed
				return false;
			}
			return performLaunchPCommand(sender, trimmedArgs);
		}
		return false;
	}
	
	private boolean performLaunchCommand(CommandSender sender, String[] args) {
		if (anonymousCheck(sender)) {
			// TODO: Output error message saying that it only works with Players
			return false;
		}
		Player player = (Player) sender;
		String launchTo;
		
		if (args.length == 0) {
			launchTo = getOtherWorld(player);
		} else if (args.length == 1) {
			launchTo = args[0];
		} else {
			// TODO: Output error message giving proper command usage
			return false;
		}
		
		this.notifyLaunch(sender, player);
		return launchPlayer(player, launchTo);
	}
	
	private boolean performLaunchPCommand(CommandSender sender, String[] args) {
		Player player;
		String launchTo;
		
		if (args.length == 1) {
			player = this.getServer().getPlayer(args[0]);
			launchTo = this.getOtherWorld(player);
		} else if (args.length == 2) {
			player = this.getServer().getPlayer(args[0]);
			launchTo = args[1];
		} else {
			// TODO: Output error message giving proper command usage
			return false;
		}
		
		this.notifyLaunch(sender, player);
		return launchPlayer(player, launchTo);
	}
	
	private void notifyLaunch(CommandSender sender, Player player) {
		// TODO: Notify player that they were launched by Sender
		
	}
	
	private String getOtherWorld(Player player) {
		if (player.getWorld().getName().equalsIgnoreCase(earthWorldName)) {
			return this.moonWorldName;
		} else if (player.getWorld().getName().equalsIgnoreCase(moonWorldName)) {
			return this.earthWorldName;
		}
		
		return "";
	}
	
	private boolean launchPlayer(Player player, String launchTo) {
		if (player.getWorld().getName().equalsIgnoreCase(launchTo)) {
			
		} else if (launchTo.equalsIgnoreCase(moonWorldText)) {
			Location loc = moonWorld.getSpawnLocation();
			player.teleportTo(loc);
			player.setCompassTarget(loc);
		} else if (launchTo.equalsIgnoreCase(earthWorldText)) {
			Location loc = earthWorld.getSpawnLocation();
			player.teleportTo(loc);
			player.setCompassTarget(loc);
		} else {
			return false;
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
