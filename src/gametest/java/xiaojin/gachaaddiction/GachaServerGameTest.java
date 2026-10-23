package xiaojin.gachaaddiction;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.vehicle.minecart.MinecartChest;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;
import xiaojin.gachaaddiction.api.MenuProviderData;
import xiaojin.gachaaddiction.mixed.IMenuProviderLootExtension;
import xiaojin.gachaaddiction.util.LootDisplayCache;

/** Runs in Fabric's SERVER environment, where client classes are unavailable. */
public final class GachaServerGameTest {
    private static final ResourceKey<LootTable> TABLE = ResourceKey.create(Registries.LOOT_TABLE,
            Identifier.withDefaultNamespace("chests/simple_dungeon"));

    @GameTest
    public void blockContainers(GameTestHelper helper) {
        helper.assertTrue(!LootDisplayCache.get(TABLE).isEmpty(), "Server loot cache must be populated");
        BlockPos pos = helper.absolutePos(new BlockPos(0, 1, 0));
        for (var block : new net.minecraft.world.level.block.Block[]{Blocks.CHEST, Blocks.BARREL,
                Blocks.DISPENSER, Blocks.DROPPER, Blocks.HOPPER, Blocks.SHULKER_BOX}) {
            helper.getLevel().setBlockAndUpdate(pos, block.defaultBlockState());
            var container = (RandomizableContainerBlockEntity) helper.getLevel().getBlockEntity(pos);
            container.setLootTable(TABLE);
            container.setLootTableSeed(12345L);
            container.unpackLootTable(null);
            assertFirstRevealAndConsume(helper, container);
        }
        helper.succeed();
    }

    @GameTest
    public void doubleChest(GameTestHelper helper) {
        BlockPos first = helper.absolutePos(new BlockPos(0, 1, 0));
        BlockPos second = first.east();
        var state = Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH);
        var level = helper.getLevel();
        level.setBlock(first, state.setValue(ChestBlock.TYPE, ChestType.LEFT), 2);
        level.setBlock(second, state.setValue(ChestBlock.TYPE, ChestType.RIGHT), 2);
        for (BlockPos pos : new BlockPos[]{first, second}) {
            var chest = (RandomizableContainerBlockEntity) level.getBlockEntity(pos);
            chest.setLootTable(TABLE);
            chest.unpackLootTable(null);
        }
        MenuProvider provider = level.getBlockState(first).getMenuProvider(level, first);
        helper.assertTrue(provider instanceof IMenuProviderLootExtension,
                "Double-chest provider mixin must apply");
        helper.assertTrue(MenuProviderData.create(provider).lootTable().size() == 2,
                "Both double-chest loot tables must be retained");
        assertFirstRevealAndConsume(helper, provider);
        helper.succeed();
    }

    @GameTest
    public void chestMinecart(GameTestHelper helper) {
        var cart = new MinecartChest(EntityTypes.CHEST_MINECART, helper.getLevel());
        cart.setPos(Vec3.atCenterOf(helper.absolutePos(new BlockPos(0, 1, 0))));
        cart.setLootTable(TABLE, 12345L);
        cart.unpackChestVehicleLootTable(null);
        assertFirstRevealAndConsume(helper, cart);
        helper.succeed();
    }

    private static void assertFirstRevealAndConsume(GameTestHelper helper, MenuProvider provider) {
        var data = MenuProviderData.create(provider);
        helper.assertTrue(data.isOpenGachaaUi(), "Generated loot must trigger its first reveal");
        helper.assertTrue(data.lootTable().contains(TABLE), "Loot table must survive generation");
        MenuProviderData.open(provider);
        helper.assertTrue(!MenuProviderData.create(provider).isOpenGachaaUi(),
                "Reopening must not trigger another reveal");
    }
}
