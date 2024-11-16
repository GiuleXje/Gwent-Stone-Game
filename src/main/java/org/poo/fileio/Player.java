package org.poo.fileio;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;

public class Player {
	private static final ObjectMapper objectMapper = new ObjectMapper();
	private ArrayList<CardInput> deck;
	private int mana;
	private ArrayList<CardInput> inHand;
	private CardInput hero;
	private boolean myTurn;
	private int wins;

	public Player() {
		myTurn = false;
		mana = 0;
		inHand = new ArrayList<CardInput>();
		deck = new ArrayList<CardInput>();
		wins = 0;
	}

	public void setDeck(ArrayList<CardInput> deck) {
		this.deck = deck;
	}
	public void setInHand(ArrayList<CardInput> inHand) {
		this.inHand = inHand;
	}
	public void setHero(CardInput hero) {
		this.hero = hero;
	}
	public void setMana(int mana) {
		this.mana = mana;
	}
	public void setMyTurn(boolean myTurn) {
		this.myTurn = myTurn;
	}
	public ArrayList<CardInput> getDeck() {
		return deck;
	}
	public ArrayList<CardInput> getInHand() {
		return inHand;
	}
	public CardInput getHero() {
		return hero;
	}
	public int getMana() {
		return mana;
	}
	public boolean isMyTurn() {
		return myTurn;
	}
	public int getWins() {
		return wins;
	}
	public void setWins(int wins) {
		this.wins = wins;
	}

	public void iJustWon() { wins++; }
	public void setDeckUnused() {
		for (CardInput card : deck) {
			card.setFrozen(false);
			card.setUsed(false);
			card.setUsedAbility(false);
		}
	}

	public void add_inHand(CardInput inHand) {
		this.inHand.add(inHand);
	}
	public ArrayNode printDeck() {
		ArrayNode deckOutput = objectMapper.createArrayNode();
		for (CardInput card : deck) {
			ObjectNode cardOutput = objectMapper.createObjectNode();
			cardOutput.put("mana", card.getMana());
			cardOutput.put("attackDamage", card.getAttackDamage());
			cardOutput.put("health", card.getHealth());
			cardOutput.put("description", card.getDescription());
			ArrayNode colorsNode = objectMapper.createArrayNode();
			for (String color : card.getColors()) {
				colorsNode.add(color);
			}
			cardOutput.set("colors", colorsNode);
			cardOutput.put("name", card.getName());
			deckOutput.add(cardOutput);
		}
		return deckOutput;
	}

	public ObjectNode printHero() {
		ObjectNode heroOutput = objectMapper.createObjectNode();
		heroOutput.put("mana", hero.getMana());
		heroOutput.put("description", hero.getDescription());
		ArrayNode heroColorsNode = objectMapper.createArrayNode();
		for (String color : hero.getColors()) {
			heroColorsNode.add(color);
		}
		heroOutput.set("colors", heroColorsNode);
		heroOutput.put("name", hero.getName());
		heroOutput.put("health", hero.getHealth());
		return heroOutput;
	}

	public ArrayNode printInHand() {
		ArrayNode deckOutput = objectMapper.createArrayNode();
		for (CardInput card : inHand) {
			ObjectNode inHandOutput = objectMapper.createObjectNode();
			inHandOutput.put("mana", card.getMana());
			inHandOutput.put("attackDamage", card.getAttackDamage());
			inHandOutput.put("health", card.getHealth());
			inHandOutput.put("description", card.getDescription());
			ArrayNode colorsNode = objectMapper.createArrayNode();
			for (String color : card.getColors()) {
				colorsNode.add(color);
			}
			inHandOutput.set("colors", colorsNode);
			inHandOutput.put("name", card.getName());
			deckOutput.add(inHandOutput);
		}
		return deckOutput;
	}

	public void addMana(int mana) {
		this.mana += mana;
	}

}
