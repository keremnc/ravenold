package net.frozenorb.Raven.Visual;

import net.frozenorb.Utilities.Types.Scrollable;

public class SpawnCooldown implements Scrollable {

	long when;

	public SpawnCooldown() {
		this.when = System.currentTimeMillis() + 90 * 1000;
	}

	public void setWhen(long when) {
		this.when = when;
	}

	public boolean done() {
		return System.currentTimeMillis() >= when;
	}

	@Override
	public String next() {
		String part = getTime((System.currentTimeMillis() - when) / 1000);
		String msg = "§dYou are on spawn cooldown! Time left:§e " + part;
		if (done() || part.contains("none")) {
			return "~";
		}
		return msg;
	}

	/**
	 * Gets the parsed time of the amount in seconds
	 * 
	 * @param i
	 *            the time to parse
	 * @return the amount of time it parses to
	 */
	public String getTime(long i) {
		return getUntrimmedTime(i).trim();
	}

	private String getUntrimmedTime(long i) {
		i = Math.abs(i);
		int hours = (int) Math.floor(i / 3600);
		int remainder = (int) (i % 3600), minutes = remainder / 60, seconds = remainder % 60;
		String toReturn;
		if (seconds == 0 && minutes == 0)
			return (hours != 0 ? (hours == 1 ? hours + " hour " : hours + " hours ") : "none!");
		if (minutes == 0) {
			if (seconds == 1)
				return (hours != 0 ? (hours == 1 ? hours + " hour " : hours + " hours ") : "") + String.format("%s second", seconds);
			return (hours != 0 ? (hours == 1 ? hours + " hour" : hours + " hours") : "") + String.format("%s seconds", seconds);
		}
		if (seconds == 0) {
			if (minutes == 1)
				return (hours != 0 ? (hours == 1 ? hours + " hour " : hours + " hours ") : "") + String.format("%s minute", minutes);
			return (hours != 0 ? (hours == 1 ? hours + " hour " : hours + " hours ") : "") + String.format("%s minutes", minutes);
		}
		if (seconds == 1) {
			if (minutes == 1)
				return (hours != 0 ? (hours == 1 ? hours + " hour " : hours + " hours ") : "") + String.format("%s minute %s second", minutes, seconds);
			return (hours != 0 ? (hours == 1 ? hours + " hour " : hours + " hours ") : "") + String.format("%s minutes %s second", minutes, seconds);
		}
		if (minutes == 1) {
			return (hours != 0 ? (hours == 1 ? hours + " hour " : hours + " hours ") : "") + String.format("%s minute %s seconds ", minutes, seconds);
		}
		toReturn = String.format("%s minutes %s seconds", minutes, seconds);
		return (hours != 0 ? (hours == 1 ? hours + " hour " : hours + " hours ") : "") + toReturn;
	}
}
