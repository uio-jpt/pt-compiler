import java.util.List;
import java.util.*;

template Graph {

    class Node {
        List<Edge> edges;

        int getValue() { return 666; }
    }

    class Edge {
        Node from, to;
    }
}
