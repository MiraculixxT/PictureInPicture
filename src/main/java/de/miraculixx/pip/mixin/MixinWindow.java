package de.miraculixx.pip.mixin;

import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.platform.ScreenManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.WindowEventHandler;
import de.miraculixx.pip.PictureInPictureKt;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public abstract class MixinWindow {
    @Shadow
    @Final
    private long window;

    @Shadow
    private int windowedX;

    @Shadow
    private int windowedY;

    @Shadow public abstract void setWindowed(int i, int j);

    @Shadow protected abstract void onMove(long l, int i, int j);

    private static int mouseX, mouseY;

    @Inject(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lorg/lwjgl/glfw/GLFW;glfwCreateWindow(IILjava/lang/CharSequence;JJ)J",
            shift = At.Shift.BEFORE,
            remap = false
        )
    )
    private void onSetWindowHints(CallbackInfo ci) {
        int mode;
        PictureInPictureKt.loadData();
        if (PictureInPictureKt.getConfig().getPinned()) mode = GLFW.GLFW_TRUE;
        else mode = GLFW.GLFW_FALSE;
        GLFW.glfwWindowHint(GLFW.GLFW_FLOATING, mode);
//        GLFW.glfwWindowHint(GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);
    }

    @Inject(
        method = "<init>",
        at = @At(
            value = "TAIL",
            remap = false
        )
    )
    private void afterWindowCreation(WindowEventHandler windowEventHandler, ScreenManager screenManager, DisplayData displayData, String string, String string2, CallbackInfo ci) {
        System.out.println("INIT ----------------");

        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwSetInputMode(window, GLFW.GLFW_STICKY_MOUSE_BUTTONS, GLFW.GLFW_TRUE);

        GLFW.glfwSetMouseButtonCallback(window, (window, button, action, mods) -> {
            System.out.println("1");
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && action == GLFW.GLFW_PRESS) {
                System.out.println("2 - " + mouseX + " " + mouseY);

                windowedX = mouseX;
                windowedY = mouseY;
            }
        });

        GLFW.glfwSetCursorPosCallback(window, (window, xpos, ypos) -> {
            mouseX = (int) xpos;
            mouseY = (int) ypos;

            if (GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS) {
                double dx = mouseX - windowedX;
                double dy = mouseY - windowedY;

                int[] xPos = new int[1];
                int[] yPos = new int[1];
                GLFW.glfwGetWindowPos(window, xPos, yPos);

                GLFW.glfwSetWindowPos(window, (int) (xPos[0] + dx), (int) (yPos[0] + dy));

                // Update the windowX and windowY variables
                windowedX = mouseX;
                windowedY = mouseY;
            }
        });

        GLFW.glfwMakeContextCurrent(window);
    }
}
