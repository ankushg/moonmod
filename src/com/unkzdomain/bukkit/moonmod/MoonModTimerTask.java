package com.unkzdomain.bukkit.moonmod;

import java.util.TimerTask;

import org.bukkit.Server;

public class MoonModTimerTask extends TimerTask {
	public Server server = null;
	public String worldName;
	public long wantedTime = 0;
	public long dayStart;
	
	@Override
	public void run() {
		long time = server.getWorld(worldName).getTime();
		long relativeTime = time % 24000;
		long startOfDay = time - relativeTime;
		
		// if day just finished and settings are day
		if (relativeTime > 12000 && this.dayStart == 0) {
			// set day
			server.getWorld(worldName).setTime(startOfDay + 0);
		}
		// if sunset just finished and settings are sunset
		else if (relativeTime > 13800 && this.dayStart == 12000) {
			
			// set sunset
			server.getWorld(worldName).setTime(startOfDay + 12000);
		}
		// if night just finished and settings are night
		else if (relativeTime > 22200 && this.dayStart == 13800) {
			// set night
			server.getWorld(worldName).setTime(startOfDay + 13800);
		}
		// if sunrise just finished and settings are sunrise
		else if (relativeTime > 0 && this.dayStart == 22200) {
			// set sunrise
			server.getWorld(worldName).setTime(startOfDay + 22200);
		}
	}
	
}
