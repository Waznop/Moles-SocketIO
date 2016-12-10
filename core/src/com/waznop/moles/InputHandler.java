package com.waznop.moles;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;

/**
 * Created by Waznop on 2016-12-08.
 */
public class InputHandler implements InputProcessor {

    private GameScreen gameScreen;
    private GameWorld world;

    public InputHandler(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
        world = gameScreen.getWorld();
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.UP) {
            return world.movePlayer(Direction.UP);
        } else if (keycode == Input.Keys.DOWN) {
            return world.movePlayer(Direction.DOWN);
        } else if (keycode == Input.Keys.LEFT) {
            return world.movePlayer(Direction.LEFT);
        } else if (keycode == Input.Keys.RIGHT) {
            return world.movePlayer(Direction.RIGHT);
        } else if (keycode == Input.Keys.SPACE) {
            return world.digOrPop();
        } else if (keycode == Input.Keys.Q) {
            gameScreen.setGameFinished(true);
        } else {
            return false;
        }
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
