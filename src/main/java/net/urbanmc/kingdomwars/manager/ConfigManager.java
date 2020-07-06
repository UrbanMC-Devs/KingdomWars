package net.urbanmc.kingdomwars.manager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;

public class ConfigManager {

    private static double startAmount, winAmount, loseAmount, truceAmount;
    private static int winningKills, allyKills,
                       townBlockBonus, townBlockLoss, townBlockMin, townBlockMax,
                       startDelay, allyStartDelay,
                       gracePeriod;
    private static long lastTime, lastTimeRevenge, endTime;

    public ConfigManager() {
        loadConfig();
    }

    private void loadConfig() {
        File file = new File("plugins/KingdomWars/config.yml");

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdir();
        }

        if (!file.exists()) {
            try {
                InputStream input = getClass().getClassLoader().getResourceAsStream("config.yml");

                Files.copy(input, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        FileConfiguration data = YamlConfiguration.loadConfiguration(file);

        gracePeriod = data.getInt("grace-period", 10);

        startDelay = data.getInt("preparation-time", 5);
        allyStartDelay = data.getInt("ally-prep-time", 10);

        startAmount = data.getDouble("start-amount", 25000);
        winAmount = data.getDouble("win-amount", 100000);
        loseAmount = data.getDouble("lose-amount", 10000);
        truceAmount = data.getDouble("truce-amount", 50000);

        winningKills = data.getInt("winning-kills", 25);
        allyKills = data.getInt("ally-bonus-kills", 5);

        lastTime = TimeUnit.HOURS.toMillis(data.getInt("hours-between", 168));
        lastTimeRevenge = TimeUnit.HOURS.toMillis(data.getInt("hours-between-revenge", 72));
        endTime = TimeUnit.HOURS.toMillis(data.getInt("hours-end", 168));

        townBlockBonus = data.getInt("town-block-bonus", 10);
        townBlockLoss = data.getInt("town-block-loss", 10);
        townBlockMin = data.getInt("town-block-min", 40);
        townBlockMax = data.getInt("town-block-max", 90);
    }

    public static double getStartAmount() {
        return startAmount;
    }

    public static double getWinAmount() {
        return winAmount;
    }

    public static double getLoseAmount() { return loseAmount; }

    public static double getTruceAmount() {
        return truceAmount;
    }

    public static int getWinningKills() {
        return winningKills;
    }

    public static int getAllyKills() { return allyKills; }

    public static int getStartDelay() {
        return startDelay;
    }

    public static int getAllyStartDelay() {
        return allyStartDelay;
    }

    public static long getLastTime() {
        return lastTime;
    }

    public static long getLastTimeRevenge() {
        return lastTimeRevenge;
    }

    public static long getEndTime() {
        return endTime;
    }

    public static int getTownBlockBonus() { return townBlockBonus; }

    public static int getTownBlockLoss() { return townBlockLoss; }

    public static int getNegTownBlockMin() { return -1 * townBlockMin; }

    public static int getMaxTownBlocksWin() { return townBlockMax; }

    public static int getGracePeriod() {
        return gracePeriod;
    }

    // Time is in seconds
    public static String formatTime(long time) {
        StringBuilder output = new StringBuilder();

        int days = (int) TimeUnit.SECONDS.toDays(time);
        time -= TimeUnit.DAYS.toSeconds(days);
        appendTime(output, days, "day");

        int hours = (int) TimeUnit.SECONDS.toHours(time);
        time -= TimeUnit.HOURS.toSeconds(hours);
        appendTime(output, hours, "hour");

        int minutes = (int) TimeUnit.SECONDS.toMinutes(time);
        time -= TimeUnit.MINUTES.toSeconds(minutes);
        appendTime(output, minutes, "minute");

        int seconds = (int) time;
        appendTime(output, seconds, "second");

        return output.toString().trim();
    }

    private static void appendTime(StringBuilder builder, int time, String unit) {
        if (time > 0)
            builder.append(time).append(" ").append(unit).append(time > 1 ? "s " : " ");
    }

}
