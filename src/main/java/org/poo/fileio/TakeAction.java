package org.poo.fileio;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class TakeAction extends ActionsInput {
	private int turns;
	private static final int MAX_MANA = 10;
	private static final ObjectMapper OBJECTMAPPER = new ObjectMapper();
	public TakeAction() {
		turns = 0;
	}
	public int getTurns() {
		return turns;
	}
	public void setTurns(final int turns) {
		this.turns = turns;
	}

	/**
	 * copies the current action's fields
	 * @param action
	 * the action that we will be performing
	 */
	public void copyFrom(final ActionsInput action) {
		this.setCommand(action.getCommand());
		this.setHandIdx(action.getHandIdx());
		this.setCardAttacker(action.getCardAttacker());
		this.setCardAttacked(action.getCardAttacked());
		this.setAffectedRow(action.getAffectedRow());
		this.setPlayerIdx(action.getPlayerIdx());
		this.setX(action.getX());
		this.setY(action.getY());
	}

	/**
	 * places a player's card on the game table
	 * @param player1
	 * player one
	 * @param player2
	 * player two
	 * @param output
	 * used to print out result in the JSON
	 * @param gameTable
	 * the current game table where we place our card
	 */
	public void placeCard(final Player player1, final Player player2,
						  final ArrayNode output, final GameTable gameTable) {
		int cardIdx = getHandIdx();
		Player player = player1.isMyTurn() ? player1 : player2;
		ObjectNode commandOutput = OBJECTMAPPER.createObjectNode();
		if (player.getInHand().isEmpty()) {
			return;
		}
		if (player.getMana() - player.getInHand().get(cardIdx).getMana() < 0) {
			commandOutput.put("command", "placeCard");
			commandOutput.put("handIdx", cardIdx);
			commandOutput.put("error", "Not enough mana to place card on table.");
			output.add(commandOutput);
			return;
		}
		CardInput newCardOnTable = player.getInHand().get(cardIdx);
		newCardOnTable.setUsed(false);
		newCardOnTable.setFrozen(false);
		newCardOnTable.setUsedAbility(false);
		int res = gameTable.placeCard(player1.isMyTurn() ? 1 : 2, newCardOnTable);
		if (res == 0) {
			player.setMana(player.getMana() - newCardOnTable.getMana());
			player.getInHand().remove(cardIdx);
		} else if (res == 1) {
			commandOutput.put("command", "placeCard");
			commandOutput.put("handIdx", cardIdx);
			commandOutput.put("error",
					"Cannot place card on table since row is full.");
			output.add(commandOutput);
		}
	}

	/**
	 * marks the end of player's turn, and unmarks his cards
	 * if we reach the end of a round, we unmark all the cards and both players' hero
	 * @param player1
	 * player one
	 * @param player2
	 * player tow
	 * @param gameTable
	 * the game table
	 */
	public void endPlayerTurn(final Player player1, final Player player2,
							  final GameTable gameTable) {
		int ply = player1.isMyTurn() ? 1 : 2;
		if (ply == 1) {
			gameTable.unfreezePlayerOne();
		} else {
			gameTable.unfreezePlayerTwo();
		}
		if (player1.isMyTurn()) {
			player1.setMyTurn(false);
			player2.setMyTurn(true);
		} else {
			player2.setMyTurn(false);
			player1.setMyTurn(true);
		}
		turns += 1;
		if (turns % 2 == 0) { // add the mana after each turn
			player1.addMana(Math.min(MAX_MANA, turns / 2 + 1));
			player2.addMana(Math.min(MAX_MANA, turns / 2 + 1));
			// add in hand card at each turn
			if (!player1.getDeck().isEmpty()) {
				player1.addInHand(player1.getDeck().getFirst());
				player1.getDeck().removeFirst();
			}
			if (!player2.getDeck().isEmpty()) {
				player2.addInHand(player2.getDeck().getFirst());
				player2.getDeck().removeFirst();
			}
			player1.getHero().setUsed(false);
			player2.getHero().setUsed(false);
		}
	}

	/**
	 * prints the data about a certaing card on the table
	 * @param output
	 * used for the JSON
	 * @param gameTable
	 * the game table
	 */
	public void getCardAtPosition(final ArrayNode output, final GameTable gameTable) {
		int xCard = getX();
		int yCard = getY();
		ObjectNode commandOutput = OBJECTMAPPER.createObjectNode();
		commandOutput.put("command", "getCardAtPosition");
		commandOutput.put("x", xCard);
		commandOutput.put("y", yCard);
		if (gameTable.isCardAtXY(xCard, yCard)) {
			CardInput cardAtXY = gameTable.getTable().get(xCard).get(yCard);
			commandOutput.set("output", cardAtXY.cardInfo());
			output.add(commandOutput);
		} else {
			commandOutput.put("output", "No card available at that position.");
			output.add(commandOutput);
		}
	}

	/**
	 * prints the current's player mana
	 * @param player1
	 * player 1
	 * @param player2
	 * player 2
	 * @param output
	 * used to output in the JSON
	 */
	public void getPlayerMana(final Player player1, final Player player2,
							  final ArrayNode output) {
		int playerIdx = getPlayerIdx();
		ObjectNode commandOutput = OBJECTMAPPER.createObjectNode();
		Player player = (playerIdx == 1) ? player1 : player2;
		commandOutput.put("command", "getPlayerMana");
		commandOutput.put("playerIdx", playerIdx);
		commandOutput.put("output", player.getMana());
		output.add(commandOutput);
	}

	/**
	 * uses a card's ability
	 * @param output
	 * used for the json
	 * @param gameTable
	 * the gameTable
	 * @param player1
	 * player 1  
	 * @param player2
	 * player 2
	 */
	public void cardUsesAttack(final ArrayNode output, final GameTable gameTable,
							   final Player player1, final Player player2) {
		Coordinates attackerCard = getCardAttacker();
		Coordinates attackedCard = getCardAttacked();
		int xAttacker = attackerCard.getX();
		int yAttacker = attackerCard.getY();
		int xAttacked = attackedCard.getX();
		int yAttacked = attackedCard.getY();

		ObjectNode commandOutput = OBJECTMAPPER.createObjectNode();
		Player player = player1.isMyTurn() ? player1 : player2;
		boolean ok = gameTable.isOpponentCard(player1.isMyTurn() ? 2 : 1,
				xAttacked, yAttacked);
		if (!ok) {
			commandOutput.put("command", "cardUsesAttack");
			ObjectNode cordAtk = OBJECTMAPPER.createObjectNode();
			cordAtk.put("x", xAttacker);
			cordAtk.put("y", yAttacker);
			commandOutput.set("cardAttacker", cordAtk);
			ObjectNode cordDef = OBJECTMAPPER.createObjectNode();
			cordDef.put("x", xAttacked);
			cordDef.put("y", yAttacked);
			commandOutput.set("cardAttacked", cordDef);
			commandOutput.put("error",
					"Attacked card does not belong to the enemy.");
			output.add(commandOutput);
			return;
		}

		if (gameTable.isCardUsed(xAttacker, yAttacker)) {
			commandOutput.put("command", "cardUsesAttack");
			ObjectNode cordAtk = OBJECTMAPPER.createObjectNode();
			cordAtk.put("x", xAttacker);
			cordAtk.put("y", yAttacker);
			commandOutput.set("cardAttacker", cordAtk);
			ObjectNode cordDef = OBJECTMAPPER.createObjectNode();
			cordDef.put("x", xAttacked);
			cordDef.put("y", yAttacked);
			commandOutput.set("cardAttacked", cordDef);
			commandOutput.put("error",
					"Attacker card has already attacked this turn.");
			output.add(commandOutput);
			return;
		}

		if (gameTable.isCardFrozen(xAttacker, yAttacker)) {
			commandOutput.put("command", "cardUsesAttack");
			ObjectNode cordAtk = OBJECTMAPPER.createObjectNode();
			cordAtk.put("x", xAttacker);
			cordAtk.put("y", yAttacker);
			commandOutput.set("cardAttacker", cordAtk);
			ObjectNode cordDef = OBJECTMAPPER.createObjectNode();
			cordDef.put("x", xAttacked);
			cordDef.put("y", yAttacked);
			commandOutput.set("cardAttacked", cordDef);
			commandOutput.put("error", "Attacker card is frozen.");
			output.add(commandOutput);
			return;
		}

		CardInput cardAttacked = gameTable.getTable().get(xAttacked).get(yAttacked);
		if (!cardAttacked.getName().equals("Goliath")
				&& !cardAttacked.getName().equals("Warden")
				&& gameTable.isTankOnTable(player1.isMyTurn() ? 2 : 1)) {
			commandOutput.put("command", "cardUsesAttack");
			ObjectNode cordAtk = OBJECTMAPPER.createObjectNode();
			cordAtk.put("x", xAttacker);
			cordAtk.put("y", yAttacker);
			commandOutput.set("cardAttacker", cordAtk);
			ObjectNode cordDef = OBJECTMAPPER.createObjectNode();
			cordDef.put("x", xAttacked);
			cordDef.put("y", yAttacked);
			commandOutput.set("cardAttacked", cordDef);
			commandOutput.put("error", "Attacked card is not of type 'Tank'.");
			output.add(commandOutput);
			return;
		}
		CardInput cardAttk = gameTable.getTable().get(xAttacker).get(yAttacker);
		if (cardAttk != null) {
			cardAttacked.reduceHealth(cardAttk.getAttackDamage());
			cardAttk.setUsed(true);
			if (cardAttacked.getHealth() <= 0) {
				gameTable.removeCard(xAttacked, yAttacked);
			}
		}
	}

	/**
	 * uses a card's ability
	 * @param output
	 * output
	 * @param player1
	 * player 1
	 * @param player2
	 * player 2
	 * @param gameTable
	 * the game table
	 */
	public void cardUsesAbility(final ArrayNode output, final Player player1,
								final Player player2, final GameTable gameTable) {
		Coordinates attackerCard = getCardAttacker();
		Coordinates attackedCard = getCardAttacked();
		int xAttacker = attackerCard.getX();
		int yAttacker = attackerCard.getY();
		int xAttacked = attackedCard.getX();
		int yAttacked = attackedCard.getY();

		ObjectNode commandOutput = OBJECTMAPPER.createObjectNode();
		commandOutput.put("command", "cardUsesAbility");

		CardInput cardAttke = gameTable.getTable().get(xAttacker).get(yAttacker);
		if (cardAttke == null) {
			return;
		}
		if (cardAttke.getFrozen()) {
			ObjectNode cardAtk = OBJECTMAPPER.createObjectNode();
			cardAtk.put("x", xAttacker);
			cardAtk.put("y", yAttacker);
			commandOutput.set("cardAttacker", cardAtk);
			ObjectNode cardDef = OBJECTMAPPER.createObjectNode();
			cardDef.put("x", xAttacked);
			cardDef.put("y", yAttacked);
			commandOutput.set("cardAttacked", cardDef);
			commandOutput.put("error", "Attacker card if frozen.");
			output.add(commandOutput);
			return;
		}
		if (cardAttke.getUsedAbility() || cardAttke.getUsed()) {
			ObjectNode cardAtk = OBJECTMAPPER.createObjectNode();
			cardAtk.put("x", xAttacker);
			cardAtk.put("y", yAttacker);
			commandOutput.set("cardAttacker", cardAtk);
			ObjectNode cardDef = OBJECTMAPPER.createObjectNode();
			cardDef.put("x", xAttacked);
			cardDef.put("y", yAttacked);
			commandOutput.set("cardAttacked", cardDef);
			commandOutput.put("error",
					"Attacker card has already attacked this turn.");
			output.add(commandOutput);
			return;
		}

		if (cardAttke.getName().equals("Disciple")) {
			if (gameTable.isOpponentCard(player1.isMyTurn() ? 2 : 1,
					xAttacked, yAttacked)) {
				ObjectNode cardAtk = OBJECTMAPPER.createObjectNode();
				cardAtk.put("x", xAttacker);
				cardAtk.put("y", yAttacker);
				commandOutput.set("cardAttacker", cardAtk);
				ObjectNode cardDef = OBJECTMAPPER.createObjectNode();
				cardDef.put("x", xAttacked);
				cardDef.put("y", yAttacked);
				commandOutput.set("cardAttacked", cardDef);
				commandOutput.put("error",
						"Attacked card does not belong to"
								+ " the current player.");
				output.add(commandOutput);
				return;
			} else {
				cardAttke.setUsedAbility(true);
				gameTable.getTable().get(xAttacked).
						get(yAttacked).incrementHealth(2);
			}
		} else if (cardAttke.getName().equals("The Ripper")
				|| cardAttke.getName().equals("The Cursed One")
				|| cardAttke.getName().equals("Miraj")) {
			if (!gameTable.isOpponentCard(player1.isMyTurn() ? 2 : 1,
					xAttacked, yAttacked)) {
				ObjectNode cardAtk = OBJECTMAPPER.createObjectNode();
				cardAtk.put("x", xAttacker);
				cardAtk.put("y", yAttacker);
				commandOutput.set("cardAttacker", cardAtk);
				ObjectNode cardDef = OBJECTMAPPER.createObjectNode();
				cardDef.put("x", xAttacked);
				cardDef.put("y", yAttacked);
				commandOutput.set("cardAttacked", cardDef);
				commandOutput.put("error",
						"Attacked card does not belong to the enemy.");
				output.add(commandOutput);
			} else {
				if (gameTable.isTankOnTable(player1.isMyTurn() ? 2 : 1)) {
					if (!gameTable.getTable().get(xAttacked).get(yAttacked).isCardTank()) {
						ObjectNode cardAtk = OBJECTMAPPER.createObjectNode();
						cardAtk.put("x", xAttacker);
						cardAtk.put("y", yAttacker);
						commandOutput.set("cardAttacker", cardAtk);
						ObjectNode cardDef = OBJECTMAPPER.createObjectNode();
						cardDef.put("x", xAttacked);
						cardDef.put("y", yAttacked);
						commandOutput.set("cardAttacked", cardDef);
						commandOutput.put("error",
								"Attacked card is not of type 'Tank'.");
						output.add(commandOutput);
						return;
					}
				}
				cardAttke.setUsedAbility(true);
				if (cardAttke.getName().equals("Miraj")) {
					int enemyHealth = gameTable.getTable().
							get(xAttacked).get(yAttacked).getHealth();
					gameTable.getTable().get(xAttacked).get(yAttacked).
							setHealth(cardAttke.getHealth());
					cardAttke.setHealth(enemyHealth);
				} else if (cardAttke.getName().equals("The Ripper")) {
					gameTable.getTable().get(xAttacked)
							.get(yAttacked).decreaseAttackDamage(2);
				} else {
					int enemyHealth = gameTable.getTable().get(xAttacked).
							get(yAttacked).getHealth();
					int enemyAttack = gameTable.getTable().get(xAttacked).
							get(yAttacked).getAttackDamage();

					gameTable.getTable().get(xAttacked)
							.get(yAttacked).setHealth(enemyAttack);
					gameTable.getTable().get(xAttacked)
							.get(yAttacked).setAttackDamage(enemyHealth);
					if (gameTable.getTable().get(xAttacked).get(yAttacked).getHealth() == 0) {
						gameTable.removeCard(xAttacked, yAttacked);
					}

				}
			}
		}
	}

	/**
	 * attack's the enemy hero
	 * @param output
	 * for the JSON
	 * @param player1
	 * player 1
	 * @param player2
	 * player 2
	 * @param gameTable
	 * game table
	 * @return
	 * return 1 if player one won, 2 if player two won, 0 otherwise
	 */
	public int useAttackHero(final ArrayNode output, final Player player1,
							  final Player player2, final GameTable gameTable) {
		Coordinates attackedCard = getCardAttacker();
		int xAttacker = attackedCard.getX();
		int yAttacker = attackedCard.getY();

		if (gameTable.getTable().get(xAttacker).get(yAttacker) == null) {
			return 0;
		}

		ObjectNode commandOutput = OBJECTMAPPER.createObjectNode();
		if (gameTable.getTable().get(xAttacker).get(yAttacker).getFrozen()) {
			commandOutput.put("command", "useAttackHero");
			ObjectNode cardAtk = OBJECTMAPPER.createObjectNode();
			cardAtk.put("x", xAttacker);
			cardAtk.put("y", yAttacker);
			commandOutput.set("cardAttacker", cardAtk);
			commandOutput.put("error", "Attacker card is frozen.");
			output.add(commandOutput);
			return 0;
		}
		if (gameTable.getTable().get(xAttacker).get(yAttacker).getUsedAbility()
				|| gameTable.getTable().get(xAttacker).get(yAttacker).getUsed()) {
			commandOutput.put("command", "useAttackHero");
			ObjectNode cardAtk = OBJECTMAPPER.createObjectNode();
			cardAtk.put("x", xAttacker);
			cardAtk.put("y", yAttacker);
			commandOutput.set("cardAttacker", cardAtk);
			commandOutput.put("error",
					"Attacker card has already attacked this turn.");
			output.add(commandOutput);
			return 0;
		}

		if (gameTable.isTankOnTable(player1.isMyTurn() ? 2 : 1)) {
			commandOutput.put("command", "useAttackHero");
			ObjectNode cardAtk = OBJECTMAPPER.createObjectNode();
			cardAtk.put("x", xAttacker);
			cardAtk.put("y", yAttacker);
			commandOutput.set("cardAttacker", cardAtk);
			commandOutput.put("error", "Attacked card is not of type 'Tank'.");
			output.add(commandOutput);
			return 0;
		}

		Player player = player1.isMyTurn() ? player2 : player1;
		player.getHero().reduceHealth(gameTable.getTable()
				.get(xAttacker).get(yAttacker).getAttackDamage());
		gameTable.getTable().get(xAttacker).get(yAttacker).setUsed(true);
		if (player.getHero().getHealth() <= 0) {
			if (player1.isMyTurn()) {
				commandOutput.put("gameEnded",
						"Player one killed the enemy hero.");
				player1.iJustWon();
				output.add(commandOutput);
				return 1;
			} else {
				commandOutput.put("gameEnded",
						"Player two killed the enemy hero.");
				player2.iJustWon();
				output.add(commandOutput);
				return 2;
			}
		}
		return 0;
	}

	/**
	 * uses a hero's ability
	 * @param output
	 * for the JSON
	 * @param player1
	 * player one
	 * @param player2
	 * player two
	 * @param gameTable
	 * game table
	 */
	public void useHeroAbility(final ArrayNode output, final Player player1,
							   final Player player2, final GameTable gameTable) {
		int row = getAffectedRow();
		Player player = player1.isMyTurn() ? player1 : player2;
		ObjectNode commandOutput = OBJECTMAPPER.createObjectNode();
		if (player.getMana() < player.getHero().getMana()) {
			commandOutput.put("command", "useHeroAbility");
			commandOutput.put("affectedRow", row);
			commandOutput.put("error", "Not enough mana to use hero's ability.");
			output.add(commandOutput);
			return;
		}

		if (player.getHero().getUsed()) {
			commandOutput.put("command", "useHeroAbility");
			commandOutput.put("affectedRow", row);
			commandOutput.put("error", "Hero has already attacked this turn.");
			output.add(commandOutput);
			return;
		}

		if (player.getHero().getName().equals("Lord Royce")
				|| player.getHero().getName().equals("Empress Thorina")) {
			if (gameTable.checkRow(player1.isMyTurn() ? 1 : 2, row)) {
				commandOutput.put("command", "useHeroAbility");
				commandOutput.put("affectedRow", row);
				commandOutput.put("error",
						"Selected row does not belong to the enemy.");
				output.add(commandOutput);
				return;
			}
			// decrease the cost of using the hero
			player.addMana(-player.getHero().getMana());
			player.getHero().setUsed(true);
			if (player.getHero().getName().equals("Lord Royce")) {
				gameTable.performLordRoyce(row);
				return;
			} else {
				gameTable.performEmpressThorina(row);
			}
		} else {
			if (!gameTable.checkRow(player1.isMyTurn() ? 1 : 2, row)) {
				commandOutput.put("command", "useHeroAbility");
				commandOutput.put("affectedRow", row);
				commandOutput.put("error",
						"Selected row does not belong to the current player.");
				output.add(commandOutput);
				return;
			}
			player.addMana(-player.getHero().getMana());
			player.getHero().setUsed(true);
			if (player.getHero().getName().equals("King Mudface")) {
				gameTable.performKingMudface(row);
			} else {
				gameTable.performGeneralKocioraw(row);
			}
		}
	}
}
