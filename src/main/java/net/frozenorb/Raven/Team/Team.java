package net.frozenorb.Raven.Team;

import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.Raven.Types.ProtectedLocation;
import net.frozenorb.Raven.Types.TimestampedLocation;
import net.frozenorb.Utilities.Interfaces.Loadable;
import net.frozenorb.Utilities.Interfaces.Saveable;
import net.frozenorb.mBasic.Basic;
import net.frozenorb.qlib.uuid.FrozenUUIDCache;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;

public class Team implements Saveable, Loadable<String> {

	private String name;
	private String password;

	private Set<UUID> owners = new HashSet<>();
	private Set<UUID> members = new HashSet<UUID>();

	private ProtectedLocation hq;
	private ProtectedLocation rally;

	private boolean friendlyFire;
	private boolean changed = false;
	private boolean loading = false;
	private String friendlyName;

	private String tag;

	private HashMap<Location, Long> previousRallies = new HashMap<Location, Long>();
	private HashMap<Location, Long> previousHQs = new HashMap<Location, Long>();

	public Team(String name) {
		this.name = name;
	}

	public int getTotalBalance() {
		int bal = 0;
		for (UUID s : getMembers()) {
			bal += Basic.get().getUuidEconomyAccess().getBalance(s);
		}
		return bal;
	}

	public ArrayList<TimestampedLocation> getPreviousHQs() {
		ArrayList<TimestampedLocation> tls = new ArrayList<TimestampedLocation>();
		for (Entry<Location, Long> entry : previousHQs.entrySet()) {
			tls.add(new TimestampedLocation(entry.getKey(), entry.getValue()));
		}
		Collections.sort(tls, new Comparator<TimestampedLocation>() {
			@Override
			public int compare(TimestampedLocation o1, TimestampedLocation o2) {
				return ((Long) o1.timestamp).compareTo(o2.timestamp);
			}
		});
		return tls;
	}

	public ArrayList<TimestampedLocation> getPreviousRallies() {
		ArrayList<TimestampedLocation> tls = new ArrayList<TimestampedLocation>();
		for (Entry<Location, Long> entry : previousRallies.entrySet()) {
			tls.add(new TimestampedLocation(entry.getKey(), entry.getValue()));
		}
		Collections.sort(tls, new Comparator<TimestampedLocation>() {
			@Override
			public int compare(TimestampedLocation o1, TimestampedLocation o2) {
				return ((Long) o1.timestamp).compareTo(o2.timestamp);
			}
		});
		return tls;
	}

	public void setChanged(boolean hasChanged) {
		this.changed = hasChanged;
	}

	public String getFriendlyName() {
		return friendlyName;
	}

	public void setFriendlyName(String friendlyName) {
		changed = true;
		this.friendlyName = friendlyName;
	}

	public String getName() {
		return name;
	}

	public void setPassword(String password) {
		this.password = password;
	}


    public ProtectedLocation getHQ() {
		return hq;
	}

	public ProtectedLocation getRally() {
		return rally;
	}

	public Set<UUID> getOwners() {
		return owners;
	}

	public Set<UUID> getMembers() {
		return members;
	}


    public String getPassword() {
		return password;
	}

	public void setTag(String tag) {
		changed = true;
		this.tag = tag;
	}

	public String getTag() {
		changed = true;
		return tag;
	}

	public void addMember(UUID member) {
		changed = true;
		members.add(member);

	}

	public void addOwner(UUID owner) {
		changed = true;
		owners.add(owner);
		members.add(owner);

	}

	public void setHQ(Location hq, boolean update) {
		setHQ(hq, "", update);
	}

	public void setHQ(Location hq, String password) {
		setHQ(hq, password, true);
	}

	public void setHQ(Location hq, String password, boolean update) {
		changed = true;
		this.hq = new ProtectedLocation(hq, password);
		if (update) {
			previousHQs.put(hq, System.currentTimeMillis());
		}
	}

	public void setHQ(Location hq) {
		setHQ(hq, true);
	}

	public void setRally(Location r) {
		setRally(r, true);
	}

	public void setRally(Location rally, String password) {
		setRally(rally, password, true);
	}

	public void setRally(Location rally, boolean update) {
        setRally(rally, "", update);
	}

	public void setRally(Location rally, String password, boolean update) {
		changed = true;
		this.rally = new ProtectedLocation(rally, password);
		if (update) {
			previousRallies.put(rally, System.currentTimeMillis());
		}
	}

	public boolean isOwner(UUID uuid) {
		for (UUID owner : owners) {
			if (uuid.equals(owner))
				return true;
		}
		return false;
	}

	public String getActualPlayerName(String pName) {//
		for (UUID str : members) {
			if (FrozenUUIDCache.name(str).equalsIgnoreCase(pName))
				return FrozenUUIDCache.name(str);
		}
		return null;
	}

	public boolean isFriendlyFire() {
		return friendlyFire;
	}

	public void setFriendlyFire(boolean friendlyFire) {
		changed = true;
		this.friendlyFire = friendlyFire;

	}

	public boolean isOnTeam(Player pl) {
		return isOnTeam(pl.getUniqueId());
	}

	public boolean isOnTeam(UUID id) {
		for (UUID member : members) {
			if (id.equals(member))
				return true;
		}

		return false;
	}

	public void setMember(UUID name) {
		changed = true;
		for (Iterator<UUID> iterator = owners.iterator(); iterator.hasNext();) {
			if (iterator.next().equals(name)) {
				iterator.remove();
				break;
			}
		}

	}

	public boolean remove(UUID name) {
		changed = true;
		for (Iterator<UUID> iterator = owners.iterator(); iterator.hasNext();) {
			if (iterator.next().equals(name)) {
				iterator.remove();
				break;
			}
		}

		for (Iterator<UUID> iterator = members.iterator(); iterator.hasNext();) {
			if (iterator.next().equals(name)) {
				iterator.remove();
				break;
			}
		}

		boolean emptyTeam = owners.size() == 0 && members.size() == 0;

		if (!emptyTeam)
			save(RavenPlugin.syncRedis);
		else
			delete();

		return owners.size() == 0 && members.size() == 0;
	}

	public boolean hasChanged() {
		return changed;
	}

	private void delete() {
		File teamFile = new File("teams" + File.separator + getName().toLowerCase() + ".txt");

		if (teamFile.exists())
			teamFile.delete();
	}

	public int getOnlineMemberAmount() {
		int amt = 0;

		for (UUID m : getMembers()) {
			if (Bukkit.getPlayer(m) != null && !Bukkit.getPlayer(m).hasMetadata("invisible")) {
				amt++;
			}
		}
		return amt;

	}

	public int getMemberAmount() {
		int amt = 0;

		for (UUID m : getMembers()) {
			amt++;
		}
		return amt;
	}

	public int getSize() {
		return getMembers().size();
	}

	public void load(String str) {
		loading = true;
		String[] lines = str.split("\n");
		for (String line : lines) {
			String identifier = line.substring(0, line.indexOf(':'));
			String[] lineParts = line.substring(line.indexOf(':') + 1).split(",");

			if (identifier.equalsIgnoreCase("Password")) {
				setPassword(lineParts[0]);
			} else if (identifier.equalsIgnoreCase("Owners")) {
				for (String name : lineParts) {
				    if (name.isEmpty()) {
						continue;
					}
					addOwner(UUID.fromString(name.trim())); //
				}

			} else if (identifier.equalsIgnoreCase("Members")) {
				for (String name : lineParts) {
					if (name.isEmpty()) {
						continue;
					}
					addMember(UUID.fromString(name.trim()));
				}
			} else if (identifier.equalsIgnoreCase("HQ")) {
				ProtectedLocation hqq = parseProtectedLocation(lineParts);
				setHQ(hqq.getParent(), hqq.getPassword(), false);
			} else if (identifier.equalsIgnoreCase("Rally")) {
				ProtectedLocation rallyq = parseProtectedLocation(lineParts);
				setRally(rallyq.getParent(), rallyq.getPassword(), false);
			} else if (identifier.equalsIgnoreCase("FriendlyFire")) {
				setFriendlyFire(Boolean.parseBoolean(lineParts[0]));
			} else if (identifier.equalsIgnoreCase("FriendlyName")) {
				setFriendlyName(lineParts[0]);
			} else if (identifier.equalsIgnoreCase("PreviousRallies")) {

				for (String meta : lineParts) {
					if (!meta.contains(" - "))
						continue;
					long timestamp = Long.parseLong(meta.split(" - ")[1]);
					String coords = meta.split(" - ")[0].replace("(", "").replace(")", "");
					double x = Double.parseDouble(coords.split(" ")[0]);
					double y = Double.parseDouble(coords.split(" ")[1]);
					double z = Double.parseDouble(coords.split(" ")[2]);
					float n = Float.parseFloat(coords.split(" ")[3]); //
					float n2 = Float.parseFloat(coords.split(" ")[4]);
					String world = coords.split(" ")[5];
					Location loc = new Location(Bukkit.getWorld(world), x, y, z, n, n2);
					previousRallies.put(loc, timestamp);

				}

			} else if (identifier.equalsIgnoreCase("PreviousHQs")) {
				for (String meta : lineParts) {
					if (!meta.contains(" - "))
						continue;
					long timestamp = Long.parseLong(meta.split(" - ")[1]);
					String coords = meta.split(" - ")[0].replace("(", "").replace(")", "");
					double x = Double.parseDouble(coords.split(" ")[0]);
					double y = Double.parseDouble(coords.split(" ")[1]);
					double z = Double.parseDouble(coords.split(" ")[2]);
					float n = Float.parseFloat(coords.split(" ")[3]);
					float n2 = Float.parseFloat(coords.split(" ")[4]);
					String world = coords.split(" ")[5];
					Location loc = new Location(Bukkit.getWorld(world), x, y, z, n, n2);
					previousHQs.put(loc, timestamp);

				}
			} else if (identifier.equalsIgnoreCase("Tag")) {
				tag = lineParts[0];
			}
		}
		loading = false;
		changed = false;
	}

	public void save(Jedis j) {
		changed = false;
		if (loading)
			return;
		StringBuilder teamString = new StringBuilder();
		String owners = "";
		String members = "";
		ProtectedLocation hqLocation = getHQ();
		ProtectedLocation rallyLocation = getRally();

		boolean oFirst = true;
		for (UUID owner : getOwners()) {
			if (!oFirst)
				owners += ",";
			owners += owner.toString();
			oFirst = false;
		}
		boolean mFirst = true;
		for (UUID member : getMembers()) {
			if (!mFirst)
				members += ",";
			members += member.toString();
			mFirst = false;
		}
		String prevHQ = "";
		String prevRally = "";
		boolean hqFirst = true;
		for (Entry<Location, Long> entry : previousHQs.entrySet()) {
			if (!hqFirst)
				prevHQ += ",";
			hqFirst = false;
			Location l = entry.getKey();
			prevHQ += ("(" + l.getX() + " " + l.getY() + " " + l.getZ() + " " + l.getYaw() + " " + l.getPitch() + " " + l.getWorld().getName() + ") - " + entry.getValue());
		}
		boolean rallyFirst = true;
		for (Entry<Location, Long> entry : previousRallies.entrySet()) {
			if (!rallyFirst)
				prevRally += ",";
			rallyFirst = false;
			Location l = entry.getKey();
			prevRally += ("(" + l.getX() + " " + l.getY() + " " + l.getZ() + " " + l.getYaw() + " " + l.getPitch() + " " + l.getWorld().getName() + ") - " + entry.getValue());
		}
		teamString.append("Password:" + getPassword() + '\n');
		teamString.append("Owners:" + owners + '\n');
		teamString.append("Members:" + members + '\n');
		if (hqLocation != null)
			teamString.append("HQ:" + hqLocation.getParent().getWorld().getName() + "," + hqLocation.getParent().getX() + "," + hqLocation.getParent().getY() + "," + hqLocation.getParent().getZ() + "," + hqLocation.getParent().getYaw() + "," + hqLocation.getParent().getPitch()
					+ (hqLocation.isProtected() ? "," + hqLocation.getPassword() +'\n' : '\n'));

		if (rallyLocation != null)
			teamString.append("Rally:" + rallyLocation.getParent().getWorld().getName() + "," + rallyLocation.getParent().getX() + "," + rallyLocation.getParent().getY() + "," + rallyLocation.getParent().getZ() + "," + rallyLocation.getParent().getYaw() + "," + rallyLocation.getParent().getPitch() +
					(rallyLocation.isProtected() ? "," + rallyLocation.getPassword() +'\n' : '\n'));

		teamString.append("FriendlyFire:" + friendlyFire + '\n');
		teamString.append("FriendlyName:" + friendlyName + '\n');
		teamString.append("PreviousRallies:" + prevRally + '\n');
		teamString.append("PreviousHQs:" + prevHQ + '\n');
		if (tag != null) {
			teamString.append("Tag:" + tag + '\n');
		}
		j.set("teams." + getName().toLowerCase(), teamString.toString());
		j.disconnect();
	}

	private Location parseLocation(String[] args) {
		if (!(args.length >= 6))
			return null;
		World world = Bukkit.getWorld(args[0]);
		double x = Double.parseDouble(args[1]);
		double y = Double.parseDouble(args[2]);
		double z = Double.parseDouble(args[3]);
		float yaw = Float.parseFloat(args[4]);
		float pitch = Float.parseFloat(args[5]);

		return new Location(world, x, y, z, yaw, pitch);
}


	private ProtectedLocation parseProtectedLocation(String[] args) {
		Location location = parseLocation(args);
		try {
			return new ProtectedLocation(location, args[6]);
		} catch (Exception ex) {
			// there is no args[6] meaning no password
			return new ProtectedLocation(location, "");
		}
	}


	@Override
	public void save() {
		Jedis j = new Jedis("localhost");
		j.connect();
		if (j.isConnected()) {
			save(j);
		}
	}

	/**
	 * @return true if the HQ is protected, false if not
	 */
	public boolean isHQProtected() {
		return this.hq.isProtected();
	}

	public boolean isRallyProtected() {
		return this.rally.isProtected();
	}

}
