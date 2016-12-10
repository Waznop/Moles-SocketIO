package com.waznop.moles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

/**
 * Created by Waznop on 2016-12-08.
 */
public class Room {

    private int roomId;
    private String owner;
    private int maxPlayers;
    private int curPlayers;

    public Room(int roomId, String owner, int curPlayers, int maxPlayers) {
        this.roomId = roomId;
        this.owner = owner;
        this.curPlayers = curPlayers;
        this.maxPlayers = maxPlayers;
    }

    public int getRoomId() {
        return roomId;
    }

    public String getOwner() {
        return owner;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public int getCurPlayers() {
        return curPlayers;
    }

    public void setCurPlayers(int curPlayers) {
        this.curPlayers = curPlayers;
    }

    @Override
    public String toString() {
        return owner + "'s room - " + curPlayers + "/" + maxPlayers;
    }
}
