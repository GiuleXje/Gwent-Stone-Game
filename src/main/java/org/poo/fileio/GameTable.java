package org.poo.fileio;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;

public class GameTable {
	private ArrayList<ArrayList<CardInput>> table;
	private static final ObjectMapper objectMapper = new ObjectMapper();
	public static final int LINELIMIT = 4;
	public static final int COLLIMIT = 5;

	public GameTable() {
		table = new ArrayList<>(LINELIMIT);
		for (int i = 0;  i < LINELIMIT; i++) {
			ArrayList<CardInput> row = new ArrayList<>(COLLIMIT);
			for (int j = 0; j < COLLIMIT; j++) {
				row.add(null);
			}
			table.add(row);
		}
	}

	/**
	 * sets up the game table
	 * @param table
	 * the matrix in which the cards are placed
	 */
	public void setTable(ArrayList<ArrayList<CardInput>> table) {
		this.table = table;
	}

	/**
	 *
	 * @return
	 * returns the game table
	 */
	public ArrayList<ArrayList<CardInput>> getTable() {
		return table;
	}

	/**
	 *
	 * @return
	 * return the number of lines of the game table
	 */
	public int getLINELIMIT() { return LINELIMIT; }

	/**
	 *
	 * @return
	 * return the number of columns of the game table
	 */
	public int getCOLLIMIT() { return COLLIMIT; }

	/**
	 * places a card on the table
	 * @param player
	 * used to know which rows we card place the card into
	 * @param card
	 * the card to be placed
	 * @return
	 * return 0 if it can find a free spot, 1 otherwise
	 */
	public int place_card(int player, CardInput card) {
		if (player == 1) {
			if (card.getName().equals("Sentinel") || card.getName().equals("Berserker")
					|| card.getName().equals("The Cursed One")
					|| card.getName().equals("Disciple")) {
				for (int i = 0; i < COLLIMIT; i++) {
					if (table.get(3).get(i) == null) {
						table.get(3).set(i, card);
						return 0;
					}
				}
			} else if (card.getName().equals("Goliath") || card.getName().equals("Warden")
					|| card.getName().equals("The Ripper") || card.getName().equals("Miraj")) {
				for (int i = 0; i < COLLIMIT; i++) {
					if (table.get(2).get(i) == null) {
						table.get(2).set(i, card);
						return 0;
					}
				}
			}
		} else {
			if (card.getName().equals("Sentinel") || card.getName().equals("Berserker")
					||card.getName().equals("The Cursed One")
					|| card.getName().equals("Disciple")) {
				for (int i = 0; i < COLLIMIT; i++) {
					if (table.getFirst().get(i) == null) {
						table.getFirst().set(i, card);
						return 0;
					}
				}
			} else if (card.getName().equals("Goliath") || card.getName().equals("Warden")
					|| card.getName().equals("The Ripper")
					|| card.getName().equals("Miraj")) {
				for (int i = 0; i < COLLIMIT; i++) {
					if (table.get(1).get(i) == null) {
						table.get(1).set(i, card);
						return 0;
					}
				}
			}
		}
		return 1;
	}

	/**
	 * prints the game table
	 * @return
	 * return the ArrayNode needed for the JSON output
	 */
	public ArrayNode printTable() {
		ArrayNode tableOutput = objectMapper.createArrayNode();

		for (int i = 0; i < LINELIMIT; i++) {
			ArrayNode rowOutput = objectMapper.createArrayNode();
			for (int j = 0; j < COLLIMIT; j++) {
				if (table.get(i).get(j) != null) {
					CardInput card = table.get(i).get(j);
					ObjectNode cardOutput = objectMapper.createObjectNode();
					cardOutput.put("mana", card.getMana());
					cardOutput.put("attackDamage", card.getAttackDamage());
					cardOutput.put("health", card.getHealth());
					cardOutput.put("description", card.getDescription());

					// Add colors array
					ArrayNode colorsNode = objectMapper.createArrayNode();
					for (String color : card.getColors()) {
						colorsNode.add(color);
					}
					cardOutput.set("colors", colorsNode);
					cardOutput.put("name", card.getName());

					rowOutput.add(cardOutput);
				} else {
					break;
				}
			}
			tableOutput.add(rowOutput);
		}

		return tableOutput;
	}

	/**
	 * checks if a card belongs to the opponent
	 * @param opponent
	 * the player
	 * @param x
	 * the line of the card
	 * @param y
	 * the column of the card
	 * @return
	 * return true if the card belongs to the opponent, false otherwise
	 */
	public boolean isOpponentCard(int opponent, final int x, final int y) {
		if (opponent == 1) {
			return (x >= 2 && x < LINELIMIT) && (y >= 0 && y < COLLIMIT)
					&& (table.get(x).get(y) != null);
		}
		return (x >= 0 && x <= 1) && (y >= 0 && y < COLLIMIT) && (table.get(x).get(y) != null);
	}

	/**
	 * checks if the card belongs to the attacker
	 * @param attacker
	 * the player
	 * @param x
	 * the line of the card
	 * @param y
	 * the column of the card
	 * @return
	 * return true if the card belongs to the current player, false otherwise
	 */
	public boolean isAttackerCard(int attacker, final int x, final int y) {
		if (attacker == 1) {
			return (x >= 2 && x < LINELIMIT) && (y >= 0 && y < COLLIMIT)
					&& table.get(x).get(y) != null;
		}
		return (x >= 0 && x <= 1) && (y >= 0 && y < COLLIMIT) && table.get(x).get(y) != null;
	}

	/**
	 * checks if a card was used in the current turn
	 * @param x
	 * the line of the card
	 * @param y
	 * the column of the card
	 * @return
	 * return true if the card was used, false otherwise
	 */
	public boolean isCardUsed(final int x, final int y) {
		return table.get(x).get(y) != null && table.get(x).get(y).getUsed();
	}

	/**
	 * checks if a current card was frozen by a hero's ability
	 * @param x
	 * the line of the card
	 * @param y
	 * the column of the card
	 * @return
	 * return true if the card was frozen, false otherwise
	 */
	public boolean isCardFrozen(final int x, final int y) {
		return table.get(x).get(y) != null && table.get(x).get(y).getFrozen();
	}

	/**
	 * checks the opponent's rows to find a Tank card
	 * @param defender
	 * the player
	 * @return
	 * return true if the opponent has a card of type Tank, false otherwise
	 */
	public boolean isTankOnTable(int defender) {
		if (defender == 1) {
			for (int i = 2; i < LINELIMIT; i++) {
				for (int j = 0; j < COLLIMIT; j++) {
					if (table.get(i).get(j) != null) {
						if (table.get(i).get(j).getName().equals("Warden")
								|| table.get(i).get(j).getName().equals("Goliath")) {
							return true;
						}
					}
				}
			}
		} else {
			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < COLLIMIT; j++) {
					if (table.get(i).get(j) != null) {
						if (table.get(i).get(j).getName().equals("Warden")
								|| table.get(i).get(j).getName().equals("Goliath")) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * checks if there is a card placed at a certain position of the game table
	 * @param x
	 * the line of the card
	 * @param y
	 * the column of the card
	 * @return
	 * return true if a card was placed there, false otherwise
	 */
	public boolean isCardAtXY(final int x, final int y) {
		return (x >= 0  && x < LINELIMIT)
				&& (y >= 0 && y < COLLIMIT) && table.get(x).get(y) != null;
	}

	/**
	 * removes a card form a certain position of the game table
	 * @param x
	 * the line of the card
	 * @param y
	 * the column of the card
	 */
	public void removeCard(final int x, final int y) {
		for (int i = y; i < LINELIMIT; i++) {
			table.get(x).set(i, table.get(x).get(i + 1));
		}
		table.get(x).set(LINELIMIT, null);
	}

	/**
	 *
	 * @return
	 * returns the ArrayNode containing the information regarding all frozen card from the table
	 */
	public ArrayNode getFrozenCards() {
		ArrayNode frozenCards = objectMapper.createArrayNode();
		for (int i = 0; i < LINELIMIT; i++) {
			for (int j = 0; j < COLLIMIT; j++) {
				if (table.get(i).get(j) != null) {
					if (table.get(i).get(j).getFrozen()) {
						ObjectNode cardOutput = objectMapper.createObjectNode();
						cardOutput.put("mana", table.get(i).get(j).getMana());
						cardOutput.put("attackDamage",
								table.get(i).get(j).getAttackDamage());
						cardOutput.put("health", table.get(i).get(j).getHealth());
						cardOutput.put("description", table.get(i).get(j).getDescription());
						ArrayNode colorsNode = objectMapper.createArrayNode();
						for (String color : table.get(i).get(j).getColors()) {
							colorsNode.add(color);
						}
						cardOutput.set("colors", colorsNode);
						cardOutput.put("name", table.get(i).get(j).getName());
						frozenCards.add(cardOutput);
					}
				}
			}
		}
		return frozenCards;
	}

	/**
	 * checks if a certain row belongs to the current player
	 * @param player
	 * the player
	 * @param row
	 * the row
	 * @return
	 * return true if the row belongs to the current player, false otherwise
	 */
	public boolean checkRow(int player, final int row) {
		if (player == 1) {
			return row >= 2 && row < LINELIMIT;
		}
		return row == 0 || row == 1;
	}

	/**
	 * performs the ability of Lord Royce
	 * @param row
	 * the row affected by the ability
	 */
	public void performLordRoyce(final int row) {
		for (int i = 0; i < COLLIMIT; i++) {
			if (table.get(row).get(i) != null) {
				table.get(row).get(i).setFrozen(true);
			}
		}
	}

	/**
	 * performs the ability of Empress Thorina
	 * @param row
	 * the row affected by this ability
	 */
	public void performEmpressThorina(final int row) {
		int maxHealth = 0;
		int idx = 0;
		for (int i = 0; i < COLLIMIT; i++) {
			if (table.get(row).get(i) != null) {
				if (table.get(row).get(i).getHealth() > maxHealth) {
					maxHealth = table.get(row).get(i).getHealth();
					idx = i;
				}
			}
		}
		removeCard(row, idx);
	}

	/**
	 * performs the ability of King Mudface
	 * @param row
	 * the row affected by this ability
	 */
	public void performKingMudface(final int row) {
		for (int i = 0; i < COLLIMIT; i++) {
			if (table.get(row).get(i) != null) {
				table.get(row).get(i).incrementHealth(1);
			}
		}
	}

	/**
	 * performs the ability of General Kocioraw
	 * @param row
	 * the row affected by this ability
	 */
	public void performGeneralKocioraw(final int row) {
		for (int i = 0; i < COLLIMIT; i++) {
			if (table.get(row).get(i) != null) {
				table.get(row).get(i).increaseAttackDamage(1);
			}
		}
	}

	/**
	 * unfreezes the cards that belong to player one
	 */
	public void unfreezePlayerOne() {
		for (int i = 2; i < LINELIMIT; i++) {
			for (int j = 0; j < COLLIMIT; j++) {
				if (table.get(i).get(j) != null) {
					table.get(i).get(j).setFrozen(false);
					table.get(i).get(j).setUsed(false);
					table.get(i).get(j).setUsedAbility(false);
				}
			}
		}
	}

	/**
	 * unfreezes the cards that belong to player two
	 */
	public void unfreezePlayerTwo() {
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < COLLIMIT; j++) {
				if (table.get(i).get(j) != null) {
					table.get(i).get(j).setFrozen(false);
					table.get(i).get(j).setUsed(false);
					table.get(i).get(j).setUsedAbility(false);
				}
			}
		}
	}
}
