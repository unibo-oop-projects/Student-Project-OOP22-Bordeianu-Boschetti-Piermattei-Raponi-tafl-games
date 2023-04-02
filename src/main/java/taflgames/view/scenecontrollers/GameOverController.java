package taflgames.view.scenecontrollers;

/**
 * This interface extends a generic {@link BasicSceneController}
 * to add specific functions for a {@link taflgames.view.scenes.GameOverScene}.
 */
public interface GameOverController extends BasicSceneController {
    /**
     * Sets the scene to a {@link taflgames.view.scenes.UserRegistrationScene}.
     */
    void goToRegistrationScene();

}
