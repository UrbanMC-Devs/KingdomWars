package net.urbanmc.kingdomwars.data;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class WarAbstract {
    protected String declaringName,
                   declaredName;

    protected Set<String> declaringAllies,
                        declaredAllies;

    protected long startTIme;

    public WarAbstract(String declaringName, String declaredName) {
        this.declaringName = declaringName;
        this.declaredName = declaredName;
    }

    public boolean isDeclaringNation(String nation) {
        return declaringName.equalsIgnoreCase(nation);
    }

    public String getDeclaringNation() {
        return declaringName;
    }

    public void setDeclaringNation(String name) {
        this.declaringName = name;
    }

    public boolean isDeclaredNation(String nation) {
        return declaredName.equalsIgnoreCase(nation);
    }

    public String getDeclaredNation() {
        return declaredName;
    }

    public void setDeclaredNation(String name) {
        this.declaredName = name;
    }

    public boolean isDeclaringAlly(String nation) {
        return declaringAllies != null && declaringAllies.contains(nation);
    }

    public boolean isDeclaredAlly(String nation) {
        return declaredAllies != null && declaredAllies.contains(nation);
    }

    public boolean isAlly(String nation) {
        return isDeclaredAlly(nation) || isDeclaringAlly(nation);
    }

    public boolean isOnDeclaringSide(String nation) {
        return isDeclaringNation(nation) || isDeclaringAlly(nation);
    }

    public boolean isOnDeclaredSide(String nation) {
        return isDeclaredNation(nation) || isDeclaredAlly(nation);
    }

    public boolean isInWar(String nation) {
        return isOnDeclaringSide(nation) || isOnDeclaredSide(nation);
    }

    public boolean hasAllies() {
        return (declaringAllies != null && !declaringAllies.isEmpty()) ||
                (declaredAllies != null && !declaredAllies.isEmpty());
    }

    public void addDeclaringAlly(String nation) {
        if (declaringAllies == null)
            declaringAllies = new HashSet<>();

        declaringAllies.add(nation);
    }

    public void addDeclaredAlly(String nation) {
        if (declaredAllies == null)
            declaredAllies = new HashSet<>();

        declaredAllies.add(nation);
    }

    public void removeAlly(String nation) {
        if (isDeclaringAlly(nation)) {
            declaringAllies.remove(nation);
        }
        else if (declaredAllies != null) {
            declaredAllies.remove(nation);
        }
    }

    public boolean isOnSameSide(String nation1, String nation2) {
        // Assumes that both nations are in the war
        return isOnDeclaringSide(nation1) == isOnDeclaringSide(nation2);
    }

    public Collection<String> getDeclaringAllies() {
        if (declaringAllies == null)
            return Collections.emptySet();

        return declaringAllies;
    }

    public Collection<String> getDeclaredAllies() {
        if (declaredAllies == null)
            return Collections.emptySet();

        return declaredAllies;
    }

    public Collection<String> getAllies() {
        Collection<String> allies = new HashSet<>();

        allies.addAll(getDeclaringAllies());
        allies.addAll(getDeclaredAllies());

        return allies;
    }

    public Collection<String> getAllParticipatingNations() {
        Set<String> nationNames = new HashSet<>(2);
        nationNames.add(declaringName);
        nationNames.add(declaredName);

        nationNames.addAll(getDeclaringAllies());
        nationNames.addAll(getDeclaredAllies());

        return nationNames;
    }

    public void renameNation(String oldName, String newName) {
        if (isDeclaringNation(oldName)) {
            setDeclaringNation(newName);
        }
        else if(isDeclaredNation(oldName)) {
            setDeclaredNation(newName);
        }
        else if (isDeclaringAlly(oldName)) {
            declaringAllies.remove(oldName);
            addDeclaringAlly(newName);
        }
        else if (isDeclaredAlly(oldName)) {
            declaredAllies.remove(oldName);
            addDeclaredAlly(newName);
        }
    }

    public void setStartTime(long time) {
        this.startTIme = time;
    }

    public long getStartTIme() {
        return startTIme;
    }

    public abstract WarStage getWarStage();
}
