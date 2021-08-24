package pers.solid.mod.mixin;

import net.minecraft.block.Block;
import net.minecraft.data.family.BlockFamily;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pers.solid.mod.MixinHelper;

import java.util.Map;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin extends Item {
	public BlockItemMixin(Settings settings) {
		super(settings);
	}

	@Shadow public abstract Block getBlock();

	@Inject(at = @At(value = "INVOKE",target = "Lnet/minecraft/block/Block;appendStacks(Lnet/minecraft/item/ItemGroup;Lnet/minecraft/util/collection/DefaultedList;)V",shift = At.Shift.AFTER), method = "appendStacks")
	private void appendMoreBuildingBlockStacks(ItemGroup group, DefaultedList<ItemStack> stacks, CallbackInfo ci) {
		/* 在BlockItem.java中：
		  public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
				if (this.isIn(group)) {
					this.getBlock().appendStacks(group, stacks);
					<--- 插入到此处
						 }
			}
		 */
		// 这个APPENDED_ITEMS，在ItemGroup调用appendStacks时，就已经清空了。
		if (group != ItemGroup.BUILDING_BLOCKS) return;
		Block block = this.getBlock();
		if (MixinHelper.BASE_BLOCKS_TO_FAMILIES.containsKey(block)) {
			BlockFamily blockFamily = MixinHelper.BASE_BLOCKS_TO_FAMILIES.get(block);
			Map<BlockFamily.Variant, Block> variants = blockFamily.getVariants();
			for (var variant : MixinHelper.BUILDING_BLOCK_VARIANTS) {
				if (variants.containsKey(variant)) {
					Block block1 = variants.get(variant);
					block1.appendStacks(group,stacks);
				}
			}
		}
//		MixinHelper.APPENDED_ITEMS.add(this);
	}

	@Inject(at = @At("HEAD"),method = "appendStacks", cancellable = true)
	private void excludedAppendedStacks(ItemGroup group, DefaultedList<ItemStack> stacks, CallbackInfo ci) {
		if (MixinHelper.isBuildingBlockContained(this.getBlock())) {
			ci.cancel();
		}
	}
}
