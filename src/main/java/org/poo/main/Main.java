package org.poo.main;

import org.poo.checker.Checker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.checker.CheckerConstants;
import org.poo.fileio.ActionsInput;
import org.poo.fileio.GameInput;
import org.poo.fileio.GameTable;
import org.poo.fileio.Input;
import org.poo.fileio.Player;
import org.poo.fileio.StartGameInput;
import org.poo.fileio.TakeAction;


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
        ArrayList<GameInput> games = inputData.getGames();
        int oneWins = 0;
        int twoWins = 0;
        int gamesPlayed = 0;
        for (GameInput game : games) { //loop through all existent games
            gamesPlayed++;
            StartGameInput startGame = game.getStartGame();
            int pl1Idx = startGame.getPlayerOneDeckIdx();
            int pl2Idx = startGame.getPlayerTwoDeckIdx();
            Player player1 = new Player();
            Player player2 = new Player();
            //deep copy of the decks
            player1.deepCopyDeck(inputData.getPlayerOneDecks().getDecks().get(pl1Idx));
            player2.deepCopyDeck(inputData.getPlayerTwoDecks().getDecks().get(pl2Idx));
            // set a new game table
            GameTable gameTable = new GameTable();
            //shuffle each player's deck
            Collections.shuffle(player1.getDeck(), new Random(startGame.getShuffleSeed()));
            Collections.shuffle(player2.getDeck(), new Random(startGame.getShuffleSeed()));
            // set each player's in hand card
            if (!player1.getDeck().isEmpty()) {
                player1.initInHand(player1.getDeck().getFirst());
                player1.getDeck().removeFirst();
            }
            if (!player2.getDeck().isEmpty()) {
                player2.initInHand(player2.getDeck().getFirst());
                player2.getDeck().removeFirst();
            }
            // set each player's hero
            player1.setHero(startGame.getPlayerOneHero());
            player2.setHero(startGame.getPlayerTwoHero());
            // set the starting player
            if (startGame.getStartingPlayer() == 1) {
                player1.setMyTurn(true);
            } else {
                player2.setMyTurn(true);
            }
            //get game commands
            ArrayList<ActionsInput> actions = game.getActions();
            TakeAction takeAction = new TakeAction();
            for (ActionsInput action : actions) { //loop through every command of the game
                String command = action.getCommand();
                takeAction.copyFrom(action);
                int playerIdx;
                Player player;
                // for the output needed at each command
                ObjectNode commandOutput = objectMapper.createObjectNode();
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
                        takeAction.endPlayerTurn(player1, player2, gameTable);
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
                        takeAction.placeCard(player1, player2, output, gameTable);
                        break;
                    case "getPlayerMana":
                        takeAction.getPlayerMana(player1, player2, output);
                        break;
                    case "getCardsOnTable":
                        commandOutput.put("command", "getCardsOnTable");
                        commandOutput.set("output", gameTable.printTable());
                        output.add(commandOutput);
                        break;
                    case "cardUsesAttack":
                        takeAction.cardUsesAttack(output, gameTable, player1, player2);
                        break;
                    case "getCardAtPosition"://check if the card at a certain position is valid
                        takeAction.getCardAtPosition(output, gameTable);
                        break;

                    case "cardUsesAbility":
                        takeAction.cardUsesAbility(output, player1, player2, gameTable);
                        break;

                    case "getFrozenCardsOnTable":
                        commandOutput.put("command", "getFrozenCardsOnTable");
                        commandOutput.set("output", gameTable.getFrozenCards());
                        output.add(commandOutput);
                        break;
                    case "useAttackHero":
                        int x = takeAction.useAttackHero(output, player1, player2, gameTable);
                        if (x == 1) {
                            oneWins++;
                        } else if (x == 2) {
                            twoWins++;
                        }
                        break;
                    case "useHeroAbility":
                        takeAction.useHeroAbility(output, player1, player2, gameTable);
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
