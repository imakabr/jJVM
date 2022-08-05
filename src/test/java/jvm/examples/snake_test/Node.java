package jvm.examples.snake_test;

import java.util.Objects;

class Node {
    int row;
    int column;

    Node(int row, int column) {
        this.row = row;
        this.column = column;
    }

    @Override
    public boolean equals(Object node1) {
        if (node1 == null) {
            return false;
        }
        Node node = (Node) node1;
        return this.row == node.row && this.column == node.column;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.row, this.column);
    }

    @Override
    public String toString() {
        return " row = " + row + " column = " + column;
    }
}
