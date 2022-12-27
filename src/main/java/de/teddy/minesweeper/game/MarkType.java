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
        int nextId = this.ordinal() + 1;

        MarkType[] values = values();

        if (player == null) {
            if (values.length <= nextId)
                nextId = 0;
        } else if (PersonalModifier
                .getPersonalModifier(player.getPersistentDataContainer())
                .isEnableQuestionMark()
                .orElse(false)
                && values.length - 1 <= nextId) {
            nextId = 0;
        }

        return values[nextId];

    }

    public boolean isNone() {
        return this == MarkType.NONE;
    }
}
