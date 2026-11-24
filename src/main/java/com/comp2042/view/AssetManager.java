package com.comp2042.view;

import com.comp2042.model.CoinType;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads and caches image and color assets from the classpath for the view layer.
 */
public final class AssetManager {
    private static final String RESOURCE_ROOT = "/resources/views/res/";
    private static final Map<String, Image> IMAGES = new HashMap<>();

    /** Background fill color for the game board canvas. */
    public static final Color BACKGROUND_COLOR = Color.web("feffe4");
    /** Fill color used to render board obstacles. */
    public static final Color OBSTACLE_COLOR = Color.BLACK;

    private static final Image SNAKE_HEAD = loadImage("snakehead.png");
    private static final Image SNAKE_BODY = loadImage("snakebody.png");
    private static final Image SNAKE_TURN = loadImage("snaketurnleft.png");
    private static final Image SNAKE_TAIL = loadImage("snaketail.png");
    private static final Image RED_COIN = loadImage("redcoin.png");
    private static final Image GOLD_COIN = loadImage("goldcoin.png");
    private static final Image SILVER_COIN = loadImage("silvercoin.png");
    private static final Image BLUE_COIN = loadImage("bluecoin.png");
    private static final Image RAINBOW_COIN = loadImage("rainbowcoin.png");
    private static final Image SNAKE_LOGO = loadImage("SnakeLogoWhite.png");

    private AssetManager() {
    }

    // Loads one image and stores it by file name.
    private static Image loadImage(String fileName) {
        Image cachedImage = IMAGES.get(fileName);
        if (cachedImage != null) {
            return cachedImage;
        }

        URL location = AssetManager.class.getResource(RESOURCE_ROOT + fileName);
        if (location == null) {
            throw new IllegalStateException("Missing asset: " + fileName);
        }

        Image image = new Image(location.toExternalForm());
        IMAGES.put(fileName, image);
        return image;
    }

    /**
     * Returns the cached snake head sprite.
     *
     * @return snake head image
     */
    public static Image getSnakeHead() {
        return SNAKE_HEAD;
    }

    /**
     * Returns the cached straight snake body sprite.
     *
     * @return snake body image
     */
    public static Image getSnakeBody() {
        return SNAKE_BODY;
    }

    /**
     * Returns the cached snake corner turn sprite.
     *
     * @return snake turn image
     */
    public static Image getSnakeTurn() {
        return SNAKE_TURN;
    }

    /**
     * Returns the cached snake tail sprite.
     *
     * @return snake tail image
     */
    public static Image getSnakeTail() {
        return SNAKE_TAIL;
    }

    /**
     * Returns the cached application logo image used for window icons.
     *
     * @return snake logo image
     */
    public static Image getSnakeLogo() {
        return SNAKE_LOGO;
    }

    /**
     * Maps a logical coin type to its cached texture image.
     *
     * @param type logical coin type
     * @return matching coin sprite image
     */
    public static Image getCoinImage(CoinType type) {
        return switch (type) {
            case GOLD -> GOLD_COIN;
            case SILVER -> SILVER_COIN;
            case BLUE -> BLUE_COIN;
            case RAINBOW -> RAINBOW_COIN;
            case RED -> RED_COIN;
        };
    }

}
