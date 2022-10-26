package pers.solid.mod.mixin;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pers.solid.mod.Configs;
import pers.solid.mod.TransferRule;

import java.util.Set;
import java.util.stream.Collectors;

@Mixin(Item.class)
public abstract class ItemMixin {
  @Shadow
  public abstract Item asItem();

  @Shadow
  public abstract boolean isItemBarVisible(ItemStack stack);
  
  /**
   * 判断物品是否在转移规则中指定的组中的任意一个。如果转移规则没有此物品，则按照原版进行。
   */
  @Inject(method = "isIn", at = @At("HEAD"), cancellable = true)
  public void isInMixin(ItemGroup group, CallbackInfoReturnable<Boolean> cir) {
    if (group == ItemGroup.INVENTORY || group == ItemGroup.SEARCH || group == ItemGroup.HOTBAR || !Configs.instance.enableGroupTransfer) return;
    final Item item = this.asItem();
    final Set<ItemGroup> groups = TransferRule.streamTransferredGroupOf(item).collect(Collectors.toSet());
    if (!groups.isEmpty()) {
      cir.setReturnValue(groups.contains(group));
      cir.cancel();
    }
  }
}
