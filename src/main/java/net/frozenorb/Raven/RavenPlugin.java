package net.frozenorb.Raven;

import net.frozenorb.Raven.CommandSystem.*;
import net.frozenorb.Raven.EconomySystem.Economy;
import net.frozenorb.Raven.GameEvents.Koth.KOTH;
import net.frozenorb.Raven.GameEvents.Koth.KOTHHandler;
import net.frozenorb.Raven.Listeners.GeneralListener;
import net.frozenorb.Raven.Listeners.ScoreboardBalanceListener;
import net.frozenorb.Raven.Managers.*;
import net.frozenorb.Raven.Tasks.ChestRefillTask;
import net.frozenorb.Raven.Tasks.RedisSaveTask;
import net.frozenorb.Raven.Tasks.TrackingEventTask;
import net.frozenorb.Raven.Team.Team;
import net.frozenorb.Raven.Traps.TrapManager;
import net.frozenorb.Raven.Types.Scoreboard.ScoreboardHandler;
import net.frozenorb.Raven.Visual.BossBarManager;
import net.frozenorb.Utilities.DataSystem.Regioning.RegionManager;
import net.frozenorb.mBasic.Basic;
import net.frozenorb.mBasic.EconomySystem.UUID.UUIDEconomyManager;
import net.frozenorb.qlib.scoreboard.*;
import net.frozenorb.qlib.uuid.FrozenUUIDCache;
import net.minecraft.server.v1_7_R4.EntityInsentient;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftEntity;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.Jedis;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

@SuppressWarnings({"deprecation", "unchecked"})
public class RavenPlugin extends JavaPlugin implements CommandExecutor {
    private static RavenPlugin instance;

    public static ArrayList<String> spawnprot = new ArrayList<String>();
    public static HashMap<String, Integer> tasks = new HashMap<String, Integer>();

    public static final Random RANDOM = new Random();
    public static final Timer TIMER = new Timer();

    public static Jedis syncRedis;

    private AlarmManager alarmManager;
    private ServerManager serverManager;
    private HomeManager homeManager;
    private TeamManager teamManager;
    private WarpManager warpManager;
    private CommandRegistrar commandRegistrar;
    private WarpAmountManager warpAmountManager;
    private RedisSaveTask redisSaveTask;
    private RecipeManager recipeManager;
    private BossBarManager bossBarManager;
    private TrapManager trapManager;
    private TrackingEventTask trackingEventTask;
    private ChestRefillTask chestRefillTask;

    private KOTHHandler kothHandler;

    private BufferedImage endermanImage;

    private UUIDEconomyManager uuidEconomyManager;

//    public String getSpacer(Player player) {
//        return "\u0588§7§m--------------------";
//    }

    public String getSpacer(Player player) {
        return "&7&m----------*----------";
    }


    public String getBalance(Player player) {
        DecimalFormat format = new DecimalFormat("###,###.##");
        return "§e§lGold§7: §f" + format.format(getUuidEconomyManager().getBalance(player.getUniqueId()));
    }

    public String getTeamName(Player player) {
        return "§e§lTeam§7: §f" + (RavenPlugin.get().getTeamManager().getPlayerTeam(player.getUniqueId()) == null ?
                "No Team" : RavenPlugin.get().getTeamManager().getPlayerTeam(player.getUniqueId()).getFriendlyName());
    }


    public String getEnd(KOTH koth) {
        DecimalFormat format = new DecimalFormat("#.#");
        return "§e§lEnd§7: §f" + format.format(koth.getPercentage()) + "%";
    } //


    public String[] getValues(Player player) {

        KOTH koth = (getKOTHHandler().getKOTH("end") == null ? getKOTHHandler().getKOTH("endevent") :
                getKOTHHandler().getKOTH("end"));
        boolean include_end = koth != null && koth.isActive();

        List<String> t = new ArrayList<String>(Arrays.asList(new String[]{
                getSpacer(player),
                getBalance(player),
                getTeamName(player)
        }));

        if (include_end) {
            t.add(getEnd(koth));
        }
        t.add(getSpacer(player));

        return t.toArray(new String[]{});

    }


    @Override
    public void onEnable() {
        instance = this;
        RegionManager.register(this);
        serverManager = new ServerManager(this);

        try {
            syncRedis = new Jedis("localhost");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        syncRedis.connect();

        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        loadDataFiles();
        getConfig().options().copyDefaults(true);
        WarpCommand warp = new WarpCommand(this);
        commandRegistrar = new CommandRegistrar();
        getCommand("warp").setExecutor(warp);
        getCommand("kwho").setExecutor(new WhoCommand(this));
        getCommand("track").setExecutor(new TrackCommand());
        getCommand("hud").setExecutor(new HudCommand());
        getCommand("spawn").setExecutor(new SpawnCommand(this));
        getCommand("kits").setExecutor(new KitCommands(this));
        getCommand("kit").setExecutor(new KitCommands(this));


        try {
            commandRegistrar.register();

            alarmManager = new AlarmManager();
            warpAmountManager = new WarpAmountManager();
            homeManager = new HomeManager(this);
            warpManager = new WarpManager(this);
            teamManager = new TeamManager(this);
            // scoreboardTask = new ScoreboardTask();
            redisSaveTask = new RedisSaveTask();
            recipeManager = new RecipeManager();
            bossBarManager = new BossBarManager();
            trapManager = new TrapManager();
            trackingEventTask = new TrackingEventTask();
            kothHandler = new KOTHHandler();
            chestRefillTask = new ChestRefillTask();
            uuidEconomyManager = (UUIDEconomyManager) Basic.get().getUuidEconomyAccess();
            Economy.getInstance().load(syncRedis);
            FrozenScoreboardHandler.setConfiguration(configure("&6&lMCTeams Map 2"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        recipeManager.setupRecipes();
        bossBarManager.runTaskTimer(this, 20L, 20L);
        redisSaveTask.runTaskTimerAsynchronously(this, 13200L, 13200L);
        trackingEventTask.runTaskTimer(this, ((60 * 20) * 60) * 4, ((60 * 20) * 60) * 4); // Occur every 4 hours.
        chestRefillTask.runTaskTimer(this, ((60 * 20) * 60) * 6, ((60 * 20) * 60) * 6);   // refill every 6 hours

        // have to do this since i am using the https protocol, and i need a user agent
        try {
            File endermanFile = new File(this.getDataFolder(), "MHF_Enderman.png");
            FileInputStream stream = new FileInputStream(endermanFile);
            endermanImage = ImageIO.read(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Bukkit.getPluginManager().registerEvents(new GeneralListener(this), this);


        Bukkit.getScheduler().runTaskLater(this, new Runnable() {

            @Override
            public void run() {
                for (Entity entity : Bukkit.getWorlds().get(0).getEntities()) {
                    if (!(entity instanceof Chicken || entity instanceof MushroomCow || entity instanceof Villager)) {
                        if (((CraftEntity) entity).getHandle() instanceof EntityInsentient) {
                            EntityInsentient ie = (EntityInsentient) ((CraftEntity) entity).getHandle();
                            ie.fromMobSpawner = true;
                        }
                    }
                }
                Basic.get().getUuidEconomyAccess().registerListener(new ScoreboardBalanceListener());
            }
        }, 20L);


    }

    public ScoreboardConfiguration configure(String title) {
        ScoreboardConfiguration configuration = new ScoreboardConfiguration();
        configuration.setTitleGetter(new TitleGetter(ChatColor.translateAlternateColorCodes('&', title)));
        configuration.setScoreGetter(new ScoreGetter() {
            @Override
            public String[] getScores(Player player) {
                return ScoreboardHandler.isHidden(player) ? new String[]{} : RavenPlugin.this.getValues(player);
            }
        });

        return configuration;
    }


    public void onDisable() {
        saveDataFiles();
        serverManager.save();
        warpManager.saveAllWarps(syncRedis);
        trapManager.saveAllData(syncRedis);
        for (Team t : teamManager.getTeams())
            if (t.hasChanged())
                t.save(syncRedis);
        alarmManager.saveAllData(syncRedis);
        Economy.getInstance().saveUsingJedis(syncRedis);
        kothHandler.saveKOTHs();
        TIMER.purge();
        TIMER.cancel();
    }

    public void loadDataFiles() {
        File hashmapfile = new File(getDataFolder() + File.separator + "spawnprot.bin");
        if (hashmapfile.exists()) {
            try {
                final ObjectInputStream ois = new ObjectInputStream(new FileInputStream(getDataFolder() + File.separator + "spawnprot.bin"));
                final Object result = ois.readObject();
                ois.close();
                spawnprot = (ArrayList<String>) result;

            } catch (final Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void saveDataFiles() {
        try {
            File f = new File(getDataFolder() + File.separator + "spawnprot.bin");

            if (!f.exists())
                f.createNewFile();
            final ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
            oos.writeObject(spawnprot);
            oos.flush();
            oos.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isInt(String i) {
        try {
            Integer.parseInt(i);
            return true;

        } catch (Exception ex) {
            return false;
        }
    }

    public int getValidNearbyEntities(Player p, double d) {
        int counter = 0;
        for (Entity e : p.getNearbyEntities(d, d, d)) {
            if (!(e instanceof Player)) {
                counter += 1;
            }

        }
        return counter;
    }

    @SuppressWarnings("rawtypes")
    public LinkedHashMap sortHashMapByValuesD(HashMap passedMap) {
        List mapKeys = new ArrayList(passedMap.keySet());
        List mapValues = new ArrayList(passedMap.values());
        Collections.sort(mapValues);
        Collections.sort(mapKeys);

        LinkedHashMap sortedMap = new LinkedHashMap();

        Iterator valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            Object val = valueIt.next();
            Iterator keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                Object key = keyIt.next();
                String comp1 = passedMap.get(key).toString();
                String comp2 = val.toString();

                if (comp1.equals(comp2)) {
                    passedMap.remove(key);
                    mapKeys.remove(key);
                    sortedMap.put(key, val);
                    break;
                }

            }

        }
        return sortedMap;
    }

    public boolean onCommand(final CommandSender sender, Command cmd, String lbl, String[] args) {
        if (cmd.getName().equalsIgnoreCase("logout")) {
            if (!RavenPlugin.get().getServerManager().safeToLogout((Player) sender)) {
                sender.sendMessage(ChatColor.RED + "You are not able to logout safely right now!");
            } else
                ((Player) sender).kickPlayer("§cSafely logged out from MCTeams.com!");
        }

        final Player p = (Player) sender;
        if (cmd.getName().equalsIgnoreCase("lock")) {
            if (sender.hasPermission("raven.lock")) {
                if (args.length == 1) {
                    if (args[0].equalsIgnoreCase("all")) {
                        Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "The server is now locked. Only STAFF can join.");
                        serverManager.setLockMessage("all");
                    } else if (args[0].equalsIgnoreCase("vip")) {
                        serverManager.setLockMessage("vip");
                        Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "The server is now locked. Only VIPs or higher can join.");
                    } else if (args[0].equalsIgnoreCase("off")) {
                        Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "The server is no longer locked.");
                        serverManager.setServerLocked(false);
                        serverManager.setLockMessage("not");
                        serverManager.setServerLocked(false);
                        return true;
                    }
                    serverManager.setServerLocked(true);
                } else {
                    if (args.length == 0) {
                        sender.sendMessage(ChatColor.RED + "/lock VIP|ALL|OFF");
                    }
                }
            }
        }

        if (cmd.getName().equalsIgnoreCase("help")) {

            sender.sendMessage(new String[]{
                    "§7Border is at §9" + new DecimalFormat().format(ServerManager.getBorderDistance()) + "x" + new DecimalFormat().format(ServerManager.getBorderDistance()) + " §7blocks.",
                    " §7§m------§r §9Commands §7§m------",
                    " §9/alarm §7- shows more information about alarms",
                    " §9/home §7- warps you to your 'home'",
                    " §9/sethome §7- sets your 'home' position.",
                    " §9/spawn §7- warps you to spawn.",
                    " §9/team §7- team related commands.",
                    " §9/track §7- used to track other players.",
                    " §9/warp §7- used to go to, set, and list warps.",
                    " §9/who §7- tells you who is currently online.",
                    " §9/hud §7- used to toggle the HUD, on and off.",
                    " §9/features §7- view the new features that have been added.",
                    " §9/items §7- Displays the quantity of each item in your inventory."
            });
        }

        if (cmd.getName().equalsIgnoreCase("features")) {
            sender.sendMessage(new String[]{
                    " §7§m------§r §9Features §7§m------",
                    " §9Supply Crates §7- Supply crates drop every four hours!",
                    "",
                    " §9Wrench Tool §7- Wrench is used to remove a spawner from its location and could be placed anywhere!",
                    "",
                    " §9Ascended Pickaxe/Sword §7- Obtainable by mining, PvPing, looting END Towers or Capturing END KOTH"
            });
        }

        if (cmd.getName().equalsIgnoreCase("sethome")) {
            UUID id = p.getUniqueId();
            Location playerLocation = p.getLocation();
            if ((playerLocation.getWorld().getEnvironment() == Environment.THE_END)) {
                sender.sendMessage(ChatColor.RED + "You cannot set home in the end.");
                return true;
            }
            if (!(playerLocation.getWorld().getEnvironment() == Environment.NETHER)) {
                if ((Math.abs(playerLocation.getX()) < 512.0D) && (Math.abs(playerLocation.getZ()) < 512.0D)) {
                    sender.sendMessage(ChatColor.RED + "You cannot set warps within 512 blocks of spawn.");
                    return true;
                }
            }

            homeManager.setHome(id, playerLocation);
            p.sendMessage(ChatColor.GRAY + "Your home has been set!");
        }
        if (cmd.getName().equalsIgnoreCase("home")) {

            final Player player = (Player) sender;
            if (player.getWorld().getEnvironment() == Environment.THE_END) {
                player.sendMessage(ChatColor.RED + "You can only exit the End through the End Portal!");
                return true;
            }

            if (args.length > 0 && sender.hasPermission("raven.teamas")) {

                final String name = args[0].toLowerCase();
                final UUID id = FrozenUUIDCache.uuid(name);
                if (id == null) {
                    p.sendMessage(ChatColor.RED + "Could not find player '" + name + "'.");
                    return true;
                }

                if (homeManager.getHome(id) == null) {
                    p.sendMessage(ChatColor.RED + "That player does not have a home set.");
                } else {

                    player.teleport(homeManager.getHome(id));
                }
            } else {
                final UUID id = p.getUniqueId();
                if (homeManager.getHome(id) == null) {
                    p.sendMessage(ChatColor.RED + "You do not have a home set!");
                } else {
                    if (serverManager.isSpawn(((Player) sender).getLocation())) {
                        sender.sendMessage(ChatColor.RED + "You cannot warp in spawn!");
                        return true;
                    }

                    final Location loc = homeManager.getHome(id);
                    if (serverManager.canWarp(player)) {
                        RavenPlugin.get().getServerManager().disablePlayerAttacking(p);
                        player.teleport(loc);
                    } else {
                        sender.sendMessage(ChatColor.GRAY + "Someone is nearby! Warping in 10 seconds. Don't move.");
                        int taskid = Bukkit.getScheduler().scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin("Raven"), new Runnable() {
                            public void run() {
                                if (tasks.containsKey(sender.getName())) {
                                    tasks.remove(sender.getName());
                                    player.teleport(loc);
                                    RavenPlugin.get().getServerManager().disablePlayerAttacking(p);

                                }
                            }
                        }, 200L);
                        tasks.put(sender.getName(), taskid);
                    }
                }
            }
        }
        if (cmd.getName().equalsIgnoreCase("tas")) {
            if (sender.hasPermission("raven.teamas")) {
                if (args.length == 2) {
                    String teamName = args[0].toLowerCase();

                    if (teamManager.teamExists(teamName)) {
                        net.frozenorb.Raven.Team.Team team = teamManager.getTeam(teamName);

                        if (args[1].equalsIgnoreCase("hq")) {
                            if (team.getHQ() == null) {
                                sender.sendMessage(ChatColor.GRAY + "HQ not set.");
                                return true;
                            }

                            ((Player) sender).teleport(team.getHQ().getParent());

                        } else if (args[1].equalsIgnoreCase("rally")) {
                            if (team.getRally() == null) {
                                sender.sendMessage(ChatColor.GRAY + "Rally not set.");
                                return true;
                            }
                            ((Player) sender).teleport(team.getRally().getParent());
                        } else {
                            sender.sendMessage("§c/teamas <teamName> hq|rally");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "Team " + args[0] + " couldn't be found.");
                    }
                } else {
                    sender.sendMessage("§c/teamas <teamName> hq|rally");
                }
            }
        }
        if (cmd.getName().equalsIgnoreCase("savedata")) {
            if (sender.isOp()) {
                Bukkit.getScheduler().runTaskAsynchronously(RavenPlugin.get(), new Runnable() {

                    @Override
                    public void run() {
                        Jedis j = new Jedis("localhost");
                        int done = warpManager.saveAllWarps(j);
                        int teams = 0;
                        for (Team t : teamManager.getTeams()) {
                            if (t.hasChanged()) {
                                t.save(j);
                                teams += 1;
                            }
                        }
                        Economy.getInstance().saveUsingJedis(j);
                        alarmManager.saveAllData(j);

                        j.disconnect();
                        sender.sendMessage(ChatColor.RED + "Saved " + teams + " teams and " + done + " warps to redis!");
                    }
                });
            }
        }
        return false;
    }

    /*
     * -------INSTANCE GETTERS------------
     */
    public WarpManager getWarpManager() {
        return warpManager;
    }

    public TrapManager getTrapManager() {
        return trapManager;
    }

    public WarpAmountManager getWarpAmountManager() {
        return warpAmountManager;
    }

    public AlarmManager getAlarmManager() {
        return alarmManager;
    }

    public ServerManager getServerManager() {
        return serverManager;
    }

    public CommandRegistrar getCommandRegistrar() {
        return commandRegistrar;
    }

    public HomeManager getHomeManager() {
        return homeManager;
    }

    public TeamManager getTeamManager() {
        return teamManager;
    }

    public RedisSaveTask getRedisSaveTask() {
        return redisSaveTask;
    }

    public RecipeManager getRecipeManager() {
        return recipeManager;
    }

    public BossBarManager getBossBarManager() {
        return bossBarManager;
    }

    public KOTHHandler getKOTHHandler() {
        return kothHandler;
    }

    public BufferedImage getEndermanImage() {
        return endermanImage;
    }

    public ChestRefillTask getChestRefillTask() {
        return chestRefillTask;
    }

    public UUIDEconomyManager getUuidEconomyManager() {
        return uuidEconomyManager;
    }

    /*
     * -------SINGLETON INSTANCE GETTER---------
	 */

    /**
     * Gets the instance of the main plugin class
     *
     * @return main class
     */
    public static RavenPlugin get() {
        return instance;
    }

    public static void main(String[] args) {

        System.out.println("30x68 with 2 claims: " + getPrice(30, 68, 2, true));

    }

    public static int getPrice(int x, int z, int amts, boolean buying) {
        int blocks = x * z;
        int done = 0;
        double mod = 0.4D;
        double curPrice = 0D;

        while (blocks > 0) {
            blocks--;
            done++;

            curPrice += mod;

            if (done == 250) {
                done = 0;
                mod += 0.4D;
            }
        }

        // Multiple price by 0.8 (requested by @itsjhalt)

        // price here is 1000

        curPrice += (500 * amts); // 1000

        // 2800
        curPrice *= 0.8F;

        return ((int) curPrice);

        // x + y
        // (0.8) (x+y)
        // 0.8x + y
        // -35,-1069 || -5, -1001

        // 30x68

    }


}