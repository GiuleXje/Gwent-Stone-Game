package org.poo.fileio;

import java.util.ArrayList;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class CardInput {
    private ObjectMapper objectMapper = new ObjectMapper();
    private int mana;
    private int attackDamage;
    private int health;
    private String description;
    private ArrayList<String> colors;
    private String name;
    private boolean used;
    private boolean frozen;
    private boolean usedAbility;
    //added 3 more fields to handle used, used ability(for heroes) and frozen cards

    public CardInput() {
    }

    /**
     * clones a card's attributes
     * @return
     * return a new instance of the card
     */
    @Override
    public CardInput clone() {
        CardInput clone = new CardInput();
        clone.mana = mana;
        clone.health = health;
        clone.attackDamage = attackDamage;
        clone.description = description;
        clone.colors = colors;
        clone.name = name;
        clone.used = used;
        clone.frozen = frozen;
        clone.usedAbility = usedAbility;
        return clone;

    }

    public int getMana() {
        return mana;
    }

    public void setMana(final int mana) {
        this.mana = mana;
    }

    public int getAttackDamage() {
        return attackDamage;
    }

    public void setAttackDamage(final int attackDamage) {
        this.attackDamage = attackDamage;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(final int health) {
        this.health = health;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public ArrayList<String> getColors() {
        return colors;
    }

    public void setColors(final ArrayList<String> colors) {
        this.colors = colors;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public boolean getUsed() {
        return used;
    }

    public void setUsed(final boolean used) {
        this.used = used;
    }

    public boolean getFrozen() {
        return frozen;
    }

    public void setFrozen(final boolean frozen) {
        this.frozen = frozen;
    }

    public void setUsedAbility(final boolean usedAbility) {
        this.usedAbility = usedAbility;
    }

    public boolean getUsedAbility() {
        return usedAbility;
    }

    /**
     *
     * @param damage
     * reduces the card's health by damage points
     */
    public void reduceHealth(final int damage) {
        health -= damage;
    }

    /**
     *
     * @param add
     * increases the card's health by add points
     */
    public void incrementHealth(final int add) {
        health += add;
    }

    /**
     *
     * @return
     * return true if the card if of type Tank, false otherwise
     */
    public boolean isCardTank() {
        return name.equals("Goliath") || name.equals("Warden");
    }

    /**
     *
     * @param decrease
     * decreases the attack damage by decrease points
     */
    public void decreaseAttackDamage(final int decrease) {
        attackDamage = Math.max(0, attackDamage - decrease);
    }

    /**
     *
     * @param increase
     * increases attack damage by increase points
     */
    public void increaseAttackDamage(final int increase) {
        attackDamage += increase;
    }

    /**
     * gets the card's info
     * @return
     * returns the object node needed for the JSON output
     */
    public ObjectNode cardInfo() {
        ObjectNode cardOutput = objectMapper.createObjectNode();
        cardOutput.put("mana", mana);
        cardOutput.put("attackDamage", attackDamage);
        cardOutput.put("health", health);
        cardOutput.put("description", description);

        // Add colors array
        ArrayNode colorsNode = objectMapper.createArrayNode();
        for (String color : colors) {
            colorsNode.add(color);
        }
        cardOutput.set("colors", colorsNode);
        cardOutput.put("name", name);
        return cardOutput;
    }

    @Override
    public String toString() {
        return "CardInput{"
                +  "mana="
                + mana
                +  ", attackDamage="
                + attackDamage
                + ", health="
                + health
                +  ", description='"
                + description
                + '\''
                + ", colors="
                + colors
                + ", name='"
                + name
                + '\''
                + '}';
    }
}
