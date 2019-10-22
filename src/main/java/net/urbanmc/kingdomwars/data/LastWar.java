package net.urbanmc.kingdomwars.data;

public class LastWar {

    private String declaringNation;
    private String declaredNation;
    private boolean isDeclaringWinner;
    private boolean truce;
    private long millisTillNextWar, revengeMillis;

    public LastWar(String declaringNation, String declaredNation, boolean isDeclaringWinner, boolean truce,
                   long millis, long revengeMillis) {
        this.declaringNation = declaringNation;
        this.declaredNation = declaredNation;
        this.isDeclaringWinner = isDeclaringWinner;
        this.truce = truce;
        this.millisTillNextWar = millis;
        this.revengeMillis = revengeMillis;
    }

    public String getDeclaringNation() {
        return declaringNation;
    }

    public void setDeclaringNation(String nation) {
        declaringNation = nation;
    }

    public String getDeclaredNation() {
        return declaredNation;
    }

    public void setDeclaredNation(String nation) {
        declaredNation = nation;
    }

    private String getLoser() {
        if (isDeclaringWinner)
            return declaredNation;
        else
            return declaringNation;
    }

    public boolean wasTruce() {
        return truce;
    }

    public long getMillisTillNextWar() {
        return millisTillNextWar;
    }

    public long getRevengeMillis() {
        return revengeMillis;
    }

    public boolean isDeclaringNation(String nation) {
        return declaringNation.equals(nation);
    }

    public boolean isDeclaringWinner() {
        return isDeclaringWinner;
    }

    public boolean isLosingNation(String nation) {
        return getLoser().equals(nation);
    }

    public void setRevengeMillis(long millis) {
        this.revengeMillis = millis;
    }
}
