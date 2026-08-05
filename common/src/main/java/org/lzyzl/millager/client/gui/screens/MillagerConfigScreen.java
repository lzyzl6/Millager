package org.lzyzl.millager.client.gui.screens;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.lzyzl.millager.config.MillagerConfig;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class MillagerConfigScreen extends Screen {

    private final Screen parent;
    private final List<Row> rows = new ArrayList<>();
    private List<FormattedCharSequence> overviewLines = List.of();
    private MillagerConfig.ConfigData data;
    private Category category = Category.GAME_RULES;
    private int page;
    private int rowsTop;
    private Component error;

    public MillagerConfigScreen(Screen parent) {
        super(Component.translatable("millager.config.title"));
        this.parent = parent;
        this.data = MillagerConfig.copy();
    }

    @Override
    protected void init() {
        rebuild();
    }

    private void rebuild() {
        clearWidgets();
        rows.clear();

        int categoryLeft = (this.width - 310) / 2;
        for (int i = 0; i < Category.values().length; i++) {
            Category value = Category.values()[i];
            Button button = addRenderableWidget(Button.builder(value.title, ignored -> {
                if (!commitPage()) return;
                this.category = value;
                this.page = 0;
                this.error = null;
                rebuild();
            }).bounds(categoryLeft + i * 105, 28, 100, 20).build());
            button.active = value != this.category;
        }

        int contentWidth = Math.min(560, this.width - 20);
        int left = (this.width - contentWidth) / 2;
        this.overviewLines = this.font.split(this.category.overview, contentWidth);
        this.rowsTop = 57 + this.overviewLines.size() * 9 + 7;

        List<Field> fields = fields();
        int rowsPerPage = rowsPerPage();
        int pageCount = Math.max(1, (fields.size() + rowsPerPage - 1) / rowsPerPage);
        this.page = Math.min(this.page, pageCount - 1);
        int labelWidth = labelWidth(fields, contentWidth);
        int inputLeft = left + labelWidth + 8;
        int inputWidth = contentWidth - labelWidth - 8;
        int first = this.page * rowsPerPage;
        int last = Math.min(first + rowsPerPage, fields.size());
        Object section = this.category.section(this.data);

        for (int i = first; i < last; i++) {
            Field field = fields.get(i);
            int y = this.rowsTop + (i - first) * 36;
            try {
                Component name = displayName(field);
                Tooltip tooltip = Tooltip.create(description(field));
                if (field.getType() == boolean.class) {
                    Button toggle = addRenderableWidget(Button.builder(booleanText(field.getBoolean(section)), ignored -> {
                        try {
                            boolean value = !field.getBoolean(section);
                            field.setBoolean(section, value);
                            ignored.setMessage(booleanText(value));
                        } catch (IllegalAccessException exception) {
                            this.error = Component.translatable("millager.config.error");
                        }
                    }).bounds(inputLeft, y, inputWidth, 20).build());
                    toggle.setTooltip(tooltip);
                    rows.add(new Row(name, field, null, toggle, y + 6));
                } else {
                    EditBox input = new EditBox(this.font, inputLeft, y, inputWidth, 20, name);
                    input.setMaxLength(64);
                    input.setValue(String.valueOf(field.get(section)));
                    input.setTooltip(tooltip);
                    addRenderableWidget(input);
                    rows.add(new Row(name, field, input, null, y + 6));
                }
            } catch (IllegalAccessException exception) {
                this.error = Component.translatable("millager.config.error");
            }
        }

        Button previous = addRenderableWidget(Button.builder(Component.literal("<"), ignored -> {
            if (!commitPage()) return;
            this.page--;
            this.error = null;
            rebuild();
        }).bounds(this.width / 2 - 75, this.height - 52, 20, 20).build());
        previous.active = this.page > 0;

        Button next = addRenderableWidget(Button.builder(Component.literal(">"), ignored -> {
            if (!commitPage()) return;
            this.page++;
            this.error = null;
            rebuild();
        }).bounds(this.width / 2 + 55, this.height - 52, 20, 20).build());
        next.active = this.page + 1 < pageCount;

        addRenderableWidget(Button.builder(Component.translatable("controls.reset"), ignored -> {
            this.data = new MillagerConfig.ConfigData();
            this.error = null;
            rebuild();
        }).bounds(this.width / 2 - 155, this.height - 26, 100, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), ignored -> onClose())
                .bounds(this.width / 2 - 50, this.height - 26, 100, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), ignored -> save())
                .bounds(this.width / 2 + 55, this.height - 26, 100, 20).build());
    }

    private boolean commitPage() {
        Object section = this.category.section(this.data);

        for (Row row : this.rows) {
            if (row.input == null) continue;

            try {
                Class<?> type = row.field.getType();
                String value = row.input.getValue().trim();
                if (type == int.class) {
                    row.field.setInt(section, Integer.parseInt(value));
                } else if (type == float.class) {
                    row.field.setFloat(section, Float.parseFloat(value));
                } else if (type == double.class) {
                    row.field.setDouble(section, Double.parseDouble(value));
                } else {
                    row.field.set(section, value);
                }
            } catch (IllegalAccessException | NumberFormatException exception) {
                this.error = Component.translatable("millager.config.invalid", row.name);
                return false;
            }
        }

        return true;
    }

    private void save() {
        if (!commitPage()) return;

        if (MillagerConfig.save(this.data)) {
            this.minecraft.setScreen(this.parent);
        } else {
            this.error = Component.translatable("millager.config.save_failed");
        }
    }

    private List<Field> fields() {
        return Arrays.asList(this.category.type.getFields());
    }

    private int rowsPerPage() {
        return Math.max(1, Math.min(6, (this.height - this.rowsTop - 67) / 36));
    }

    private int labelWidth(List<Field> fields, int contentWidth) {
        int widest = 0;
        for (Field field : fields) {
            widest = Math.max(widest, this.font.width(displayName(field)));
        }
        return Math.min(widest + 8, Math.max(80, contentWidth - 108));
    }

    private static Component booleanText(boolean value) {
        return Component.translatable(value ? "options.on" : "options.off");
    }

    private Component displayName(Field field) {
        return Component.translatable(optionKey(field) + ".name");
    }

    private Component description(Field field) {
        return Component.translatable(optionKey(field) + ".description");
    }

    private String optionKey(Field field) {
        String name = field.getName().replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
        return "millager.config.option." + this.category.key + "." + name;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0xE0101010);
        graphics.centeredText(this.font, this.title, this.width / 2, 9, 0xFFFFFFFF);

        int contentWidth = Math.min(560, this.width - 20);
        int left = (this.width - contentWidth) / 2;
        for (int i = 0; i < this.overviewLines.size(); i++) {
            graphics.text(this.font, this.overviewLines.get(i), left, 57 + i * 9, 0xFFA0A0A0);
        }
        for (Row row : this.rows) {
            graphics.text(this.font, row.name, left, row.y, 0xFFE0E0E0);
        }

        int rowsPerPage = rowsPerPage();
        int pageCount = Math.max(1, (fields().size() + rowsPerPage - 1) / rowsPerPage);
        graphics.centeredText(this.font, Component.literal((this.page + 1) + " / " + pageCount), this.width / 2, this.height - 46, 0xFFA0A0A0);
        if (this.error != null) {
            graphics.centeredText(this.font, this.error, this.width / 2, this.height - 64, 0xFFFF5555);
        }
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    private record Row(Component name, Field field, EditBox input, Button toggle, int y) {
    }

    private enum Category {
        GAME_RULES("game_rule_defaults",
                Component.translatable("millager.config.category.gamerules"),
                Component.translatable("millager.config.overview.game_rule_defaults"),
                MillagerConfig.GameRuleDefaults.class),
        DEFENDER("defender",
                Component.translatable("millager.config.category.defender"),
                Component.translatable("millager.config.overview.defender"),
                MillagerConfig.Defender.class),
        PATROL("patrol",
                Component.translatable("millager.config.category.patrol"),
                Component.translatable("millager.config.overview.patrol"),
                MillagerConfig.Patrol.class);

        private final String key;
        private final Component title;
        private final Component overview;
        private final Class<?> type;

        Category(String key, Component title, Component overview, Class<?> type) {
            this.key = key;
            this.title = title;
            this.overview = overview;
            this.type = type;
        }

        private Object section(MillagerConfig.ConfigData data) {
            return switch (this) {
                case GAME_RULES -> data.gameRuleDefaults;
                case DEFENDER -> data.defender;
                case PATROL -> data.patrol;
            };
        }
    }
}
