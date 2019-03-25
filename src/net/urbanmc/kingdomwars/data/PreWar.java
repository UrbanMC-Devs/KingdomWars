package net.urbanmc.kingdomwars.data;

import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class PreWar {

    private String declaringNation,declaredNation;

    private boolean allies;
    private List<String> nation1Allies = new ArrayList<>(), nation2Allies = new ArrayList<>();

    private BukkitTask task;

    public PreWar(String declaring, String declared) {
        declaringNation = declaring;
        declaredNation = declared;
    }

    public boolean alreadyDeclared(String nation) {
        if (declaringNation.equalsIgnoreCase(nation) || declaredNation.equalsIgnoreCase(nation)) return true;

        return isAllied(nation);
    }

    public void addAlly(boolean declaring, String ally) {
        List<String> allies = declaring ? nation1Allies : nation2Allies;

        allies.add(ally);
    }

    public boolean isAllied(String ally) {
        return (nation1Allies.contains(ally) || nation2Allies.contains(ally));
    }

    public String getDeclaringNation() { return declaringNation; }

    public String getDeclaredNation() { return declaredNation; }

    public String getOtherNation(String nation) {
        if (declaredNation.equalsIgnoreCase(nation)) return declaringNation;

        if (declaringNation.equalsIgnoreCase(nation)) return declaredNation;

        return declaringNation;
    }

    public boolean isMainNation(String nation) {
        return (declaringNation.equalsIgnoreCase(nation) || declaredNation.equalsIgnoreCase(nation));
    }

    public List<String> getAllies(boolean declaring) {
        return declaring ? nation1Allies : nation2Allies;
    }

    public void setTask(BukkitTask task) {
        this.task = task;
    }

    public void cancelTask() {
        if (task != null) task.cancel();
    }

    public boolean calledForAllies() {
        return allies;
    }

    public void callAllies() {
        allies = true;
    }

    public void renameNation(String oldName, String newName) {
        if (declaringNation.equalsIgnoreCase(oldName)) {
            declaringNation = newName;
            return;
        }

        if (declaredNation.equalsIgnoreCase(oldName)) {
            declaredNation = newName;
            return;
        }

        List<String> allies = nation1Allies.contains(oldName) ? nation1Allies : nation2Allies;

        allies.set(allies.indexOf(oldName), newName);
    }




}
