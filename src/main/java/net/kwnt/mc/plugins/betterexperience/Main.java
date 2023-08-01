package net.kwnt.mc.plugins.betterexperience;

import net.kwnt.mc.plugins.betterexperience.bottles.BottleInteractions;
import net.kwnt.mc.plugins.betterexperience.evolve.PlayerAttributes;
import net.kwnt.mc.plugins.betterexperience.evolve.PlayerAbilityEffects;
import net.kwnt.mc.plugins.betterexperience.evolve.PlayerDamageProtection;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    @Override
    public void onEnable() {
        saveDefaultConfig();

        FileConfiguration configuration = getConfig();

        // Register evolve mechanics controllers
        Bukkit.getPluginManager().registerEvents(new PlayerAttributes(configuration), this);
        Bukkit.getPluginManager().registerEvents(new PlayerDamageProtection(configuration), this);
        Bukkit.getPluginManager().registerEvents(new PlayerAbilityEffects(configuration), this);

        // Register experience bottles mechanics controller
        Bukkit.getPluginManager().registerEvents(new BottleInteractions(configuration, this), this);
    }

}
