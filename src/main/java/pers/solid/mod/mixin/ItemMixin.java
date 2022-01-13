package pers.solid.mod.mixin;

import com.google.common.collect.ImmutableCollection;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pers.solid.mod.Configs;
import pers.solid.mod.MixinHelper;

@Mixin(Item.class)
public abstract class ItemMixin {
  @Shadow
  public abstract Item asItem();

  /** 判断物品是否在转移规则中指定的组中的任意一个。如果转移规则没有此物品，则按照原版进行。 */
  @Inject(method = "isIn", at = @At("HEAD"), cancellable = true)
  public void isInMixin(ItemGroup group, CallbackInfoReturnable<Boolean> cir) {
    if (!Configs.CONFIG_HOLDER.getConfig().enableGroupTransfer) return;
    final Item item = this.asItem();
    final @Nullable ImmutableCollection<ItemGroup> itemGroups =
        MixinHelper.ABSTRACT_ITEM_GROUP_TRANSFER_RULES.get(item);
    if (itemGroups != null && group != ItemGroup.SEARCH) {
      cir.setReturnValue(itemGroups.contains(group));
      cir.cancel();
    }
  }
}
