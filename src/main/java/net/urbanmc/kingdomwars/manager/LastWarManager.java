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
            lastWars = new ArrayList<>();
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
        try (PrintWriter writer = new PrintWriter(
                new File("plugins/KingdomWars/last.json")))
        {
            Gson gson = new Gson();

            writer.write(gson.toJson(lastWars));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public boolean hasLast(String nation1, String nation2) {
        filterLastWars();

        for (LastWar lastWar : lastWars) {
            if (lastWar.getDeclaringNation().equals(nation1) && lastWar.getDeclaredNation().equals(nation2))
                return true;

            if (lastWar.getDeclaringNation().equals(nation2) && lastWar.getDeclaredNation().equals(nation1))
                return true;
        }

        return false;
    }

    public void addLast(LastWar lastWar) {
        // Before we add a new last war, we have to filter the list to remove previous last wars with the same two nations.
        final Set<String> names = new HashSet<>(8 / 3);

        names.add(lastWar.getDeclaringNation());
        names.add(lastWar.getDeclaredNation());

        lastWars.removeIf(war -> names.contains(war.getDeclaringNation()) && names.contains(war.getDeclaredNation()));

        lastWars.add(lastWar);
    }

    public LastWar getLast(Nation nation1, Nation nation2) {
        for (LastWar lastWar : lastWars) {
            if ((lastWar.getDeclaringNation().equals(nation1.getName()) && lastWar.getDeclaredNation().equals(nation2.getName())) ||
                    (lastWar.getDeclaringNation().equals(nation2.getName()) && lastWar.getDeclaredNation().equals(nation1.getName())))
                return lastWar;
        }

        return null;
    }

    public LastWar getLastWar(Nation nation1) {

        lastWars.removeIf((lw) ->
                !(lw.getDeclaredNation().equalsIgnoreCase(nation1.getName()) || lw.getDeclaringNation().equalsIgnoreCase(nation1.getName())));

        if (lastWars.isEmpty()) return null;

        if (lastWars.size() == 1) return lastWars.get(0);

        // Bigger millis means more recent. This sorts the list by the most recent lastwar.
        lastWars.sort(Comparator.comparingLong(LastWar::getMillisTillNextWar));

        return lastWars.get(0);
    }

    public void removeAllLastWars(String nation) {
        lastWars.removeIf(lastWar -> lastWar.getDeclaringNation().equals(nation) || lastWar.getDeclaredNation().equals(nation));
    }

    public void removeLast(LastWar lastWar) {
        lastWars.remove(lastWar);

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
        for (LastWar lastWar : lastWars) {
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
        long currentTime = System.currentTimeMillis();
        lastWars.removeIf(lastWar -> currentTime > lastWar.getMillisTillNextWar());
    }

}
