package com.waznop.moles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by Waznop on 2016-12-08.
 */
public class MenuScreen implements Screen {

    private MolesGame game;
    private Stage stage;
    private Table table;
    private Table buttonsTable;
    private List list;
    private ScrollPane scrollPane;
    private Array<Room> rooms;
    private TextButton create;
    private TextButton join;
    private Skin skin;
    private Label label;
    private Label nameLabel;
    private String name;
    private NetworkManager networkManager;
    private Room roomToGo;
    private int victories;
    private Preferences data;

    public MenuScreen(MolesGame game) {
        this.game = game;
        rooms = new Array<Room>();
        networkManager = new NetworkManager();
        data = AssetLoader.data;
    }

    @Override
    public void show() {
        stage = new Stage();
        Gdx.input.setInputProcessor(stage);
        skin = AssetLoader.uiSkin;

        rooms.clear();
        roomToGo = null;
        victories = data.getInteger("victories");

        list = new List(skin, "dimmed");
        list.setItems(rooms.size > 0 ? rooms : "No open rooms currently.");

        label = new Label("MOLES", skin, "title");
        label.setFontScale(2);
        nameLabel = new Label(name == null ? "[?] Anonymous" : "[" + getLevel() + "] " + name, skin);
        scrollPane = new ScrollPane(list, skin, "android");
        create = new TextButton("CREATE", skin, "round");
        create.pad(10);
        join = new TextButton("JOIN", skin, "round");
        join.pad(10);
        join.setDisabled(true);
        buttonsTable = new Table(skin);
        buttonsTable.add(join).padRight(10);
        buttonsTable.add(create);

        create.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.input.getTextInput(new Input.TextInputListener() {
                    @Override
                    public void input(String text) {
                        try {
                            int numPlayers;
                            if (text.trim().length() == 0) {
                                numPlayers = 2;
                            } else {
                                numPlayers = Integer.parseInt(text);
                                if (numPlayers > 8) {
                                    numPlayers = 8;
                                } else if (numPlayers < 2) {
                                    numPlayers = 2;
                                }
                            }
                            createRoom(numPlayers);
                        } catch (NumberFormatException e) {
                            Gdx.app.log("LibGDX", "Did not input a number");
                        }
                    }

                    @Override
                    public void canceled() {
                        Gdx.app.log("LibGDX", "Room creation cancelled");
                    }
                }, "Enter the maximum number of players", "", "2-8");
            }
        });

        join.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Room room = (Room)list.getSelected();
                if (room.getCurPlayers() == room.getMaxPlayers()) {
                    Gdx.app.log("LibGDX", "Room is full");
                    return;
                }
                joinRoom(room.getRoomId());
            }
        });

        table = new Table(skin);
        table.setFillParent(true);
        table.pad(40);
        table.add(label);
        table.row();
        table.add(nameLabel);
        table.row().padTop(20);
        table.add(scrollPane).expand();
        table.row().padTop(20);
        table.add(buttonsTable);

        stage.addActor(table);

        if (name == null) {
            Gdx.input.getTextInput(new Input.TextInputListener() {
                @Override
                public void input(String text) {
                    if (text.trim().length() > 0) {
                        name = text;
                    } else {
                        name = "Anonymous";
                    }
                    setup();
                }

                @Override
                public void canceled() {
                    name = "Anonymous";
                    setup();
                }
            }, "Enter your name:", "", "Name");
        } else {
            JSONObject data = new JSONObject();
            try {
                data.put("name", name);
                data.put("level", getLevel());
                networkManager.getSocket().emit("playerRegistered", data);
            } catch (JSONException e) {
                Gdx.app.log("SocketIO", "Error sending registration info");
            }
        }
    }

    private void setup() {
        table.getCell(nameLabel).getActor().setText("[" + getLevel() + "] " + name);
        configSocketEvents();
        networkManager.connectSocket();
        if (data.getBoolean("firstTime")) {
            Dialog dialog = new Dialog("Welcome to Moles!", skin);
            dialog.text("\nChoose a room and battle it out with your friends!\n\n" +
                    "- Arrow keys to move, space bar to dig/pop.\n" +
                    "- You can only be underground if you have enough energy.\n" +
                    "- You get a point if you pop up underneath someone else.\n" +
                    "- You lose a point if you get popped or if you pop up underneath a rock.\n" +
                    "- First one to 10 points wins!\n\n" +
                    "Good luck and have fun :)\n\n" +
                    "- Waznop");
            dialog.button("OK", true);
            dialog.key(Input.Keys.ENTER, true);
            dialog.show(stage);
            data.putBoolean("firstTime", false);
            data.flush();
        }
    }

    private void configSocketEvents() {
        final Socket socket = networkManager.getSocket();
        socket.once(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Gdx.app.log("SocketIO", "Connected to server");
                JSONObject data = new JSONObject();
                try {
                    data.put("name", name);
                    data.put("level", getLevel());
                    socket.emit("playerRegistered", data);
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error sending registration info");
                }
            }
        }).once(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Gdx.app.log("SocketIO", "Server is offline");
                create.setDisabled(true);
                Dialog dialog = new Dialog("Error", AssetLoader.uiSkin);
                dialog.text("Server is offline.");
                dialog.button("OK", true);
                dialog.key(Input.Keys.ENTER, true);
                dialog.show(stage);
            }
        }).on("roomCreationFail", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Gdx.app.log("SocketIO", "Failed to create room");
                create.setDisabled(true);
            }
        }).on("roomCreationSuccess", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {
                    int roomId = data.getInt("id");
                    int maxPlayers = data.getInt("maxPlayers");
                    goToRoom(new Room(roomId, name, 1, maxPlayers));
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error getting room ID");
                }
            }
        }).on("newRoom", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {
                    int roomId = data.getInt("id");
                    String owner = data.getString("owner");
                    int maxPlayers = data.getInt("maxPlayers");
                    rooms.add(new Room(roomId, owner, 1, maxPlayers));
                    list.setItems(rooms);
                    if (join.isDisabled()) {
                        join.setDisabled(false);
                    }
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error getting new room");
                }
            }
        }).on("getRooms", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                Iterator<String> iter = data.keys();
                while (iter.hasNext()) {
                    String key = iter.next();
                    try {
                        JSONObject room = (JSONObject) data.get(key);
                        boolean open = room.getBoolean("open");
                        if (! open) {
                            continue;
                        }
                        int roomId = room.getInt("id");
                        String owner = room.getString("owner");
                        int curPlayers = room.getInt("curPlayers");
                        int maxPlayers = room.getInt("maxPlayers");
                        rooms.add(new Room(roomId, owner, curPlayers, maxPlayers));
                    } catch (JSONException e) {
                        Gdx.app.log("SocketIO", "Error getting existing room");
                    }
                }
                if (rooms.size > 0) {
                    list.setItems(rooms);
                    join.setDisabled(false);
                }
            }
        }).on("roomHidden", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                removeRoom(data);
            }
        }).on("roomDestroyed", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                removeRoom(data);
            }
        }).on("joinRoomFail", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Gdx.app.log("SocketIO", "Failed to join room");
            }
        }).on("joinRoomSuccess", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {
                    int roomId = data.getInt("id");
                    for (Room room : rooms) {
                        if (room.getRoomId() == roomId) {
                            goToRoom(room);
                            break;
                        }
                    }
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error getting room to join");
                }
            }
        }).on("roomJoined", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {
                    int roomId = data.getInt("id");
                    for (Room room : rooms) {
                        if (room.getRoomId() == roomId) {
                            room.setCurPlayers(room.getCurPlayers() + 1);
                            break;
                        }
                    }
                    list.setItems(rooms);
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error getting room with new player");
                }
            }
        }).on("roomQuit", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {
                    int roomId = data.getInt("id");
                    for (Room room : rooms) {
                        if (room.getRoomId() == roomId) {
                            room.setCurPlayers(room.getCurPlayers() - 1);
                            break;
                        }
                    }
                    list.setItems(rooms);
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error getting room with new player");
                }
            }
        });
    }

    private void removeRoom(JSONObject data) {
        try {
            int roomId = data.getInt("id");
            for (int i = 0; i < rooms.size; ++i) {
                Room room = rooms.get(i);
                if (room.getRoomId() == roomId) {
                    rooms.removeIndex(i);
                    break;
                }
            }
            if (create.isDisabled()) {
                create.setDisabled(false);
            }
            if (rooms.size > 0) {
                list.setItems(rooms);
            } else {
                list.setItems("No open rooms currently.");
                join.setDisabled(true);
            }
        } catch (JSONException e) {
            Gdx.app.log("SocketIO", "Error getting removed room");
        }
    }

    private int getLevel() {
        return 1 + victories / 10;
    }

    private void goToRoom(Room room) {
        roomToGo = room;
    }

    private void joinRoom(int roomId) {
        networkManager.getSocket().emit("requestJoinRoom", roomId);
    }

    private void createRoom(int maxPlayers) {
        JSONObject data = new JSONObject();
        try {
            data.put("owner", this.name);
            data.put("maxPlayers", maxPlayers);
            networkManager.getSocket().emit("roomCreated", data);
        } catch (JSONException e) {
            Gdx.app.log("SocketIO", "Error creating room");
        }
    }

    @Override
    public void render(float delta) {
        if (delta > .15f) {
            delta = .15f;
        }
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();

        if (roomToGo != null) {
            game.setScreen(new GameScreen(game, this, networkManager, roomToGo, name));
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
}
