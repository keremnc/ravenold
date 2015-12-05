package net.frozenorb.Raven.Types;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.UUID;

public class Warp {
	private UUID playerName;
	private String warpName;
	private double x;
	private double y;
	private double z;
	private double yaw;
	private double pitch;
	private String name;
	private boolean active = true;

	public Warp(String warpName) {
		this.warpName = warpName;
	}

	public Warp(UUID player_name, String warp_name, Location loc, boolean active) {
		this.playerName = player_name;
		this.warpName = warp_name;
		this.x = loc.getX();
		this.y = loc.getY();
		this.z = loc.getZ();
		this.yaw = loc.getYaw();
		this.pitch = loc.getPitch();
		this.name = loc.getWorld().getName();
		this.active = active;
	}

	public Warp(UUID player_name, String warp_name, double warp_x, double warp_y, double warp_z, float warp_yaw, float warp_pitch, String world_name, boolean active) {
		this.playerName = player_name;
		this.warpName = warp_name;
		this.x = warp_x;
		this.y = warp_y;
		this.z = warp_z;
		this.yaw = warp_yaw;
		this.pitch = warp_pitch;
		this.name = world_name;
		this.active = active;

	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 instanceof Warp)
			return ((Warp) arg0).getName().equals(getName()) && ((Warp) arg0).getPlayerName().equals(getPlayerName());
		return super.equals(arg0);
	}

	@Override
	public int hashCode() {
		return getName().hashCode() + getPlayerName().hashCode();
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Location getLocation() {
		Location loc = new Location(null, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F);
		loc.setX(this.x);
		loc.setY(this.y);
		loc.setZ(this.z);
		loc.setYaw((float) this.yaw);
		loc.setPitch((float) this.pitch);
		loc.setWorld(Bukkit.getWorld(this.name));
		return loc;
	}

	public UUID getPlayerName() {
		return this.playerName;
	}

	public String getName() {
		return this.warpName;
	}

	public double getX() {
		return this.x;
	}

	public double getY() {
		return this.y;
	}

	public double getZ() {
		return this.z;
	}

	public double getYaw() {
		return this.yaw;
	}

	public double getPitch() {
		return this.pitch;
	}

	public String getWorldName() {
		return this.name;
	}

	public void setWarpName(String name) {
		this.warpName = name;
	}

	public void setPlayerName(UUID player_name) {
		this.playerName = player_name;
	}

	public void setX(double warp_x) {
		this.x = warp_x;
	}

	public void setY(double warp_y) {
		this.y = warp_y;
	}

	public void setZ(double warp_z) {
		this.z = warp_z;
	}

	public void setYaw(double warp_yaw) {
		this.yaw = warp_yaw;
	}

	public void setPitch(double warp_pitch) {
		this.pitch = warp_pitch;
	}

	public void setWorldName(String world_name) {
		this.name = world_name;
	}
}