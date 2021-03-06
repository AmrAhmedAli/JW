package agent;

import java.util.ArrayList;
import java.util.PriorityQueue;

import agent.structures.SearchTreeNode;

public class AS extends Search{
	private PriorityQueue<SearchTreeNode> queue;
	private int heuristic;

	public AS(int heuristic, SearchTreeNode root) {
		this.heuristic = heuristic;
		this.root = root;
		root.setHeursticCost(heuristic ==1 ? heuristic1(root) :heuristic2(root));
		queue = new PriorityQueue<SearchTreeNode>();
		queue.add(root);
		cumelativeExpansions = 0;
	}

	@Override
	public SearchTreeNode begin() {
		while(true){
			if(queue.isEmpty()){
				System.out.println("no more moves -GS");
				return null;
			}
			SearchTreeNode current = queue.poll();
			if (isGoal(current))
					return current;
		    ArrayList<SearchTreeNode> expandednodes = expandNode(current);
			for (int j = expandednodes.size() - 1; j >= 0; j--) {
				SearchTreeNode curr = expandednodes.get(j);
				if(heuristic==1)
				    curr.setHeursticCost(heuristic1(curr));
				if(heuristic == 2)
					curr.setHeursticCost(heuristic2(curr));
			    queue.add(expandednodes.get(j));
				cumelativeExpansions++;
			}
		}
	}

}