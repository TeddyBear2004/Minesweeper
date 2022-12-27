package de.teddy.minesweeper.game;

import de.teddy.minesweeper.game.modifier.PersonalModifier;
import org.bukkit.Material;
import org.bukkit.entity.Player;

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

    public MarkType next(Player player) {
        MarkType[] values = values();
        int nextId = this.ordinal() + 1;

        if (nextId >= values.length) {
            nextId = 0;
        }

        if (player != null) {
            PersonalModifier personalModifier = PersonalModifier.getPersonalModifier(player);
            if (!personalModifier.isEnableQuestionMark().orElse(false) && values.length > 2 && nextId == 2) {
                nextId = 0;
            }
        }

        return values[nextId];
    }

    public boolean isNone() {
        return this == MarkType.NONE;
    }
}
