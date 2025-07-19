package nofrills.mixin;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.entity.EntityIndex;
import net.minecraft.world.entity.EntityLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityIndex.class)
public interface EntityIndexAccessor<T extends EntityLike> {
    @Accessor("idToEntity")
    Int2ObjectMap<T> getEntityMap();
}
