package fr.kiza.leagueuhc.core.game.cycle;

import fr.kiza.leagueuhc.LeagueUHC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class DayCycleManager {

    private final LeagueUHC instance;

    private static final int DAY_DURATION = 5 * 60;
    private static final int NIGHT_DURATION = 5 * 60;
    private static final int EPISODE_DURATION = DAY_DURATION + NIGHT_DURATION;

    private static final long MC_DAY_START = 0;
    private static final long MC_NIGHT_START = 13000;

    private BukkitTask cycleTask;
    private int currentSecond = 0;
    private int currentEpisode = 1;
    private boolean isDay = true;
    private boolean isRunning = false;

    private World gameWorld;

    public DayCycleManager(LeagueUHC instance) {
        this.instance = instance;
    }

    public void start(World world) {
        if (isRunning) return;

        this.gameWorld = world;
        this.currentSecond = 0;
        this.currentEpisode = 1;
        this.isDay = true;
        this.isRunning = true;

        world.setGameRuleValue("doDaylightCycle", "false");
        world.setTime(MC_DAY_START);
        world.setStorm(false);
        world.setThundering(false);
        world.setWeatherDuration(Integer.MAX_VALUE);

        broadcastEpisodeStart();

        cycleTask = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(instance, 20L, 20L);
    }

    public void stop() {
        if (cycleTask != null) {
            cycleTask.cancel();
            cycleTask = null;
        }
        isRunning = false;
    }

    public void pause() {
        if (cycleTask != null) {
            cycleTask.cancel();
            cycleTask = null;
        }
    }

    public void resume() {
        if (!isRunning || cycleTask != null) return;

        cycleTask = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(instance, 20L, 20L);
    }

    private void tick() {
        if (gameWorld == null) return;

        currentSecond++;

        int cycleSecond = currentSecond % EPISODE_DURATION;
        int secondInPhase;

        boolean wasDay = isDay;
        isDay = cycleSecond < DAY_DURATION;
        secondInPhase = isDay ? cycleSecond : (cycleSecond - DAY_DURATION);

        if (wasDay && !isDay) {
            onNightStart();
        }

        if (!wasDay && isDay && currentSecond > 0) {
            currentEpisode++;
            onDayStart();
            broadcastEpisodeStart();
        }

        updateWorldTime(secondInPhase);

        checkCountdown(cycleSecond);
    }

    private void updateWorldTime(int secondInPhase) {
        long targetTime;

        if (isDay) {
            float progress = (float) secondInPhase / DAY_DURATION;
            targetTime = (long) (MC_DAY_START + (12000 * progress));
        } else {
            float progress = (float) secondInPhase / NIGHT_DURATION;
            targetTime = (long) (MC_NIGHT_START + (10000 * progress));
        }

        gameWorld.setTime(targetTime);
    }

    private void onDayStart() {
        playSound(Sound.NOTE_PLING, 1.0f, 1.5f);

        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        Bukkit.broadcastMessage(ChatColor.YELLOW + "  ☀ " + ChatColor.BOLD + "Lever du soleil" + ChatColor.YELLOW + " ☀");
        Bukkit.broadcastMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        Bukkit.broadcastMessage("");
    }

    private void onNightStart() {
        playSound(Sound.AMBIENCE_THUNDER, 0.5f, 0.5f);

        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(ChatColor.DARK_RED + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        Bukkit.broadcastMessage(ChatColor.RED + "  ☾ " + ChatColor.BOLD + "Tombée de la nuit" + ChatColor.RED + " ☾");
        Bukkit.broadcastMessage(ChatColor.DARK_RED + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        Bukkit.broadcastMessage("");
    }

    private void broadcastEpisodeStart() {
        playSound(Sound.LEVEL_UP, 1.0f, 1.0f);

        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(ChatColor.AQUA + "╔══════════════════╗");
        Bukkit.broadcastMessage(ChatColor.AQUA + "║" + ChatColor.WHITE + "   ⚔ " + ChatColor.BOLD + "ÉPISODE " + currentEpisode + ChatColor.RESET + ChatColor.WHITE + " ⚔   " + ChatColor.AQUA + "║");
        Bukkit.broadcastMessage(ChatColor.AQUA + "╚══════════════════╝");
        Bukkit.broadcastMessage("");
    }

    private void checkCountdown(int cycleSecond) {
        // Countdown 5-4-3-2-1 avant la nuit
        if (cycleSecond >= DAY_DURATION - 5 && cycleSecond < DAY_DURATION) {
            int remaining = DAY_DURATION - cycleSecond;
            broadcastCountdown(remaining);
        }

        // Countdown 5-4-3-2-1 avant le jour
        if (cycleSecond >= EPISODE_DURATION - 5 && cycleSecond < EPISODE_DURATION) {
            int remaining = EPISODE_DURATION - cycleSecond;
            broadcastCountdown(remaining);
        }
    }

    private void broadcastCountdown(int seconds) {
        playSound(Sound.CLICK, 1.0f, 1.0f + (0.1f * (5 - seconds)));

        ChatColor color;
        if (seconds <= 3) {
            color = ChatColor.RED;
        } else {
            color = ChatColor.YELLOW;
        }

        Bukkit.broadcastMessage(color + "» " + seconds + "...");
    }

    private void playSound(Sound sound, float volume, float pitch) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    // Getters

    public int getCurrentEpisode() {
        return currentEpisode;
    }

    public int getTotalSeconds() {
        return currentSecond;
    }

    public boolean isDay() {
        return isDay;
    }

    public boolean isNight() {
        return !isDay;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public int getSecondsUntilNight() {
        if (!isDay) return 0;
        int cycleSecond = currentSecond % EPISODE_DURATION;
        return DAY_DURATION - cycleSecond;
    }

    public int getSecondsUntilDay() {
        if (isDay) return 0;
        int cycleSecond = currentSecond % EPISODE_DURATION;
        return EPISODE_DURATION - cycleSecond;
    }

    public int getSecondsInCurrentPhase() {
        int cycleSecond = currentSecond % EPISODE_DURATION;
        return isDay ? cycleSecond : (cycleSecond - DAY_DURATION);
    }

    public String getFormattedTime() {
        int minutes = currentSecond / 60;
        int seconds = currentSecond % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public String getPhaseTimeRemaining() {
        int remaining = isDay ? getSecondsUntilNight() : getSecondsUntilDay();
        int minutes = remaining / 60;
        int seconds = remaining % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public static int getDayDuration() {
        return DAY_DURATION;
    }

    public static int getNightDuration() {
        return NIGHT_DURATION;
    }

    public static int getEpisodeDuration() {
        return EPISODE_DURATION;
    }

    public static void forceDay(World world) {
        if (world == null) return;
        world.setTime(MC_DAY_START);
        world.setGameRuleValue("doDaylightCycle", "false");
        world.setStorm(false);
        world.setThundering(false);
        world.setWeatherDuration(Integer.MAX_VALUE);
    }
}