package me.ryanhamshire.GPFlags.flags;

import com.destroystokyo.paper.event.entity.PreCreatureSpawnEvent;
import me.ryanhamshire.GPFlags.*;
import me.ryanhamshire.GPFlags.util.Util;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class FlagDef_NoMobSpawns extends FlagDefinition {

    public FlagDef_NoMobSpawns(FlagManager manager, GPFlags plugin) {
        super(manager, plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntitySpawn(CreatureSpawnEvent event) {
        SpawnReason reason = event.getSpawnReason();
        if (!blocksSpawn(event.getEntity(), reason)) return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreCreatureSpawn(PreCreatureSpawnEvent event) {
        EntityType type = event.getType();
        SpawnReason reason = event.getReason();
        Location location = event.getSpawnLocation();
        if (!blocksSpawn(location, type, reason)) return;

        event.setCancelled(true);
        event.setShouldAbortSpawn(true);
    }

    boolean blocksSpawn(Entity entity, SpawnReason reason) {
        if (Util.isPyroFishingCrabSpawn(entity, reason)) return false;
        return shouldBlockSpawn(entity.getLocation(), entity.getType(), reason);
    }

    boolean blocksSpawn(Location location, EntityType type, SpawnReason reason) {
        if (Util.isPyroFishingCrabSpawn(type, location, reason)) return false;
        return shouldBlockSpawn(location, type, reason);
    }

    private boolean shouldBlockSpawn(Location location, EntityType type, SpawnReason reason) {
        if (type == EntityType.PLAYER || type == EntityType.ARMOR_STAND) return false;
        if (reason == SpawnReason.SLIME_SPLIT) return false;
        if (reason == SpawnReason.COMMAND) return false;

        WorldSettings settings = this.settingsManager.get(location.getWorld());
        if (settings.noMobSpawnIgnoreSpawners && Util.isSpawnerReason(reason)) return false;

        Flag flag = this.getFlagInstanceAtLocation(location, null);
        return flag != null;
    }

    @Override
    public String getName() {
        return "NoMobSpawns";
    }

    @Override
    public MessageSpecifier getSetMessage(String parameters) {
        return new MessageSpecifier(Messages.DisableMobSpawns);
    }

    @Override
    public MessageSpecifier getUnSetMessage() {
        return new MessageSpecifier(Messages.EnableMobSpawns);
    }

}
