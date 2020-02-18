package net.urbanmc.kingdomwars.manager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.palmergames.bukkit.towny.object.Nation;
import net.urbanmc.kingdomwars.data.LastWar;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class LastWarManager {

    private List<LastWar> lastWars;

    public void loadLastWars() {
        final File FILE = new File("plugins/KingdomWars/last.json");

        if (!FILE.exists()) {
            try {
                FILE.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        else {
            try (Scanner scanner = new Scanner(FILE)) {

                Gson gson = new Gson();

                Type lastWarListType = new TypeToken<ArrayList<LastWar>>() {
                }.getType();

                lastWars = gson.fromJson(scanner.nextLine(), lastWarListType);
            } catch (Exception ignored) {
            }
        }
    }

    public void saveLastWars() {
        if (lastWars == null) return; // Prevent save for empty list

        try (PrintWriter writer = new PrintWriter(
                new File("plugins/KingdomWars/last.json")))
        {
            Gson gson = new Gson();

            writer.write(gson.toJson(lastWars));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Lazy Loading method. Return empty array list if last wars is null
    private List<LastWar> getLastWars() {
        return lastWars == null ? new ArrayList<>() : lastWars;
    }

    public boolean hasLast(String nation1, String nation2) {
        filterLastWars();

        List<LastWar> scopeLastWars = getLastWars();


        for (LastWar lastWar : scopeLastWars) {
            if (lastWar.getDeclaringNation().equals(nation1) && lastWar.getDeclaredNation().equals(nation2))
                return true;

            if (lastWar.getDeclaringNation().equals(nation2) && lastWar.getDeclaredNation().equals(nation1))
                return true;
        }

        return false;
    }

    public void addLast(LastWar lastWar) {
        if (lastWars == null) lastWars = new ArrayList<>(); // Create new array list if none there

        // Before we add a new last war, we have to filter the list to remove previous last wars with the same two nations.
        final Set<String> names = new HashSet<>(8 / 3);

        names.add(lastWar.getDeclaringNation());
        names.add(lastWar.getDeclaredNation());

        lastWars.removeIf(war -> war.getDeclaringNation() != null && names.contains(war.getDeclaringNation())
                && war.getDeclaredNation() != null && names.contains(war.getDeclaredNation()));

        lastWars.add(lastWar);

        saveLastWars();
    }

    public LastWar getLast(Nation nation1, Nation nation2) {
        for (LastWar lastWar : getLastWars()) {
            if ((lastWar.getDeclaringNation().equals(nation1.getName()) && lastWar.getDeclaredNation().equals(nation2.getName())) ||
                    (lastWar.getDeclaringNation().equals(nation2.getName()) && lastWar.getDeclaredNation().equals(nation1.getName())))
                return lastWar;
        }

        return null;
    }

    public LastWar getLastWar(Nation nation1) {

        List<LastWar> copyList = new ArrayList<>(getLastWars());

        copyList.removeIf((lw) ->
                !(lw.getDeclaredNation().equalsIgnoreCase(nation1.getName()) || lw.getDeclaringNation().equalsIgnoreCase(nation1.getName())));

        if (copyList.isEmpty()) return null;

        if (copyList.size() == 1) return copyList.get(0);

        // Bigger millis means more recent. This sorts the list by the most recent lastwar.
        copyList.sort(Comparator.comparingLong(LastWar::getMillisTillNextWar));

        return copyList.get(0);
    }

    public void removeAllLastWars(String nation) {
        getLastWars().removeIf(lastWar -> lastWar.getDeclaringNation().equals(nation) || lastWar.getDeclaredNation().equals(nation));
    }

    public void removeLast(LastWar lastWar) {
        getLastWars().remove(lastWar);

        saveLastWars();
    }

    public boolean canRevenge(Nation nation1, Nation nation2) {
        LastWar lastWar = getLast(nation1, nation2);

        if (lastWar == null)
            return false;

        if (!(lastWar.isDeclaringWinner() && lastWar.isDeclaringNation(nation2.getName())))
            return false;

        return System.currentTimeMillis() >= lastWar.getRevengeMillis();
    }

    public void lastNationRename(String oldName, String newName) {
        for (LastWar lastWar : getLastWars()) {
            if (!lastWar.getDeclaringNation().equals(oldName) || lastWar.getDeclaredNation().equals(oldName))
                continue;

            boolean declaring = lastWar.isDeclaringNation(oldName);

            if (declaring) {
                lastWar.setDeclaringNation(newName);
            } else {
                lastWar.setDeclaredNation(newName);
            }
        }

        saveLastWars();
    }

    public void filterLastWars() {
        if (lastWars == null) return; // Might be null for lazy loading

        long currentTime = System.currentTimeMillis();
        lastWars.removeIf(lastWar ->
                lastWar == null ||
                currentTime > lastWar.getMillisTillNextWar());
    }

}
