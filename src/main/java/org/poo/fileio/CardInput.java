package org.poo.fileio;

import java.util.ArrayList;

public final class CardInput {
    private int mana;
    private int attackDamage;
    private int health;
    private String description;
    private ArrayList<String> colors;
    private String name;
    private boolean used;
    private boolean frozen;
    private boolean usedAbility;
    //added 2 more fields to handle used and frozen cards

    public CardInput() {
    }

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

    public void setUsed(boolean used) {
        this.used = used;
    }

    public boolean getFrozen() {
        return frozen;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    public void setUsedAbility(boolean usedAbility) {
        this.usedAbility = usedAbility;
    }

    public boolean getUsedAbility() {
        return usedAbility;
    }

    public void reduceHealth(int damage) {
        health -= damage;
    }

    public void incrementHealth(int add) {
        health += add;
    }

    public boolean isCardTank() {
        return name.equals("Goliath") || name.equals("Warden");
    }

    public void decreaseAttackDamage(int decrease) {
        attackDamage = Math.max(0, attackDamage - decrease);
    }

    public void increaseAttackDamage(int increase) {
        attackDamage += increase;
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