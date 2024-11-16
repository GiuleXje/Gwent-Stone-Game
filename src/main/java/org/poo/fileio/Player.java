package org.poo.fileio;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;

public class Player {
	private static final ObjectMapper OBJECTMAPPER = new ObjectMapper();
	private ArrayList<CardInput> deck;
	private int mana;
	private ArrayList<CardInput> inHand;
	private CardInput hero;
	private boolean myTurn;
	private int wins;
	private static final int HERO_HEALTH = 30;

	public Player() {
		myTurn = false;
		mana = 1;
		inHand = new ArrayList<CardInput>();
		wins = 0;
	}

	/**
	 * sets the player's game deck
	 * @param deck
	 * the deck of cards
	 */
	public void setDeck(final ArrayList<CardInput> deck) {
		this.deck = deck;
	}

	/**
	 * sets the first card at the beginning of a new game
	 * @param card
	 * the first card
	 */
	public void initInHand(final CardInput card) {
		inHand.add(card);
	}

	/**
	 * copies a player's deck so it doesn't affect the input given deck
	 * @param deck
	 * the deck that will be used by the player for this current game
	 */
	public void deepCopyDeck(final ArrayList<CardInput> deck) {
		this.deck = new ArrayList<>();
		for (CardInput card : deck) {
			this.deck.add(card.clone());
		}
		setDeckUnused();
	}
	/**
	 * sets the first card that can be used by a player
	 * @param inHand
	 * the cards
	 */
	public void setInHand(final ArrayList<CardInput> inHand) {
		this.inHand = inHand;
	}

	/**
	 * sets the player's hero
	 * @param hero
	 *  the hero card
	 */
	public void setHero(final CardInput hero) {
		this.hero = hero;
		this.hero.setUsed(false);
		this.hero.setUsedAbility(false);
		this.hero.setFrozen(false);
		this.hero.setHealth(HERO_HEALTH);
	}

	/**
	 * sets player's mana
	 * @param mana
	 *  mana
	 */
	public void setMana(final int mana) {
		this.mana = mana;
	}

	/**
	 * sets a player's turn
	 * @param myTurn
	 * true is it's the players turn, false otherwise
	 */
	public void setMyTurn(final boolean myTurn) {
		this.myTurn = myTurn;
	}

	/**
	 *
	 * @return
	 * returns the cards that the player has
	 */
	public ArrayList<CardInput> getDeck() {
		return deck;
	}

	/**
	 *
	 * @return
	 * return the cards that are still placed in the player's hand
	 */
	public ArrayList<CardInput> getInHand() {
		return inHand;
	}

	/**
	 *
	 * @return
	 * returns the hero card of the player
	 */
	public CardInput getHero() {
		return hero;
	}

	/**
	 *
	 * @return
	 * return the mana of the player
	 */
	public int getMana() {
		return mana;
	}

	/**
	 *
	 * @return
	 * returns true if it's the player's turn, false otherwise
	 */
	public boolean isMyTurn() {
		return myTurn;
	}

	/**
	 *
	 * @return
	 * returns the number of games won by a player
	 */
	public int getWins() {
		return wins;
	}

	/**
	 * sets the wins
	 * @param wins
	 * number of wins
	 */
	public void setWins(final int wins) {
		this.wins = wins;
	}

	/**
	 * increments the number of games won
	 */
	public void iJustWon() { wins++; }

	/**
	 * done at the end of a player's turn to unmark all the cards that were used
	 */
	public void setDeckUnused() {
		for (CardInput card : deck) {
			card.setFrozen(false);
			card.setUsed(false);
			card.setUsedAbility(false);
		}
	}

	/**
	 * add a card to deck of cards held in hand
	 * @param inHand
	 * the card to be added
	 */
	public void addInHand(final CardInput inHand) {
		this.inHand.add(inHand);
	}

	/**
	 * prints a player's deck
	 * @return
	 * return the ArrayNode used for the output in JSON
	 */
	public ArrayNode printDeck() {
		ArrayNode deckOutput = OBJECTMAPPER.createArrayNode();
		for (CardInput card : deck) {
			ObjectNode cardOutput = OBJECTMAPPER.createObjectNode();
			cardOutput.put("mana", card.getMana());
			cardOutput.put("attackDamage", card.getAttackDamage());
			cardOutput.put("health", card.getHealth());
			cardOutput.put("description", card.getDescription());
			ArrayNode colorsNode = OBJECTMAPPER.createArrayNode();
			for (String color : card.getColors()) {
				colorsNode.add(color);
			}
			cardOutput.set("colors", colorsNode);
			cardOutput.put("name", card.getName());
			deckOutput.add(cardOutput);
		}
		return deckOutput;
	}

	/**
	 * prints the player's hero card
	 * @return
	 * the ObjectNode used for the output in JSON
	 */
	public ObjectNode printHero() {
		ObjectNode heroOutput = OBJECTMAPPER.createObjectNode();
		heroOutput.put("mana", hero.getMana());
		heroOutput.put("description", hero.getDescription());
		ArrayNode heroColorsNode = OBJECTMAPPER.createArrayNode();
		for (String color : hero.getColors()) {
			heroColorsNode.add(color);
		}
		heroOutput.set("colors", heroColorsNode);
		heroOutput.put("name", hero.getName());
		heroOutput.put("health", hero.getHealth());
		return heroOutput;
	}

	/**
	 * prints the cards held in hand
	 * @return
	 * return the ArrayNode used for the output in JSON
	 */
	public ArrayNode printInHand() {
		ArrayNode deckOutput = OBJECTMAPPER.createArrayNode();
		for (CardInput card : inHand) {
			deckOutput.add(card.cardInfo());
		}
		return deckOutput;
	}

	/**
	 * increments a player's mana
	 * @param mana
	 * is added to the current mana
	 */
	public void addMana(final int mana) {
		this.mana += mana;
	}
}
