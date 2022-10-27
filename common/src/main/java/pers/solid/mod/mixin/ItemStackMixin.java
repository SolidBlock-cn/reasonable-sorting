package pers.solid.mod.mixin;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pers.solid.mod.Configs;
import pers.solid.mod.TransferRule;
import pers.solid.mod.TransferRules;
import pers.solid.mod.interfaces.ItemStackInterface;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ItemStackInterface {
    //
    @Shadow public abstract Item getItem();

    // Suggested alternative

    /**
     * 判断物品是否在转移规则中指定的组中的任意一个。如果转移规则没有此物品，则按照原版进行。
     */

    @Inject(method = "isIn", at = @At("HEAD"), cancellable = true)
    public void isInMixin(TagKey<Item> tag, CallbackInfoReturnable<Boolean> cir) {
        /*
        var item = this.getItem();
        var containedGroups = Arrays.stream(ItemGroups.GROUPS).filter((itemGroup)-> { return TransferRules.itemInGroup(item, itemGroup); });;
        final Set<ItemGroup> groups = TransferRule.streamTransferredGroupOf(item).collect(Collectors.toSet());

        AtomicBoolean contained = new AtomicBoolean(false);
        containedGroups.forEach((group)->{
            if (group == ItemGroups.INVENTORY || group == ItemGroups.SEARCH || group == ItemGroups.HOTBAR || !Configs.instance.enableGroupTransfer) return;
            if (groups.contains(group)) { contained.set(true); };
        });

        if (!groups.isEmpty()) {
            cir.setReturnValue(contained.get());
            cir.cancel();
        }*/
    }
}
