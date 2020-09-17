package Ozone.Commands;

import Atom.Time.Countdown;
import arc.Core;
import arc.scene.style.TextureRegionDrawable;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.Vars;
import mindustry.ai.Astar;
import mindustry.content.Blocks;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Icon;
import mindustry.gen.Player;
import mindustry.world.Block;
import mindustry.world.Tile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Commands {

    public static final HashMap<String, Command> commandsList = new HashMap<>();
    private static final ArrayList<Seq<Tile>> modifiedTiles = new ArrayList<>();
    private static boolean init = false;
    private volatile static boolean falseVote = false;

    public static void init() {
        if (init) return;
        init = true;
        commandsList.put("help", new Command(Commands::help, "help"));
        commandsList.put("chaos-kick", new Command(Commands::chaosKick, "chaosKick"));
        commandsList.put("task-move", new Command(Commands::taskMove, "taskMove"));
        commandsList.put("info-pos", new Command(Commands::infoPos, "infoPos"));
        commandsList.put("info-pathfinding", new Command(Commands::infoPathfinding, "infoPathfinding"));
        Log.infoTag("Ozone", "Commands Center Initialized");
    }

    public static String getTranslation(String name) {
        return Core.bundle.get("ozone.commands." + name);
    }

    public static boolean call(String message) {
        //if (!message.startsWith(Settings.commandsPrefix)) return false;
        String[] arg = message.replaceFirst(",", "").split(" ");
        if (!commandsList.containsKey(arg[0].toLowerCase())) {
            tellUser("Commands not found");
            help(new ArrayList<>());
            return false;
        }
        Command comm = commandsList.get(arg[0].toLowerCase());
        ArrayList<String> args;
        if (message.contains(" ")) {
            message = message.replaceFirst(arg[0], "").replaceFirst(" ", "");
            arg = message.split(" ");
            args = new ArrayList<>(Arrays.asList(arg));
        } else {
            args = new ArrayList<>();
        }
        comm.method.accept(args);
        return true;
    }

    public static void infoPathfinding(ArrayList<String> s) {
        if (s.size() < 4) {
            tellUser("Not enough arguments");
            tellUser("usage: " + "info-pathfinding x(source-coordinate) y(source-coordinate) x(target-coordinate) y(target-coordinate) block(Blocks)(optional)");
            return;
        }
        try {
            String block = "";
            int xS = Integer.parseInt(s.get(0));
            int yS = Integer.parseInt(s.get(1));
            if (Vars.world.tile(xS, yS) == null) {
                tellUser("Non existent source tiles");
                return;
            }
            int xT = Integer.parseInt(s.get(2));
            int yT = Integer.parseInt(s.get(3));
            if ((xS - xT) < 4 || (yS - yT) < 4) {
                tellUser("distance too short");
            }
            if (s.size() <= 5) block = s.get(4);
            Block pathfindingBlock = null;
            if (!block.isEmpty()) {
                pathfindingBlock = Vars.content.block(block);
                if (pathfindingBlock == null)
                    tellUser("Nonexistent block, using default block: magmarock/dirtwall");
            }
            if (Vars.world.tile(xT, yT) == null) {
                tellUser("Non existent target tiles");
                return;
            }
            Tile target = Vars.world.tile(xT, yT);
            Tile source = Vars.world.tile(xS, yS);
            Seq<Tile> tiles = Astar.pathfind(source, target, h -> 0, Tile::passable);

            for (Tile t : tiles) {
                if (t.block() == null)
                    tellUser("Null block: " + t.toString());
                else if (pathfindingBlock != null)
                    t.setOverlay(pathfindingBlock);
                else if (t.block().isFloor())
                    t.setOverlay(Blocks.magmarock);
                else if (t.block().isStatic())
                    t.setOverlay(Blocks.dirtWall);
            }
            tellUser("to clear pathfinding overlay use /sync");
        } catch (NumberFormatException f) {
            tellUser("Failed to parse integer, are you sure that argument was integer ?");
            Vars.ui.showException(f);
        }
    }

    public static void infoPos(ArrayList<String> a) {
        tellUser("Player x,y: " + Vars.player.x + ", " + Vars.player.y);
        tellUser("Player tile x,y: " + Vars.player.tileX() + ", " + Vars.player.tileY());
    }

    public static void help(ArrayList<String> a) {
        StringBuilder sb = new StringBuilder();
        //sb.append("\n").append("Prefix: ").append(Settings.commandsPrefix).append("\n");
        sb.append("Available Commands:").append("\n");
        for (Map.Entry<String, Command> s : commandsList.entrySet()) {
            sb.append(s.getKey()).append(": ").append(s.getValue().description).append("\n");
        }
        tellUser(sb.toString());
    }

    public static void taskMove(ArrayList<String> s) {
        if (s.size() < 2) {
            tellUser("Not enough arguments");
            tellUser("usage: " + "task-move x(coordinate) y(coordinate)");
            return;
        }
        try {
            int x = Integer.parseInt(s.get(0));
            int y = Integer.parseInt(s.get(1));
            if (Vars.world.tile(x, y) == null) {
                tellUser("Non existent tiles");
                return;
            }
            long start = System.currentTimeMillis();
            PlayerInterface.moveTo(x, y, a -> tellUser("Reached in " + Countdown.result(start, TimeUnit.SECONDS)));
        } catch (NumberFormatException f) {
            tellUser("Failed to parse integer, are you sure that argument was integer ?");
            Vars.ui.showException(f);
        }

    }

    public static void chaosKick(ArrayList<String> unused) {
        falseVote = !falseVote;
        if (falseVote) {
            Thread s1 = new Thread(() -> {
                while (Vars.net.active() && falseVote)
                    for (Player target : Groups.player) {
                        if (!target.name.equals(Vars.player.name)) {
                            Call.sendChatMessage("/votekick " + target.name);
                            try {
                                Thread.sleep(200);
                            } catch (Throwable ignored) {
                            }
                        }
                    }
            });
            s1.start();
            tellUser("kicking started");
        } else {
            tellUser("kicking ended");
        }
    }


    public static void tellUser(String s) {
        if (Vars.ui.scriptfrag.shown())
            Log.infoTag("Ozone", s);
        else
            Vars.ui.chatfrag.addMessage("[white][[[royal]Ozone[white]]: " + s, null);
    }

    public static class Command {
        public final Consumer<ArrayList<String>> method;
        public final String description;
        public final TextureRegionDrawable icon;
        public boolean supportGUI = false;

        public Command(Consumer<ArrayList<String>> method, String description) {
            this.method = method;
            this.description = getTranslation(description);
            icon = Icon.add;
        }

        public Command(Consumer<ArrayList<String>> method, String description, TextureRegionDrawable icon) {
            this.method = method;
            this.description = getTranslation(description);
            this.icon = icon;
        }

        public Command(Consumer<ArrayList<String>> method, String description, TextureRegionDrawable icon, boolean supportGUI) {
            this.method = method;
            this.description = getTranslation(description);
            this.icon = icon;
            this.supportGUI = supportGUI;
        }
    }
}
