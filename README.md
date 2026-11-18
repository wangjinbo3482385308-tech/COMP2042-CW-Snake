## GitHub

https://github.com/wangjinbo3482385308-tech/COMP2042-CW-Snake

## Compilation Instructions

- Prerequisites: Java 21, JavaFX 21.0.5, Maven.
- Test: `mvn test`
- Run from source: `mvn javafx:run`
- Build and run JAR: `mvn clean package` then `java -jar target/snake-javafx.jar`

## Implemented and Working Properly

- **Bug Fixes**
  - Prevent reverse-key moves that cause immediate self-collision.
  - Correct rainbow coin spawning through the intended weighted branch.
  - Keep every obstacle in bounds and reject duplicate cells.
  - Correct wrap-around collision and short-snake self-collision handling.
  - Stop timers correctly and fully reset scores on restart.
  - Scale fullscreen layouts and route Esc to the correct exit level.
  - Debounce held directional keys during movement ticks.
  - Roll back the pending body segment on the death frame.
- **Refactoring & Testing**
  - Separate controller, model, and view responsibilities into MVC packages.
  - Remove JavaFX dependencies from model classes to enforce SRP.
  - Use polymorphic coin effects to satisfy the Open-Closed Principle.
  - Centralize coin creation in `CoinFactory`.
  - Cache rotated textures to avoid repeated snapshot rendering.
  - Add a JUnit 5 suite covering model behavior and edge cases.

## Implemented but Not Working Properly

- None. All implemented maintenance changes and unit tests pass.

## Features Not Implemented

- Gameplay extensions (levels, combo scoring, new power-ups) are scheduled for the next milestone.

## New Java Classes

- `com.comp2042.model.CoinFactory` - Creates weighted coin variants without controller changes.
- `com.comp2042.model.CoinType` - Identifies coin behavior independently of JavaFX assets.
- `com.comp2042.model.GameState` - Defines the lifecycle states of one game session.
- `com.comp2042.view.AssetManager` - Loads and caches classpath images and colors.
- `com.comp2042.Launcher` - Starts the JavaFX application safely from the fat JAR.
- `com.comp2042.model.SnakeTest` - Verifies movement, wrapping, growth, and collision behavior.
- `com.comp2042.model.ObstacleTest` - Verifies bounded and unique obstacle generation.
- `com.comp2042.model.CoinAndFactoryTest` - Verifies coin placement, effects, types, and weighted creation.
- `com.comp2042.model.GameTest` - Verifies difficulty settings, scoring, wins, and reset behavior.

## Modified Java Classes

- `com.comp2042.model.Snake` - Moved into MVC's model layer and separated collision logic from rendering.
- `com.comp2042.model.Obstacle` - Moved into the model layer with bounded, duplicate-free generation.
- `com.comp2042.model.Coin` - Moved into the model layer with safe placement and polymorphic effects.
- `com.comp2042.model.BlueCoin` - Moved into the model layer and attached border-passing behavior through overrides.
- `com.comp2042.model.GoldCoin` - Moved into the model layer and centralized high-value scoring behavior.
- `com.comp2042.model.RainbowCoin` - Moved into the model layer and attached temporary speed-boost behavior.
- `com.comp2042.model.SilverCoin` - Moved into the model layer and attached obstacle-spawning behavior.
- `com.comp2042.model.Game` - Moved into the model layer and removed JavaFX coupling for testable scoring.
- `com.comp2042.controller.GameController` - Moved into MVC's controller layer and fixed input, timing, restart, and exit flows.
- `com.comp2042.controller.MainMenuController` - Moved into the controller layer and rewired MVC collaborators for startup.
- `com.comp2042.view.GameView` - Moved into MVC's view layer with improved scaling, drawing, and rotation caching.
- `com.comp2042.view.ViewManager` - Moved into the view layer and centralized stage, FXML, and asset setup.
- `com.comp2042.Main` - Updated startup wiring for the MVC view manager and packaged launcher flow.

## Unexpected Problems

- Maven Wrapper 404 link was replaced with a valid distribution URL.
- Fat JAR used `Launcher` to avoid missing JavaFX runtime components.
- Fullscreen Esc conflict was resolved with `KeyCombination.NO_MATCH`.
- Long-snake frame lag was fixed by caching rotated snapshots by angle.
