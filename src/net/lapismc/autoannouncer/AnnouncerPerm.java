package net.lapismc.autoannouncer;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

class AnnouncerPerm {
    private static Permission Permissions = null;
    private final Announcer plugin;
    private boolean permissionsEnabled = false;

    AnnouncerPerm(Announcer plugin) {
        this.plugin = plugin;
    }

    void enablePermissions() {
        RegisteredServiceProvider<Permission> permissionProvider = this.plugin.getServer().getServicesManager().getRegistration(Permission.class);
        if (permissionProvider != null) {
            Permissions = permissionProvider.getProvider();
            this.permissionsEnabled = true;
            plugin.logger.info("Permission support enabled!");
        } else {
            plugin.logger.warning("Permission system not found!");
        }
    }

    boolean has(Player player, Boolean op) {
        if (this.permissionsEnabled) {
            return Permissions.has(player, "announcer.admin");
        }
        return op;
    }

    boolean group(Player player, String group) {
        return this.permissionsEnabled && Permissions.playerInGroup(player, group);
    }
}
