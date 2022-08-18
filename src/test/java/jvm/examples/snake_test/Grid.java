package jvm.examples.snake_test;

import java.util.ArrayList;
import java.util.HashSet;

public class Grid {
    private final int rows;
    private final int columns;
    private HashSet<Node> walls;

    public Grid(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
    }

    public void setWalls(HashSet<Node> walls) {
        this.walls = walls;
    }

    private Node[] getClosestNodes(Node node) {
        return new Node[]{new Node(node.row + 1, node.column),
                new Node(node.row - 1, node.column),
                new Node(node.row, node.column + 1),
                new Node(node.row, node.column - 1)};
    }

    public ArrayList<Node> getNeighbors(Node node) {
        Node[] neighbors = getClosestNodes(node);
        ArrayList<Node> result = new ArrayList<>();
        for (Node actualNode : neighbors) {
            if (inBounds(actualNode) && !walls.contains(actualNode)) {
                result.add(actualNode);
            }
        }
        return result;
    }

    private boolean inBounds(Node node) {
        return node.row >= 0 && node.row < rows &&
                node.column >= 0 && node.column < columns;
    }
}
