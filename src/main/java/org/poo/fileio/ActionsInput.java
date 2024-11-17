package org.poo.fileio;

public class ActionsInput {
    private String command;
    private int handIdx;
    private Coordinates cardAttacker;
    private Coordinates cardAttacked;
    private int affectedRow;
    private int playerIdx;
    private int x;
    private int y;

    public ActionsInput() {
    }

    /**
     *
     * @return
     * return the command given as input
     */
    public String getCommand() {
        return command;
    }

    /**
     *
     * @param command
     * the command to be set
     */
    public void setCommand(final String command) {
        this.command = command;
    }

    /**
     *
     * @return
     * return the index of the in hand card to be displayed
     */
    public int getHandIdx() {
        return handIdx;
    }

    /**
     *
     * @param handIdx
     * to index of the card
     */
    public void setHandIdx(final int handIdx) {
        this.handIdx = handIdx;
    }

    /**
     *
     * @return
     * return the attacker's card coordinates
     */
    public Coordinates getCardAttacker() {
        return cardAttacker;
    }

    /**
     *
     * @param cardAttacker
     * sets the attacker's card coordinates
     */
    public void setCardAttacker(final Coordinates cardAttacker) {
        this.cardAttacker = cardAttacker;
    }

    /**
     *
     * @return
     * return the coordinates of the defender's card
     */
    public Coordinates getCardAttacked() {
        return cardAttacked;
    }

    /**
     *
     * @param cardAttacked
     * the coordinates of the card attacked
     */
    public void setCardAttacked(final Coordinates cardAttacked) {
        this.cardAttacked = cardAttacked;
    }

    /**
     *
     * @return
     * the row affected by some hero's ability
     */
    public int getAffectedRow() {
        return affectedRow;
    }

    /**
     *
     * @param affectedRow
     * the row affected
     */
    public void setAffectedRow(final int affectedRow) {
        this.affectedRow = affectedRow;
    }

    /**
     *
     * @return
     * return the player idx
     */
    public int getPlayerIdx() {
        return playerIdx;
    }

    /**
     *
     * @param playerIdx
     * sets the player's idx
     */
    public void setPlayerIdx(final int playerIdx) {
        this.playerIdx = playerIdx;
    }

    /**
     *
     * @return
     * return the line of the card
     */
    public int getX() {
        return x;
    }

    /**
     *
     * @param x
     * sets the line of the card
     */
    public void setX(final int x) {
        this.x = x;
    }

    /**
     *
     * @return
     * return the column of the card
     */
    public int getY() {
        return y;
    }

    /**
     *
     * @param y
     * sets the column of the card
     */
    public void setY(final int y) {
        this.y = y;
    }

    /**
     *
     * @return
     * debug purpose, prints the class' fields
     */
    @Override
    public String toString() {
        return "ActionsInput{"
                +  "command='"
                + command + '\''
                +  ", handIdx="
                + handIdx
                +  ", cardAttacker="
                + cardAttacker
                +  ", cardAttacked="
                + cardAttacked
                + ", affectedRow="
                + affectedRow
                + ", playerIdx="
                + playerIdx
                + ", x="
                + x
                + ", y="
                + y
                + '}';
    }
}
