package org.lzyzl.millager.behavior;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import org.lzyzl.millager.MillagerEntityTypes;
import org.lzyzl.millager.entity.millager.AbstractMillager;

import java.util.List;

public final class MillagerEntityPool {

    public static final List<Entry> CAVALRY = List.of(
            new Entry(MillagerEntityTypes.Breachers.get(), 34f, 30),
            new Entry(MillagerEntityTypes.Lancers.get(), 28f, 30),
            new Entry(MillagerEntityTypes.Scouters.get(), 26f, 40)
    );
    public static final List<Entry> INFANTRY = List.of(
            new Entry(MillagerEntityTypes.Archers.get(), 24f, 30),
            new Entry(MillagerEntityTypes.Swordmasters.get(), 30f, 25),
            new Entry(MillagerEntityTypes.Maulers.get(), 32f, 20),
            new Entry(MillagerEntityTypes.Rioters.get(), 40f, 15),
            new Entry(MillagerEntityTypes.Doctors.get(), 22f, 10)
    );

    public static Entry weightedPick(List<Entry> pool, RandomSource random) {
        int total = 0;
        for (Entry e : pool) total += e.weight();
        if (total == 0) return null;
        int roll = random.nextInt(total);
        int cursor = 0;
        for (Entry e : pool) {
            cursor += e.weight();
            if (roll < cursor) return e;
        }
        return pool.get(pool.size() - 1);
    }

    public record Entry(EntityType<? extends AbstractMillager> type, float maxHealth, int weight) {
    }
}
