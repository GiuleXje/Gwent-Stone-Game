package org.poo.main;

import org.poo.checker.Checker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.checker.CheckerConstants;
import org.poo.fileio.ActionsInput;
import org.poo.fileio.GameTable;
import org.poo.fileio.Player;
import org.poo.fileio.CardInput;
import org.poo.fileio.Coordinates;
import org.poo.fileio.DecksInput;
import org.poo.fileio.GameInput;
import org.poo.fileio.Input;
import org.poo.fileio.StartGameInput;


import java.io.File;
import java.io.IOException;

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
        int oneWins = 0;
        int twoWins = 0;
        int gamesPlayed = 0;
        for (GameInput game : games) { //loop through all existent games
            gamesPlayed++;
            StartGameInput startGame = game.getStartGame();
            int pl1Idx = startGame.getPlayerOneDeckIdx();
            int pl2Idx = startGame.getPlayerTwoDeckIdx();
            int turns = 0;
            final int heroHealth = 30;
            final int maxMana = 10;
            //deep copy of the decks
            DecksInput playerOneDecks = new DecksInput();
            DecksInput playerTwoDecks = new DecksInput();
            ArrayList<ArrayList<CardInput>> pla1DeckCopy = new ArrayList<>();
            for (ArrayList<CardInput> deck : auxPlayerOneDecks.getDecks()) {
                ArrayList<CardInput> newDeck = new ArrayList<>();
                for (CardInput card : deck) {
                    newDeck.add(card.clone());
                }
                pla1DeckCopy.add(newDeck);
            }
            playerOneDecks.setDecks(pla1DeckCopy);

            // Copy outer list (deck structure) and inner list (cards) for player two
            ArrayList<ArrayList<CardInput>> pla2DeckCopy = new ArrayList<>();
            for (ArrayList<CardInput> deck : auxPlayerTwoDecks.getDecks()) {
                ArrayList<CardInput> newDeck = new ArrayList<>();
                for (CardInput card : deck) {
                    newDeck.add(card.clone());
                }
                pla2DeckCopy.add(newDeck);
            }
            playerTwoDecks.setDecks(pla2DeckCopy);
            // set a new game table
            GameTable gameTable = new GameTable();
            Player player1 = new Player();
            Player player2 = new Player();
            //setting hero health to 30
            startGame.getPlayerTwoHero().setHealth(heroHealth);
            startGame.getPlayerOneHero().setHealth(heroHealth);

            ArrayList<ArrayList<CardInput>> pl1 = playerOneDecks.getDecks();
            ArrayList<ArrayList<CardInput>> pl2 = playerTwoDecks.getDecks();
            ArrayList<CardInput> pl1DeckCopy = new ArrayList<>(pl1.get(pl1Idx));
            ArrayList<CardInput> pl2DeckCopy = new ArrayList<>(pl2.get(pl2Idx));

            Collections.shuffle(pl1DeckCopy, new Random(startGame.getShuffleSeed()));
            Collections.shuffle(pl2DeckCopy, new Random(startGame.getShuffleSeed()));

            player1.setDeck(pl1DeckCopy);
            player2.setDeck(pl2DeckCopy);

            player1.setDeckUnused();
            player2.setDeckUnused();

            // set each player's in hand card
            if (!player1.getDeck().isEmpty()) {
                player1.add_inHand(player1.getDeck().getFirst());
                player1.getDeck().removeFirst();
            }
            if (!player2.getDeck().isEmpty()) {
                player2.add_inHand(player2.getDeck().getFirst());
                player2.getDeck().removeFirst();
            }

            // set each player's hero
            player1.setHero(startGame.getPlayerOneHero());
            player2.setHero(startGame.getPlayerTwoHero());
            player1.getHero().setUsed(false);
            player2.getHero().setUsed(false);
            player1.getHero().setFrozen(false);
            player2.getHero().setFrozen(false);
            player1.getHero().setUsedAbility(false);
            player2.getHero().setUsedAbility(false);

            // set the starting player
            if (startGame.getStartingPlayer() == 1) {
                player1.setMyTurn(true);
            } else {
                player2.setMyTurn(true);
            }


            // set each player's mana
            player1.setMana(1);
            player2.setMana(1);

            //get game commands
            ArrayList<ActionsInput> actions = game.getActions();
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
                int xAttacker;
                int yAttacker;
                int xAttacked;
                int yAttacked;

                switch (command) {
                    case "getPlayerDeck":
                        playerIdx = action.getPlayerIdx();
                        commandOutput.put("command", command);
                        commandOutput.put("playerIdx", playerIdx);
                        player = (playerIdx == 1) ? player1 : player2;
                        ArrayNode deckOutput = player.printDeck();
                        commandOutput.set("output", deckOutput);
                        output.add(commandOutput);
                        break;

                    case "getPlayerHero":
                        playerIdx = action.getPlayerIdx();
                        commandOutput.put("command", command);
                        commandOutput.put("playerIdx", playerIdx);
                        ObjectNode heroOutput = (playerIdx == 1) ? player1.printHero()
                                : player2.printHero();
                        commandOutput.set("output", heroOutput);
                        output.add(commandOutput);
                        break;

                    case "getPlayerTurn":
                        commandOutput.put("command", "getPlayerTurn");
                        commandOutput.put("output", player1.isMyTurn() ? 1 : 2);
                        output.add(commandOutput);
                        break;

                    case "endPlayerTurn":
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
                            player1.addMana(Math.min(maxMana, turns / 2 + 1));
                            player2.addMana(Math.min(maxMana, turns / 2 + 1));
                            // add in hand card at each turn
                            if (!player1.getDeck().isEmpty()) {
                                player1.add_inHand(player1.getDeck().getFirst());
                                player1.getDeck().removeFirst();
                            }
                            if (!player2.getDeck().isEmpty()) {
                                player2.add_inHand(player2.getDeck().getFirst());
                                player2.getDeck().removeFirst();
                            }
                            player1.getHero().setUsed(false);
                            player2.getHero().setUsed(false);
                        }
                        break;

                    case "getCardsInHand":
                        playerIdx = action.getPlayerIdx();
                        player = (playerIdx == 1) ? player1 : player2;
                        commandOutput.put("command", "getCardsInHand");
                        commandOutput.put("playerIdx", playerIdx);
                        commandOutput.set("output", player.printInHand());
                        output.add(commandOutput);
                        break;

                    case "placeCard":// add the card on the game table
                        int cardIdx = action.getHandIdx();
                        player = player1.isMyTurn() ? player1 : player2;
                        if (player.getInHand().isEmpty()) {
                            break;
                        }
                        if (player.getMana() - player.getInHand().get(cardIdx).getMana() < 0) {
                            commandOutput.put("command", "placeCard");
                            commandOutput.put("handIdx", cardIdx);
                            commandOutput.put("error", "Not enough mana to place card on table.");
                            output.add(commandOutput);
                            break;
                        }
                        CardInput newCardOnTable = player.getInHand().get(cardIdx);
                        newCardOnTable.setUsed(false);
                        newCardOnTable.setFrozen(false);
                        newCardOnTable.setUsedAbility(false);
                        int res = gameTable.place_card(player1.isMyTurn() ? 1 : 2, newCardOnTable);
                        if (res == 0) {
                            player.setMana(player.getMana() - newCardOnTable.getMana());
                            player.getInHand().remove(cardIdx);
                            break;
                        } else if (res == 1) {
                            commandOutput.put("command", "placeCard");
                            commandOutput.put("handIdx", cardIdx);
                            commandOutput.put("error",
                                    "Cannot place card on table since row is full.");
                            output.add(commandOutput);
                            break;
                        }
                        break;

                    case "getPlayerMana":
                        playerIdx = action.getPlayerIdx();
                        player = (playerIdx == 1) ? player1 : player2;
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
                        xAttacker = attackerCard.getX();
                        yAttacker = attackerCard.getY();
                        xAttacked = attackedCard.getX();
                        yAttacked = attackedCard.getY();

                        player = player1.isMyTurn() ? player1 : player2;
                        boolean ok = gameTable.isOpponentCard(player1.isMyTurn() ? 2 : 1,
                                xAttacked, yAttacked);
                        if (!ok) {
                            commandOutput.put("command", "cardUsesAttack");
                            ObjectNode cordAtk = objectMapper.createObjectNode();
                            cordAtk.put("x", xAttacker);
                            cordAtk.put("y", yAttacker);
                            commandOutput.set("cardAttacker", cordAtk);
                            ObjectNode cordDef = objectMapper.createObjectNode();
                            cordDef.put("x", xAttacked);
                            cordDef.put("y", yAttacked);
                            commandOutput.set("cardAttacked", cordDef);
                            commandOutput.put("error",
                                    "Attacked card does not belong to the enemy.");
                            output.add(commandOutput);
                            break;
                        }

                        if (gameTable.isCardUsed(xAttacker, yAttacker)) {
                            commandOutput.put("command", "cardUsesAttack");
                            ObjectNode cordAtk = objectMapper.createObjectNode();
                            cordAtk.put("x", xAttacker);
                            cordAtk.put("y", yAttacker);
                            commandOutput.set("cardAttacker", cordAtk);
                            ObjectNode cordDef = objectMapper.createObjectNode();
                            cordDef.put("x", xAttacked);
                            cordDef.put("y", yAttacked);
                            commandOutput.set("cardAttacked", cordDef);
                            commandOutput.put("error",
                                    "Attacker card has already attacked this turn.");
                            output.add(commandOutput);
                            break;
                        }

                        if (gameTable.isCardFrozen(xAttacker, yAttacker)) {
                            commandOutput.put("command", "cardUsesAttack");
                            ObjectNode cordAtk = objectMapper.createObjectNode();
                            cordAtk.put("x", xAttacker);
                            cordAtk.put("y", yAttacker);
                            commandOutput.set("cardAttacker", cordAtk);
                            ObjectNode cordDef = objectMapper.createObjectNode();
                            cordDef.put("x", xAttacked);
                            cordDef.put("y", yAttacked);
                            commandOutput.set("cardAttacked", cordDef);
                            commandOutput.put("error", "Attacker card is frozen.");
                            output.add(commandOutput);
                            break;
                        }

                        CardInput cardAttacked = gameTable.getTable().get(xAttacked).get(yAttacked);
                        if (!cardAttacked.getName().equals("Goliath")
                                && !cardAttacked.getName().equals("Warden")
                                && gameTable.isTankOnTable(player1.isMyTurn() ? 2 : 1)) {
                            commandOutput.put("command", command);
                            ObjectNode cordAtk = objectMapper.createObjectNode();
                            cordAtk.put("x", xAttacker);
                            cordAtk.put("y", yAttacker);
                            commandOutput.set("cardAttacker", cordAtk);
                            ObjectNode cordDef = objectMapper.createObjectNode();
                            cordDef.put("x", xAttacked);
                            cordDef.put("y", yAttacked);
                            commandOutput.set("cardAttacked", cordDef);
                            commandOutput.put("error", "Attacked card is not of type 'Tank'.");
                            output.add(commandOutput);
                            break;
                        }
                        CardInput cardAttacker = gameTable.getTable().get(xAttacker).get(yAttacker);
                        if (cardAttacker != null) {
                            cardAttacked.reduceHealth(cardAttacker.getAttackDamage());
                            cardAttacker.setUsed(true);
                            if (cardAttacked.getHealth() <= 0) {
                                gameTable.removeCard(xAttacked, yAttacked);
                            }
                        }
                        break;

                    case "getCardAtPosition"://check if the card at a certain position is valid
                        int xCard = action.getX();
                        int yCard = action.getY();
                        commandOutput.put("command", "getCardAtPosition");
                        commandOutput.put("x", xCard);
                        commandOutput.put("y", yCard);
                        if (gameTable.isCardAtXY(xCard, yCard)) {
                            CardInput cardAtXY = gameTable.getTable().get(xCard).get(yCard);
                            ObjectNode cardInfo = objectMapper.createObjectNode();
                            cardInfo.put("mana", cardAtXY.getMana());
                            cardInfo.put("attackDamage", cardAtXY.getAttackDamage());
                            cardInfo.put("health", cardAtXY.getHealth());
                            cardInfo.put("description", cardAtXY.getDescription());
                            ArrayNode cardColours = objectMapper.createArrayNode();
                            for (String color : cardAtXY.getColors()) {
                                cardColours.add(color);
                            }
                            cardInfo.set("colors", cardColours);
                            cardInfo.put("name", cardAtXY.getName());
                            commandOutput.set("output", cardInfo);
                            output.add(commandOutput);
                        } else {
                            commandOutput.put("output", "No card available at that position.");
                            output.add(commandOutput);
                        }
                        break;

                    case "cardUsesAbility":
                        attackerCard = action.getCardAttacker();
                        attackedCard = action.getCardAttacked();
                        xAttacker = attackerCard.getX();
                        yAttacker = attackerCard.getY();
                        xAttacked = attackedCard.getX();
                        yAttacked = attackedCard.getY();

                        commandOutput.put("command", "cardUsesAbility");

                        CardInput CardAttacker = gameTable.getTable().get(xAttacker).get(yAttacker);
                        if (CardAttacker == null) {
                            break;
                        }
                        if (CardAttacker.getFrozen()) {
                            ObjectNode cardAtk = objectMapper.createObjectNode();
                            cardAtk.put("x", xAttacker);
                            cardAtk.put("y", yAttacker);
                            commandOutput.set("cardAttacker", cardAtk);
                            ObjectNode cardDef = objectMapper.createObjectNode();
                            cardDef.put("x", xAttacked);
                            cardDef.put("y", yAttacked);
                            commandOutput.set("cardAttacked", cardDef);
                            commandOutput.put("error", "Attacker card if frozen.");
                            output.add(commandOutput);
                            break;
                        }
                        if (CardAttacker.getUsedAbility() || CardAttacker.getUsed()) {
                            ObjectNode cardAtk = objectMapper.createObjectNode();
                            cardAtk.put("x", xAttacker);
                            cardAtk.put("y", yAttacker);
                            commandOutput.set("cardAttacker", cardAtk);
                            ObjectNode cardDef = objectMapper.createObjectNode();
                            cardDef.put("x", xAttacked);
                            cardDef.put("y", yAttacked);
                            commandOutput.set("cardAttacked", cardDef);
                            commandOutput.put("error", "Attacker card has already attacked this turn.");
                            output.add(commandOutput);
                            break;
                        }

                        if (CardAttacker.getName().equals("Disciple")) {
                            if (gameTable.isOpponentCard(player1.isMyTurn() ? 2 : 1,
                                    xAttacked, yAttacked)) {
                                ObjectNode cardAtk = objectMapper.createObjectNode();
                                cardAtk.put("x", xAttacker);
                                cardAtk.put("y", yAttacker);
                                commandOutput.set("cardAttacker", cardAtk);
                                ObjectNode cardDef = objectMapper.createObjectNode();
                                cardDef.put("x", xAttacked);
                                cardDef.put("y", yAttacked);
                                commandOutput.set("cardAttacked", cardDef);
                                commandOutput.put("error",
                                        "Attacked card does not belong to"
                                                + " the current player.");
                                output.add(commandOutput);
                                break;
                            } else {
                                CardAttacker.setUsedAbility(true);
                                gameTable.getTable().get(xAttacked).
                                        get(yAttacked).incrementHealth(2);
                            }
                        } else if (CardAttacker.getName().equals("The Ripper")
                                || CardAttacker.getName().equals("The Cursed One")
                                || CardAttacker.getName().equals("Miraj")) {
                            if (!gameTable.isOpponentCard(player1.isMyTurn() ? 2 : 1,
                                    xAttacked, yAttacked)) {
                                ObjectNode cardAtk = objectMapper.createObjectNode();
                                cardAtk.put("x", xAttacker);
                                cardAtk.put("y", yAttacker);
                                commandOutput.set("cardAttacker", cardAtk);
                                ObjectNode cardDef = objectMapper.createObjectNode();
                                cardDef.put("x", xAttacked);
                                cardDef.put("y", yAttacked);
                                commandOutput.set("cardAttacked", cardDef);
                                commandOutput.put("error", "Attacked card does not belong to the enemy.");
                                output.add(commandOutput);
                            } else {
                                if (gameTable.isTankOnTable(player1.isMyTurn() ? 2 : 1)) {
                                    if (!gameTable.getTable().get(xAttacked).get(yAttacked).isCardTank()) {
                                        ObjectNode cardAtk = objectMapper.createObjectNode();
                                        cardAtk.put("x", xAttacker);
                                        cardAtk.put("y", yAttacker);
                                        commandOutput.set("cardAttacker", cardAtk);
                                        ObjectNode cardDef = objectMapper.createObjectNode();
                                        cardDef.put("x", xAttacked);
                                        cardDef.put("y", yAttacked);
                                        commandOutput.set("cardAttacked", cardDef);
                                        commandOutput.put("error", "Attacked card is not of type 'Tank'.");
                                        output.add(commandOutput);
                                        break;
                                    }
                                }
                                CardAttacker.setUsedAbility(true);
                                if (CardAttacker.getName().equals("Miraj")) {
                                    int enemyHealth = gameTable.getTable().
                                            get(xAttacked).get(yAttacked).getHealth();
                                    gameTable.getTable().get(xAttacked).get(yAttacked).
                                            setHealth(CardAttacker.getHealth());
                                    CardAttacker.setHealth(enemyHealth);
                                } else if (CardAttacker.getName().equals("The Ripper")) {
                                    gameTable.getTable().get(xAttacked).get(yAttacked).decreaseAttackDamage(2);
                                }
                                else {
                                    int enemyHealth = gameTable.getTable().get(xAttacked).
                                            get(yAttacked).getHealth();
                                    int enemyAttack = gameTable.getTable().get(xAttacked).
                                            get(yAttacked).getAttackDamage();

                                    gameTable.getTable().get(xAttacked).get(yAttacked).setHealth(enemyAttack);
                                    gameTable.getTable().get(xAttacked).get(yAttacked).setAttackDamage(enemyHealth);
                                    if (gameTable.getTable().get(xAttacked).get(yAttacked).getHealth() == 0) {
                                        gameTable.removeCard(xAttacked, yAttacked);
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
                        xAttacker = attackedCard.getX();
                        yAttacker = attackedCard.getY();

                        if (gameTable.getTable().get(xAttacker).get(yAttacker) == null) {
                            break;
                        }
                        if (gameTable.getTable().get(xAttacker).get(yAttacker).getFrozen()) {
                            commandOutput.put("command", command);
                            ObjectNode cardAtk = objectMapper.createObjectNode();
                            cardAtk.put("x", xAttacker);
                            cardAtk.put("y", yAttacker);
                            commandOutput.set("cardAttacker", cardAtk);
                            commandOutput.put("error", "Attacker card is frozen.");
                            output.add(commandOutput);
                            break;
                        }
                        if (gameTable.getTable().get(xAttacker).get(yAttacker).getUsedAbility()
                                || gameTable.getTable().get(xAttacker).get(yAttacker).getUsed()) {
                            commandOutput.put("command", command);
                            ObjectNode cardAtk = objectMapper.createObjectNode();
                            cardAtk.put("x", xAttacker);
                            cardAtk.put("y", yAttacker);
                            commandOutput.set("cardAttacker", cardAtk);
                            commandOutput.put("error",
                                    "Attacker card has already attacked this turn.");
                            output.add(commandOutput);
                            break;
                        }

                        if (gameTable.isTankOnTable(player1.isMyTurn() ? 2 : 1)) {
                            commandOutput.put("command", command);
                            ObjectNode cardAtk = objectMapper.createObjectNode();
                            cardAtk.put("x", xAttacker);
                            cardAtk.put("y", yAttacker);
                            commandOutput.set("cardAttacker", cardAtk);
                            commandOutput.put("error", "Attacked card is not of type 'Tank'.");
                            output.add(commandOutput);
                            break;
                        }

                        player = player1.isMyTurn() ? player2 : player1;
                        player.getHero().reduceHealth(gameTable.getTable().get(xAttacker).get(yAttacker).getAttackDamage());
                        gameTable.getTable().get(xAttacker).get(yAttacker).setUsed(true);
                        if (player.getHero().getHealth() <= 0) {
                            if (player1.isMyTurn()) {
                                commandOutput.put("gameEnded", "Player one killed the enemy hero.");
                                player1.iJustWon();
                                oneWins++;
                                endGame = true;
                            } else {
                                commandOutput.put("gameEnded", "Player two killed the enemy hero.");
                                player2.iJustWon();
                                twoWins++;
                                endGame = true;
                            }
                            output.add(commandOutput);
                            break;
                        }
                        break;

                    case "useHeroAbility":
                        int row = action.getAffectedRow();
                        player = player1.isMyTurn() ? player1 : player2;
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

                        if (player.getHero().getName().equals("Lord Royce")
                                || player.getHero().getName().equals("Empress Thorina")) {
                            if (gameTable.checkRow(player1.isMyTurn() ? 1 : 2, row)) {
                                commandOutput.put("command", command);
                                commandOutput.put("affectedRow", row);
                                commandOutput.put("error", "Selected row does not belong to the enemy.");
                                output.add(commandOutput);
                                break;
                            }
                            // decrease the cost of using the hero
                            player.addMana(-player.getHero().getMana());
                            player.getHero().setUsed(true);
                            if (player.getHero().getName().equals("Lord Royce")) {
                                gameTable.performLordRoyce(row);
                                break;
                            } else {
                                gameTable.performEmpressThorina(row);
                            }
                        } else {
                            if (!gameTable.checkRow(player1.isMyTurn() ? 1 : 2, row)) {
                                commandOutput.put("command", command);
                                commandOutput.put("affectedRow", row);
                                commandOutput.put("error", "Selected row does not belong to the current player.");
                                output.add(commandOutput);
                                break;
                            }
                            player.addMana(-player.getHero().getMana());
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
                        commandOutput.put("output", oneWins);
                        output.add(commandOutput);
                        break;
                    case "getPlayerTwoWins":
                        commandOutput.put("command", command);
                        commandOutput.put("output", twoWins);
                        output.add(commandOutput);
                        break;
                    default:
                        break;
                }
            }


        }

        ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
        objectWriter.writeValue(new File(filePath2), output);
    }
}
