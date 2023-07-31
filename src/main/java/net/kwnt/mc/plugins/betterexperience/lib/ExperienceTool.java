package net.kwnt.mc.plugins.betterexperience.lib;

import org.bukkit.entity.Player;

public class ExperienceTool {
    private Player currentPlayer;

    public ExperienceTool(Player player) {
        currentPlayer = player;
    }

    // Change current player of experience manager
    public void setPlayer(Player player) {
        currentPlayer = player;
    }

    // Calculate player's current exp amount
    public int getExperience() {
        int exp = 0;
        int level = currentPlayer.getLevel();

        // Get the amount of XP in past levels
        exp += getExperienceAtLevel(level);

        // Get amount of XP towards next level
        exp += Math.round(getExperienceToNextLevel(level) * currentPlayer.getExp());

        return exp;
    }

    // Set specific amount exp to a player
    public void setExperience(int experience) {
        clearPlayerExperience();

        currentPlayer.giveExp(experience);
    }

    // Add (or subtract) player's exp
    public void addExperience(int experience) {
        int currentExperience = getExperience();

        clearPlayerExperience();

        currentPlayer.giveExp(currentExperience + experience);
    }

    // Clear player's exp
    private void clearPlayerExperience() {
        currentPlayer.setLevel(0);
        currentPlayer.setExp(0);
    }

    // Calculate amount of exp needed to level up
    private int getExperienceToNextLevel(int level) {
        if (level <= 15) return 2 * level + 7;

        if (level <= 30) return 5 * level - 38;

        return 9 * level - 158;
    }

    // Calculate total experience up to a level
    private int getExperienceAtLevel(int level) {
        if (level <= 16) return (int) (Math.pow(level, 2) + 6 * level);

        if (level <= 31) return (int) (2.5 * Math.pow(level, 2) - 40.5 * level + 360.0);

        return (int) (4.5 * Math.pow(level, 2) - 162.5 * level + 2220.0);
    }
}