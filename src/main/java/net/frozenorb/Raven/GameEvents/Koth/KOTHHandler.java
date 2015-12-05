package net.frozenorb.Raven.GameEvents.Koth;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import lombok.Getter;
import net.frozenorb.Raven.GameEvents.Koth.Listeners.KOTHListener;
import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.qlib.command.FrozenCommandHandler;
import net.frozenorb.qlib.qLib;
import net.frozenorb.qlib.serialization.LocationSerializer;
import net.minecraft.util.com.google.gson.JsonParser;
import net.minecraft.util.org.apache.commons.io.FileUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class KOTHHandler {
    
    @Getter
    private Set<KOTH> KOTHs = new HashSet<>();
    @Getter private Map<KOTHScheduledTime, String> KOTHSchedule = new TreeMap<>();
    @Getter private Set<Location> KOTHSigns = new HashSet<>();
 
    public KOTHHandler() {
        loadKOTHs();
        loadSchedules();
        loadSigns();
 
        RavenPlugin.get().getServer().getPluginManager().registerEvents(new KOTHListener(), RavenPlugin.get());
        FrozenCommandHandler.registerParameterType(KOTH.class, new KOTHType());
        FrozenCommandHandler.loadCommandsFromPackage(RavenPlugin.get(), "net.frozenorb.Raven.GameEvents.Koth.Commands");
 
        new BukkitRunnable() {
 
            public void run() {
                for (KOTH koth : KOTHs) {
                    if (koth.isActive()) {
                        koth.tick();
                    }
                }
 
                for (Location signLocation : KOTHSigns) {
                    if (!signLocation.getChunk().isLoaded()) {
                        continue;
                    }
 
                    if (signLocation.getBlock().getState() instanceof Sign) {
                        Sign sign = (Sign) signLocation.getBlock().getState();
                        KOTH updateFor = getKOTH(ChatColor.stripColor(sign.getLine(0)));
 
                        if (updateFor != null) {
                            sign.setLine(0, sign.getLine(0));
                            sign.setLine(1, updateFor.isActive() ? ChatColor.GREEN.toString() + updateFor.getPercentage() + "%" : ChatColor.DARK_RED.toString() + updateFor.getPercentage() + "%");
                            sign.setLine(2, "");
                            sign.setLine(3, ChatColor.AQUA.toString() + updateFor.getCapLocation().getBlockX() + ", " + updateFor.getCapLocation().getBlockZ());
 
                            sign.update();
                        }
                    }
                }
            }
 
        }.runTaskTimer(RavenPlugin.get(), 5L, 20L);

        // The initial delay of 5 ticks is to 'offset' us with the scoreboard handler.
    }
 
    public void loadKOTHs() {
        try {
            File kothsBase = new File("KOTHs");
 
            if (!kothsBase.exists()) {
                kothsBase.mkdir();
            }
 
            for (File kothFile : kothsBase.listFiles()) {
                if (kothFile.getName().endsWith(".json")) {
                    KOTHs.add(qLib.GSON.fromJson(FileUtils.readFileToString(kothFile), KOTH.class));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
    public void loadSchedules() {
        KOTHSchedule.clear();
 
        try {
            File kothSchedule = new File("kothSchedule.json");
 
            if (!kothSchedule.exists()) {
                kothSchedule.createNewFile();
                FileUtils.write(kothSchedule, qLib.GSON.toJson(new JsonParser().parse(new BasicDBObject().toString())));
            }
 
            BasicDBObject dbo = (BasicDBObject) JSON.parse(FileUtils.readFileToString(kothSchedule));
 
            if (dbo != null) {
                for (Map.Entry<String, Object> entry : dbo.entrySet()) {
                    KOTHScheduledTime scheduledTime = KOTHScheduledTime.parse(entry.getKey());
                    this.KOTHSchedule.put(scheduledTime, String.valueOf(entry.getValue()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
    public void loadSigns() {
        KOTHSigns.clear();
 
        try {
            File kothSigns = new File("kothSigns.json");
 
            if (!kothSigns.exists()) {
                kothSigns.createNewFile();
                FileUtils.write(kothSigns, qLib.GSON.toJson(new JsonParser().parse(new BasicDBObject().toString())));
            }
 
            BasicDBObject dbo = (BasicDBObject) JSON.parse(FileUtils.readFileToString(kothSigns));
 
            if (dbo != null) {
                if (dbo.containsField("signs")) {
                    for (Object signObj : (BasicDBList) dbo.get("signs")) {
                        this.KOTHSigns.add(LocationSerializer.deserialize((BasicDBObject) signObj));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
    public void saveKOTHs() {
        try {
            File kothsBase = new File("KOTHs");
 
            if (!kothsBase.exists()) {
                kothsBase.mkdir();
            }
 
            for (File kothFile : kothsBase.listFiles()) {
                kothFile.delete();
            }
 
            for (KOTH koth : KOTHs) {
                File kothFile = new File(kothsBase, koth.getName() + ".json");
                FileUtils.write(kothFile, qLib.GSON.toJson(koth));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
    public void saveSigns() {
        try {
            File kothSigns = new File("kothSigns.json");
            BasicDBObject dbo = new BasicDBObject();
            BasicDBList signs = new BasicDBList();
 
            for (Location signLocation : KOTHSigns) {
                signs.add(LocationSerializer.serialize(signLocation));
            }
 
            dbo.put("signs", signs);
            kothSigns.delete();
            FileUtils.write(kothSigns, qLib.GSON.toJson(new JsonParser().parse(dbo.toString())));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
    public KOTH getKOTH(String name) {
        for (KOTH koth : KOTHs) {
            if (koth.getName().equalsIgnoreCase(name)) {
                return (koth);
            }
        }
 
        return (null);
    }
 
}