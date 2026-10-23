package xiaojin.gachaaddiction.mixed;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;
import xiaojin.gachaaddiction.api.GachaaData;

public interface IAbstractContainerMenu {
    static IAbstractContainerMenu of(AbstractContainerMenu menu) {
        return (IAbstractContainerMenu) menu;
    }

    @Nullable
    GachaaData gachaaddiction$getGachaaData();

    void gachaaddiction$setGachaaData(GachaaData data);
}
