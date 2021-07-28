package net.urbanmc.kingdomwars.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.TownyObject;
import com.palmergames.bukkit.towny.object.metadata.CustomDataField;
import com.palmergames.bukkit.towny.object.metadata.CustomDataFieldType;
import com.palmergames.bukkit.towny.object.metadata.IntegerDataField;
import net.urbanmc.kingdomwars.KingdomWars;
import net.urbanmc.kingdomwars.manager.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.utils.CombatUtil;
import org.bukkit.event.entity.EntityDamageEvent;

public class TownyUtil {

    public static Collection<String> getNationNames() {
        return TownyUniverse.getInstance().getNations()
                .stream().map(TownyObject::getName).collect(Collectors.toList());
    }

	public static long getNationCreationTime(Nation nation) {
		return nation.getRegistered();
	}

	public static void sendNationMessage(Nation nation, String message) {
		TownyMessaging.sendNationMessagePrefixed(nation, ChatColor.AQUA + message);
	}

	public static Nation getNation(Player p) {
    	Resident res = TownyAPI.getInstance().getResident(p.getUniqueId());

    	if (res != null)
    		return TownyAPI.getInstance().getResidentNationOrNull(res);

    	return null;
	}

	public static Nation getNation(String name) {
		Nation nation = null;

		if (name != null) {
			try {
				nation = TownyAPI.getInstance().getDataSource().getNation(name);
			} catch (NotRegisteredException ex) {
			}
		}

		return nation;
	}

	public static UUID getNationUUID(String nationName) {
		Nation nat = getNation(nationName);
		if (nat != null)
			return nat.getUuid();

		return null;
	}

	public static UUID getNationUUID(Nation nation) {
    	return nation.getUUID();
	}

	public static double getNationBalance(Nation nation) {
		return nation.getAccount().getHoldingBalance();
	}

	public static void setNationBalance(Nation nation, double balance, String reason) {
		nation.getAccount().setBalance(balance, reason);
	}
	public static void addMoneyToNation(Nation nation, double amount, String reason) {
		if (amount < 0) {
			nation.getAccount().deposit(amount, reason);
		} else if (amount > 0) {
			nation.getAccount().withdraw(amount, reason);
		}
	}


	public static void saveNation(Nation nation) {
		TownyAPI.getInstance().getDataSource().saveNation(nation);
	}

	public static boolean isNationKing(Player p) {
		try {
			return TownyAPI.getInstance().getDataSource().getResident(p.getName()).isKing();
		} catch (NotRegisteredException ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public static void truceQuestion(KingdomWars plugin, Nation receivingNation, Nation otherNation) {
		plugin.getQuestionUtil().askQuestion("Would you like to accept a truce with " + otherNation.getName() + "? You will receive $"
						+ ConfigManager.getTruceAmount() + " from their nation bank.",
				receivingNation.getUUID(),
				() -> {
					plugin.getWarManager().truceWar(receivingNation, otherNation);
				},
				() -> {
					sendNationMessage(receivingNation,
							"Your nation has declined the request to truce with " + otherNation.getName() + ".");
					sendNationMessage(otherNation, receivingNation.getName() + " has declined your request to truce.");
				},
				getOnlineInNation(receivingNation, "kingdomwars.nationstaff")
		);
	}

	public static void deleteNation(Nation nation) {
		TownyAPI.getInstance().getDataSource().removeNation(nation);
	}

	public static List<Player> getOnlineInNation(Nation nation, String permission) {
		List<Player> players = new ArrayList<>();
		List<String> residents = new ArrayList<>();

		for (Resident res : nation.getResidents()) {
			residents.add(res.getName());
		}

		for (Player p : Bukkit.getOnlinePlayers()) {
			if (residents.contains(p.getName()) && p.hasPermission(permission)) {
				players.add(p);
			}
		}

		return players;
	}

	public static boolean damageCancelled(KingdomWars plugin, Entity attacker, Entity defender) {
		return CombatUtil.preventDamageCall(plugin.getTowny(), attacker, defender, EntityDamageEvent.DamageCause.ENTITY_ATTACK);
	}

	private static int getIntegerNationMeta(Nation nation, String key, int def) {
		if (nation.hasMeta()) {
			IntegerDataField idf = nation.getMetadata(key, IntegerDataField.class);

			if (idf != null)
				return idf.getValue();
		}

		return def;
	}

	private static final String WARBLOCKS_KEY = "kwars_bonusblocks";

	// Return the number of townblocks the nation has from previous wars (can be negative)
	public static int getNationWarBlocks(Nation nation) {
		return getIntegerNationMeta(nation, WARBLOCKS_KEY, 0);
	}


	public static void addNationWarBlocks(Nation nation, int blocks) {
		if (blocks == 0)
			return;

		final String key = WARBLOCKS_KEY;

		boolean hasMeta = false;

		if (nation.hasMeta()) {
			IntegerDataField idf = nation.getMetadata(key, IntegerDataField.class);

			if (idf != null) {
				hasMeta = true;

				int newVal = idf.getValue() + blocks;

				if (newVal == 0) {
					nation.removeMetaData(idf);
				}
				else {
					if (!idf.hasLabel())
						idf.setLabel("War Blocks");

					idf.setValue(newVal);
					saveNation(nation);
				}
			}
		}

		if (!hasMeta) {
			IntegerDataField metadata = new IntegerDataField(key, blocks, "War Blocks");
			// Adding metadata automatically saves the nation
			nation.addMetaData(metadata);
		}
	}

	public static boolean isSameNation(Nation nation1, Nation nation2) {
		return nation1.equals(nation2) || nation1.getUuid().equals(nation2.getUuid());
	}
}
