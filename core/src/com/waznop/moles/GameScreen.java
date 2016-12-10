package com.waznop.moles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;

/**
 * Created by Waznop on 2016-12-07.
 */
public class GameScreen implements Screen {

    private MolesGame game;
    private MenuScreen menuScreen;
    private NetworkManager networkManager;
    private GameWorld world;
    private GameRenderer renderer;
    private OrthographicCamera cam;
    private float runTime;
    private boolean gameFinished;
    private Stage stage;

    public GameScreen(MolesGame game, MenuScreen menuScreen, NetworkManager networkManager, Room room, String name) {
        this.game = game;
        this.menuScreen = menuScreen;
        this.networkManager = networkManager;
        stage = new Stage();
        cam = new OrthographicCamera();
        cam.setToOrtho(true, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        world = new GameWorld(networkManager, this, stage, room, name);
        renderer = new GameRenderer(world, cam);
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(stage);
        inputMultiplexer.addProcessor(new InputHandler(this));
        Gdx.input.setInputProcessor(inputMultiplexer);
        runTime = 0;
        gameFinished = false;
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        if (delta > .15f) {
            delta = .15f;
        }
        runTime += delta;
        world.update(delta);
        renderer.render(delta, runTime);
        stage.act(delta);
        stage.draw();
        if (gameFinished) {
            networkManager.getSocket().emit("quitRoom");
            game.setScreen(menuScreen);
        }
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }

    public GameWorld getWorld() {
        return world;
    }

    public void setGameFinished(boolean gameFinished) {
        this.gameFinished = gameFinished;
    }
}
