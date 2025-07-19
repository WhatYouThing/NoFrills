package nofrills.mixin;

import net.minecraft.world.entity.ClientEntityManager;
import net.minecraft.world.entity.EntityLike;
import net.minecraft.world.entity.EntityLookup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientEntityManager.class)
public interface ClientEntityManagerAccessor<T extends EntityLike> {
    @Accessor("lookup")
    EntityLookup<T> getLookup();
}
