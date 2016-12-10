package com.waznop.moles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by Waznop on 2016-12-08.
 */
public class Mole {

    private GameWorld world;
    private String id;
    private String name;
    private Vector2 actualPos;
    private Vector2 tilePos;
    private boolean isAlive;
    private boolean underground;
    private boolean player;
    private boolean facingLeft;
    private float respawnTimer;
    private double energy;
    private float animationTimer;
    private boolean transitioning;
    private int score;
    private int level;
    private Sound digSound;
    private Sound popSound;
    private Sound spawnSound;
    private Sound deathSound;

    public Mole(GameWorld world, String id, String name, int x, int y,
                boolean underground, boolean alive, boolean player, int score, int level) {
        this.world = world;
        this.id = id;
        this.name = name;
        this.score = score;
        this.level = level;
        tilePos = new Vector2(x, y);
        actualPos = new Vector2(tilePos);
        isAlive = alive;
        this.underground = underground;
        this.player = player;
        facingLeft = false;
        respawnTimer = 0;
        energy = Constants.ENERGY_MAX;
        animationTimer = 0;
        transitioning = false;
        digSound = AssetLoader.digSound;
        popSound = AssetLoader.popSound;
        spawnSound = AssetLoader.spawnSound;
        deathSound = AssetLoader.deathSound;
    }

    public void update(float delta) {
        if (player) {
            if (isAlive) {
                double energyMax = Constants.ENERGY_MAX;
                if (underground) {
                    energy -= Constants.ENERGY_LOSS * delta;
                    if (energy <= 0) {
                        forcePop();
                        energy = 0;
                    }
                } else if (energy < energyMax) {
                    energy += Constants.ENERGY_GAIN * delta;
                    if (energy > energyMax) {
                        energy = energyMax;
                    }
                }
            } else {
                if (respawnTimer > 0) {
                    respawnTimer -= delta;
                    if (respawnTimer <= 0) {
                        respawn();
                    }
                }
            }
        }

        double speed = Constants.MOLE_SPEED;

        if (actualPos.x < tilePos.x) {
            actualPos.x += speed * delta;
            facingLeft = false;
            if (actualPos.x > tilePos.x) actualPos.x = tilePos.x;
        } else if (actualPos.x > tilePos.x) {
            facingLeft = true;
            actualPos.x -= speed * delta;
            if (actualPos.x < tilePos.x) actualPos.x = tilePos.x;
        }

        if (actualPos.y < tilePos.y) {
            actualPos.y += speed * delta;
            if (actualPos.y > tilePos.y) actualPos.y = tilePos.y;
        } else if (actualPos.y > tilePos.y) {
            actualPos.y -= speed * delta;
            if (actualPos.y < tilePos.y) actualPos.y = tilePos.y;
        }
    }

    private void respawn() {
        world.getSocket().emit("playerRespawned");
    }

    public void respawnSuccess(int x, int y) {
        isAlive = true;
        tilePos.x = x;
        tilePos.y = y;
        actualPos.x = x;
        actualPos.y = y;
        underground = false;
        facingLeft = false;
        respawnTimer = 0;
        energy = Constants.ENERGY_MAX;
        animationTimer = 0;
        transitioning = false;
        spawnSound.play();
    }

    public void die() {
        respawnTimer = Constants.RESPAWN_TIMER;
        isAlive = false;
        transitioning = true;
        animationTimer = 0;
        deathSound.play();
    }

    private void forcePop() {
        pop();
        world.getSocket().emit("playerPopped");
    }

    public void pop() {
        underground = false;
        transitioning = true;
        animationTimer = 0;
        popSound.play();
    }

    public void dig() {
        underground = true;
        transitioning = true;
        animationTimer = 0;
        digSound.play();
    }

    public boolean checkDig() {
        if (energy < Constants.ENERGY_MIN) {
            return false;
        }
        dig();
        return true;
    }

    public void setTilePos(Vector2 pos) {
        tilePos.x = pos.x;
        tilePos.y = pos.y;
    }

    public Vector2 getTilePos() {
        return tilePos;
    }

    public Vector2 getActualPos() {
        return actualPos;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public boolean isUnderground() {
        return underground;
    }

    public boolean isPlayer() {
        return player;
    }

    public boolean isFacingLeft() {
        return facingLeft;
    }

    public float getAnimationTimer() {
        return animationTimer;
    }

    public void setAnimationTimer(float animationTimer) {
        this.animationTimer = animationTimer;
    }

    public String getId() {
        return id;
    }

    public boolean isTransitioning() {
        return transitioning;
    }

    public void setTransitioning(boolean transitioning) {
        this.transitioning = transitioning;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public int getLevel() {
        return level;
    }

    public double getEnergy() {
        return energy;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
