package de.teddy.minesweeper.game.modifier;

import de.teddy.minesweeper.game.GameManager;
import de.teddy.minesweeper.Minesweeper;
import de.teddy.minesweeper.game.Board;
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
    private final static NamespacedKey ENABLE_MARKS = new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "enable_marks");
    private final static NamespacedKey ENABLE_DOUBLE_CLICK = new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "enable_double_click");
    private final static NamespacedKey HIDE_PLAYER = new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "hide_player");
    private final static NamespacedKey HIDE_PLAYER_DISTANCE = new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "hide_player_distance");
    private final static NamespacedKey REVEAL_ON_DOUBLE_CLICK = new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "reveal_on_double_click");
    private final static NamespacedKey USE_MULTI_FLAG = new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "use_multi_flag");
    private final Player player;
    private final PersistentDataContainer container;
    private String resourcePackUrl;
    private Integer doubleClickDuration;
    private String painterClass;
    private Boolean enableQuestionMark;
    private Boolean enableMarks;
    private Boolean enableDoubleClick;
    private Boolean hidePlayer;
    private Double hidePlayerDistance;
    private Boolean revealOnDoubleClick;
    private Boolean useMultiFlag;

    private PersonalModifier(Player player,
                             PersistentDataContainer container,
                             String resourcePackUrl,
                             Integer doubleClickDuration,
                             String painterClass,
                             Boolean enableQuestionMark,
                             Boolean enableMarks,
                             Boolean enableDoubleClick,
                             Boolean hidePlayer,
                             Double hidePlayerDistance,
                             Boolean revealOnDoubleClick,
                             Boolean useMultiFlag) {
        this.player = player;
        this.container = container;
        this.resourcePackUrl = resourcePackUrl;
        this.doubleClickDuration = doubleClickDuration;
        this.painterClass = painterClass;
        this.enableQuestionMark = enableQuestionMark;
        this.enableMarks = enableMarks;
        this.enableDoubleClick = enableDoubleClick;
        this.hidePlayer = hidePlayer;
        this.hidePlayerDistance = hidePlayerDistance;
        this.revealOnDoubleClick = revealOnDoubleClick;
        this.useMultiFlag = useMultiFlag;
    }

    public static PersonalModifier getPersonalModifier(Player player) {
        return getPersonalModifier(player, player.getPersistentDataContainer());
    }

    public static PersonalModifier getPersonalModifier(Player player, PersistentDataContainer container) {
        String resourcePackUrl = container.get(RESOURCE_PACK_URL_KEY, PersistentDataType.STRING);
        Integer doubleClickDuration = container.get(DOUBLE_CLICK_DURATION_KEY, PersistentDataType.INTEGER);
        String painterClass = container.get(PAINTER_CLASS_KEY, PersistentDataType.STRING);
        Byte enableQuestionMark = container.get(ENABLE_QUESTION_MARK_KEY, PersistentDataType.BYTE);
        Byte enableMarks = container.get(ENABLE_MARKS, PersistentDataType.BYTE);
        Byte enableDoubleClick = container.get(ENABLE_DOUBLE_CLICK, PersistentDataType.BYTE);
        Byte hidePlayer = container.get(HIDE_PLAYER, PersistentDataType.BYTE);
        Double hidePlayerDistance = container.get(HIDE_PLAYER_DISTANCE, PersistentDataType.DOUBLE);
        Byte revealOnDoubleClick = container.get(REVEAL_ON_DOUBLE_CLICK, PersistentDataType.BYTE);
        Byte useMultiFlag = container.get(USE_MULTI_FLAG, PersistentDataType.BYTE);

        return new PersonalModifier(player,
                                    container,
                                    resourcePackUrl,
                                    doubleClickDuration == null ? 350 : doubleClickDuration,
                                    painterClass,
                                    enableQuestionMark == null ? null : enableQuestionMark == 0b1,
                                    enableMarks == null ? null : enableMarks == 0b1,
                                    enableDoubleClick == null ? null : enableDoubleClick == 0b1,
                                    hidePlayer == null ? null : hidePlayer == 0b1,
                                    hidePlayerDistance,
                                    revealOnDoubleClick == null ? null : revealOnDoubleClick == 0b1,
                                    useMultiFlag == null ? null : useMultiFlag == 0b1);
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

        GameManager gameManager = Minesweeper.getPlugin(Minesweeper.class).getGameManager();
        Board board = gameManager.getBoard(player);
        if (board == null) {
            board = gameManager.getBoardWatched(player);
        }

        if (board != null) {
            Painter painter = Painter.PAINTER_MAP.get(Painter.loadPainterClass(player.getPersistentDataContainer()));
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
            Painter painter = Painter.PAINTER_MAP.get(Painter.loadPainterClass(player.getPersistentDataContainer()));
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

    public Optional<Boolean> isEnableMarks() {
        return Optional.ofNullable(enableMarks);
    }

    public void setEnableMarks(Boolean enableMarks) {
        this.enableMarks = enableMarks;

        if (this.enableMarks != null) {
            this.container.set(ENABLE_MARKS, PersistentDataType.BYTE, this.enableMarks ? (byte) 1 : (byte) 0);
        } else {
            this.container.remove(ENABLE_MARKS);
        }
    }

    public Optional<Boolean> isEnableDoubleClick() {
        return Optional.ofNullable(enableDoubleClick);
    }

    public void setEnableDoubleClick(Boolean enableQuestionMark) {
        this.enableDoubleClick = enableQuestionMark;

        if (this.enableDoubleClick != null) {
            this.container.set(ENABLE_DOUBLE_CLICK, PersistentDataType.BYTE, this.enableDoubleClick ? (byte) 1 : (byte) 0);
        } else {
            this.container.remove(ENABLE_DOUBLE_CLICK);
        }
    }

    public Optional<Boolean> isHidePlayer() {
        return Optional.ofNullable(hidePlayer);
    }

    public void setHidePlayer(Boolean hidePlayer) {
        this.hidePlayer = hidePlayer;

        if (this.hidePlayer != null) {
            this.container.set(HIDE_PLAYER, PersistentDataType.BYTE, this.hidePlayer ? (byte) 1 : (byte) 0);
        } else {
            this.container.remove(HIDE_PLAYER);
        }
    }

    public Optional<Double> getHidePlayerDistance() {
        return Optional.ofNullable(hidePlayerDistance);
    }

    public void setHidePlayerDistance(Double hidePlayerDistance) {
        this.hidePlayerDistance = hidePlayerDistance;

        if (this.hidePlayerDistance != null) {
            this.container.set(HIDE_PLAYER_DISTANCE, PersistentDataType.DOUBLE, this.hidePlayerDistance);
        } else {
            this.container.remove(HIDE_PLAYER_DISTANCE);
        }
    }

    public Optional<Boolean> isRevealOnDoubleClick() {
        return Optional.ofNullable(revealOnDoubleClick);
    }

    public void setRevealOnDoubleClick(Boolean revealOnDoubleClick) {
        this.revealOnDoubleClick = revealOnDoubleClick;

        if (this.revealOnDoubleClick != null) {
            this.container.set(REVEAL_ON_DOUBLE_CLICK, PersistentDataType.BYTE, this.revealOnDoubleClick ? (byte) 1 : (byte) 0);
        } else {
            this.container.remove(REVEAL_ON_DOUBLE_CLICK);
        }
    }

    public Optional<Boolean> isUseMultiFlag() {
        return Optional.ofNullable(useMultiFlag);
    }

    public void setUseMultiFlag(Boolean useMultiFlag) {
        this.useMultiFlag = useMultiFlag;

        if (this.useMultiFlag != null) {
            this.container.set(USE_MULTI_FLAG, PersistentDataType.BYTE, this.useMultiFlag ? (byte) 1 : (byte) 0);
        } else {
            this.container.remove(USE_MULTI_FLAG);
        }
    }

}
