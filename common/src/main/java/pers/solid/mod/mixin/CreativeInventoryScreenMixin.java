package pers.solid.mod.mixin;

import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pers.solid.mod.Configs;
import pers.solid.mod.TransferRule;

import java.util.Collection;
import java.util.Iterator;

@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin {

  private ItemStack stack = null;

  @Shadow
  public abstract void removed();

  /**
   * 将方法调用中的 <code>stack</code> 存储到此类的私有变量中以供 {@link #renderTooltipMixin} 使用。
   */
  @Inject(method = "renderTooltip", at = @At("HEAD"))
  public void sendStack(MatrixStack matrices, ItemStack stack, int x, int y, CallbackInfo ci) {
    this.stack = stack;
  }

  /**
   * 在创造模式物品栏的搜索部分，选中物品会显示其物品组。该 mixin 则会使其显示修改后的物品组。修改后的物品组可能为多个，都会显示。
   *
   * @param instance 物品所属的原版物品组。
   * @return 物品所属的物品组所代表的文本。可能是原版的，也有可能是修改后的。
   */
  @Redirect(method = "renderTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemGroup;getTranslationKey()Lnet/minecraft/text/Text;"))
  public Text renderTooltipMixin(ItemGroup instance) {
    final Collection<ItemGroup> itemGroups =
        TransferRule.streamTransferredGroupOf(stack.getItem()).toList();
    if (Configs.instance.enableGroupTransfer && !itemGroups.isEmpty()) {
      MutableText text = new LiteralText("").styled(style -> style.withColor(0x88ccff));
      for (Iterator<ItemGroup> iterator = itemGroups.iterator(); iterator.hasNext(); ) {
        ItemGroup itemGroup = iterator.next();
        text.append(itemGroup.getTranslationKey());
        if (iterator.hasNext()) text.append(" / ");
      }
      return new LiteralText("").append(text);
    }
    return instance.getTranslationKey();
  }
}
