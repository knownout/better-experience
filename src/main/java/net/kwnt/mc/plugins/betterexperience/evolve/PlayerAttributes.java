package net.kwnt.mc.plugins.betterexperience.evolve;

import net.kwnt.mc.plugins.betterexperience.lib.PlayerPermissions;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

import java.util.Map;
import java.util.Objects;

public class PlayerAttributes implements Listener {
    private final ConfigurationSection evolveAttributesConfiguration;
    private final FileConfiguration configuration;

    /**
     * Player evolution attributes controller.
     * <p>
     * Requires permission <code>betterexperience.bottles.evolve</code>.
     *
     * @param configuration Root of main configuration file
     */
    public PlayerAttributes(FileConfiguration configuration) {
        this.configuration = configuration;

        evolveAttributesConfiguration = configuration.getConfigurationSection("evolve.attributes");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLevelChangeEvent(PlayerLevelChangeEvent event) {
        updatePlayerAttributes(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        updatePlayerAttributes(event.getPlayer());
    }

    /**
     * Update player's generic attributes based on it level
     * <p>
     * Requires permission <code>betterexperience.bottles.evolve</code>.
     *
     * @param player Player instance
     */
    private void updatePlayerAttributes(Player player) {
        PlayerPermissions playerPermissions = new PlayerPermissions(player, configuration);

        if (!configuration.getBoolean("evolve.enable") || !playerPermissions.canEvolve()) return;

        int playerLevel = player.getLevel();

        for (String minLevel : Objects.requireNonNull(evolveAttributesConfiguration).getKeys(false)) {
            int minRequiredLevel = Integer.parseInt(minLevel);

            if (playerLevel < minRequiredLevel) continue;

            // Get all generic attributes to update
            Map<String, Object> genericAttributes = Objects.requireNonNull(
                    evolveAttributesConfiguration.getConfigurationSection(minLevel)
            ).getValues(false);

            // Update all attributes values
            for (Map.Entry<String, Object> entry : genericAttributes.entrySet()) {
                Objects.requireNonNull(
                        player.getAttribute(Attribute.valueOf(entry.getKey()))
                ).setBaseValue(Double.parseDouble(entry.getValue().toString()));
            }
        }
    }
}
