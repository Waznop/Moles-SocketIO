package com.waznop.moles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Random;

/**
 * Created by Waznop on 2016-12-08.
 */
public class GameWorld {

    private NetworkManager networkManager;
    private Room room;
    private Random rng;
    private Mole player;
    private HashMap<String, Mole> others;
    private HashMap<Vector2, MapObject> rocks;
    private HashMap<Vector2, MapObject> borders;
    private HashMap<Vector2, MapObject> ground;
    private GameScreen gameScreen;
    private Stage stage;
    private String name;

    public GameWorld(NetworkManager networkManager, GameScreen gameScreen, Stage stage, Room room, String name) {
        this.networkManager = networkManager;
        this.gameScreen = gameScreen;
        this.stage = stage;
        this.room = room;
        this.name = name;
        rng = new Random();
        others = new HashMap<String, Mole>();
        rocks = new HashMap<Vector2, MapObject>();
        borders = new HashMap<Vector2, MapObject>();
        ground = new HashMap<Vector2, MapObject>();
        initMap();
        configSocketEvents();
        JSONObject data = new JSONObject();
        try {
            data.put("roomId", room.getRoomId());
            data.put("name", name);
            networkManager.getSocket().emit("requestPlayer", data);
        } catch (JSONException e) {
            Gdx.app.log("SocketIO", "Error requesting player");
        }
    }

    private void initMap() {
        int cols = Constants.COLS;
        int rows = Constants.ROWS;
        TextureRegion rock1 = AssetLoader.rock1;
        TextureRegion rock2 = AssetLoader.rock2;
        TextureRegion rock3 = AssetLoader.rock3;
        TextureRegion ground1 = AssetLoader.ground1;
        TextureRegion ground2 = AssetLoader.ground2;
        TextureRegion ground3 = AssetLoader.ground3;
        for (int i = 0; i < cols; ++i) {
            for (int j = 0; j < rows; ++j) {
                TextureRegion image;
                if (i == 0 || i == cols-1 || j == 0 || j == rows-1) {
                    switch (rng.nextInt(3)) {
                        case 0: image = rock1;
                            break;
                        case 1: image = rock2;
                            break;
                        default: image = rock3;
                    }
                    borders.put(new Vector2(i, j), new MapObject(image));
                } else {
                    switch (rng.nextInt(3)) {
                        case 0: image = ground1;
                            break;
                        case 1: image = ground2;
                            break;
                        default: image = ground3;
                    }
                    ground.put(new Vector2(i, j), new MapObject(image));
                }
            }
        }
    }

    public void configSocketEvents() {
        final Socket socket = networkManager.getSocket();
        socket.once("playerCreated", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject)args[0];
                try {
                    String id = data.getString("id");
                    int x = data.getInt("x");
                    int y = data.getInt("y");
                    int level = data.getInt("level");
                    player = createMole(id, name, x, y, false, true, true, 0, level);
                    // Gdx.app.log("SocketIO", "Player created: " + id);
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error creating player");
                }
            }
        }).on("playerConnected", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject)args[0];
                try {
                    String id = data.getString("id");
                    String name = data.getString("name");
                    int x = data.getInt("x");
                    int y = data.getInt("y");
                    int level = data.getInt("level");
                    others.put(id, createMole(id, name, x, y, false, true, false, 0, level));
                    AssetLoader.spawnSound.play();
                    // Gdx.app.log("SocketIO", "Player connected: " + id);
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error connecting player");
                }
            }
        }).on("playerDisconnected", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject)args[0];
                try {
                    String id = data.getString("id");
                    others.remove(id);
                    // Gdx.app.log("SocketIO", "Player disconnected: " + id);
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error disconnecting player");
                }
            }
        }).on("playerMoved", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject)args[0];
                try {
                    String id = data.getString("id");
                    int x = data.getInt("x");
                    int y = data.getInt("y");
                    Mole other = others.get(id);
                    if (other != null) {
                        other.setTilePos(new Vector2(x, y));
                    }
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error moving player");
                }
            }
        }).on("playerPop", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject)args[0];
                try {
                    String id = data.getString("id");
                    Mole other = others.get(id);
                    if (other != null) {
                        other.pop();
                    }
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error popping player");
                }
            }
        }).on("playerDig", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject)args[0];
                try {
                    String id = data.getString("id");
                    Mole other = others.get(id);
                    if (other != null) {
                        other.dig();
                    }
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error digging player");
                }
            }
        }).on("playerRespawn", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject)args[0];
                try {
                    String id = data.getString("id");
                    int x = data.getInt("x");
                    int y = data.getInt("y");
                    if (id.equals(player.getId())) {
                        player.respawnSuccess(x, y);
                    } else {
                        Mole other = others.get(id);
                        if (other != null) {
                            other.respawnSuccess(x, y);
                        }
                    }
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error respawning player");
                }
            }
        }).on("playerDie", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject)args[0];
                try {
                    String id = data.getString("id");
                    String killer = data.getString("killer");

                    if (killer.equals(player.getId())) {
                        player.setScore(player.getScore() + 1);
                    } else {
                        Mole other = others.get(killer);
                        if (other != null) {
                            other.setScore(other.getScore() + 1);
                        }
                    }

                    if (id.equals(player.getId())) {
                        player.die();
                        player.setScore(player.getScore() - 1);
                    } else {
                        Mole other = others.get(id);
                        if (other != null) {
                            other.die();
                            other.setScore(other.getScore() - 1);
                        }
                    }
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error respawning player");
                }
            }
        }).once("playerWin", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject)args[0];
                try {
                    String id = data.getString("id");
                    String victoryMsg;
                    if (id.equals(player.getId())) {
                        int victories = AssetLoader.data.getInteger("victories");
                        AssetLoader.data.putInteger("victories", victories + 1);
                        AssetLoader.data.flush();
                        victoryMsg = "You win!";
                    } else {
                        Mole other = others.get(id);
                        if (other != null) {
                            victoryMsg = other.getName() + " wins!";
                        } else {
                            victoryMsg = "A bug wins!";
                        }
                    }
                    AssetLoader.victorySound.play();
                    Dialog dialog = new Dialog("Game Finished", AssetLoader.uiSkin) {
                        public void result(Object obj) {
                            gameScreen.setGameFinished(true);
                        }
                    };
                    dialog.text(victoryMsg);
                    dialog.button("OK", true);
                    dialog.key(Input.Keys.ENTER, true);
                    dialog.show(stage);
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error getting winning player");
                }
            }
        }).once("getPlayers", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONArray objects = (JSONArray) args[0];
                try {
                    for (int i = 0; i < objects.length(); ++i) {
                        JSONObject data = objects.getJSONObject(i);
                        String id = data.getString("id");
                        String name = data.getString("name");
                        int x = data.getInt("x");
                        int y = data.getInt("y");
                        int level = data.getInt("level");
                        int score = data.getInt("score");
                        boolean underground = data.getBoolean("underground");
                        boolean alive = data.getBoolean("alive");
                        others.put(id, createMole(id, name, x, y, underground, alive, false, score, level));
                    }
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error getting players");
                }
            }
        }).once("getRocks", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONArray objects = (JSONArray) args[0];
                try {
                    TextureRegion rock1 = AssetLoader.rock1;
                    TextureRegion rock2 = AssetLoader.rock2;
                    TextureRegion rock3 = AssetLoader.rock3;
                    TextureRegion image;
                    for (int i = 0; i < objects.length(); ++i) {
                        int idx = objects.getInt(i);
                        int x = (idx / 8) + 1;
                        int y = (idx % 8) + 1;
                        switch (rng.nextInt(3)) {
                            case 0:
                                image = rock1;
                                break;
                            case 1:
                                image = rock2;
                                break;
                            default:
                                image = rock3;
                        }
                        rocks.put(new Vector2(x, y), new MapObject(image));
                    }
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error getting rocks");
                }
            }
        });
    }

    public void update(float delta) {
        if (player == null) {
            return;
        }

        player.update(delta);
        for (HashMap.Entry<String, Mole> entry: others.entrySet()) {
            entry.getValue().update(delta);
        }
    }

    private boolean isFreeUnderground(Vector2 pos) {
        return ! (borders.containsKey(pos));
    }

    private boolean isFree(Vector2 pos) {
        return ! (borders.containsKey(pos) || rocks.containsKey(pos));
    }

    private Mole createMole(String id, String name, int x, int y,
                            boolean underground, boolean alive, boolean player, int score, int level) {
        return new Mole(this, id, name, x, y, underground, alive, player, score, level);
    }

    public boolean digOrPop() {
        if (player == null || ! player.isAlive()) {
            return false;
        }

        if (player.isUnderground()) {
            networkManager.getSocket().emit("playerPopped");
            player.pop();
            return true;
        } else if (player.checkDig()) {
            networkManager.getSocket().emit("playerDigged");
            return true;
        }

        return false;
    }

    public boolean movePlayer(Direction dir) {
        if (player == null || ! player.isAlive()) {
            return false;
        }

        Vector2 newPos = new Vector2(player.getTilePos());
        switch(dir) {
            case UP: newPos.y -= 1;
                break;
            case DOWN: newPos.y += 1;
                break;
            case LEFT: newPos.x -= 1;
                break;
            case RIGHT: newPos.x += 1;
        }
        if (player.isUnderground() ? isFreeUnderground(newPos) : isFree(newPos)) {
            JSONObject data = new JSONObject();
            try {
                data.put("x", newPos.x);
                data.put("y", newPos.y);
                networkManager.getSocket().emit("playerMoved", data);
            } catch (JSONException e) {
                Gdx.app.log("SocketIO", "Error sending move data");
                return false;
            }
            player.setTilePos(newPos);
            return true;
        }
        return false;
    }

    public Socket getSocket() {
        return networkManager.getSocket();
    }

    public Mole getPlayer() {
        return player;
    }

    public HashMap<String, Mole> getOthers() {
        return others;
    }

    public HashMap<Vector2, MapObject> getBorders() {
        return borders;
    }

    public HashMap<Vector2, MapObject> getRocks() {
        return rocks;
    }

    public HashMap<Vector2, MapObject> getGround() {
        return ground;
    }
}
