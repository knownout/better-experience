package net.kwnt.mc.plugins.betterexperience.evolve;

import net.kwnt.mc.plugins.betterexperience.lib.PlayerPermissions;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Objects;

public class PlayerDamageProtection implements Listener {
    private final ConfigurationSection damageProtectionConfiguration;
    private final FileConfiguration configuration;

    /**
     * Player evolution damage protection controller.
     * <p>
     * Requires permission <code>betterexperience.bottles.evolve</code>.
     *
     * @param configuration Root of main configuration file
     */
    public PlayerDamageProtection(FileConfiguration configuration) {
        this.configuration = configuration;

        damageProtectionConfiguration = configuration.getConfigurationSection("evolve.damageProtection");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDamageEvent(EntityDamageEvent event) {
        Entity entity = event.getEntity();

        if (event.getEntityType() != EntityType.PLAYER) return;

        Player player = (Player) entity;

        PlayerPermissions playerPermissions = new PlayerPermissions(player, configuration);

        if (!configuration.getBoolean("evolve.enable") || !playerPermissions.canEvolve()) return;

        int playerLevel = player.getLevel();

        double damageAbsorbPercent = 0.0;

        // Get damage absorption percent based on player level
        for (String minLevel : Objects.requireNonNull(damageProtectionConfiguration).getKeys(false)) {
            int minRequiredLevel = Integer.parseInt(minLevel);

            if (playerLevel < minRequiredLevel) continue;

            damageAbsorbPercent = (Double) damageProtectionConfiguration.getValues(false).get(minLevel);
        }

        // Subtract absorption percent from final damage
        event.setDamage(event.getFinalDamage() * (1 - damageAbsorbPercent));
    }
}
