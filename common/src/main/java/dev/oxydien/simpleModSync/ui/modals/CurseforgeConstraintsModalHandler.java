package dev.oxydien.simpleModSync.ui.modals;

import dev.oxydien.simpleModSync.ui.widgets.LabelBox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CurseforgeConstraintsModalHandler {
    public static void open(Screen parentScreen) {
        var mc = Minecraft.getInstance();

        var title = Component.translatable("simple_mod_sync.ui.modal.curseforge_constraints.title").withStyle(style -> style.withBold(true));

        var modal = new BasicModalWindow.Builder(title, parentScreen)
                .withSize(220, 210)
                .addBodyWidget((screen, mX, mY, mW, mH) -> {
                    var padding = 5;
                    var pX = mX + padding;
                    var pY = mY + padding;
                    var pW = mW - 2 * padding;
                    var pH = mH - 2 * padding;
                    var cY = pY + 20; // current Y
                    var font = screen.getFont();

                    var content = Component.translatable("simple_mod_sync.ui.modal.curseforge_constraints.content");

                    var text = new LabelBox(pX, cY, pW, pH, content, font);
                    screen.addModalWidget(text);
                })
                .build();

        mc.setScreenAndShow(modal);
    }
}
