package jvm.examples.snake_test;

import java.util.Objects;

class Node {
    int row;
    int column;
    private int hashCode;

    Node(int row, int column) {
        this.row = row;
        this.column = column;
    }

    @Override
    public boolean equals(Object node1) {
        if (node1 == null) {
            return false;
        }
        if (node1 instanceof Node) {
            Node node = (Node) node1;
            return this.row == node.row && this.column == node.column;
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            int result = 1;
            result = 31 * result + row;
            result = 31 * result + column;
            hashCode = result;
        }
        return hashCode;
    }

    @Override
    public String toString() {
        return " row = " + row + " column = " + column;
    }
}
