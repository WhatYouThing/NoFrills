package nofrills.mixin;

import net.minecraft.world.entity.EntityIndex;
import net.minecraft.world.entity.EntityLike;
import net.minecraft.world.entity.SimpleEntityLookup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SimpleEntityLookup.class)
public interface SimpleEntityLookupAccessor<T extends EntityLike> {
    @Accessor("index")
    EntityIndex<T> getIndex();
}
