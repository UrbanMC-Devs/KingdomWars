package net.urbanmc.kingdomwars.data;

import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.object.Nation;
import net.urbanmc.kingdomwars.manager.ConfigManager;
import net.urbanmc.kingdomwars.util.TownyUtil;

public class WarReward {

    private double rewardFactor = 1;

    private double monetaryReward = 0,
                   monetaryLoss = 0;

    private int townblockWon = 0,
                townblocksLoss = 0;

    private boolean didTruce = false,
                    rewardMoney = true,
                    rewardTownBlocks = true,
                    returnStartingCost = true;


    public void process(Nation winner, Nation loser) {
        if (rewardTownBlocks)
            rewardTownBlocks(winner, loser);

        if (rewardMoney)
            rewardMoney();
    }

    public WarReward setTruce(boolean b) {
        this.didTruce = b;
        return this;
    }

    public boolean didTruce() {
        return didTruce;
    }

    public WarReward setRewardFactor(double factor) {
        this.rewardFactor = factor;
        return this;
    }

    public WarReward rewardMoney(boolean b) {
        this.rewardMoney = b;
        return this;
    }

    public WarReward returnStartingCost(boolean b) {
        this.returnStartingCost = b;
        return this;
    }

    public WarReward rewardTownBlocks(boolean b) {
        this.rewardTownBlocks = b;
        return this;
    }

    public double getMonetaryReward() {
        return monetaryReward;
    }

    public double getMonetaryLoss() {
        return monetaryLoss;
    }

    public int getTownblockWon() {
        return townblockWon;
    }

    public int getTownblocksLoss() {
        return townblocksLoss;
    }


    private void rewardMoney() {
        double winAmount,
               loseAmount;

        if (didTruce) {
            winAmount = loseAmount = ConfigManager.getTruceAmount();
        }
        else {
            winAmount = ConfigManager.getWinAmount();
            loseAmount = ConfigManager.getLoseAmount();
        }

        winAmount *= rewardFactor;
        loseAmount *= rewardFactor;

        if (returnStartingCost) {
            winAmount += ConfigManager.getStartAmount();
        }

        this.monetaryReward = winAmount;
        this.monetaryLoss = loseAmount;
    }

    private void rewardTownBlocks(Nation winner, Nation loser) {
        // Townblock Bonus
        // Behavioural Restraints:
        // If a losing nation has less than the town block minimum, then don't give the winner town blocks
        // If a losing nation can't give all the town blocks, make the winning townblocks proportional to the ones lost
        // Winning townblock cap, give townblocks up to the cap.
        // Half-war rewards

        int negTownBlockMin = ConfigManager.getNegTownBlockMin();
        int townBlockLoss = ConfigManager.getTownBlockLoss();
        int townBlockWinBonus = ConfigManager.getTownBlockBonus();

        negTownBlockMin *= rewardFactor;
        townBlockLoss *= rewardFactor;
        townBlockWinBonus *= rewardFactor;

        // transferTownBlock boolean checks whether we should award/take townblocks from the winning/losing nations.
        // Check if the losing nation bonus is greater than than the minimum
        boolean transferTownBlocks = TownyUtil.getNationWarBlocks(loser) > negTownBlockMin;

        // BonusDifference is positive if losing nation cannot afford to lose any more townblocks
        // We expect the nation bonus blocks to be a positive number and the townblock loss to be a smaller number
        // than the nation blocks
        int bonusDifference = townBlockLoss - TownySettings.getNationBonusBlocks(loser);

        // If the bound difference is 0 that means the nation has 0 nation bonus and we don't want to transfer townblocks.
        transferTownBlocks &= bonusDifference != 0 && townBlockLoss > bonusDifference ;

        // If the bonus difference is positive then that means they can't afford to give all the town blocks.
        if (bonusDifference > 0) {
            // Reduce the win bonus proportionally to the ratio between the win bonus and the take amount.
            // 30 : 10 * x : 3
            // (townBlockWinBonus * bonusDifference) / townBlockLoss
            townBlockWinBonus = (townBlockWinBonus * bonusDifference) / townBlockLoss;
            townBlockLoss = bonusDifference;
        }

        if (transferTownBlocks) {
            if (winner != null) {
                int maxTownBlocks = ConfigManager.getMaxTownBlocksWin();
                int currentWinnerWarBlocks = TownyUtil.getNationWarBlocks(winner);

                if ((maxTownBlocks  - currentWinnerWarBlocks) < townBlockWinBonus) {
                    int townBlockDifference = maxTownBlocks - currentWinnerWarBlocks;

                    townBlockWinBonus = Math.max(townBlockDifference, 0);
                }

                if (townBlockWinBonus > 0) {
                    this.townblockWon = townBlockWinBonus;
                }
            }

            this.townblocksLoss = townBlockLoss;
        }
    }


}
