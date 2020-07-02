package net.urbanmc.kingdomwars.data;

import net.urbanmc.kingdomwars.data.war.War;
import org.bukkit.scheduler.BukkitTask;

public class PreWar extends WarAbstract {

    private boolean allies;

    private transient BukkitTask task;

    public PreWar(String declaringName, String declaredName) {
        super(declaringName, declaredName);
    }

    public String getOtherNation(String nation) {
        return isDeclaringNation(nation) ? getDeclaredNation() : getDeclaringNation();
    }

    public boolean isMainNation(String nation) {
        return isDeclaringNation(nation) || isDeclaredNation(nation);
    }

    public void setTask(BukkitTask task) {
        this.task = task;
    }

    public void cancelTask() {
        if (task != null) task.cancel();
    }

    @Override
    public boolean hasAllies() {
        return allies;
    }

    public void callAllies() {
        allies = true;
    }

    @Override
    public WarStage getWarStage() {
        return WarStage.DECLARED;
    }

    public War toFullWar() {
        War war = new War(getDeclaringNation(), getDeclaredNation());
        if (hasAllies()) {
            war.declaredAllies = this.declaredAllies;
            war.declaringAllies = this.declaringAllies;
        }

        return war;
    }


}
