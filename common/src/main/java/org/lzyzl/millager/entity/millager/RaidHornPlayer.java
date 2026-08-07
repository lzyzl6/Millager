package org.lzyzl.millager.entity.millager;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.raid.Raid;
import org.lzyzl.millager.MillagerSounds;

public interface RaidHornPlayer {

    static void play(Scouter scouter) {
        if (!(scouter.level() instanceof ServerLevel level)) return;
        BlockPos center = scouter.getRaidReinforcementCenter();
        Raid raid = center == null ? null : level.getRaidAt(center);
        if (raid instanceof RaidHornPlayer player) player.millager$playReinforcementHorn(level, scouter.blockPosition());
        else scouter.playSound(MillagerSounds.REINFORCE_HORN, 16.0F, 0.8F);
    }

    void millager$playReinforcementHorn(ServerLevel level, BlockPos sourcePos);
}
