package xiaojin.gachaaddiction;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import xiaojin.gachaaddiction.client.gui.screen.SlotMachineScreen;
import xiaojin.gachaaddiction.util.LootDisplayCache;
import xiaojin.gachaaddiction.mixed.IAbstractContainerMenu;

/** Exercises real loot generation, mixins, networking, rendering, and skip input. */
public final class GachaClientGameTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (var world = context.worldBuilder().create()) {
            var server = world.getServer();
            var connection = world.getConnection();
            ResourceKey<LootTable> table = ResourceKey.create(Registries.LOOT_TABLE,
                    Identifier.withDefaultNamespace("chests/simple_dungeon"));
            BlockPos pos = server.computeOnServer(mc -> {
                if (LootDisplayCache.get(table).isEmpty()) {
                    throw new AssertionError("Vanilla loot table decoys were not cached");
                }
                var player = connection.getServerPlayer();
                BlockPos chestPos = player.blockPosition().offset(2, 0, 0);
                var level = player.level();
                var block = GachaAddiction.LOOTR_LOADED
                        ? BuiltInRegistries.BLOCK.getValue(Identifier.parse("lootr:chest"))
                        : Blocks.CHEST;
                if (block == Blocks.AIR) throw new AssertionError("Lootr chest was not registered");
                level.setBlockAndUpdate(chestPos, block.defaultBlockState());
                var chest = (ChestBlockEntity) level.getBlockEntity(chestPos);
                chest.setLootTable(table);
                chest.setLootTableSeed(12345L);
                level.getBlockState(chestPos).useWithoutItem(level, player,
                        new BlockHitResult(Vec3.atCenterOf(chestPos), Direction.UP, chestPos, false));
                var data = IAbstractContainerMenu.of(player.containerMenu).gachaaddiction$getGachaaData();
                if (data == null || !data.isOpenGachaaUi()) {
                    throw new AssertionError("Chest did not supply first-open reveal data: " + data);
                }
                return chestPos;
            });
            context.waitForScreen(SlotMachineScreen.class);
            context.waitTicks(20);
            GachaAddiction.LOGGER.info("Reveal screenshot: {}", context.takeScreenshot("26.3-chest-reveal"));
            // SDL-backed key handling must still skip a reel.
            context.getInput().pressKey(InputConstants.KEY_SPACE);
            context.getInput().pressKey(InputConstants.KEY_ESCAPE);
            context.waitFor(mc -> mc.gui.screen() != null
                    && !(mc.gui.screen() instanceof SlotMachineScreen));
            int remaining = server.computeOnServer(mc -> {
                var player = connection.getServerPlayer();
                int count = 0;
                for (int slot = 0; slot < 27; slot++) {
                    count += player.containerMenu.getSlot(slot).getItem().getCount();
                }
                if (count == 0) throw new AssertionError("Generated chest loot disappeared");
                return count;
            });
            context.runOnClient(mc -> mc.player.closeContainer());
            connection.waitForServerboundPackets();
            server.runOnServer(mc -> {
                var player = connection.getServerPlayer();
                var level = player.level();
                level.getBlockState(pos).useWithoutItem(level, player,
                        new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
            });
            connection.waitForClientboundPackets();
            context.waitTicks(5);
            context.runOnClient(mc -> {
                if (mc.gui.screen() instanceof SlotMachineScreen) {
                    throw new AssertionError("Reopening an already revealed chest replayed the animation");
                }
                mc.player.closeContainer();
            });
            GachaAddiction.LOGGER.info("26.3 chest reveal smoke test passed; {} loot items preserved", remaining);
            connection.waitForServerboundPackets();
            // Exercise both new server-only item-drop paths used by /gacha.
            server.runCommand("gacha Player0 gachaaddiction:slot_machine minecraft:chests/simple_dungeon 1");
            context.waitForScreen(SlotMachineScreen.class);
            context.getInput().pressKey(InputConstants.KEY_ESCAPE);
            context.waitFor(mc -> !(mc.gui.screen() instanceof SlotMachineScreen));
            server.runOnServer(mc -> {
                var player = connection.getServerPlayer();
                if (player.getInventory().isEmpty()) {
                    throw new AssertionError("/gacha did not deliver rewards to the inventory");
                }
                for (int slot = 0; slot < 36; slot++) {
                    player.getInventory().setItem(slot, new ItemStack(Items.BARRIER, 64));
                }
                // Exclude visual pickup entities from the preceding command.
                player.level().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(8))
                        .forEach(ItemEntity::discard);
            });
            server.runCommand("gacha Player0 gachaaddiction:slot_machine minecraft:chests/simple_dungeon 1");
            context.waitForScreen(SlotMachineScreen.class);
            server.runOnServer(mc -> {
                var player = connection.getServerPlayer();
                if (player.level().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(8),
                        entity -> !entity.getItem().isEmpty() && !entity.getItem().is(Items.BARRIER)).isEmpty()) {
                    throw new AssertionError("/gacha did not drop overflow rewards");
                }
            });
            context.getInput().pressKey(InputConstants.KEY_ESCAPE);
            context.waitFor(mc -> !(mc.gui.screen() instanceof SlotMachineScreen));
            GachaAddiction.LOGGER.info("26.3 /gacha inventory and overflow smoke tests passed");
        }
    }
}
