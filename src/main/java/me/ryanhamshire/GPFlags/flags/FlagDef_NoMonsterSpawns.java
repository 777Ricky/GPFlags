package me.ryanhamshire.GPFlags.flags;

import com.destroystokyo.paper.event.entity.PreCreatureSpawnEvent;
import me.ryanhamshire.GPFlags.Flag;
import me.ryanhamshire.GPFlags.FlagManager;
import me.ryanhamshire.GPFlags.GPFlags;
import me.ryanhamshire.GPFlags.MessageSpecifier;
import me.ryanhamshire.GPFlags.Messages;
import me.ryanhamshire.GPFlags.WorldSettings;
import me.ryanhamshire.GPFlags.util.Util;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class FlagDef_NoMonsterSpawns extends FlagDefinition {

    public FlagDef_NoMonsterSpawns(FlagManager manager, GPFlags plugin) {
        super(manager, plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntitySpawn(CreatureSpawnEvent event) {
        EntityType type = event.getEntityType();
        SpawnReason reason = event.getSpawnReason();
        if (Util.isPyroFishingCrabSpawn(event.getEntity(), reason)) return;
        if (!shouldBlockSpawn(event.getLocation(), type, reason)) return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreCreatureSpawn(PreCreatureSpawnEvent event) {
        EntityType type = event.getType();
        SpawnReason reason = event.getReason();
        Location location = event.getSpawnLocation();
        if (Util.isPyroFishingCrabSpawn(type, location, reason)) return;
        if (!shouldBlockSpawn(location, type, reason)) return;

        event.setCancelled(true);
        event.setShouldAbortSpawn(true);
    }

    private boolean shouldBlockSpawn(Location location, EntityType type, SpawnReason reason) {
        if (!Util.isMonster(type)) return false;
        if (reason == SpawnReason.SLIME_SPLIT) return false;
        if (reason == SpawnReason.COMMAND) return false;

        WorldSettings settings = this.settingsManager.get(location.getWorld());
        if (settings.noMonsterSpawnIgnoreSpawners && Util.isSpawnerReason(reason)) return false;

        Flag flag = this.getFlagInstanceAtLocation(location, null);
        return flag != null;
    }

    @Override
    public String getName() {
        return "NoMonsterSpawns";
    }

    @Override
    public MessageSpecifier getSetMessage(String parameters) {
        return new MessageSpecifier(Messages.DisableMonsterSpawns);
    }

    @Override
    public MessageSpecifier getUnSetMessage() {
        return new MessageSpecifier(Messages.EnableMonsterSpawns);
    }

}
