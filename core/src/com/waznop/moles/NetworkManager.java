package com.waznop.moles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Waznop on 2016-12-08.
 */
public class NetworkManager {

    private Socket socket;

    public NetworkManager() {
        try {
            socket = IO.socket(Constants.SERVER);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void connectSocket() {
        socket.connect();
    }

    public Socket getSocket() {
        return socket;
    }
}
