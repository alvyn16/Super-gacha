package xiaojin.gachaaddiction.mixin;

import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import xiaojin.gachaaddiction.api.GachaaData;
import xiaojin.gachaaddiction.mixed.IAbstractContainerMenu;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin implements IAbstractContainerMenu {
    @Unique
    @Nullable
    private GachaaData gachaaddiction$gachaaData;

    @Override
    public @Nullable GachaaData gachaaddiction$getGachaaData() {
        return gachaaddiction$gachaaData;
    }

    @Override
    public void gachaaddiction$setGachaaData(@Nullable GachaaData data) {
        gachaaddiction$gachaaData = data;
    }
}
