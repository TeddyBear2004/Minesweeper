package de.teddy.minesweeper.game;

import de.teddy.minesweeper.game.modifier.PersonalModifier;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

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

    public MarkType next(@Nullable Player player) {
        MarkType[] values = values();
        int nextId = this.ordinal() + 1;

        if (nextId >= values.length) {
            nextId = NONE.ordinal();
            return values[nextId];
        }

        if (player != null) {
            PersonalModifier personalModifier = PersonalModifier.getPersonalModifier(player);
            if (!personalModifier.<Boolean>get(PersonalModifier.ModifierType.ENABLE_QUESTION_MARK).orElse(false) && nextId == QUESTION_MARK.ordinal()) {
                nextId = NONE.ordinal();
            }
        }

        return values[nextId];
    }

    public boolean isNone() {
        return this == MarkType.NONE;
    }
}
