package net.kwnt.mc.plugins.betterexperience.evolve.setup;

import net.kwnt.mc.plugins.betterexperience.lib.ExperienceTool;
import net.kwnt.mc.plugins.betterexperience.lib.PlayerPermissions;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Map;

public class PlayerAttributesSetup {
    public PlayerAttributesSetup(Player player, FileConfiguration configuration) {
        PlayerPermissions playerPermissions = new PlayerPermissions(player, configuration);
        ExperienceTool experienceTool = new ExperienceTool(player);

        if (!configuration.getBoolean("evolve.enabled")) return;

        ConfigurationSection attributesConfiguration = configuration.getConfigurationSection("evolve.attributes");

        assert attributesConfiguration != null;
        Map<String, Object> values = attributesConfiguration.getValues(true);

        int playerLevel = player.getLevel();

        for (Map.Entry<String, Object> entry : values.entrySet()) {
            int minRequiredLevel = Integer.parseInt(entry.getKey());

            if (playerLevel < minRequiredLevel) continue;

            System.out.println(entry.getValue());
        }
    }
}
