/**
 * 
 */
package com.unkzdomain.bukkit.moonmod;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerListener;

public class MoonModPlayerListener extends PlayerListener {
	
	private final Server server;
	private final MoonMod parent;
	
	public MoonModPlayerListener(MoonMod parent) {
		this.parent = parent;
		this.server = parent.getServer();
	}
	
	@Override
	public void onPlayerCommand(PlayerChatEvent event) {
		if (!event.isCancelled()) {
			String[] command = event.getMessage().split(" ");
			Player sender = event.getPlayer();
			
			if (command[0].equalsIgnoreCase("/launch")
					|| command[0].equalsIgnoreCase("/l")) {
				if (!MoonMod.perms.has((Player) sender, "moonmod.launch")) {
					sender.sendMessage("You don't have permission to /launch.");
					return;
				}
				performLaunchCommand(sender, command);
				event.setCancelled(true);
			} else if ((command[0].equalsIgnoreCase("/launchp"))
					|| (command[0].equalsIgnoreCase("/lp"))) {
				if (!MoonMod.perms.has((Player) sender, "moonmod.launchp")) {
					sender.sendMessage("You don't have permission to /launchp.");
					return;
				}
				performLaunchPCommand(sender, command);
				event.setCancelled(true);
			}
		}
		
	}
	
	private boolean performLaunchCommand(Player sender, String[] args) {
		String launchTo;
		
		if (args.length == 1) {
			launchTo = getOtherWorld(sender);
		} else if (args.length == 2) {
			launchTo = args[1];
		} else {
			sender.sendMessage("Incorrect syntax.");
			sender.sendMessage("Correct usage: /launch [Moon|Earth]");
			return false;
		}
		
		this.notifyLaunch(sender, sender);
		return launchPlayer(sender, launchTo);
	}
	
	private boolean performLaunchPCommand(Player sender, String[] args) {
		Player player;
		String launchTo;
		
		if (args.length == 2) {
			player = this.server.getPlayer(args[1]);
			launchTo = this.getOtherWorld(player);
		} else if (args.length == 3) {
			player = this.server.getPlayer(args[1]);
			launchTo = args[2];
		} else {
			sender.sendMessage("Incorrect syntax.");
			sender.sendMessage("Correct usage: /launchp <player> [Moon|Earth]");
			return false;
		}
		
		this.notifyLaunch(sender, player);
		return launchPlayer(player, launchTo);
	}
	
	private void notifyLaunch(Player sender, Player player) {
		if (sender.getName().equals(player.getName())) {
			player.sendMessage("You launched yourself.");
		} else {
			player.sendMessage("You were launched by "
					+ sender.getDisplayName());
		}
		
	}
	
	private String getOtherWorld(Player player) {
		if (player.getWorld().getName().equalsIgnoreCase(parent.earthWorldName)) {
			return parent.moonWorldName;
		} else if (player.getWorld().getName()
				.equalsIgnoreCase(parent.moonWorldName)) {
			return parent.earthWorldName;
		}
		
		return "";
	}
	
	private boolean launchPlayer(Player player, String launchTo) {
		if (player.getWorld().getName().equalsIgnoreCase(launchTo)) {
			
		} else if (launchTo.equalsIgnoreCase(parent.moonWorldText)) {
			Location loc = parent.moonWorld.getSpawnLocation();
			player.teleportTo(loc);
			player.setCompassTarget(loc);
		} else if (launchTo.equalsIgnoreCase(parent.earthWorldText)) {
			Location loc = parent.earthWorld.getSpawnLocation();
			player.teleportTo(loc);
			player.setCompassTarget(loc);
		} else {
			return false;
		}
		
		return true;
	}
}
