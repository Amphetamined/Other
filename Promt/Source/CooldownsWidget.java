package wtf.vanquish.client.ui.widget.overlay;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import wtf.vanquish.api.utils.color.UIColors;
import wtf.vanquish.api.utils.other.TextUtil;
import wtf.vanquish.api.utils.render.RenderUtil;
import wtf.vanquish.api.utils.render.fonts.Fonts;
import wtf.vanquish.client.ui.widget.ContainerWidget;

import java.awt.*;
import java.util.*;
import java.util.List;

public class CooldownsWidget extends ContainerWidget {
    private final Map<String, Float> animMap = new HashMap<>();

    public CooldownsWidget() {
        super(100f, 100f);
    }

    @Override public String getName() { return "Cooldowns"; }

    @Override
    public void render(MatrixStack ms) {
        if (mc.player == null) return;

        Map<String, ContainerElement.ColoredString> data = getCurrentCooldowns();

        // Обновление анимаций
        data.keySet().forEach(k -> animMap.put(k, animMap.getOrDefault(k, 0f) + (1f - animMap.getOrDefault(k, 0f)) * 0.15f));
        animMap.entrySet().removeIf(e -> !data.containsKey(e.getKey()) && e.getValue() < 0.05f);

        float h = scaled(11), p = scaled(3.5f), fS = scaled(6f), iconS = scaled(8f);

        // 1. Расчет размеров
        Map<String, Float> widthMap = new HashMap<>();
        float maxCDW = 0;
        for (Map.Entry<String, ContainerElement.ColoredString> entry : data.entrySet()) {
            float nW = getMediumFont().getWidth(entry.getKey(), fS);
            float tW = getMediumFont().getWidth(entry.getValue().text(), fS);
            // Формула: отступ + название + большой пробел + время в рамке + отступ
            float w = p + nW + p * 4 + (tW + scaled(4)) + p;
            widthMap.put(entry.getKey(), w);
            if (w > maxCDW) maxCDW = w;
        }

        String title = "Cooldowns";
        String iconChar = "C";
        float titleW = getMediumFont().getWidth(title, fS);
        float iconW = Fonts.ICOMOON.getWidth(iconChar, iconS);
        float headerW = Math.max(titleW + iconW + p * 5, maxCDW);

        List<String> sortedKeys = new ArrayList<>(data.keySet());
        sortedKeys.sort((a, b) -> Float.compare(widthMap.getOrDefault(b, 0f), widthMap.getOrDefault(a, 0f)));

        float x = getDraggable().getX(), y = getDraggable().getY();
        float dWidth = getDraggable().getWidth();
        boolean isRightSide = x + (dWidth / 2f) > mc.getWindow().getScaledWidth() / 2f;
        float headerX = isRightSide ? (x + dWidth - headerW) : x;

        // --- РЕНДЕР ШАПКИ ---
        RenderUtil.RECT.draw(ms, headerX, y, headerW, h, 3f, new Color(12, 12, 18, 240));
        getMediumFont().drawText(ms, title, headerX + p, y + h/2f - fS/2f, fS, Color.WHITE, 0f);
        Fonts.ICOMOON.drawGradientText(ms, iconChar, headerX + headerW - p - iconW, y + h/2f - iconS/2f, iconS,
                UIColors.primary(), UIColors.secondary(), 1.1f);

        float currentY = y + h + 1.5f;

        // --- РЕНДЕР СПИСКА ---
        for (String key : sortedKeys) {
            float anim = animMap.getOrDefault(key, 0f);
            if (anim <= 0.05f) continue;

            float itemW = widthMap.getOrDefault(key, headerW);
            float itemX = isRightSide ? (x + dWidth - itemW) : x;
            float rowH = h * anim;
            int alpha = (int)(255 * anim);

            RenderUtil.BLUR_RECT.draw(ms, itemX, currentY, itemW, rowH, 3f, new Color(0, 0, 0, (int)(205 * anim)));

            float tY = currentY + (rowH / 2f) - fS/2f;

            // Название предмета (слева)
            getMediumFont().drawText(ms, key, itemX + p, tY, fS, new Color(255, 255, 255, alpha), 0f);

            // Время (справа в рамке)
            String time = data.get(key).text();
            float tW = getMediumFont().getWidth(time, fS);
            float kRectW = tW + scaled(4);
            float kRectH = (fS + scaled(2)) * anim;
            float kRectX = itemX + itemW - p - kRectW;
            float kRectY = currentY + (rowH / 2f) - (kRectH / 2f);

            if (anim > 0.5f) {
                RenderUtil.RECT.drawOutline(ms, kRectX, kRectY, kRectW, kRectH, 2f, 0.5f,
                        new Color(100, 100, 100, alpha), new Color(0, 0, 0, (int)(180 * anim)));
                getMediumFont().drawText(ms, time, kRectX + (kRectW / 2f) - (tW / 2f), tY, fS,
                        new Color(255, 255, 255, alpha), 0f);
            }

            currentY += rowH + 1.5f;
        }

        getDraggable().setWidth(headerW);
        getDraggable().setHeight(currentY - y);
    }

    private Map<String, ContainerElement.ColoredString> getCurrentCooldowns() {
        Map<String, ContainerElement.ColoredString> cooldownData = new HashMap<>();
        if (mc.player == null) return cooldownData;

        ItemCooldownManager manager = mc.player.getItemCooldownManager();
        float delta = mc.getRenderTickCounter().getTickDelta(false);

        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;

            if (manager.isCoolingDown(stack)) {
                Identifier groupId = manager.getGroup(stack);
                ItemCooldownManager.Entry entry = manager.entries.get(groupId);

                if (entry != null) {
                    int remaining = Math.max(0, entry.endTick() - (manager.tick + (int) delta));
                    if (remaining > 0) {
                        String name = stack.getItem().getName().getString();
                        String time = TextUtil.getDurationText(remaining);
                        cooldownData.put(name, new ContainerElement.ColoredString(time));
                    }
                }
            }
        }
        return cooldownData;
    }

    @Override protected Map<String, ContainerElement.ColoredString> getCurrentData() { return null; }
}
