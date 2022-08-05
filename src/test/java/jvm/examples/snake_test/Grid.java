package jvm.examples.snake_test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Grid {
    private final int rows;
    private final int columns;
    private Set<Node> walls;

    public Grid(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
    }

    public void setWalls(Set<Node> walls) {
        this.walls = walls;
    }

    private Node[] getClosestNodes(Node node) {
        return new Node[]{new Node(node.row + 1, node.column),
                new Node(node.row - 1, node.column),
                new Node(node.row, node.column + 1),
                new Node(node.row, node.column - 1)};
    }

    public List<Node> getNeighbors(Node node) {
        Node[] neighbors = getClosestNodes(node);
        List<Node> result = new ArrayList<>();
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
