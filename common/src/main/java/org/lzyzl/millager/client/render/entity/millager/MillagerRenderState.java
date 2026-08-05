package org.lzyzl.millager.client.render.entity.millager;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.lzyzl.millager.entity.millager.AbstractMillager;

public class MillagerRenderState extends HumanoidRenderState {
    public boolean isRiding;
    public AbstractMillager.MillagerPose armPose = AbstractMillager.MillagerPose.NEUTRAL;
    public boolean isLeftHanded = false;
}
