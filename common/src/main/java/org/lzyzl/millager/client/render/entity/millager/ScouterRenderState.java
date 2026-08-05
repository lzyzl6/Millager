package org.lzyzl.millager.client.render.entity.millager;

import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemStack;

public class ScouterRenderState extends MillagerRenderState {
    public ItemStack offhandStack = ItemStack.EMPTY;
    public ItemStackRenderState hornRenderState = new ItemStackRenderState();
}
