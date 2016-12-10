package com.waznop.moles;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by Waznop on 2016-12-08.
 */
public class MapObject {

    private TextureRegion image;

    public MapObject(TextureRegion image) {
        this.image = image;
    }

    public TextureRegion getImage() {
        return image;
    }
}
