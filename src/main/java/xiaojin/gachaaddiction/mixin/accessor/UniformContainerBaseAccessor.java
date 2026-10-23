package xiaojin.gachaaddiction.mixin.accessor;

import net.minecraft.world.level.storage.loot.entries.UniformContainerBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(UniformContainerBase.class)
public interface UniformContainerBaseAccessor {
    @Accessor("weight")
    int gachaaddiction$getWeight();
}
