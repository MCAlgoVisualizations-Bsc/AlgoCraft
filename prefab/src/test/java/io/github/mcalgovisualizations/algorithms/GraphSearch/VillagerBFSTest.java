package io.github.mcalgovisualizations.algorithms.GraphSearch;

import io.github.mcalgovisualizations.prefab.events.Compare;
import io.github.mcalgovisualizations.prefab.events.Message;
import io.github.mcalgovisualizations.prefab.algorithms.GraphSearch.Node;
import io.github.mcalgovisualizations.prefab.algorithms.GraphSearch.NodeContext;
import io.github.mcalgovisualizations.prefab.algorithms.GraphSearch.VillagerBFS;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VillagerBFSTest {
    @Test
    void emitsEdgeStatusForNewAndAlreadyVisitedUndirectedEdge() {
        Node a = new Node(0, 1, Node.NodeTarget.Start);
        Node b = new Node(1, 2, Node.NodeTarget.None);
        a.addNeighbor(b);
        b.addNeighbor(a);

        NodeContext context = new NodeContext(a);
        new VillagerBFS().run(context);

        long newEdges = context.getEvents().stream()
                .filter(Message.class::isInstance)
                .map(Message.class::cast)
                .filter(msg -> msg.message().contains(": NEW"))
                .count();

        long alreadyVisitedEdges = context.getEvents().stream()
                .filter(Message.class::isInstance)
                .map(Message.class::cast)
                .filter(msg -> msg.message().contains(": ALREADY_VISITED"))
                .count();

        assertEquals(1, newEdges);
        assertEquals(1, alreadyVisitedEdges);
    }

    @Test
    void doesNotMoveToDequeuedNodeWhenAllItsEdgesAreAlreadyInspected() {
        Node a = new Node(0, 1, Node.NodeTarget.Start);
        Node b = new Node(1, 2, Node.NodeTarget.None);
        connectUndirected(a, b);

        NodeContext context = new NodeContext(a);
        new VillagerBFS().run(context);

        List<Integer> comparedValues = context.getEvents().stream()
                .filter(Compare.class::isInstance)
                .map(Compare.class::cast)
                .map(compare -> (Integer) compare.xValue())
                .toList();

        assertEquals(List.of(1, 2), comparedValues);
    }

    @Test
    void secondVisitTo92ComesDirectlyFrom67() {
        Node n38 = new Node(0, 38, Node.NodeTarget.Start);
        Node n21 = new Node(1, 21);
        Node n40 = new Node(2, 40);
        Node n67 = new Node(3, 67);
        Node n30 = new Node(4, 30);
        Node n41 = new Node(5, 41);
        Node n49 = new Node(6, 49);
        Node n92 = new Node(7, 92);



        connectUndirected(n38, n21);
        connectUndirected(n38, n40);
        connectUndirected(n38, n67);
        connectUndirected(n41, n30);
        connectUndirected(n21, n41);
        connectUndirected(n40, n92);
        connectUndirected(n67, n49);
        connectUndirected(n67, n92);

        NodeContext context = new NodeContext(n38);
        new VillagerBFS().run(context);

        List<Integer> comparedValues = context.getEvents().stream()
                .filter(Compare.class::isInstance)
                .map(Compare.class::cast)
                .map(compare -> (Integer) compare.xValue())
                .toList();

        List<Integer> expected = List.of(
                38, 21, 38, 40, 38, 67, 38,
                21, 41, 21, 38, 40, 92, 40, 38, 67, 92, 67, 49, 67, 38, 21, 41, 30);
        assertEquals(comparedValues, expected);
    }

    private static void connectUndirected(Node a, Node b) {
        a.addNeighbor(b);
        b.addNeighbor(a);
    }
}
