package net.urbanmc.kingdomwars;

import java.util.ArrayList;
import java.util.List;

import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.TownyUniverse;

import ca.xshade.questionmanager.Option;
import ca.xshade.questionmanager.Question;
import ca.xshade.questionmanager.QuestionTask;

public class TownyUtil {

	public static void sendNationMessage(Nation nation, String message) {
		TownyMessaging.sendNationMessage(nation, message);
	}

	public static Nation getNation(String name) {
		Nation nation = null;

		try {
			nation = TownyUniverse.getDataSource().getNation(name);
		} catch (NotRegisteredException ex) {
			;
		}

		if (nation == null) {
			for (Nation dataNation : TownyUniverse.getDataSource().getNations()) {
				if (dataNation.getName().equalsIgnoreCase(name)) {
					nation = dataNation;
					break;
				}
			}
		}

		return nation;
	}
	
	public void truceQuestion(String playername) {
	    List<Option> options = new ArrayList<Option>();
	    options.add(new Option("accept", new QuestionTask() {
	      public void run() {
	        
	      }
	    }));
	    options.add(new Option("deny", new QuestionTask() {
	      public void run() {
	        
	      }
	    }));
	    Question question = new Question(playername, "Would you like to accept a truce with %nation%? You will receive %configTruceAmount% from their nation bank.", options);
	    try {
	      KingdomWars.questionerplugin.getQuestionManager().appendQuestion(question);
	    } catch (Exception e) {
	      System.out.println(e.getMessage());
	    }
	}
}
