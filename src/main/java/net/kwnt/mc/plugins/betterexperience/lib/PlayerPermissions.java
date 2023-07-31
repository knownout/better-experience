package net.kwnt.mc.plugins.betterexperience.lib;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

public class PlayerPermissions {
    private final Player player;
    private final boolean evolvePermissionsEnabled;
    private final boolean bottlesPermissionsEnabled;

    public PlayerPermissions(Player player, FileConfiguration configuration) {
        this.player = player;

        this.evolvePermissionsEnabled = configuration.getBoolean("evolve.permissions");
        this.bottlesPermissionsEnabled = configuration.getBoolean("bottles.permissions");
    }

    private boolean checkBasicPermissions(boolean permissionsEnabled, List<String> permissions) {
        if (player.isOp() || !permissionsEnabled) return true;

        for (String permission : permissions) {
            if (!player.hasPermission(permission)) return false;
        }

        return true;
    }

    /* Bottles permissions */

    public boolean canCreateBottles() {
        return checkBasicPermissions(bottlesPermissionsEnabled, List.of("betterexperience.bottles.create"));
    }

    public boolean canLockBottles() {
        return checkBasicPermissions(bottlesPermissionsEnabled, List.of("betterexperience.bottles.lock"));
    }

    public boolean canUseLockedBottles() {
        return checkBasicPermissions(bottlesPermissionsEnabled, List.of("betterexperience.bottles.useLocked"));
    }

    /* Evolve permissions */

    public boolean canEvolve() {
        return checkBasicPermissions(evolvePermissionsEnabled, List.of("betterexperience.evolve"));
    }

    public boolean canViewEvolveState() {
        return checkBasicPermissions(evolvePermissionsEnabled, List.of("betterexperience.evolve.view"));
    }
}
