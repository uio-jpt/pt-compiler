import java.util.List;

template graph {

    class Node {
        List<Edge> edges;
    }

    class Edge {
        Node from;
        Node to;
    }
}
