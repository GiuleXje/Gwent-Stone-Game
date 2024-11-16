package org.poo.fileio;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;

public class GameTable {
	private ArrayList<ArrayList<CardInput>> table;
	private static final ObjectMapper objectMapper = new ObjectMapper();

	public GameTable() {
		table = new ArrayList<>(4);
		for (int i = 0 ; i < 4 ; i++) {
			ArrayList<CardInput> row = new ArrayList<>(5);
			for (int j = 0 ; j < 5 ; j++) {
				row.add(null);
			}
			table.add(row);
		}
	}

	public void setTable(ArrayList<ArrayList<CardInput>> table) {
		this.table = table;
	}
	public ArrayList<ArrayList<CardInput>> getTable() {
		return table;
	}

	public int place_card(int player, CardInput card) {
		if (player == 1) {
			if (card.getName().equals("Sentinel") || card.getName().equals("Berserker") ||
					card.getName().equals("The Cursed One") || card.getName().equals("Disciple")) {
				for (int i = 0 ; i < 5 ; i++) {
					if (table.get(3).get(i) == null) {
						table.get(3).set(i, card);
						return 0;
					}
				}
			} else if (card.getName().equals("Goliath") || card.getName().equals("Warden") ||
					card.getName().equals("The Ripper") || card.getName().equals("Miraj")) {
				for (int i = 0 ; i < 5 ; i++) {
					if (table.get(2).get(i) == null) {
						table.get(2).set(i, card);
						return 0;
					}
				}
			}
		} else {
			if (card.getName().equals("Sentinel") || card.getName().equals("Berserker") ||
					card.getName().equals("The Cursed One") || card.getName().equals("Disciple")) {
				for (int i = 0 ; i < 5 ; i++) {
					if (table.getFirst().get(i) == null) {
						table.getFirst().set(i, card);
						return 0;
					}
				}
			} else if (card.getName().equals("Goliath") || card.getName().equals("Warden") ||
					card.getName().equals("The Ripper") || card.getName().equals("Miraj")) {
				for (int i = 0 ; i < 5 ; i++) {
					if (table.get(1).get(i) == null) {
						table.get(1).set(i, card);
						return 0;
					}
				}
			}
		}
		return 1;
	}

	public ArrayNode printTable() {
		ArrayNode tableOutput = objectMapper.createArrayNode();

		for (int i = 0; i < 4; i++) {
			ArrayNode rowOutput = objectMapper.createArrayNode();
			for (int j = 0; j < 5; j++) {
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

	public boolean isOpponentCard (int opponent, final int x, final int y) {
		if (opponent == 1) {
			return (x >= 2 && x <= 3) && (y >= 0 && y <= 4) && (table.get(x).get(y) != null);
		}
		return (x >= 0 && x <= 1) && (y >= 0 && y <= 4) && (table.get(x).get(y) != null);
	}

	public boolean isAttackerCard (int attacker, final int x, final int y) {
		if (attacker == 1) {
			return (x >= 2 && x <= 3) && (y >= 0 && y <= 4) && table.get(x).get(y) != null;
		}
		return (x >= 0 && x <= 1) && (y >= 0 && y <= 4) && table.get(x).get(y) != null;
	}
	public boolean isCardUsed (final int x, final int y) {
		return table.get(x).get(y) != null && table.get(x).get(y).getUsed();
	}

	public boolean isCardFrozen (final int x, final int y) {
		return table.get(x).get(y) != null && table.get(x).get(y).getFrozen();
	}

	public void UnfreezeAndUnUse () { //after each round unfreeze and set use to false
		for (int i = 0 ; i < 4 ; i++) {
			for (int j = 0 ; j < 5 ; j++) {
				if (table.get(i).get(j) != null) {
					table.get(i).get(j).setUsed(false);
					table.get(i).get(j).setFrozen(false);
					table.get(i).get(j).setUsedAbility(false);
				}
			}
		}
	}

	public boolean isTankOnTable (int defender) {
		if (defender == 1) {
			for (int i = 2 ; i < 4 ; i++) {
				for (int j = 0 ; j < 5 ; j++) {
					if (table.get(i).get(j) != null) {
						if (table.get(i).get(j).getName().equals("Warden") ||
								table.get(i).get(j).getName().equals("Goliath")) {
							return true;
						}
					}
				}
			}
		} else {
			for (int i = 0 ; i < 2 ; i++) {
				for (int j = 0 ; j < 5 ; j++) {
					if (table.get(i).get(j) != null) {
						if (table.get(i).get(j).getName().equals("Warden") ||
								table.get(i).get(j).getName().equals("Goliath")) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public boolean isCardAtXY (final int x, final int y) {
		return (x >= 0  && x < 4) && (y >= 0 && y < 5) && table.get(x).get(y) != null;
	}

	public void removeCard (final int x, final int y) {
		for (int i = y ; i < 4 ; i++) {
			table.get(x).set(i, table.get(x).get(i + 1));
		}
		table.get(x).set(4, null);
	}

	public ArrayNode getFrozenCards () {
		ArrayNode frozenCards = objectMapper.createArrayNode();
		for (int i = 0 ; i < 4 ; i++) {
			for (int j = 0 ; j < 5 ; j++) {
				if (table.get(i).get(j) != null) {
					if (table.get(i).get(j).getFrozen()) {
						ObjectNode cardOutput = objectMapper.createObjectNode();
						cardOutput.put("mana", table.get(i).get(j).getMana());
						cardOutput.put("attackDamage", table.get(i).get(j).getAttackDamage());
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

	//returns true if the row corresponds
	public boolean checkRow (int player, int row) {
		if (player == 1) {
			return row == 2 || row == 3;
		}
		return row == 0 || row == 1;
	}

	public void performLordRoyce (int row) {
		for (int i = 0 ; i < 5 ; i++) {
			if (table.get(row).get(i) != null) {
				table.get(row).get(i).setFrozen(true);
			}
		}
	}

	public void performEmpressThorina (int row) {
		int max_health = 0;
		int idx = 0;
		for (int i = 0 ; i < 5 ; i++) {
			if (table.get(row).get(i) != null) {
				if (table.get(row).get(i).getHealth() > max_health) {
					max_health = table.get(row).get(i).getHealth();
					idx = i;
				}
			}
		}
		removeCard(row, idx);
	}

	public void performKingMudface (int row) {
		for (int i = 0 ; i < 5 ; i++) {
			if (table.get(row).get(i) != null) {
				table.get(row).get(i).incrementHealth(1);
			}
		}
	}

	public void performGeneralKocioraw (int row) {
		for (int i = 0 ; i < 5 ; i++) {
			if (table.get(row).get(i) != null) {
				table.get(row).get(i).increaseAttackDamage(1);
			}
		}
	}

	public void unfreezePlayerOne () {
		for (int i = 2 ; i < 4 ; i++) {
			for (int j = 0 ; j < 5 ; j++) {
				if (table.get(i).get(j) != null) {
					table.get(i).get(j).setFrozen(false);
					table.get(i).get(j).setUsed(false);
					table.get(i).get(j).setUsedAbility(false);
				}
			}
		}
	}

	public void unfreezePlayerTwo () {
		for (int i = 0 ; i < 2 ; i++) {
			for (int j = 0 ; j < 5 ; j++) {
				if (table.get(i).get(j) != null) {
					table.get(i).get(j).setFrozen(false);
					table.get(i).get(j).setUsed(false);
					table.get(i).get(j).setUsedAbility(false);
				}
			}
		}
	}
}