package de.teddy.minesweeper.game;

import org.bukkit.Material;

public enum MarkType {
    NONE(Material.AIR),
    BOMB_MARK(Material.REDSTONE_TORCH),
    QUESTION_MARK(Material.TORCH);

    private final Material material;

    MarkType(Material material) {
        this.material = material;
    }

    public Material getMaterial() {
        return material;
    }

    public MarkType next() {
        int nextId = this.ordinal() + 1;

        MarkType[] values = values();

        if (values.length <= nextId)
            nextId = 0;

        return values[nextId];
    }

    public boolean isNone() {
        return this == MarkType.NONE;
    }
}
