package org.poo.main;

import org.poo.checker.Checker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.checker.CheckerConstants;
import org.poo.fileio.*;

import javax.management.OperationsException;
import javax.swing.plaf.basic.BasicSliderUI;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;

import java.util.Collections;
import java.util.Random;

/**
 * The entry point to this homework. It runs the checker that tests your implentation.
 */
public final class Main {
    /**
     * for coding style
     */
    private Main() {
    }

    /**
     * DO NOT MODIFY MAIN METHOD
     * Call the checker
     * @param args from command line
     * @throws IOException in case of exceptions to reading / writing
     */
    public static void main(final String[] args) throws IOException {
        File directory = new File(CheckerConstants.TESTS_PATH);
        Path path = Paths.get(CheckerConstants.RESULT_PATH);

        if (Files.exists(path)) {
            File resultFile = new File(String.valueOf(path));
            for (File file : Objects.requireNonNull(resultFile.listFiles())) {
                file.delete();
            }
            resultFile.delete();
        }
        Files.createDirectories(path);

        for (File file : Objects.requireNonNull(directory.listFiles())) {
            String filepath = CheckerConstants.OUT_PATH + file.getName();
            File out = new File(filepath);
            boolean isCreated = out.createNewFile();
            if (isCreated) {
                action(file.getName(), filepath);
            }
        }

        Checker.calculateScore();
    }

    /**
     * @param filePath1 for input file
     * @param filePath2 for output file
     * @throws IOException in case of exceptions to reading / writing
     */
    public static void action(final String filePath1,
                              final String filePath2) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Input inputData = objectMapper.readValue(new File(CheckerConstants.TESTS_PATH + filePath1),
                Input.class);

        ArrayNode output = objectMapper.createArrayNode();

        // get decks
        DecksInput auxPlayerOneDecks = inputData.getPlayerOneDecks();
        DecksInput auxPlayerTwoDecks = inputData.getPlayerTwoDecks();
        ArrayList<GameInput> games = inputData.getGames();
        int OneWins = 0;
        int TwoWins = 0;
        int gamesPlayed = 0;
        for (GameInput game : games) { //loop through all existent games
            gamesPlayed++;
            StartGameInput startGame = game.getStartGame();
            int pl1_idx = startGame.getPlayerOneDeckIdx();
            int pl2_idx = startGame.getPlayerTwoDeckIdx();
            int turns = 0;
            DecksInput playerOneDecks = new DecksInput();
            DecksInput playerTwoDecks = new DecksInput();
            ArrayList<ArrayList<CardInput>> pla1DeckCopy = new ArrayList<>();
            for (ArrayList<CardInput> deck : auxPlayerOneDecks.getDecks()) {
                ArrayList<CardInput> newDeck = new ArrayList<>();
                for (CardInput card : deck) {
                    newDeck.add(card.clone()); // Assuming CardInput has a copy constructor
                }
                pla1DeckCopy.add(newDeck);
            }
            playerOneDecks.setDecks(pla1DeckCopy);

            // Copy outer list (deck structure) and inner list (cards) for player two
            ArrayList<ArrayList<CardInput>> pla2DeckCopy = new ArrayList<>();
            for (ArrayList<CardInput> deck : auxPlayerTwoDecks.getDecks()) {
                ArrayList<CardInput> newDeck = new ArrayList<>();
                for (CardInput card : deck) {
                    newDeck.add(card.clone()); // Assuming CardInput has a copy constructor
                }
                pla2DeckCopy.add(newDeck);
            }
            playerTwoDecks.setDecks(pla2DeckCopy);
            // set a new game table
            GameTable gameTable = new GameTable();
            Player Player1 = new Player();
            Player Player2 = new Player();
            //setting hero health to 30
            startGame.getPlayerTwoHero().setHealth(30);
            startGame.getPlayerOneHero().setHealth(30);

            ArrayList<ArrayList<CardInput>> pl1 = playerOneDecks.getDecks();
            ArrayList<ArrayList<CardInput>> pl2 = playerTwoDecks.getDecks();
            ArrayList<CardInput> pl1DeckCopy = new ArrayList<>(pl1.get(pl1_idx));
            ArrayList<CardInput> pl2DeckCopy = new ArrayList<>(pl2.get(pl2_idx));

            Collections.shuffle(pl1DeckCopy, new Random(startGame.getShuffleSeed()));
            Collections.shuffle(pl2DeckCopy, new Random(startGame.getShuffleSeed()));

            Player1.setDeck(pl1DeckCopy);
            Player2.setDeck(pl2DeckCopy);

            Player1.setDeckUnused();
            Player2.setDeckUnused();

            // set each player's in hand card
            if (!Player1.getDeck().isEmpty()) {
                Player1.add_inHand(Player1.getDeck().getFirst());
                Player1.getDeck().removeFirst();
            }
            if (!Player2.getDeck().isEmpty()) {
                Player2.add_inHand(Player2.getDeck().getFirst());
                Player2.getDeck().removeFirst();
            }

            // set each player's hero
            Player1.setHero(startGame.getPlayerOneHero());
            Player2.setHero(startGame.getPlayerTwoHero());
            Player1.getHero().setUsed(false);
            Player2.getHero().setUsed(false);
            Player1.getHero().setFrozen(false);
            Player2.getHero().setFrozen(false);
            Player1.getHero().setUsedAbility(false);
            Player2.getHero().setUsedAbility(false);

            // set the starting player
            if (startGame.getStartingPlayer() == 1) {
                Player1.setMyTurn(true);
            } else {
                Player2.setMyTurn(true);
            }


            // set each player's mana
            Player1.setMana(1);
            Player2.setMana(1);

            //get game commands
            ArrayList < ActionsInput > actions = game.getActions();
            boolean endGame = false;
            for (ActionsInput action : actions) {
//                if (endGame)
//                    break;
                String command = action.getCommand();
                int playerIdx;
                Player player;
                // for the output needed at each command
                ObjectNode commandOutput = objectMapper.createObjectNode();

                Coordinates attackerCard;
                Coordinates attackedCard;
                int x_attacker;
                int y_attacker;
                int x_attacked;
                int y_attacked;

                switch (command) {
                    case "getPlayerDeck":
                        playerIdx = action.getPlayerIdx();
                        commandOutput.put("command", command);
                        commandOutput.put("playerIdx", playerIdx);
                        player = (playerIdx == 1) ? Player1 : Player2;
                        ArrayNode deckOutput = player.printDeck();
                        commandOutput.set("output", deckOutput);
                        output.add(commandOutput);
                        break;

                    case "getPlayerHero":
                        playerIdx = action.getPlayerIdx();
                        commandOutput.put("command", command);
                        commandOutput.put("playerIdx", playerIdx);
                        ObjectNode heroOutput = (playerIdx == 1) ? Player1.printHero() :
                                Player2.printHero();
                        commandOutput.set("output", heroOutput);
                        output.add(commandOutput);
                        break;

                    case "getPlayerTurn":
                        commandOutput.put("command", "getPlayerTurn");
                        commandOutput.put("output", Player1.isMyTurn() ? 1 : 2);
                        output.add(commandOutput);
                        break;

                    case "endPlayerTurn":
                        int ply = Player1.isMyTurn() ? 1 : 2;
                        if (ply == 1) {
                            gameTable.unfreezePlayerOne();
                        } else {
                            gameTable.unfreezePlayerTwo();
                        }
                        if (Player1.isMyTurn()) {
                            Player1.setMyTurn(false);
                            Player2.setMyTurn(true);
                        } else {
                            Player2.setMyTurn(false);
                            Player1.setMyTurn(true);
                        }
                        turns += 1;
                        if (turns % 2 == 0) { // add the mana after each turn
                            Player1.add_mana(Math.min(10, turns / 2 + 1));
                            Player2.add_mana(Math.min(10, turns / 2 + 1));
                            // add in hand card at each turn
                            if (!Player1.getDeck().isEmpty()) {
                                Player1.add_inHand(Player1.getDeck().getFirst());
                                Player1.getDeck().removeFirst();
                            }
                            if (!Player2.getDeck().isEmpty()) {
                                Player2.add_inHand(Player2.getDeck().getFirst());
                                Player2.getDeck().removeFirst();
                            }
                            Player1.getHero().setUsed(false);
                            Player2.getHero().setUsed(false);
                        }
                        break;

                    case "getCardsInHand":
                        playerIdx = action.getPlayerIdx();
                        player = (playerIdx == 1) ? Player1 : Player2;
                        commandOutput.put("command", "getCardsInHand");
                        commandOutput.put("playerIdx", playerIdx);
                        commandOutput.set("output", player.printInHand());
                        output.add(commandOutput);
                        break;

                    case "placeCard":// add the card on the game table
                        int card_idx = action.getHandIdx();
                        player = Player1.isMyTurn() ? Player1 : Player2;
                        if (player.getInHand().isEmpty()) {
                            break;
                        }
                        if (player.getMana() - player.getInHand().get(card_idx).getMana() < 0) {
                            commandOutput.put("command", "placeCard");
                            commandOutput.put("handIdx", card_idx);
                            commandOutput.put("error", "Not enough mana to place card on table.");
                            output.add(commandOutput);
                            break;
                        }
                        CardInput new_card_on_table = player.getInHand().get(card_idx);
                        new_card_on_table.setUsed(false);
                        new_card_on_table.setFrozen(false);
                        new_card_on_table.setUsedAbility(false);
                        int res = gameTable.place_card(Player1.isMyTurn() ? 1 : 2, new_card_on_table);
                        if (res == 0) {
                            player.setMana(player.getMana() - new_card_on_table.getMana());
                            player.getInHand().remove(card_idx);
                            break;
                        } else if (res == 1) {
                            commandOutput.put("command", "placeCard");
                            commandOutput.put("handIdx", card_idx);
                            commandOutput.put("error", "Cannot place card on table since row is full.");
                            output.add(commandOutput);
                            break;
                        }
                        break;

                    case "getPlayerMana":
                        playerIdx = action.getPlayerIdx();
                        player = (playerIdx == 1) ? Player1 : Player2;
                        commandOutput.put("command", "getPlayerMana");
                        commandOutput.put("playerIdx", playerIdx);
                        commandOutput.put("output", player.getMana());
                        output.add(commandOutput);
                        break;

                    case "getCardsOnTable":
                        commandOutput.put("command", "getCardsOnTable");
                        commandOutput.set("output", gameTable.printTable());
                        output.add(commandOutput);
                        break;

                    case "cardUsesAttack":
                        attackerCard = action.getCardAttacker();
                        attackedCard = action.getCardAttacked();
                        x_attacker = attackerCard.getX();
                        y_attacker = attackerCard.getY();
                        x_attacked = attackedCard.getX();
                        y_attacked = attackedCard.getY();

                        player = Player1.isMyTurn() ? Player1 : Player2;
                        boolean ok = gameTable.isOpponentCard(Player1.isMyTurn() ? 2 : 1, x_attacked, y_attacked);
                        if (!ok) {
                            commandOutput.put("command", "cardUsesAttack");
                            ObjectNode cord_atk = objectMapper.createObjectNode();
                            cord_atk.put("x", x_attacker);
                            cord_atk.put("y", y_attacker);
                            commandOutput.set("cardAttacker", cord_atk);
                            ObjectNode cord_def = objectMapper.createObjectNode();
                            cord_def.put("x", x_attacked);
                            cord_def.put("y", y_attacked);
                            commandOutput.set("cardAttacked", cord_def);
                            commandOutput.put("error", "Attacked card does not belong to the enemy.");
                            output.add(commandOutput);
                            break;
                        }

                        if (gameTable.isCardUsed(x_attacker, y_attacker)) {
                            commandOutput.put("command", "cardUsesAttack");
                            ObjectNode cord_atk = objectMapper.createObjectNode();
                            cord_atk.put("x", x_attacker);
                            cord_atk.put("y", y_attacker);
                            commandOutput.set("cardAttacker", cord_atk);
                            ObjectNode cord_def = objectMapper.createObjectNode();
                            cord_def.put("x", x_attacked);
                            cord_def.put("y", y_attacked);
                            commandOutput.set("cardAttacked", cord_def);
                            commandOutput.put("error", "Attacker card has already attacked this turn.");
                            output.add(commandOutput);
                            break;
                        }

                        if (gameTable.isCardFrozen(x_attacker, y_attacker)) {
                            commandOutput.put("command", "cardUsesAttack");
                            ObjectNode cord_atk = objectMapper.createObjectNode();
                            cord_atk.put("x", x_attacker);
                            cord_atk.put("y", y_attacker);
                            commandOutput.set("cardAttacker", cord_atk);
                            ObjectNode cord_def = objectMapper.createObjectNode();
                            cord_def.put("x", x_attacked);
                            cord_def.put("y", y_attacked);
                            commandOutput.set("cardAttacked", cord_def);
                            commandOutput.put("error", "Attacker card is frozen.");
                            output.add(commandOutput);
                            break;
                        }

                        CardInput cardAttacked = gameTable.getTable().get(x_attacked).get(y_attacked);
                        if (!cardAttacked.getName().equals("Goliath") &&
                                !cardAttacked.getName().equals("Warden") &&
                                gameTable.isTankOnTable(Player1.isMyTurn() ? 2 : 1)) {
                            commandOutput.put("command", command);
                            ObjectNode cord_atk = objectMapper.createObjectNode();
                            cord_atk.put("x", x_attacker);
                            cord_atk.put("y", y_attacker);
                            commandOutput.set("cardAttacker", cord_atk);
                            ObjectNode cord_def = objectMapper.createObjectNode();
                            cord_def.put("x", x_attacked);
                            cord_def.put("y", y_attacked);
                            commandOutput.set("cardAttacked", cord_def);
                            commandOutput.put("error", "Attacked card is not of type 'Tank'.");
                            output.add(commandOutput);
                            break;
                        }
                        CardInput cardAttacker = gameTable.getTable().get(x_attacker).get(y_attacker);
                        if (cardAttacker != null) {
                            cardAttacked.reduceHealth(cardAttacker.getAttackDamage());
                            cardAttacker.setUsed(true);
                            if (cardAttacked.getHealth() <= 0) {
                                gameTable.removeCard(x_attacked, y_attacked);
                            }
                        }
                        break;

                    case "getCardAtPosition"://check if the card at a certain position is valid
                        int x_card = action.getX();
                        int y_card = action.getY();
                        commandOutput.put("command", "getCardAtPosition");
                        commandOutput.put("x", x_card);
                        commandOutput.put("y", y_card);
                        if (gameTable.isCardAtXY(x_card, y_card)) {
                            CardInput cardAtXY = gameTable.getTable().get(x_card).get(y_card);
                            ObjectNode card_info = objectMapper.createObjectNode();
                            card_info.put("mana", cardAtXY.getMana());
                            card_info.put("attackDamage", cardAtXY.getAttackDamage());
                            card_info.put("health", cardAtXY.getHealth());
                            card_info.put("description", cardAtXY.getDescription());
                            ArrayNode card_colours = objectMapper.createArrayNode();
                            for (String color : cardAtXY.getColors()) {
                                card_colours.add(color);
                            }
                            card_info.set("colors", card_colours);
                            card_info.put("name", cardAtXY.getName());
                            commandOutput.set("output", card_info);
                            output.add(commandOutput);
                        } else {
                            commandOutput.put("output", "No card available at that position.");
                            output.add(commandOutput);
                        }
                        break;

                    case "cardUsesAbility":
                        attackerCard = action.getCardAttacker();
                        attackedCard = action.getCardAttacked();
                        x_attacker = attackerCard.getX();
                        y_attacker = attackerCard.getY();
                        x_attacked = attackedCard.getX();
                        y_attacked = attackedCard.getY();

                        commandOutput.put("command", "cardUsesAbility");

                        CardInput CardAttacker = gameTable.getTable().get(x_attacker).get(y_attacker);
                        if (CardAttacker == null) {
                            break;
                        }
                        if (CardAttacker.getFrozen()) {
                            ObjectNode card_atk = objectMapper.createObjectNode();
                            card_atk.put("x", x_attacker);
                            card_atk.put("y", y_attacker);
                            commandOutput.set("cardAttacker", card_atk);
                            ObjectNode card_def = objectMapper.createObjectNode();
                            card_def.put("x", x_attacked);
                            card_def.put("y", y_attacked);
                            commandOutput.set("cardAttacked", card_def);
                            commandOutput.put("error", "Attacker card if frozen.");
                            output.add(commandOutput);
                            break;
                        }
                        if (CardAttacker.getUsedAbility() || CardAttacker.getUsed()) {
                            ObjectNode card_atk = objectMapper.createObjectNode();
                            card_atk.put("x", x_attacker);
                            card_atk.put("y", y_attacker);
                            commandOutput.set("cardAttacker", card_atk);
                            ObjectNode card_def = objectMapper.createObjectNode();
                            card_def.put("x", x_attacked);
                            card_def.put("y", y_attacked);
                            commandOutput.set("cardAttacked", card_def);
                            commandOutput.put("error", "Attacker card has already attacked this turn.");
                            output.add(commandOutput);
                            break;
                        }

                        if (CardAttacker.getName().equals("Disciple")) {
                            if (gameTable.isOpponentCard(Player1.isMyTurn() ? 2 : 1,
                                    x_attacked, y_attacked)) {
                                ObjectNode card_atk = objectMapper.createObjectNode();
                                card_atk.put("x", x_attacker);
                                card_atk.put("y", y_attacker);
                                commandOutput.set("cardAttacker", card_atk);
                                ObjectNode card_def = objectMapper.createObjectNode();
                                card_def.put("x", x_attacked);
                                card_def.put("y", y_attacked);
                                commandOutput.set("cardAttacked", card_def);
                                commandOutput.put("error",
                                        "Attacked card does not belong to" +
                                                " the current player.");
                                output.add(commandOutput);
                                break;
                            } else {
                                CardAttacker.setUsedAbility(true);
                                gameTable.getTable().get(x_attacked).
                                        get(y_attacked).incrementHealth(2);
                            }
                        } else if (CardAttacker.getName().equals("The Ripper") ||
                                CardAttacker.getName().equals("The Cursed One") ||
                                CardAttacker.getName().equals("Miraj")) {
                            if (!gameTable.isOpponentCard(Player1.isMyTurn() ? 2 : 1,
                                    x_attacked, y_attacked)) {
                                ObjectNode card_atk = objectMapper.createObjectNode();
                                card_atk.put("x", x_attacker);
                                card_atk.put("y", y_attacker);
                                commandOutput.set("cardAttacker", card_atk);
                                ObjectNode card_def = objectMapper.createObjectNode();
                                card_def.put("x", x_attacked);
                                card_def.put("y", y_attacked);
                                commandOutput.set("cardAttacked", card_def);
                                commandOutput.put("error", "Attacked card does not belong to the enemy.");
                                output.add(commandOutput);
                            } else {
                                if (gameTable.isTankOnTable(Player1.isMyTurn() ? 2 : 1)) {
                                    if (!gameTable.getTable().get(x_attacked).get(y_attacked).isCardTank()) {
                                        ObjectNode card_atk = objectMapper.createObjectNode();
                                        card_atk.put("x", x_attacker);
                                        card_atk.put("y", y_attacker);
                                        commandOutput.set("cardAttacker", card_atk);
                                        ObjectNode card_def = objectMapper.createObjectNode();
                                        card_def.put("x", x_attacked);
                                        card_def.put("y", y_attacked);
                                        commandOutput.set("cardAttacked", card_def);
                                        commandOutput.put("error", "Attacked card is not of type 'Tank'.");
                                        output.add(commandOutput);
                                        break;
                                    }
                                }
                                CardAttacker.setUsedAbility(true);
                                if (CardAttacker.getName().equals("Miraj")) {
                                    int enemy_health = gameTable.getTable().
                                            get(x_attacked).get(y_attacked).getHealth();
                                    gameTable.getTable().get(x_attacked).get(y_attacked).
                                            setHealth(CardAttacker.getHealth());
                                    CardAttacker.setHealth(enemy_health);
                                } else if (CardAttacker.getName().equals("The Ripper")) {
                                    gameTable.getTable().get(x_attacked).get(y_attacked).decreaseAttackDamage(2);}
                                else {
                                    int enemy_health = gameTable.getTable().get(x_attacked).
                                            get(y_attacked).getHealth();
                                    int enemy_attack = gameTable.getTable().get(x_attacked).
                                            get(y_attacked).getAttackDamage();

                                    gameTable.getTable().get(x_attacked).get(y_attacked).setHealth(enemy_attack);
                                    gameTable.getTable().get(x_attacked).get(y_attacked).setAttackDamage(enemy_health);
                                    if (gameTable.getTable().get(x_attacked).get(y_attacked).getHealth() == 0) {
                                        gameTable.removeCard(x_attacked, y_attacked);
                                    }

                                }
                            }
                        }
                        break;

                    case "getFrozenCardsOnTable":
                        commandOutput.put("command","getFrozenCardsOnTable");
                        commandOutput.set("output", gameTable.getFrozenCards());
                        output.add(commandOutput);
                        break;

                    case "useAttackHero":
                        attackedCard = action.getCardAttacker();
                        x_attacker = attackedCard.getX();
                        y_attacker = attackedCard.getY();

                        if (gameTable.getTable().get(x_attacker).get(y_attacker) == null) {
                            break;
                        }
                        if (gameTable.getTable().get(x_attacker).get(y_attacker).getFrozen()) {
                            commandOutput.put("command", command);
                            ObjectNode card_atk = objectMapper.createObjectNode();
                            card_atk.put("x", x_attacker);
                            card_atk.put("y", y_attacker);
                            commandOutput.set("cardAttacker", card_atk);
                            commandOutput.put("error", "Attacker card is frozen.");
                            output.add(commandOutput);
                            break;
                        }
                        if (gameTable.getTable().get(x_attacker).get(y_attacker).getUsedAbility() ||
                                gameTable.getTable().get(x_attacker).get(y_attacker).getUsed()) {
                            commandOutput.put("command", command);
                            ObjectNode card_atk = objectMapper.createObjectNode();
                            card_atk.put("x", x_attacker);
                            card_atk.put("y", y_attacker);
                            commandOutput.set("cardAttacker", card_atk);
                            commandOutput.put("error", "Attacker card has already attacked this turn.");
                            output.add(commandOutput);
                            break;
                        }

                        if (gameTable.isTankOnTable(Player1.isMyTurn() ? 2 : 1)) {
//                            if (!gameTable.getTable().get(x_attacker).get(y_attacker).isCardTank()) {
                            commandOutput.put("command", command);
                            ObjectNode card_atk = objectMapper.createObjectNode();
                            card_atk.put("x", x_attacker);
                            card_atk.put("y", y_attacker);
                            commandOutput.set("cardAttacker", card_atk);
                            commandOutput.put("error", "Attacked card is not of type 'Tank'.");
                            output.add(commandOutput);
                            break;
                        }

                        player = Player1.isMyTurn() ? Player2 : Player1;
                        player.getHero().reduceHealth(gameTable.getTable().get(x_attacker).get(y_attacker).getAttackDamage());
                        gameTable.getTable().get(x_attacker).get(y_attacker).setUsed(true);
                        if (player.getHero().getHealth() <= 0) {
                            if (Player1.isMyTurn()) {
                                commandOutput.put("gameEnded", "Player one killed the enemy hero.");
                                Player1.IJustWon();
                                OneWins++;
                                endGame = true;
                            } else {
                                commandOutput.put("gameEnded", "Player two killed the enemy hero.");
                                Player2.IJustWon();
                                TwoWins++;
                                endGame = true;
                            }
                            output.add(commandOutput);
                            break;
                        }
                        break;

                    case "useHeroAbility":
                        int row = action.getAffectedRow();
                        player = Player1.isMyTurn() ? Player1 : Player2;
                        if (player.getMana() < player.getHero().getMana()) {
                            commandOutput.put("command", command);
                            commandOutput.put("affectedRow", row);
                            commandOutput.put("error", "Not enough mana to use hero's ability.");
                            output.add(commandOutput);
                            break;
                        }

                        if (player.getHero().getUsed()) {
                            commandOutput.put("command", command);
                            commandOutput.put("affectedRow", row);
                            commandOutput.put("error", "Hero has already attacked this turn.");
                            output.add(commandOutput);
                            break;
                        }

                        if (player.getHero().getName().equals("Lord Royce") ||
                                player.getHero().getName().equals("Empress Thorina")) {
                            if (gameTable.checkRow(Player1.isMyTurn() ? 1 : 2, row)) {
                                commandOutput.put("command", command);
                                commandOutput.put("affectedRow", row);
                                commandOutput.put("error", "Selected row does not belong to the enemy.");
                                output.add(commandOutput);
                                break;
                            }
                            // decrease the cost of using the hero
                            player.add_mana(-player.getHero().getMana());
                            player.getHero().setUsed(true);
                            if (player.getHero().getName().equals("Lord Royce")) {
                                gameTable.performLordRoyce(row);
                                break;
                            } else {
                                gameTable.performEmpressThorina(row);
                            }
                        } else {
                            if (!gameTable.checkRow(Player1.isMyTurn() ? 1 : 2, row)) {
                                commandOutput.put("command", command);
                                commandOutput.put("affectedRow", row);
                                commandOutput.put("error", "Selected row does not belong to the current player.");
                                output.add(commandOutput);
                                break;
                            }
                            player.add_mana(-player.getHero().getMana());
                            player.getHero().setUsed(true);
                            if (player.getHero().getName().equals("King Mudface")) {
                                gameTable.performKingMudface(row);
                            } else {
                                gameTable.performGeneralKocioraw(row);
                            }
                        }
                        break;

                    case "getTotalGamesPlayed":
                        commandOutput.put("command", command);
                        commandOutput.put("output", gamesPlayed);
                        output.add(commandOutput);
                        break;
                    case "getPlayerOneWins":
                        commandOutput.put("command", command);
                        commandOutput.put("output", OneWins);
                        output.add(commandOutput);
                        break;
                    case "getPlayerTwoWins":
                        commandOutput.put("command", command);
                        commandOutput.put("output", TwoWins);
                        output.add(commandOutput);
                        break;
                }
            }


        }

        ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
        objectWriter.writeValue(new File(filePath2), output);
    }
}