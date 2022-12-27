package de.teddy.minesweeper.game.modifier;

import de.teddy.minesweeper.Minesweeper;
import de.teddy.minesweeper.game.Board;
import de.teddy.minesweeper.game.Game;
import de.teddy.minesweeper.game.painter.Painter;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collections;
import java.util.Optional;

public class PersonalModifier {

    private final static NamespacedKey RESOURCE_PACK_URL_KEY = new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "resource_pack_url");
    private final static NamespacedKey DOUBLE_CLICK_DURATION_KEY = new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "double_click_duration");
    private final static NamespacedKey PAINTER_CLASS_KEY = new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "painter_class");
    private final static NamespacedKey ENABLE_QUESTION_MARK_KEY = new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "enable_question_mark");
    private final Player player;
    private final PersistentDataContainer container;
    private String resourcePackUrl;
    private Integer doubleClickDuration;
    private String painterClass;
    private Boolean enableQuestionMark;

    private PersonalModifier(Player player, PersistentDataContainer container, String resourcePackUrl, Integer doubleClickDuration, String painterClass, Boolean enableQuestionMark) {
        this.player = player;
        this.container = container;
        this.resourcePackUrl = resourcePackUrl;
        this.doubleClickDuration = doubleClickDuration;
        this.painterClass = painterClass;
        this.enableQuestionMark = enableQuestionMark;
    }

    public static PersonalModifier getPersonalModifier(Player player) {
        return getPersonalModifier(player, player.getPersistentDataContainer());
    }

    public static PersonalModifier getPersonalModifier(Player player, PersistentDataContainer container) {
        String resourcePackUrl = container.get(RESOURCE_PACK_URL_KEY, PersistentDataType.STRING);
        Integer doubleClickDuration = container.get(DOUBLE_CLICK_DURATION_KEY, PersistentDataType.INTEGER);
        String painterClass = container.get(PAINTER_CLASS_KEY, PersistentDataType.STRING);
        Byte enableQuestionMark = container.get(ENABLE_QUESTION_MARK_KEY, PersistentDataType.BYTE);

        return new PersonalModifier(player,
                                    container,
                                    resourcePackUrl,
                                    doubleClickDuration == null ? 350 : doubleClickDuration,
                                    painterClass,
                                    enableQuestionMark != null && enableQuestionMark == 0b1);
    }

    public Optional<String> getResourcePackUrl() {
        return Optional.ofNullable(resourcePackUrl);
    }

    public void setResourcePackUrl(String resourcePackUrl) {
        this.resourcePackUrl = resourcePackUrl;

        if (this.resourcePackUrl != null) {
            this.container.set(RESOURCE_PACK_URL_KEY, PersistentDataType.STRING, this.resourcePackUrl);
        } else {
            this.container.remove(RESOURCE_PACK_URL_KEY);
        }
    }

    public Optional<Integer> getDoubleClickDuration() {
        return Optional.ofNullable(doubleClickDuration);
    }

    public void setDoubleClickDuration(Integer doubleClickDuration) {
        this.doubleClickDuration = doubleClickDuration;

        if (this.doubleClickDuration != null) {
            this.container.set(DOUBLE_CLICK_DURATION_KEY, PersistentDataType.INTEGER, this.doubleClickDuration);
        } else {
            this.container.remove(DOUBLE_CLICK_DURATION_KEY);
        }
    }

    public Optional<String> getPainterClass() {
        return Optional.ofNullable(painterClass);
    }

    public void setPainterClass(String painterClass) {
        this.painterClass = painterClass;

        Board board = Game.getBoard(player);
        if (board == null) board = Game.getBoardWatched(player);

        if (board != null) {
            Painter painter = Game.PAINTER_MAP.get(Painter.loadPainterClass(player.getPersistentDataContainer()));
            painter.drawBlancField(board, Collections.singletonList(player));
        }

        if (this.painterClass != null) {
            try{
                Class<? extends Painter> painterClazz = Class.forName(painterClass).asSubclass(Painter.class);
                this.container.set(PAINTER_CLASS_KEY, PersistentDataType.STRING, this.painterClass);
                Painter.storePainterClass(container, painterClazz);
            }catch(ClassNotFoundException ignored){
            }
        } else {
            this.container.remove(PAINTER_CLASS_KEY);
            Painter.storePainterClass(container, Painter.DEFAULT_PAINTER);
        }

        if (board != null) {
            Painter painter = Game.PAINTER_MAP.get(Painter.loadPainterClass(player.getPersistentDataContainer()));
            painter.drawField(board, Collections.singletonList(player));
        }
    }

    public Optional<Boolean> isEnableQuestionMark() {
        return Optional.ofNullable(enableQuestionMark);
    }

    public void setEnableQuestionMark(Boolean enableQuestionMark) {
        this.enableQuestionMark = enableQuestionMark;

        if (this.enableQuestionMark != null) {
            this.container.set(ENABLE_QUESTION_MARK_KEY, PersistentDataType.BYTE, this.enableQuestionMark ? (byte) 1 : (byte) 0);
        } else {
            this.container.remove(ENABLE_QUESTION_MARK_KEY);
        }
    }

}
