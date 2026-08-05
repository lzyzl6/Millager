package org.lzyzl.millager.client.util;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class BookAnimationController {

    public float x, y, z, prevX, prevY, prevZ;
    public float yaw, pitch, roll, prevYaw, prevPitch, prevRoll;
    public float open, prevOpen;
    public float flip1, prevFlip1, flip2, prevFlip2;
    private int bookColor = 0xFFFFFF;

    private float targetX, targetY, targetZ;
    private float targetYaw, targetPitch, targetRoll;
    private float targetOpen;
    private float pathTimer;
    private PathMode pathMode = PathMode.STATIONARY;

    private int flipTimer = 0;
    private boolean isAutoFlipping = false;

    public void tick() {
        savePrevValues();

        pathTimer += 1.0f;
        Vec3 pathPos = calculatePath(pathTimer);
        float lerpPos = 0.2f;
        x = Mth.lerp(lerpPos, x, (float)pathPos.x);
        y = Mth.lerp(lerpPos, y, (float)pathPos.y);
        z = Mth.lerp(lerpPos, z, (float)pathPos.z);

        float lerpRot = 0.15f;
        yaw += Mth.degreesDifference(yaw, targetYaw) * lerpRot;
        pitch += Mth.degreesDifference(pitch, targetPitch) * lerpRot;
        roll += Mth.degreesDifference(roll, targetRoll) * lerpRot;

        open = Mth.lerp(0.1f, open, targetOpen);

        handleAutoFlip();
    }

    private void savePrevValues() {
        prevX = x; prevY = y; prevZ = z;
        prevYaw = yaw; prevPitch = pitch; prevRoll = roll;
        prevOpen = open;
        prevFlip1 = flip1; prevFlip2 = flip2;
    }

    private Vec3 calculatePath(float t) {
        double ox = targetX, oy = targetY, oz = targetZ;
        switch (this.pathMode) {
            case ORBIT -> {
                ox = Math.cos(t * 0.1) * 1.3;
                oz = Math.sin(t * 0.1) * 1.3;
                this.targetYaw = (float)(Mth.atan2(oz, ox) * (180 / Math.PI)) + 180f;
            }
            case FIGURE_EIGHT -> {
                ox = Math.sin(t * 0.1);
                oy = targetY + Math.sin(t * 0.2) * 0.3;
                oz = 0.6;
            }
            case SINE_WAVE -> oy = targetY + Math.sin(t * 0.1) * 0.15;
        }
        return new Vec3(ox, oy, oz);
    }

    private void handleAutoFlip() {
        if (isAutoFlipping && open > 0.8f) {
            if (--flipTimer <= 0) {
                if (Math.random() > 0.5) flip1 = 1.0f; else flip2 = 1.0f;
                flipTimer = 30 + (int)(Math.random() * 50);
            }
        }
        flip1 = Mth.lerp(0.1f, flip1, 0.0f);
        flip2 = Mth.lerp(0.1f, flip2, 0.0f);
    }

    public void lookAt(Vec3 targetPos, Vec3 bookGlobalPos) {
        double dX = targetPos.x - bookGlobalPos.x;
        double dY = targetPos.y - bookGlobalPos.y;
        double dZ = targetPos.z - bookGlobalPos.z;
        double dH = Math.sqrt(dX * dX + dZ * dZ);
        this.targetYaw = (float)(Mth.atan2(dZ, dX) * (180.0 / Math.PI)) - 90.0F;
        this.targetPitch = (float)(-(Mth.atan2(dY, dH) * (180.0 / Math.PI)));
    }

    public void setAutoFlip(boolean active) {
        this.isAutoFlipping = active;
    }

    public void setMode(PathMode mode) {
        this.pathMode = mode;
    }

    public void setOpen(boolean open) {
        this.targetOpen = open ? 1.0f : 0.0f;
    }

    public void setColor(int color) {
        this.bookColor = color;
    }

    public int getBookColor() {
        return this.bookColor;
    }

    public void moveTo(float x, float y, float z) {
        this.targetX = x; this.targetY = y; this.targetZ = z;
    }

    public void rotateTo(float y, float p, float r) {
        this.targetYaw = y;
        this.targetPitch = p;
        this.targetRoll = r;
    }

    public enum PathMode {
        STATIONARY,
        ORBIT,
        FIGURE_EIGHT,
        SINE_WAVE
    }
}