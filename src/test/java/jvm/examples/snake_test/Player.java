package jvm.examples.snake_test;

import java.io.IOException;
import java.util.*;

import static jvm.examples.snake_test.Main.parseInt;

public class Player {

    private final Grid grid;
    private final NetworkManager networkManager;
    private String direct;

    public Player(Grid grid, NetworkManager networkManager) {
        this.grid = grid;
        this.networkManager = networkManager;
    }

    public void run() throws IOException {
        for (; ; ) {
            String[] coordinates = networkManager.getMessage().split(",");
            Node start = parseApple(coordinates);
            Node end = parseHead(coordinates);
            grid.setWalls(parseWalls(coordinates));
            HashMap<Node, Node> path = getPath(start, end, grid);
            String direction = getDirection(end, path.get(end));
            networkManager.sendMessage(direction);
        }
    }

    private Node parseHead(String[] messages) {
        return parseNode(messages[1]);
    }

    private Node parseApple(String[] message) {
        return parseNode(message[0]);
    }

    private Node parseNode(String message) {
        String[] coordinate = message.split(" ");
        return new Node(parseInt(coordinate[1]) / 10, parseInt(coordinate[0]) / 10);
    }

    private Set<Node> parseWalls(String[] messages) {
        Set<Node> set = new HashSet<>();
        for (int i = 2; i < messages.length; i++) {
            set.add(parseNode(messages[i]));
        }
        return set;
    }

    private HashMap<Node, Node> getPath(Node start, Node end, Grid grid) {
        HashMap<Node, Node> cameFrom = new HashMap<>();
        Queue<Node> queue = new ArrayDeque<>();
        queue.add(start);
        while (!queue.isEmpty()) {
            Node current = queue.poll();
            if (current.equals(end)) {
                return cameFrom;
            }
            for (Node neighbor : grid.getNeighbors(current)) {
                if (cameFrom.get(neighbor) == null) {
                    cameFrom.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }
        return cameFrom;
    }

    private String getDirection(Node current, Node next) {
        if (next == null) {
            return direct;
        }
        if (current.column + 1 == next.column) {
//            System.out.println("RIGHT");
            direct = "r";
            return direct;
        } else if (current.column - 1 == next.column) {
//            System.out.println("LEFT");
            direct = "l";
            return direct;
        } else if (current.row + 1 == next.row) {
//            System.out.println("DOWN");
            direct = "d";
            return direct;
        } else if (current.row - 1 == next.row) {
//            System.out.println("UP");
            direct = "u";
            return direct;
        } else {
            throw new RuntimeException("wrong move");
        }

    }

}
