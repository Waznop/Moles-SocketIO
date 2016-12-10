package com.waznop.moles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Waznop on 2016-12-08.
 */
public class GameRenderer {

    private GameWorld world;
    private OrthographicCamera cam;
    private SpriteBatch batcher;
    private Animation idle1;
    private Animation idle2;
    private Animation dig1;
    private Animation dig2;
    private Animation pop1;
    private Animation pop2;
    private Animation die1;
    private Animation die2;
    private BitmapFont font;

    private HashMap<Vector2, MapObject> borders;
    private HashMap<Vector2, MapObject> rocks;
    private HashMap<Vector2, MapObject> ground;

    public GameRenderer(GameWorld world, OrthographicCamera cam) {
        this.world = world;
        this.cam = cam;
        batcher = new SpriteBatch();
        batcher.setProjectionMatrix(cam.combined);
        initAssets();
        initObjects();
    }

    private void initAssets() {
        idle1 = AssetLoader.idle1;
        idle2 = AssetLoader.idle2;
        dig1 = AssetLoader.dig1;
        dig2 = AssetLoader.dig2;
        pop1 = AssetLoader.pop1;
        pop2 = AssetLoader.pop2;
        die1 = AssetLoader.die1;
        die2 = AssetLoader.die2;
        font = AssetLoader.font;
    }

    private void initObjects() {
        borders = world.getBorders();
        rocks = world.getRocks();
        ground = world.getGround();
    }

    private void drawMole(Mole mole, float delta, float runTime) {
        float animationTimer = mole.getAnimationTimer();
        boolean transitioning = mole.isTransitioning();
        boolean underground = mole.isUnderground();
        boolean isPlayer = mole.isPlayer();
        boolean isAlive = mole.isAlive();
        TextureRegion image;

        if (transitioning) {
            Animation animation = isAlive ? (underground ? (isPlayer ? dig1 : dig2) : (isPlayer ? pop1 : pop2)) :
                    (isPlayer ? die1 : die2);
            image = (TextureRegion)animation.getKeyFrame(animationTimer);
            animationTimer += delta;

            if (isAlive && animationTimer > animation.getAnimationDuration()) {
                mole.setTransitioning(false);
                mole.setAnimationTimer(0);
            } else {
                mole.setAnimationTimer(animationTimer);
            }

        } else if (underground) {
            return;
        } else {
            image = (TextureRegion)(isPlayer ? idle1.getKeyFrame(runTime) : idle2.getKeyFrame(runTime));
        }

        boolean facingLeft = mole.isFacingLeft();
        Vector2 pos = mole.getActualPos();
        int width = image.getRegionWidth();
        int height = image.getRegionHeight();
        float x = pos.x * width;
        float y = pos.y * height;
        batcher.draw(image, facingLeft ? x+width : x, y, facingLeft ? -width : width, height);

        if (! transitioning) {
            font.draw(batcher, "[" + mole.getLevel() + "] " + mole.getName(), x + width / 2, y + height, 0, 1, false);
        }
    }

    private void drawMapObject(HashMap.Entry<Vector2, MapObject> entry) {
        Vector2 pos = entry.getKey();
        TextureRegion image = entry.getValue().getImage();
        int width = image.getRegionWidth();
        int height = image.getRegionHeight();
        batcher.draw(image, pos.x*width, pos.y*height, width, height);
    }

    private void drawMapObjects() {
        batcher.disableBlending();
        for (HashMap.Entry<Vector2, MapObject> entry: ground.entrySet()) {
            drawMapObject(entry);
        }
        for (HashMap.Entry<Vector2, MapObject> entry: borders.entrySet()) {
            drawMapObject(entry);
        }
        for (HashMap.Entry<Vector2, MapObject> entry: rocks.entrySet()) {
            drawMapObject(entry);
        }
        batcher.enableBlending();
    }

    public void render(float delta, float runTime) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batcher.begin();
        drawMapObjects();

        HashMap<String, Mole> others = world.getOthers();
        Mole player = world.getPlayer();

        if (player != null) {
            for (HashMap.Entry<String, Mole> entry: others.entrySet()) {
                drawMole(entry.getValue(), delta, runTime);
            }
            drawMole(player, delta, runTime);
        }

        font.draw(batcher, "Scores (10 to win)", 10, 10);
        if (player != null) {
            font.draw(batcher, "[" + player.getLevel() + "] " + player.getName() + ": " + player.getScore(), 10, 20);
            int yOffset = 30;
            for (HashMap.Entry<String, Mole> entry: others.entrySet()) {
                Mole other = entry.getValue();
                font.draw(batcher,
                        "[" + other.getLevel() + "] " + other.getName() + ": " + other.getScore(), 10, yOffset);
                yOffset += 10;
            }
            font.draw(batcher, "Energy: " + (int)player.getEnergy(), Constants.SCREEN_WIDTH - 10, 10, 0, 4, false);
        }

        batcher.end();
    }

}
