package net.kwnt.mc.plugins.betterexperience.evolve;

import net.kwnt.mc.plugins.betterexperience.lib.ExperienceTool;
import net.kwnt.mc.plugins.betterexperience.lib.PlayerPermissions;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class PlayerAbilityEffects implements Listener {
    private final ConfigurationSection abilitiesConfiguration;
    private final FileConfiguration configuration;

    /**
     * Player evolution abilities controller.
     * <p>
     * Requires permission <code>betterexperience.bottles.evolve</code>.
     *
     * @param configuration Root of main configuration file
     */
    public PlayerAbilityEffects(FileConfiguration configuration) {
        this.configuration = configuration;

        abilitiesConfiguration = configuration.getConfigurationSection("evolve.abilities");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();

        PlayerPermissions playerPermissions = new PlayerPermissions(player, configuration);

        if (!configuration.getBoolean("evolve.enable") || !playerPermissions.canEvolve()) return;

        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR) return;
        if (event.getHand() != EquipmentSlot.HAND || !player.isSneaking()) return;

        int playerLevel = player.getLevel();

        // Iterate over abilities
        for (String minLevel : Objects.requireNonNull(abilitiesConfiguration).getKeys(false)) {
            int minRequiredLevel = Integer.parseInt(minLevel);

            if (playerLevel < minRequiredLevel) continue;

            ConfigurationSection levelConfiguration = abilitiesConfiguration.getConfigurationSection(minLevel);

            Set<String> itemsToInteract = Objects.requireNonNull(levelConfiguration).getKeys(false);

            for (String itemToInteract : itemsToInteract) {
                if (useAbilityEffect(itemToInteract, player, levelConfiguration)) event.setCancelled(true);
            }
        }
    }

    /**
     * Try to apply ability effect on player.
     * <p>
     * Requires permission <code>betterexperience.bottles.evolve</code>.
     *
     * @param useItemName Name of item that can be used to apply effect
     * @param player Player instance
     * @param levelConfiguration Configuration section of ability effect
     * @return Applying attempt result
     */
    private boolean useAbilityEffect(String useItemName, Player player, ConfigurationSection levelConfiguration) {
        Material itemInMainHand = player.getInventory().getItemInMainHand().getType();
        ExperienceTool experienceTool = new ExperienceTool(player);

        boolean enablePriseIncreases = configuration.getBoolean("evolve.enablePriceImpact");

        // Get current player's experience points
        int playerExperience = experienceTool.getExperience();

        // Check if item in hand is equal to required item
        if (!itemInMainHand.equals(Material.valueOf(useItemName))) return false;

        Map<String, Object> effectData = Objects.requireNonNull(
                levelConfiguration.getConfigurationSection(useItemName)
        ).getValues(false);

        // Variables extraction
        int experiencePrice = (Integer) effectData.get("experiencePrice");

        PotionEffectType effectType = Objects.requireNonNull(
                PotionEffectType.getByName(effectData.get("effect").toString())
        );

        boolean stackingEnabled = effectData.containsKey("stacking") && (Boolean) effectData.get("stacking");

        int durationInTicks = effectData.containsKey("duration") ? getTicksValue(effectData.get("duration")) : 20;

        int maxDurationInTicks = effectData.containsKey("maxDuration") ? getTicksValue(effectData.get("maxDuration")) : 70800;


        // Effect duration calculation
        PotionEffect currentEffect = player.getPotionEffect(effectType);

        int currentLeftDuration = currentEffect == null ? 0 : stackingEnabled ? currentEffect.getDuration() : 0;

        int nextDurationValue = durationInTicks + currentLeftDuration;

        if (playerExperience < experiencePrice || nextDurationValue > maxDurationInTicks) return false;

        // Final experience price amount calculation
        int finalExperiencePrice = calculateExperiencePrice(experiencePrice, enablePriseIncreases, currentLeftDuration, maxDurationInTicks);

        // Take experience from player
        experienceTool.addExperience(-finalExperiencePrice);

        // Apply ability effect
        applyEffect(player, effectType, effectData.get("level"), durationInTicks, stackingEnabled);

        return true;
    }

    /**
     * Convert seconds to ticks.
     *
     * @param initialValue Value in seconds
     * @return Value in ticks
     */
    private int getTicksValue(Object initialValue) {
        return (Integer) initialValue * 20;
    }

    /**
     * Calculate final experience price of ability usage including price impact (if enabled).
     * <p>
     * <code>Price impact percent = current left duration / max duration</code>.
     *
     * @param initialPrice Initial ability use price
     * @param enablePriseIncreases Is price impact enabled
     * @param currentLeftDuration Current duration of desired effect
     * @param maxDurationInTicks Max effect duration in ticks
     * @return Final ability use price
     */
    private int calculateExperiencePrice(int initialPrice, boolean enablePriseIncreases, int currentLeftDuration, int maxDurationInTicks) {
        double priceImpactPercent = 0.0;

        if (enablePriseIncreases && currentLeftDuration > 0) {
            priceImpactPercent = (currentLeftDuration * 1.0) / maxDurationInTicks;
        }

        // Final experience price amount calculation
        return (int) Math.round(initialPrice * (1 - priceImpactPercent));
    }

    /**
     * Apply specific potion effect on player.
     *
     * @param player Player instance
     * @param effectType Ability effect type
     * @param rawLevel Effect level (starting from 1)
     * @param durationInTicks Effect duration in ticks
     * @param stackable Is effect stackable
     */
    private void applyEffect(Player player, PotionEffectType effectType, Object rawLevel, int durationInTicks, boolean stackable) {
        PotionEffect currentEffect = player.getPotionEffect(effectType);

        int currentLeftDuration = currentEffect == null ? 0 : stackable ? currentEffect.getDuration() : 0;

        int nextDurationValue = durationInTicks + currentLeftDuration;

        if (currentLeftDuration > 0) player.removePotionEffect(currentEffect.getType());

        PotionEffect potionEffect = new PotionEffect(
                effectType,
                nextDurationValue,
                (Integer) rawLevel - 1,
                true,
                false
        );

        player.addPotionEffect(potionEffect);
    }
}
