package org.lzyzl.millager.client.gui.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lzyzl.millager.Millager;
import org.lzyzl.millager.config.MillagerConfig;
import org.lzyzl.millager.util.MillagerTargetingHelper;
import org.lzyzl.millager.util.TargetRelation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE;

public final class MillagerTargetConfigScreen extends Screen {
    private static final int CARD_HEIGHT = 110;
    private static final int CARD_GAP = 4;
    private static final int HEADER_HEIGHT = 20;
    private static final int LIST_TOP = 56;
    private static final int LIST_BOTTOM = 28;
    private static final int LIST_END_PADDING = 0;
    private static final int SCROLLING_TEXT_PADDING = 4;
    private static final int SCROLLING_TEXT_GAP = 20;
    private static final long SCROLLING_TEXT_NANOS_PER_PIXEL = 40_000_000L;
    private static final String SEARCH_KEY = "millager.config.target.search";
    private static final int SEARCH_HINT_RGB = 0x909090;
    private static final int SEARCH_HINT_COLOR = 0xFF000000 | SEARCH_HINT_RGB;
    private static final int SEARCH_BACKGROUND_COLOR = 0xFF000000;
    private final Screen parent;
    private final MillagerConfig.ConfigData data;
    private final Map<String, String> overrides = new LinkedHashMap<>();
    private final Map<String, Boolean> beeGolemOverrides = new LinkedHashMap<>();
    private final Map<String, Boolean> expanded = new LinkedHashMap<>();
    private final Map<String, Float> zooms = new LinkedHashMap<>();
    private final Map<String, TargetRelation> defaultRelations = new LinkedHashMap<>();
    private final Map<String, Boolean> defaultBeeGolemAttacks = new LinkedHashMap<>();
    private final Map<String, LivingEntity> previewEntities = new LinkedHashMap<>();
    private final Map<String, Integer> previewRetryFrames = new LinkedHashMap<>();
    private final Set<String> failedPreviews = new HashSet<>();
    private final List<Entry> entries = new ArrayList<>();
    private final long textScrollStartedAt = System.nanoTime();
    private EditBox search;
    private int scroll;
    private int contentHeight;
    private int previewFrame;
    private boolean draggingScrollbar;
    private int scrollbarDragOffset;

    public MillagerTargetConfigScreen(Screen parent, MillagerConfig.ConfigData data) {
        super(Component.translatable("millager.config.option.misc.target_relations.name"));
        this.parent = parent;
        this.data = data;
        if (data.targeting != null && data.targeting.overrides != null) this.overrides.putAll(data.targeting.overrides);
        if (data.targeting != null && data.targeting.beeGolemOverrides != null) this.beeGolemOverrides.putAll(data.targeting.beeGolemOverrides);
    }

    @Override
    protected void init() {
        Component searchHint = Component.translatable(SEARCH_KEY).withStyle(style -> style.withColor(SEARCH_HINT_RGB));
        this.search = addRenderableWidget(new EditBox(this.font, 20, 28, this.width - 40, 20, searchHint));
        this.search.setHint(searchHint);
        this.search.setMaxLength(128);
        this.search.setResponder(value -> this.scroll = 0);
        addRenderableWidget(Button.builder(Component.translatable("controls.reset"), ignored -> {
            this.overrides.clear();
            this.beeGolemOverrides.clear();
        })
                .bounds(this.width / 2 - 155, this.height - 24, 100, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), ignored -> returnToParent())
                .bounds(this.width / 2 - 50, this.height - 24, 100, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), ignored -> done())
                .bounds(this.width / 2 + 55, this.height - 24, 100, 20).build());
        buildEntries();
    }

    private void buildEntries() {
        this.entries.clear();
        this.previewEntities.clear();
        this.previewRetryFrames.clear();
        this.defaultRelations.clear();
        this.defaultBeeGolemAttacks.clear();
        for (Map.Entry<ResourceKey<EntityType<?>>, EntityType<?>> entry : ENTITY_TYPE.entrySet()) {
            try {
                String id = String.valueOf(entry.getKey().location());
                String namespace = entry.getKey().location().getNamespace();
                if (id.equals("minecraft:armor_stand")) continue;
                if (!id.equals("minecraft:player") && !DefaultAttributes.hasSupplier(entry.getValue())) continue;
                addEntry(id, namespace, entry.getValue(), entry.getValue().getDescription());
            } catch (Throwable exception) {
                Millager.LOGGER.error("Failed to prepare target relation preview entry {}", entry.getKey(), exception);
            }
        }
        if (this.entries.stream().noneMatch(entry -> entry.id.equals("minecraft:player"))) {
            addEntry("minecraft:player", "minecraft", EntityType.PLAYER, Component.translatable("entity.minecraft.player"));
        }
        this.entries.sort(Comparator.comparing((Entry entry) -> !entry.namespace.equals("minecraft"))
                .thenComparing(entry -> entry.namespace, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(entry -> entry.name.getString(), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(entry -> entry.id));
    }

    private void addEntry(String id, String namespace, EntityType<?> previewType, Component name) {
        this.entries.add(new Entry(id, namespace, previewType, name));
        this.expanded.putIfAbsent(namespace, namespace.equals("minecraft"));
        this.zooms.putIfAbsent(id, 1.0F);
    }

    private @Nullable LivingEntity previewEntity(Entry entry) {
        if (this.failedPreviews.contains(entry.id)) return null;
        LivingEntity entity = this.previewEntities.get(entry.id);
        if (entity != null) return entity;
        Integer retryFrames = this.previewRetryFrames.get(entry.id);
        if (retryFrames != null && this.previewFrame < retryFrames) return null;
        if (retryFrames != null) this.previewRetryFrames.remove(entry.id);
        Minecraft minecraft = this.minecraft;
        Level level = minecraft == null ? null : minecraft.level;
        Entity created = entry.id.equals("minecraft:player")
                ? PreviewEntityLevel.createPlayer(level)
                : PreviewEntityLevel.create(entry.previewType, level);
        if (created instanceof LivingEntity living) {
            this.previewEntities.put(entry.id, living);
            return living;
        }
        this.previewRetryFrames.put(entry.id, this.previewFrame + 20);
        return null;
    }

    private void done() {
        if (this.data.targeting == null) this.data.targeting = new MillagerConfig.Targeting();
        if (this.data.targeting.overrides == null) this.data.targeting.overrides = new LinkedHashMap<>();
        if (this.data.targeting.beeGolemOverrides == null) this.data.targeting.beeGolemOverrides = new LinkedHashMap<>();
        this.data.targeting.overrides.clear();
        this.data.targeting.overrides.putAll(this.overrides);
        this.data.targeting.beeGolemOverrides.clear();
        this.data.targeting.beeGolemOverrides.putAll(this.beeGolemOverrides);
        returnToParent();
    }

    private void returnToParent() {
        Minecraft minecraft = this.minecraft;
        if (minecraft != null) minecraft.setScreen(this.parent);
    }

    @Override
    public void renderBackground(@NonNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.fill(0, 0, this.width, this.height, 0xE0101010);
    }

    @Override
    public void render(@NonNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.previewFrame++;
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 9, 0xFFFFFFFF);
        Layout layout = layout();
        this.contentHeight = layout.contentHeight;
        this.scroll = Math.min(this.scroll, maxScroll());
        Component tooltip = tooltip(layout, mouseX, mouseY);
        graphics.enableScissor(0, LIST_TOP, this.width, this.height - LIST_BOTTOM);
        for (Group group : layout.groups) {
            for (Card card : group.cards) {
                int cardY = card.y - this.scroll;
                if (cardY + CARD_HEIGHT > LIST_TOP && cardY < this.height - LIST_BOTTOM) {
                    drawCardSafely(graphics, card.entry, card.x, cardY, layout.cardWidth, mouseX, mouseY);
                }
            }
        }
        drawHeaders(graphics, layout, mouseX, mouseY);
        graphics.disableScissor();
        drawScrollbar(graphics);
        if (tooltip != null) graphics.renderTooltip(this.font, tooltip, mouseX, mouseY);
        drawSearchWidget(graphics, mouseX, mouseY, partialTick);
        drawSearchCursor(graphics);
    }

    private void drawSearchWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (this.search == null) return;
        graphics.fill(this.search.getX() - 1, this.search.getY() - 1,
                this.search.getX() + this.search.getWidth() + 1, this.search.getY() + this.search.getHeight() + 1, SEARCH_BACKGROUND_COLOR);
        this.search.renderWidget(graphics, mouseX, mouseY, partialTick);
    }

    private void drawSearchCursor(GuiGraphics graphics) {
        if (this.search != null && !this.search.isFocused() && this.search.getValue().isEmpty()
                && (System.currentTimeMillis() / 500L) % 2L == 0L) {
            graphics.drawString(this.font, Component.literal("_"), this.search.getX() + 4 + this.font.width(Component.translatable(SEARCH_KEY)),
                    this.search.getY() + 5, SEARCH_HINT_COLOR);
        }
    }

    private void drawScrollbar(GuiGraphics graphics) {
        if (maxScroll() == 0) return;
        int left = this.width - 10;
        int thumbTop = scrollbarThumbTop();
        int thumbHeight = scrollbarThumbHeight();
        graphics.fill(left, LIST_TOP, left + 4, this.height - LIST_BOTTOM, 0xFF202020);
        graphics.fill(left, thumbTop, left + 4, thumbTop + thumbHeight, this.draggingScrollbar ? 0xFFE0E0E0 : 0xFF909090);
    }

    private List<Entry> filtered(String query) {
        if (query.isEmpty()) return this.entries;
        return this.entries.stream().filter(entry -> entry.id.toLowerCase(Locale.ROOT).contains(query)
                || entry.name.getString().toLowerCase(Locale.ROOT).contains(query)).toList();
    }

    private Layout layout() {
        int width = this.width - 40;
        int columns = Math.max(1, Math.min(4, width / 116));
        int cardWidth = (width - (columns - 1) * CARD_GAP) / columns;
        String query = this.search == null ? "" : this.search.getValue().trim().toLowerCase();
        List<Entry> filtered = filtered(query);
        List<Group> groups = new ArrayList<>();
        int cursorY = LIST_TOP;
        int index = 0;
        while (index < filtered.size()) {
            String namespace = filtered.get(index).namespace;
            int end = index + 1;
            while (end < filtered.size() && filtered.get(end).namespace.equals(namespace)) end++;
            boolean isExpanded = !query.isEmpty() || this.expanded.getOrDefault(namespace, true);
            List<Card> cards = new ArrayList<>();
            int headerY = cursorY;
            cursorY += HEADER_HEIGHT + 4;
            if (isExpanded) {
                for (int cardIndex = 0; index + cardIndex < end; cardIndex++) {
                    Entry entry = filtered.get(index + cardIndex);
                    int cardX = 20 + cardIndex % columns * (cardWidth + CARD_GAP);
                    int cardY = cursorY + cardIndex / columns * (CARD_HEIGHT + CARD_GAP);
                    cards.add(new Card(entry, cardX, cardY));
                }
                cursorY += (end - index + columns - 1) / columns * (CARD_HEIGHT + CARD_GAP);
            }
            groups.add(new Group(namespace, headerY, isExpanded, cards));
            index = end;
        }
        return new Layout(cardWidth, Math.max(0, cursorY - LIST_TOP + LIST_END_PADDING), groups);
    }

    private void drawHeaders(GuiGraphics graphics, Layout layout, int mouseX, int mouseY) {
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 250.0F);
        for (int index = 0; index < layout.groups.size(); index++) {
            Group group = layout.groups.get(index);
            int headerY = stickyHeaderY(layout, index);
            if (headerY + HEADER_HEIGHT > LIST_TOP && headerY < this.height - LIST_BOTTOM) {
                boolean hovered = mouseX >= 20 && mouseX < this.width - 20 && mouseY >= headerY && mouseY < headerY + HEADER_HEIGHT;
                graphics.fill(20, headerY, this.width - 20, headerY + HEADER_HEIGHT, hovered ? 0xFF404040 : 0xFF303030);
                graphics.renderOutline(20, headerY, this.width - 40, HEADER_HEIGHT, hovered ? 0xFFE0A040 : 0xFF707070);
                graphics.drawString(this.font, Component.literal(group.namespace.equals("minecraft") ? "Minecraft" : group.namespace), 28, headerY + 5, 0xFFFFFFFF);
                graphics.drawString(this.font, Component.literal(group.expanded ? "-" : "+"), this.width - 36, headerY + 5, 0xFFFFFFFF);
            }
        }
        graphics.pose().popPose();
    }

    private int stickyHeaderY(Layout layout, int index) {
        int y = layout.groups.get(index).y - this.scroll;
        if (y >= LIST_TOP) return y;
        y = LIST_TOP;
        if (index + 1 < layout.groups.size()) {
            y = Math.min(y, layout.groups.get(index + 1).y - this.scroll - HEADER_HEIGHT);
        }
        return y;
    }

    private void drawCard(GuiGraphics graphics, Entry entry, int x, int y, int width, int mouseX, int mouseY) {
        TargetRelation relation = relation(entry);
        boolean modified = this.overrides.containsKey(entry.id) || this.beeGolemOverrides.containsKey(entry.id);
        graphics.fill(x, y, x + width, y + CARD_HEIGHT - 4, modified ? 0xFF3A3430 : 0xFF252525);
        graphics.renderOutline(x, y, width, CARD_HEIGHT - 4, modified ? 0xFFE0A040 : 0xFF505050);
        LivingEntity entity = previewEntity(entry);
        if (entity != null) {
            float zoom = this.zooms.getOrDefault(entry.id, 1.0F);
            int scale = Mth.clamp((int) (44.0F / Math.max(0.5F, entity.getBbHeight()) * zoom), 8, 80);
            InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, x + 4, y + 4, x + width - 4, y + 60, scale, 0.0625F, mouseX, mouseY, entity);
        }
        drawScrollingText(graphics, entry.name, x, y + 63, width, 0xFFFFFFFF);
        drawScrollingText(graphics, Component.literal(entry.id), x, y + 75, width, 0xFF909090);
        int relationWidth = width / 3;
        for (TargetRelation value : TargetRelation.values()) {
            int relationX = x + 1 + value.ordinal() * relationWidth;
            graphics.fill(relationX, y + 89, relationX + relationWidth - 2, y + 105, value == relation ? relationColor(value) : 0xFF404040);
            graphics.drawCenteredString(this.font, Component.translatable("millager.config.target.short." + value.serializedName()), relationX + relationWidth / 2 - 1, y + 93, 0xFFFFFFFF);
        }
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 200.0F);
        int beeX = x + width - 22;
        int beeY = y + 4;
        boolean beeAttack = beeGolemAttack(entry);
        boolean beeHovered = mouseX >= beeX && mouseX < beeX + 16 && mouseY >= beeY && mouseY < beeY + 16;
        graphics.fill(beeX, beeY, beeX + 16, beeY + 16,
                beeAttack ? beeHovered ? 0xFFD0A653 : 0xFFB58A3C : beeHovered ? 0xFF505050 : 0xFF303030);
        graphics.renderOutline(beeX, beeY, 16, 16, 0xFFB0B0B0);
        if (beeAttack) graphics.drawCenteredString(this.font, Component.literal("B"), beeX + 8, beeY + 4, 0xFFFFFFFF);
        if (this.overrides.containsKey(entry.id) || this.beeGolemOverrides.containsKey(entry.id)) {
            int resetX = x + width - 22;
            int resetY = y + 24;
            if (mouseX >= resetX && mouseX < resetX + 16 && mouseY >= resetY && mouseY < resetY + 16) {
                graphics.fill(resetX, resetY, resetX + 16, resetY + 16, 0xFF505050);
            }
            graphics.drawCenteredString(this.font, "R", resetX + 8, resetY + 4, 0xFFFFFFFF);
        }
        graphics.pose().popPose();
    }

    private void drawCardSafely(GuiGraphics graphics, Entry entry, int x, int y, int width, int mouseX, int mouseY) {
        try {
            drawCard(graphics, entry, x, y, width, mouseX, mouseY);
        } catch (Throwable exception) {
            this.previewEntities.remove(entry.id);
            if (this.failedPreviews.add(entry.id)) {
                Millager.LOGGER.error("Disabling target relation preview for {}", entry.id, exception);
            }
        }
    }

    private void drawScrollingText(GuiGraphics graphics, Component text, int x, int y, int width, int color) {
        int textWidth = this.font.width(text);
        int availableWidth = width - SCROLLING_TEXT_PADDING * 2;
        if (textWidth <= availableWidth) {
            graphics.drawCenteredString(this.font, text, x + width / 2, y, color);
            return;
        }
        int cycleWidth = textWidth + SCROLLING_TEXT_GAP;
        int offset = (int) ((System.nanoTime() - this.textScrollStartedAt) / SCROLLING_TEXT_NANOS_PER_PIXEL % cycleWidth);
        int textX = x + SCROLLING_TEXT_PADDING - offset;
        graphics.enableScissor(x + SCROLLING_TEXT_PADDING, y - 1, x + width - SCROLLING_TEXT_PADDING, y + 10);
        graphics.drawString(this.font, text, textX, y, color);
        graphics.drawString(this.font, text, textX + cycleWidth, y, color);
        graphics.disableScissor();
    }

    private TargetRelation relation(Entry entry) {
        TargetRelation override = TargetRelation.fromSerializedName(this.overrides.get(entry.id));
        if (override != null) return override;
        return defaultRelation(entry);
    }

    private boolean beeGolemAttack(Entry entry) {
        Boolean override = this.beeGolemOverrides.get(entry.id);
        if (override != null) return override;
        Boolean defaultAttack = this.defaultBeeGolemAttacks.get(entry.id);
        if (defaultAttack != null) return defaultAttack;
        LivingEntity entity = previewEntity(entry);
        if (entity == null) return false;
        boolean attack = MillagerTargetingHelper.defaultBeeGolemAttack(entity);
        this.defaultBeeGolemAttacks.put(entry.id, attack);
        return attack;
    }

    private @Nullable Component tooltip(Layout layout, int mouseX, int mouseY) {
        if (mouseY < LIST_TOP || mouseY >= this.height - LIST_BOTTOM) return null;
        if (headerAt(layout, mouseX, mouseY) != null) return null;
        for (Group group : layout.groups) {
            for (Card card : group.cards) {
                int y = card.y - this.scroll;
                if (mouseX >= card.x && mouseX < card.x + layout.cardWidth && mouseY >= y && mouseY < y + CARD_HEIGHT) {
                    int relationWidth = layout.cardWidth / 3;
                    if (mouseY >= y + 89 && mouseY < y + 105) {
                        int relationIndex = Mth.clamp((mouseX - card.x - 1) / relationWidth, 0, 2);
                        TargetRelation relation = TargetRelation.values()[relationIndex];
                        return Component.translatable("millager.config.target." + relation.serializedName() + ".description");
                    } else if (mouseX >= card.x + layout.cardWidth - 22 && mouseX < card.x + layout.cardWidth - 4 && mouseY >= y + 4 && mouseY < y + 22) {
                        return Component.translatable("millager.config.target.bee_golem.description");
                    } else if (previewEntity(card.entry) != null && mouseX >= card.x + 4 && mouseX < card.x + layout.cardWidth - 22 && mouseY >= y + 4 && mouseY < y + 60) {
                        return Component.translatable("millager.config.target.zoom");
                    }
                }
            }
        }
        return null;
    }

    private TargetRelation defaultRelation(Entry entry) {
        TargetRelation relation = this.defaultRelations.get(entry.id);
        if (relation != null) return relation;
        LivingEntity entity = previewEntity(entry);
        if (entity == null) return entry.namespace.equals("millager") ? TargetRelation.FRIENDLY : TargetRelation.NEUTRAL;
        relation = MillagerTargetingHelper.relation(entity);
        this.defaultRelations.put(entry.id, relation);
        return relation;
    }

    private static int relationColor(TargetRelation relation) {
        return switch (relation) {
            case HOSTILE -> 0xFFF05252;
            case NEUTRAL -> 0xFFD0A040;
            case FRIENDLY -> 0xFF3C914C;
        };
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseX >= this.width - 14 && mouseX < this.width - 2
                && mouseY >= LIST_TOP && mouseY < this.height - LIST_BOTTOM && maxScroll() > 0) {
            int thumbTop = scrollbarThumbTop();
            int thumbHeight = scrollbarThumbHeight();
            this.scrollbarDragOffset = mouseY >= thumbTop && mouseY < thumbTop + thumbHeight
                    ? (int) mouseY - thumbTop : thumbHeight / 2;
            this.draggingScrollbar = true;
            scrollFromScrollbar(mouseY);
            return true;
        }
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (button != 0) return false;
        if (mouseY < LIST_TOP || mouseY >= this.height - LIST_BOTTOM) return false;
        Layout layout = layout();
        Group header = headerAt(layout, mouseX, mouseY);
        if (header != null) {
            this.expanded.put(header.namespace, !this.expanded.getOrDefault(header.namespace, true));
            return true;
        }
        int y = (int) mouseY + this.scroll;
        for (Group group : layout.groups) {
            for (Card card : group.cards) {
                if (mouseX >= card.x && mouseX < card.x + layout.cardWidth && y >= card.y && y < card.y + CARD_HEIGHT) {
                    int buttonLeft = card.x + layout.cardWidth - 22;
                    int buttonRight = card.x + layout.cardWidth - 4;
                    if (y >= card.y + 89 && y < card.y + 105) {
                        TargetRelation selected = TargetRelation.values()[Mth.clamp(((int) mouseX - card.x - 1) / (layout.cardWidth / 3), 0, 2)];
                        if (selected == defaultRelation(card.entry)) this.overrides.remove(card.entry.id);
                        else this.overrides.put(card.entry.id, selected.serializedName());
                    } else if (mouseX >= buttonLeft && mouseX < buttonRight
                            && y >= card.y + 4 && y < card.y + 22) {
                        this.beeGolemOverrides.put(card.entry.id, !beeGolemAttack(card.entry));
                    } else if (mouseX >= buttonLeft && mouseX < buttonRight
                            && y >= card.y + 24 && y < card.y + 42) {
                        this.overrides.remove(card.entry.id);
                        this.beeGolemOverrides.remove(card.entry.id);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && this.draggingScrollbar) {
            scrollFromScrollbar(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && this.draggingScrollbar) {
            this.draggingScrollbar = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double delta) {
        Card hovered = hoveredPreview(mouseX, mouseY);
        if (hovered != null) {
            this.zooms.put(hovered.entry.id, Mth.clamp(this.zooms.getOrDefault(hovered.entry.id, 1.0F) + (float) delta * 0.1F, 0.5F, 2.0F));
            return true;
        }
        this.scroll = Mth.clamp(this.scroll - (int) (delta * 24), 0, maxScroll());
        return true;
    }

    private int maxScroll() {
        return Math.max(0, this.contentHeight - (this.height - LIST_TOP - LIST_BOTTOM));
    }

    private int scrollbarThumbHeight() {
        int viewportHeight = this.height - LIST_TOP - LIST_BOTTOM;
        return Math.max(20, viewportHeight * viewportHeight / this.contentHeight);
    }

    private int scrollbarThumbTop() {
        int range = this.height - LIST_TOP - LIST_BOTTOM - scrollbarThumbHeight();
        return LIST_TOP + (maxScroll() == 0 ? 0 : this.scroll * range / maxScroll());
    }

    private void scrollFromScrollbar(double mouseY) {
        int range = this.height - LIST_TOP - LIST_BOTTOM - scrollbarThumbHeight();
        int thumbTop = Mth.clamp((int) mouseY - this.scrollbarDragOffset, LIST_TOP, LIST_TOP + range);
        this.scroll = range == 0 ? 0 : (thumbTop - LIST_TOP) * maxScroll() / range;
    }

    private Card hoveredPreview(double mouseX, double mouseY) {
        if (mouseY < LIST_TOP || mouseY >= this.height - LIST_BOTTOM) return null;
        Layout layout = layout();
        if (headerAt(layout, mouseX, mouseY) != null) return null;
        int y = (int) mouseY + this.scroll;
        for (Group group : layout.groups) {
            for (Card card : group.cards) {
                if (mouseX >= card.x + 4 && mouseX < card.x + layout.cardWidth - 22
                        && y >= card.y + 4 && y < card.y + 60 && previewEntity(card.entry) != null) return card;
            }
        }
        return null;
    }

    private @Nullable Group headerAt(Layout layout, double mouseX, double mouseY) {
        if (mouseX < 20 || mouseX >= this.width - 20 || mouseY < LIST_TOP || mouseY >= this.height - LIST_BOTTOM) return null;
        for (int index = layout.groups.size() - 1; index >= 0; index--) {
            Group group = layout.groups.get(index);
            int headerY = stickyHeaderY(layout, index);
            if (mouseY >= headerY && mouseY < headerY + HEADER_HEIGHT) return group;
        }
        return null;
    }

    private record Entry(String id, String namespace, EntityType<?> previewType, Component name) {
    }

    private record Card(Entry entry, int x, int y) {
    }

    private record Group(String namespace, int y, boolean expanded, List<Card> cards) {
    }

    private record Layout(int cardWidth, int contentHeight, List<Group> groups) {
    }
}
