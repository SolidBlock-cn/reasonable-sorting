package pers.solid.mod.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import pers.solid.mod.Configs;
import pers.solid.mod.SortingRule;
import pers.solid.mod.TransferRule;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin {
  /**
   * 在创造模式物品栏的搜索部分，选中物品会显示其物品组。该 mixin 则会使其显示修改后的物品组。修改后的物品组可能为多个，都会显示。
   */
  @Inject(
      method = "renderTooltip",
      at =
      @At(
          value = "INVOKE",
          target = "Ljava/util/List;add(ILjava/lang/Object;)V", shift = At.Shift.AFTER),
      slice = @Slice(
          from = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemGroup;getTranslationKey()Lnet/minecraft/text/Text;")),
      locals = LocalCapture.CAPTURE_FAILSOFT)
  protected void renderTooltipMixin(MatrixStack matrices, ItemStack stack, int x, int y, CallbackInfo ci, List<Text> list, List<Text> list2) {
    final Collection<ItemGroup> itemGroups = TransferRule.streamTransferredGroupOf(stack.getItem()).collect(Collectors.toList());
    if (Configs.instance.enableGroupTransfer && !itemGroups.isEmpty()) {
      MutableText text = new LiteralText("").styled(style -> style.withColor(TextColor.fromRgb(0x88ccff)));
      for (Iterator<ItemGroup> iterator = itemGroups.iterator(); iterator.hasNext(); ) {
        ItemGroup group = iterator.next();
        text.append(group.getTranslationKey());
        if (iterator.hasNext()) {
          text.append(" / ");
        }
      }
      list2.set(1, text);
    }
  }

  @ModifyVariable(method = "search", at = @At(value = "STORE"))
  public Iterator<Item> modifiedIteratorInSearch(Iterator<Item> value) {
    return SortingRule.modifyIteratorInInventory(value, ItemGroup.SEARCH.getName());
  }
}
