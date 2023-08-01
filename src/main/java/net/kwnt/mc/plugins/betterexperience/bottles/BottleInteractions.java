package net.kwnt.mc.plugins.betterexperience.bottles;

import net.kwnt.mc.plugins.betterexperience.lib.ExperienceTool;
import net.kwnt.mc.plugins.betterexperience.lib.PlayerPermissions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BottleInteractions implements Listener {
    private final FileConfiguration configuration;
    private final ConfigurationSection bottlesConfiguration;

    public final NamespacedKey bottleLockedKey;

    public final NamespacedKey bottleOwnerKey;

    /**
     * Experience bottles interactions controller.
     *
     * @param configuration Root of main configuration file
     * @param plugin Current plugin
     */
    public BottleInteractions(FileConfiguration configuration, JavaPlugin plugin) {
        this.configuration = configuration;

        this.bottlesConfiguration = Objects.requireNonNull(configuration.getConfigurationSection("bottles"));

        this.bottleLockedKey = new NamespacedKey(plugin, "exp-bottle-locked");

        this.bottleOwnerKey = new NamespacedKey(plugin, "exp-bottle-owner");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!bottlesConfiguration.getBoolean("enable")) return;

        Player player = event.getPlayer();

        List<Material> whiteListBlocks = new ArrayList<>();

        for (String key : bottlesConfiguration.getStringList("interactiveBlocks")) {
            whiteListBlocks.add(Material.valueOf(key));
        }

        // Execution pipeline, modification methods should come before using methods
        // void method is always final method in pipeline
        boolean executed;

        executed = createExperienceBottle(player, whiteListBlocks, event);

        if (!executed) executed = lockUnlockBottle(player, whiteListBlocks, event);

        if (!executed) executed = useBottleWithoutBeating(player, whiteListBlocks, event);

        if (!executed) useBottle(player, event);
    }

    /**
     * Create experience bottles using self experience points
     * <p>
     * Requires permission <code>betterexperience.bottles.create</code>.
     *
     * @param player Player instance
     * @param whiteListBlocks List of blocks that can be used to do stuff with bottles
     * @param event PlayerInteractEvent
     * @return Execution result
     */
    public boolean createExperienceBottle(Player player, List<Material> whiteListBlocks, PlayerInteractEvent event) {
        PlayerPermissions playerPermissions = new PlayerPermissions(player, configuration);
        ExperienceTool experienceTool = new ExperienceTool(player);

        boolean requireSneaking = bottlesConfiguration.getBoolean("sneakingOnly");

        int experienceSubtractAmount = bottlesConfiguration.getInt("expAmountPerBottle");

        Block block = event.getClickedBlock();

        // Check if right-clicked on some block
        if (block == null || !event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return false;

        if (!playerPermissions.canCreateBottles()) return false;

        // Stop execution if block not whitelisted
        if (!whiteListBlocks.contains(block.getType())) return false;

        // Check if player sneaking when sneaking required
        if (requireSneaking && !player.isSneaking()) return false;

        // Cancel interaction with whitelisted block if sneaking
        if (player.getInventory().getItemInMainHand().getType().equals(Material.AIR) && requireSneaking)
            event.setCancelled(true);

        // Cancel execution and interaction if player handle experience bottle or if clicked with offhand
        if (
                player.getInventory().getItemInMainHand().getType().equals(Material.EXPERIENCE_BOTTLE)
                        || event.getHand() == EquipmentSlot.OFF_HAND
        ) {
            event.setCancelled(true);
            return false;
        }

        // If player handle glass bottle...
        if (player.getInventory().getItemInMainHand().getType().equals(Material.GLASS_BOTTLE)) {
            event.setCancelled(true);

            // Take experience
            experienceTool.addExperience(-experienceSubtractAmount);

            // Take glass bottle
            player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);

            ItemStack experienceBottle = new ItemStack(Material.EXPERIENCE_BOTTLE);

            if (bottlesConfiguration.getBoolean("addNameTag")) {
                ItemMeta itemMeta = experienceBottle.getItemMeta();

                final Component lore = Component.text().content("Owner: ")
                        .color(TextColor.fromHexString("#AAAAAA"))
                        .append(player.displayName().color(TextColor.fromHexString("#FFFF55")))
                        .decoration(TextDecoration.ITALIC, false)
                        .build();

                itemMeta.lore(List.of(lore));

                itemMeta.getPersistentDataContainer().set(bottleOwnerKey, PersistentDataType.STRING, player.getUniqueId().toString());
                itemMeta.getPersistentDataContainer().set(bottleLockedKey, PersistentDataType.BOOLEAN, false);

                experienceBottle.setItemMeta(itemMeta);
            }

            // Give experience bottle
            player.getInventory().addItem(experienceBottle);

            return true;
        }

        return false;
    }

    /**
     * Lock or unlock experience bottle.
     * <p>
     * Can be executed only by owner or player with <code>betterexperience.bottles.useLocked</code> permission.
     * <p>
     * Requires permission <code>betterexperience.bottles.lockUnlock</code>.
     *
     * @param player Player instance
     * @param whiteListBlocks List of blocks that can be used to do stuff with bottles
     * @param event PlayerInteractEvent
     * @return Execution result
     */
    public boolean lockUnlockBottle(Player player, List<Material> whiteListBlocks, PlayerInteractEvent event) {
        Block block = event.getClickedBlock();

        PlayerPermissions playerPermissions = new PlayerPermissions(player, configuration);
        if (!playerPermissions.canLockUnlockBottles()) return false;

        if (block == null) return false;

        if (!bottlesConfiguration.getBoolean("allowBottleLock")) return false;

        if (!checkBottleHasOwner(player)) return false;

        // Check if player can use locked bottles
        if (!checkBottleOwner(player)) {
            if (!configuration.getBoolean("bottles.permissions") || !playerPermissions.canUseLockedBottles()) return false;
        }

        // Check if clicked on whitelisted block
        if (!whiteListBlocks.contains(block.getType())) return false;

        // Get clone of item that player holds in main hand
        ItemStack experienceBottle = player.getInventory().getItemInMainHand().asOne();

        if (!experienceBottle.getType().equals(Material.EXPERIENCE_BOTTLE)) return false;

        // Modify item metadata
        ItemMeta itemMeta = experienceBottle.getItemMeta();

        List<Component> lore = itemMeta.lore();

        if (lore == null) return false;

        event.setCancelled(true);

        // Lock or unlock bottle
        if (checkBottleLocked(player)) lore.remove(1);
        else {
            lore.add(Component.text("Locked")
                    .color(TextColor.fromHexString("#FF5555"))
                    .decoration(TextDecoration.ITALIC, false));
        }

        // Change locked key value
        itemMeta.getPersistentDataContainer().set(bottleLockedKey, PersistentDataType.BOOLEAN, !checkBottleLocked(player));

        itemMeta.lore(lore);

        // Save changes
        experienceBottle.setItemMeta(itemMeta);

        player.getInventory().getItemInMainHand().setAmount(
                player.getInventory().getItemInMainHand().getAmount() - 1
        );
        player.getInventory().addItem(experienceBottle);

        return true;
    }

    /**
     * Use experience bottle and return empty bottle to a player.
     * <p>
     * Requires permission <code>betterexperience.bottles.useWithoutBreaking</code>.
     * <p>
     * Must be used after any bottle modification methods.
     *
     * @param player Player instance
     * @param whiteListBlocks List of blocks that can be used to do stuff with bottles
     * @param event PlayerInteractEvent
     * @return Execution result
     */
    public boolean useBottleWithoutBeating(Player player, List<Material> whiteListBlocks, PlayerInteractEvent event) {
        Block block = event.getClickedBlock();

        PlayerPermissions playerPermissions = new PlayerPermissions(player, configuration);
        if (!playerPermissions.canUseWithoutBreaking()) return false;

        if (block == null) return false;

        // Check if feature is enabled
        if (!bottlesConfiguration.getBoolean("keepBottleWhenSneaking")) return false;

        // Check if player handling experience bottle
        if (!player.getInventory().getItemInMainHand().getType().equals(Material.EXPERIENCE_BOTTLE)) return false;

        // Check if player sneaking
        if (!player.isSneaking()) return false;

        // Check if clicked not on whitelisted block
        if (whiteListBlocks.contains(block.getType())) return false;

        // Cancel event if permissions is disabled or if player has no permission to use locked bottles
        if (!checkBottleOwner(player) && checkBottleLocked(player)) {
            if (!configuration.getBoolean("bottles.permissions") || !playerPermissions.canUseLockedBottles()) return false;
        }

        // Return empty glass bottle to player
        player.getInventory().addItem(new ItemStack(Material.GLASS_BOTTLE));

        return true;
    }

    /**
     * Use bottle if not locked or player has permission to use locked bottles.
     * <p>
     * Player can not use locked bottles when permissions disabled, even if it is an operator.
     * <p>
     * Final method in the pipeline.
     *
     * @param player Player instance
     * @param event PlayerInteractEvent
     */
    public void useBottle(Player player, PlayerInteractEvent event) {
        Block block = event.getClickedBlock();

        PlayerPermissions playerPermissions = new PlayerPermissions(player, configuration);

        // Cancel event if permissions is disabled or if player has no permission to use locked bottles
        if (!checkBottleOwner(player) && checkBottleLocked(player)) {
            if ((!configuration.getBoolean("bottles.permissions") || !playerPermissions.canUseLockedBottles())) {
                if (block == null) event.setCancelled(true);
                else if (!block.getType().isInteractable()) event.setCancelled(true);
                else if (!block.isSolid()) event.setCancelled(true);
                else if (player.isSneaking()) event.setCancelled(true);
            }
        }
    }

    /**
     * Check if bottle locked without owner verification.
     *
     * @param player Player instance
     * @return Verification result
     */
    private boolean checkBottleLocked(Player player) {
        ItemStack bottle = player.getInventory().getItemInMainHand();

        if (!bottle.getType().equals(Material.EXPERIENCE_BOTTLE)) return false;

        ItemMeta meta = bottle.getItemMeta();

        if (meta == null) return false;

        if (!meta.getPersistentDataContainer().has(bottleLockedKey)) return false;

        return Boolean.TRUE.equals(meta.getPersistentDataContainer().get(bottleLockedKey, PersistentDataType.BOOLEAN));
    }

    /**
     * Check if bottle has an owner without owner verification.
     *
     * @param player Player instance
     * @return Verification result
     */
    private boolean checkBottleHasOwner(Player player) {
        ItemStack bottle = player.getInventory().getItemInMainHand();

        if (!bottle.getType().equals(Material.EXPERIENCE_BOTTLE)) return false;

        ItemMeta meta = bottle.getItemMeta();

        if (meta == null) return false;

        return meta.getPersistentDataContainer().has(bottleOwnerKey);
    }

    /**
     * Check if player is owner of bottle.
     * @param player Player instance
     * @return Verification result
     */
    private boolean checkBottleOwner(Player player) {
        ItemStack bottle = player.getInventory().getItemInMainHand();

        if (!bottle.getType().equals(Material.EXPERIENCE_BOTTLE)) return false;

        ItemMeta meta = bottle.getItemMeta();

        if (meta == null) return false;

        String ownerUUIDString = meta.getPersistentDataContainer().get(bottleOwnerKey, PersistentDataType.STRING);

        if (ownerUUIDString == null) return true;

        return ownerUUIDString.equals(player.getUniqueId().toString());
    }
}
