package dev.oxydien.simpleModSync.ui.modals;

import net.minecraft.client.gui.screens.Screen;

public class ModalHandlerUtil {
    @FunctionalInterface
    public interface OnIgnoredCallback {
        Screen createScreen(Screen lastScreen);
    }
}
