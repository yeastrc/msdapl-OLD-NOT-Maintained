/**
 * GreedySetCover.java
 * @author Vagisha Sharma
 * Oct 6, 2008
 * @version 1.0
 */
package edu.uwpr.protinfer.assemble.idpicker.algo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import edu.uwpr.protinfer.assemble.idpicker.graph.BipartiteGraph;
import edu.uwpr.protinfer.assemble.idpicker.graph.IBipartiteGraph;
import edu.uwpr.protinfer.assemble.idpicker.graph.Node;


/**
 * 
 */
public class GreedySetCover <L extends Node, R extends Node> {
    
    public GreedySetCover() {}
    
    public List<L> getGreedySetCover(BipartiteGraph<L, R> graph) {
        
        graph.backupEdges();
        graph.backupRightNodes();
        
        List<L> leftNodes = graph.getLeftNodes();
        PriorityQueue<L> queue = new PriorityQueue<L>(leftNodes.size(), new NodeComparator<L, R>(graph));
        for (L node: leftNodes) 
            queue.add(node);
        
        List<L> setCover = new ArrayList<L>();
        
        while (!queue.isEmpty()) {
            // take a look at the node with the max. outgoing edges
            L n = queue.peek();
            
            // if there are no outgoing edges it means we already have a set cover.
            int adjCnt = graph.getAdjacentNodes(n).size();
            if (adjCnt == 0)
                break;
            
            // add this node to the set cover
            setCover.add(n); 
            
            // remove all nodes adjacent to this node from the graph
            // make a copy to avoid ConcurrentModificationException
            List<R> adjNodes = new ArrayList<R>(adjCnt);
            adjNodes.addAll(graph.getAdjacentNodesL(n));
            for (R adjn: adjNodes) {
                graph.removeRightNode(adjn);
            }
            
            // now remove this node from the queue; this will re-adjust the queue so that the 
            // node with max outgoing edges is again at the top.
            queue.remove();
        }
        
        graph.restoreEdges();
        graph.restoreRightEdges();
        return setCover;
    }
    
    private static final class NodeComparator <L extends Node, R extends Node> implements Comparator<L> {

        private final IBipartiteGraph<L, R> graph;
        
        public NodeComparator(IBipartiteGraph<L, R> graph) {
            this.graph = graph;
        }
        
        @Override
        public int compare(L node1, L node2) {
            int node1Adj = graph.getAdjacentNodesL(node1).size();
            int node2Adj = graph.getAdjacentNodesL(node2).size();
            
            if (node1Adj > node2Adj)
                return -1;
            else if (node1Adj < node2Adj)
                return 1;
            else
                return 0;
        }
    }
}