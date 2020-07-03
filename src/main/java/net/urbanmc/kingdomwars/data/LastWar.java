package net.urbanmc.kingdomwars.data;

import net.urbanmc.kingdomwars.data.war.War;

import java.util.UUID;

public class LastWar extends WarAbstract {

    private int archiveID = 0;
    private boolean isDeclaringWinner;
    private transient UUID declaringUUID, declaredUUID;
    private long startTime, endTime;
    private double monetaryReward,
                   monetaryLoss;
    private int townBlockReward,
                townBlockLoss;

    private int declaringPoints, declaredPoints;
    private WarResult result;

    public LastWar() {
        super("", "");
    }

    public LastWar(War war, WarReward warReward, UUID declaringUUID, UUID declaredUUID) {
        super(war.getDeclaringNation(), war.getDeclaredNation());

        this.declaringUUID = declaringUUID;
        this.declaredUUID = declaredUUID;

        this.declaringPoints = war.getDeclaringPoints();
        this.declaredPoints = war.getDeclaredPoints();

        this.startTime = war.getStartTIme();
        this.endTime = System.currentTimeMillis();

        this.monetaryReward = warReward.getMonetaryReward();
        this.monetaryLoss = warReward.getMonetaryLoss();
        this.townBlockReward = warReward.getTownblockWon();
        this.townBlockLoss = warReward.getTownblocksLoss();

        this.isDeclaringWinner = declaringPoints > declaredPoints;
    }

    public  LastWar(int archiveID,
                   String declaringNation, String declaredNation,
                   int declaringPoints, int declaredPoints,
                   long startTime, long endTime,
                   double moneyWon, double moneyLoss,
                   int townblocksWon, int townblocksLost,
                   WarResult result) {
        super(declaringNation, declaredNation);
        this.archiveID = archiveID;

        this.declaringPoints = declaringPoints;
        this.declaredPoints = declaredPoints;

        this.startTime = startTime;
        this.endTime = endTime;

        this.townBlockReward = townblocksWon;
        this.townBlockLoss = townblocksLost;

        this.monetaryReward = moneyWon;
        this.monetaryLoss = moneyLoss;

        this.result = result;
    }

    private String getLoser() {
        if (isDeclaringWinner)
            return getDeclaredNation();
        else
            return getDeclaredNation();
    }

    public boolean wasTruce() {
        return result == WarResult.TRUCE;
    }

    public long getEndTime() {
        return endTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public boolean isDeclaringWinner() {
        return isDeclaringWinner;
    }

    public boolean setDeclaringWinner() {
        return isDeclaringWinner;
    }

    public boolean isLosingNation(String nation) {
        return getLoser().equals(nation);
    }

    public boolean foughtWar(String nation) {
        return isDeclaringNation(nation) || isDeclaredNation(nation);
    }

    public boolean foughtWar(String nation1, String nation2) {
        return foughtWar(nation1) && foughtWar(nation2);
    }

    public int getDeclaringPoints() {
        return declaringPoints;
    }

    public int getDeclaredPoints() {
        return declaredPoints;
    }

    public double getMoneyWon() {
        return monetaryLoss;
    }

    public double getMoneyLost() {
        return monetaryReward;
    }

    public int getTownblocksWon() {
        return townBlockReward;
    }

    public int getTownblocksLost() {
        return townBlockLoss;
    }

    public void setArchiveID(int id) {
        this.archiveID = id;
    }

    public int getArchiveID() {
        return archiveID;
    }

    @Override
    public void removeAlly(String nation) {
        throw new UnsupportedOperationException("Cannot remove allies from an archived war!");
    }

    @Override
    public WarStage getWarStage() {
        return WarStage.ARCHIVED;
    }

    public WarResult getResult() {
        return result;
    }

    public void setResult(WarResult result) {
        this.result = result;
    }

    public UUID getDeclaringUUID() {
        return declaringUUID;
    }

    public UUID getDeclaredUUID() {
        return declaredUUID;
    }
}
