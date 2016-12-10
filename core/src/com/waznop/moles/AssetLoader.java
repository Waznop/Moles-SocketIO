package com.waznop.moles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * Created by Waznop on 2016-12-07.
 */
public class AssetLoader {

    public static Texture spriteSheet;

    public static Animation idle1;
    public static Animation idle2;
    public static Animation dig1;
    public static Animation dig2;
    public static Animation pop1;
    public static Animation pop2;
    public static Animation die1;
    public static Animation die2;
    public static TextureRegion rock1;
    public static TextureRegion rock2;
    public static TextureRegion rock3;
    public static TextureRegion ground1;
    public static TextureRegion ground2;
    public static TextureRegion ground3;
    public static BitmapFont font;
    public static Preferences data;
    public static Sound popSound;
    public static Sound digSound;
    public static Sound victorySound;
    public static Sound spawnSound;
    public static Sound deathSound;
    public static Music gameMusic;

    public static Skin uiSkin;
    public static TextureAtlas uiAtlas;

    public static void load() {
        spriteSheet = new Texture(Gdx.files.internal("spritesheet.png"));
        uiAtlas = new TextureAtlas("uiskin.atlas");
        uiSkin = new Skin(Gdx.files.internal("uiskin.json"), uiAtlas);
        float animationSpeed = Constants.ANIMATION_SPEED;

        data = Gdx.app.getPreferences("gameData");
        if (! data.contains("victories")) {
            data.putInteger("victories", 0);
        }
        if (! data.contains("firstTime")) {
            data.putBoolean("firstTime", true);
        }
        data.flush();

        popSound = Gdx.audio.newSound(Gdx.files.internal("s_pop.ogg"));
        digSound = Gdx.audio.newSound(Gdx.files.internal("s_dig.ogg"));
        victorySound = Gdx.audio.newSound(Gdx.files.internal("s_victory.ogg"));
        spawnSound = Gdx.audio.newSound(Gdx.files.internal("s_spawn.ogg"));
        deathSound = Gdx.audio.newSound(Gdx.files.internal("s_death.ogg"));
        gameMusic = Gdx.audio.newMusic(Gdx.files.internal("bg_roam.mp3"));
        gameMusic.setLooping(true);
        gameMusic.play();

        rock1 = new TextureRegion(spriteSheet, 64, 96, 32, 32);
        rock2 = new TextureRegion(spriteSheet, 96, 96, 32, 32);
        rock3 = new TextureRegion(spriteSheet, 128, 96, 32, 32);
        ground1 = new TextureRegion(spriteSheet, 160, 96, 32, 32);
        ground2 = new TextureRegion(spriteSheet, 192, 96, 32, 32);
        ground3 = new TextureRegion(spriteSheet, 224, 96, 32, 32);
        rock1.flip(false, true);
        rock2.flip(false, true);
        rock3.flip(false, true);
        ground1.flip(false, true);
        ground2.flip(false, true);
        ground3.flip(false, true);

        TextureRegion idle11 = new TextureRegion(spriteSheet, 128, 64, 32, 32);
        TextureRegion idle12 = new TextureRegion(spriteSheet, 160, 64, 32, 32);
        TextureRegion idle13 = new TextureRegion(spriteSheet, 192, 64, 32, 32);
        TextureRegion idle14 = new TextureRegion(spriteSheet, 224, 64, 32, 32);
        TextureRegion idle15 = new TextureRegion(spriteSheet, 0, 96, 32, 32);
        TextureRegion idle16 = new TextureRegion(spriteSheet, 32, 96, 32, 32);
        idle11.flip(false, true);
        idle12.flip(false, true);
        idle13.flip(false, true);
        idle14.flip(false, true);
        idle15.flip(false, true);
        idle16.flip(false, true);
        idle1 = new Animation(animationSpeed, idle11, idle12, idle13, idle14, idle15, idle16);
        idle1.setPlayMode(Animation.PlayMode.LOOP);

        TextureRegion idle21 = new TextureRegion(spriteSheet, 192, 32, 32, 32);
        TextureRegion idle22 = new TextureRegion(spriteSheet, 224, 32, 32, 32);
        TextureRegion idle23 = new TextureRegion(spriteSheet, 0, 64, 32, 32);
        TextureRegion idle24 = new TextureRegion(spriteSheet, 32, 64, 32, 32);
        TextureRegion idle25 = new TextureRegion(spriteSheet, 64, 64, 32, 32);
        TextureRegion idle26 = new TextureRegion(spriteSheet, 96, 64, 32, 32);
        idle21.flip(false, true);
        idle22.flip(false, true);
        idle23.flip(false, true);
        idle24.flip(false, true);
        idle25.flip(false, true);
        idle26.flip(false, true);
        idle2 = new Animation(animationSpeed, idle21, idle22, idle23, idle24, idle25, idle26);
        idle2.setPlayMode(Animation.PlayMode.LOOP);

        TextureRegion dig11 = new TextureRegion(spriteSheet, 96, 32, 32, 32);
        TextureRegion dig12 = new TextureRegion(spriteSheet, 128, 32, 32, 32);
        TextureRegion dig13 = new TextureRegion(spriteSheet, 160, 32, 32, 32);
        TextureRegion dig21 = new TextureRegion(spriteSheet, 0, 32, 32, 32);
        TextureRegion dig22 = new TextureRegion(spriteSheet, 32, 32, 32, 32);
        TextureRegion dig23 = new TextureRegion(spriteSheet, 64, 32, 32, 32);
        TextureRegion invis = new TextureRegion(spriteSheet, 0, 128, 32, 32);
        dig11.flip(false, true);
        dig12.flip(false, true);
        dig13.flip(false, true);
        dig21.flip(false, true);
        dig22.flip(false, true);
        dig23.flip(false, true);
        dig1 = new Animation(animationSpeed/2, dig11, dig12, dig13, invis);
        dig2 = new Animation(animationSpeed/2, dig21, dig22, dig23, invis);
        pop1 = new Animation(animationSpeed/2, invis, dig13, dig12, dig11);
        pop2 = new Animation(animationSpeed/2, invis, dig23, dig22, dig21);

        TextureRegion die11 = new TextureRegion(spriteSheet, 128, 0, 32, 32);
        TextureRegion die12 = new TextureRegion(spriteSheet, 160, 0, 32, 32);
        TextureRegion die13 = new TextureRegion(spriteSheet, 192, 0, 32, 32);
        TextureRegion die14 = new TextureRegion(spriteSheet, 224, 0, 32, 32);
        TextureRegion die21 = new TextureRegion(spriteSheet, 0, 0, 32, 32);
        TextureRegion die22 = new TextureRegion(spriteSheet, 32, 0, 32, 32);
        TextureRegion die23 = new TextureRegion(spriteSheet, 64, 0, 32, 32);
        TextureRegion die24 = new TextureRegion(spriteSheet, 96, 0, 32, 32);
        die11.flip(false, true);
        die12.flip(false, true);
        die13.flip(false, true);
        die14.flip(false, true);
        die21.flip(false, true);
        die22.flip(false, true);
        die23.flip(false, true);
        die24.flip(false, true);
        die1 = new Animation(animationSpeed/2, die11, die12, die13, die14);
        die2 = new Animation(animationSpeed/2, die21, die22, die23, die24);

        font = new BitmapFont(Gdx.files.internal("font.fnt"));
        font.getData().setScale(0.5f, -0.5f);
    }

    public static void dispose() {
        spriteSheet.dispose();
        font.dispose();
        uiSkin.dispose();
        uiAtlas.dispose();
        popSound.dispose();
        digSound.dispose();
        victorySound.dispose();
        spawnSound.dispose();
        deathSound.dispose();
        gameMusic.dispose();
    }

}
