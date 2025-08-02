package it.alzy.simplehomes.utils;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

public class PermissionUtils {
    

    public static int getHomeLimit(Player player) {
        return player.getEffectivePermissions().stream()
        .map(PermissionAttachmentInfo::getPermission)
        .filter(perm -> perm.startsWith("simplehomes.limit."))
        .map(perm -> perm.substring("simplehomes.limit.".length()))
        .mapToInt(limit -> {
            try {
                return Integer.parseInt(limit);
            } catch(NumberFormatException e) {
                return 1;
            }
        })
        .filter(limit -> limit > 0)
        .max()
        .orElse(1);
    }
}
