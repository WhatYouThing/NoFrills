package nofrills.mixin;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.world.entity.ClientEntityManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientWorld.class)
public interface ClientWorldAccessor {
    @Accessor("entityManager")
    ClientEntityManager<Entity> getManager();
}
