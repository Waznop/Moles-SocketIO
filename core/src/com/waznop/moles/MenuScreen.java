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
    private Table roomsTable;
    private Table buttonsTable;
    private List list;
    private ScrollPane scrollPane;
    private Array<Room> rooms;
    private TextButton create;
    private TextButton join;
    private TextButton mute;
    private Skin skin;
    private Label label;
    private Label nameLabel;
    private String name;
    private NetworkManager networkManager;
    private Room roomToGo;
    private int victories;
    private Preferences data;
    private TextField chatField;
    private ScrollPane messagesPane;
    private List messagesList;
    private Array<Message> messages;
    private Table chatTable;
    private Table table;
    private Label welcomeLabel;
    private int numPlayers;

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

        list = new List(skin, "messages");
        list.setItems(rooms.size > 0 ? rooms : "No open rooms currently.");

        label = new Label("MOLES", skin, "title");
        label.setFontScale(2);
        nameLabel = new Label(name == null ? "Not connected" : "[" + getLevel() + "] " + name, skin);
        scrollPane = new ScrollPane(list, skin, "android");
        scrollPane.setFadeScrollBars(false);
        create = new TextButton("CREATE", skin, "round");
        create.pad(10);
        join = new TextButton("JOIN", skin, "round");
        join.pad(10);
        join.setDisabled(true);
        mute = new TextButton("MUTE", skin, "radio");
        if (AssetLoader.muted) mute.toggle();
        buttonsTable = new Table(skin);
        buttonsTable.add(join).padRight(10);
        buttonsTable.add(create).padRight(10);
        buttonsTable.add(mute);

        create.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                TextField textField = new TextField("", skin);

                Dialog dialog = new Dialog("Creating room", skin) {
                    protected void result(Object object) {
                        TextField textField = (TextField) object;
                        String text = textField.getText();
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
                            Gdx.app.log("LibGDX", "Did not input a number - Default to 2");
                            createRoom(2);
                        }
                    }
                };
                dialog.text("Please enter the maximum room size (2-8).");
                dialog.row();
                dialog.add(textField);
                dialog.key(Input.Keys.ENTER, textField);
                dialog.show(stage);
                stage.setKeyboardFocus(textField);
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

        mute.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                AssetLoader.muted = ! AssetLoader.muted;
                if (AssetLoader.muted) {
                    AssetLoader.gameMusic.setVolume(0);
                } else {
                    AssetLoader.gameMusic.setVolume(1);
                }
            }
        });

        chatField = new TextField("", skin);
        chatField.setTextFieldListener(new TextField.TextFieldListener() {
            @Override
            public void keyTyped(TextField textField, char c) {
                String text = textField.getText();
                if (c == '\r') {
                    if (! text.equals("") && name != null) {
                        networkManager.getSocket().emit("lobbyMessage", text);
                        updateMessages(name, text);
                    }
                    textField.setText("");
                }
            }
        });
        welcomeLabel = new Label("Number of players online: ?", skin);
        messages = new Array<Message>();
        messages.add(new Message("", "Welcome to Moles " + Constants.VERSION + "!"));

        messagesList = new List(skin, "messages");
        messagesList.setItems(messages);
        messagesPane = new ScrollPane(messagesList, skin, "android");
        messagesPane.setFadeScrollBars(false);

        chatTable = new Table(skin);
        chatTable.add(welcomeLabel);
        chatTable.row().padTop(20);
        chatTable.add(messagesPane).fill().expand();
        chatTable.row().padTop(20);
        chatTable.add(chatField).fill().padLeft(10).padRight(10);
        chatTable.padTop(20);
        chatTable.padBottom(20);
        chatTable.padLeft(30);

        roomsTable = new Table(skin);
        roomsTable.add(label);
        roomsTable.row();
        roomsTable.add(nameLabel);
        roomsTable.row().padTop(20);
        roomsTable.add(scrollPane).fill().expand();
        roomsTable.row().padTop(20);
        roomsTable.add(buttonsTable);

        table = new Table(skin);
        table.add(roomsTable).fill().expand();
        table.add(chatTable).fill().expand();
        table.pad(40);
        table.setFillParent(true);

        stage.setKeyboardFocus(chatField);
        stage.addActor(table);

        if (name == null) {
            TextField textField = new TextField("", skin);

            Dialog dialog = new Dialog("Greetings!", skin) {
                protected void result(Object object) {
                    TextField textField = (TextField) object;
                    String text = textField.getText();
                    if (text.trim().length() > 0) {
                        name = text;
                    } else {
                        name = "Anonymous";
                    }
                    setup();
                }
            };
            dialog.text("What is your name?");
            dialog.row();
            dialog.add(textField);
            dialog.key(Input.Keys.ENTER, textField);
            dialog.show(stage);
            stage.setKeyboardFocus(textField);
        } else {
            JSONObject data = new JSONObject();
            try {
                data.put("name", name);
                data.put("level", getLevel());
                data.put("version", Constants.VERSION);
                networkManager.getSocket().emit("playerRegistered", data);
            } catch (JSONException e) {
                Gdx.app.log("SocketIO", "Error sending registration info");
            }
        }
    }

    private void setup() {
        roomsTable.getCell(nameLabel).getActor().setText("[" + getLevel() + "] " + name);
        configSocketEvents();
        networkManager.connectSocket();
        if (data.getBoolean("firstTime")) {
            Dialog dialog = new Dialog("Welcome to Moles!", skin);
            dialog.text("\nChoose a room and battle it out with your friends!\n\n" +
                    "- Arrow keys to move, space bar to dig/pop.\n" +
                    "- Enter to chat, Q to quit a room.\n" +
                    "- You can only be underground if you have enough energy.\n" +
                    "- You get a point if you pop up underneath someone else.\n" +
                    "- You lose a point if you pop up underneath a rock.\n" +
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
                    data.put("version", Constants.VERSION);
                    socket.emit("playerRegistered", data);
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error sending registration info");
                }
            }
        }).once(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Gdx.app.log("SocketIO", "Server is offline");
                roomsTable.getCell(nameLabel).getActor().setText("Server is offline");
                create.setDisabled(true);
                Dialog dialog = new Dialog("Error", AssetLoader.uiSkin);
                dialog.text("Server is offline.");
                dialog.button("OK", true);
                dialog.key(Input.Keys.ENTER, true);
                dialog.show(stage);
            }
        }).once("wrongVersion", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Gdx.app.log("SocketIO", "Wrong version");
                roomsTable.getCell(nameLabel).getActor().setText("https://github.com/Waznop/Moles-SocketIO");
                create.setDisabled(true);
                Dialog dialog = new Dialog("Error", AssetLoader.uiSkin);
                dialog.text("\nYou have an outdated version of the game.\n\n" +
                        "Please download the latest version on GitHub.\n");
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
                        if (!open) {
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
        }).on("getNumPlayers", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {
                    numPlayers = data.getInt("numPlayers");
                    updateNumPlayers();
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error getting number of players");
                }
            }
        }).on("lobbyMessage", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {
                    String sender = data.getString("sender");
                    String content = data.getString("content");
                    updateMessages(sender, content);
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error getting message");
                }
            }
        }).on("playerLoggedIn", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {
                    String player = data.getString("name");
                    numPlayers += 1;
                    updateNumPlayers();
                    updateMessages("", player + " logged in.");
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error getting logged in player");
                }
            }
        }).on("playerLoggedOut", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {
                    String player = data.getString("name");
                    numPlayers -= 1;
                    updateNumPlayers();
                    updateMessages("", player + " logged out.");
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error getting logged out player");
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

    private void updateNumPlayers() {
        welcomeLabel.setText("Number of players online: " + numPlayers);
    }

    private void updateMessages(String sender, String content) {
        if (messages.size == Constants.MAX_MESSAGES) {
            messages.removeIndex(0);
        }
        messages.add(new Message(sender, content));
        messagesList.setItems(messages);
        messagesPane.setScrollPercentY(1);
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
