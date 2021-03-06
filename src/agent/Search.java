package agent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;

import models.MiniMap;
import agent.structures.Operator;
import agent.structures.SearchTreeNode;
import agent.structures.State;

public abstract class Search {
	protected SearchTreeNode root;
	protected final int MAX_DPETH = 1000;
	protected int cumelativeCost;
	protected int cumelativeExpansions = 0;
	
	public int getCumelativeExpansions() {
		return cumelativeExpansions;
	}
	

	public static boolean isGoal(SearchTreeNode node){
		if(node.getState().walkersLeft <= 0 
				|| node.getWorldState().walkers.size() <= 0)
			return true;
		return false;
	}
	public static boolean isLocalGoal(SearchTreeNode node){
			if(node.getState().localGoal)
				if(node.getOperatorApplied() == Operator.KILL)
					return true;
			else
				if(node.getOperatorApplied() == Operator.PICKUP)
					return true;
			
			return false;
	}
	

	public static ArrayList<SearchTreeNode> expandNode(SearchTreeNode node){
		MiniMap world = node.getWorldState();
		ArrayList<SearchTreeNode> result = new ArrayList<SearchTreeNode>(6);
		State current = node.getState();
		int walkersLeft = current.walkersLeft;
		boolean localGoal = current.localGoal;
		boolean canAttak = (node.getOperatorApplied() == Operator.KILL ||
				world.dragonGlass > 0);

		int walkersAfterKill = walkersLeft -1;
		State normalState = new State(walkersLeft, localGoal);
		State killState = new State(walkersAfterKill, localGoal);
		State pickupState = new State(walkersLeft, true);
				
		if(world.ifAttack() && canAttak){
			//Kill
			MiniMap newMap = new MiniMap(world);
			newMap.kill();
			result.add(new SearchTreeNode(newMap, killState, node, Operator.KILL, 
					node.getDepth()+1, Operator.costOfOperator(Operator.KILL)+node.getPathCost(),
					node.getSearchType()));
		}
		if(world.ifPickUp()/* && localGoal*/){
			//Pickup
			MiniMap newMap = new MiniMap(world);
			newMap.dragonGlass = world.MAX_DRAGON_GLASS;
			result.add(new SearchTreeNode(newMap, pickupState, node, Operator.PICKUP, 
				node.getDepth()+1, Operator.costOfOperator(Operator.PICKUP)+node.getPathCost(),
				node.getSearchType()));
		}

		if(world.ifMoveUp() && node.getOperatorApplied() != Operator.DOWN||
				world.ifMoveUp() && node.getOperatorApplied() == Operator.DOWN  && localGoal && world.dragonGlass<=0 && world.containStone(Operator.UP)){
			//UP
			MiniMap newMap = new MiniMap(world);
			newMap.x--;
			if(node.getOperatorApplied() == Operator.KILL)
				newMap.dragonGlass--;
            
			result.add(new SearchTreeNode(newMap, normalState, node, 
					Operator.UP, node.getDepth()+1,
					Operator.costOfOperator(Operator.UP)+node.getPathCost(),
					node.getSearchType()));
		}
		
		if(world.ifMoveLeft() && node.getOperatorApplied() != Operator.RIGHT||
				world.ifMoveLeft() && node.getOperatorApplied() == Operator.RIGHT  && localGoal && world.dragonGlass<=0 && world.containStone(Operator.LEFT)){
			//LEFT
			MiniMap newMap = new MiniMap(world);
			newMap.y--;
			if(node.getOperatorApplied() == Operator.KILL)
				newMap.dragonGlass--;
			
			result.add(new SearchTreeNode(newMap, normalState, node, 
					Operator.LEFT, node.getDepth()+1, 
					Operator.costOfOperator(Operator.DOWN)+node.getPathCost(),
					node.getSearchType()));
		}

		if(world.ifMoveDown() && node.getOperatorApplied() != Operator.UP ||
				world.ifMoveDown() && node.getOperatorApplied() == Operator.UP  && localGoal && world.dragonGlass<=0 && world.containStone(Operator.DOWN)){
			//DOWN
			MiniMap newMap = new MiniMap(world);
			newMap.x++;
			if(node.getOperatorApplied() == Operator.KILL)
				newMap.dragonGlass--;

			result.add(new SearchTreeNode(newMap, normalState, node,
					Operator.DOWN, node.getDepth()+1,
					Operator.costOfOperator(Operator.LEFT)+node.getPathCost(),
					node.getSearchType()));
		}

		if(world.ifMoveRight() && node.getOperatorApplied() != Operator.LEFT ||
				world.ifMoveRight() && node.getOperatorApplied() == Operator.LEFT  && localGoal && world.dragonGlass<=0 && world.containStone(Operator.RIGHT)){
			//RIGHT
			MiniMap newMap = new MiniMap(world);
			newMap.y++;
			if(node.getOperatorApplied() == Operator.KILL)
				newMap.dragonGlass--;
			
			result.add(new SearchTreeNode(newMap, normalState, node,
					Operator.RIGHT, node.getDepth()+1, 
					Operator.costOfOperator(Operator.RIGHT)+node.getPathCost(),
					node.getSearchType()));
		}
		
		
		return result;
	}

	public static LinkedList<String> backTrack(SearchTreeNode node,boolean visualize){
		
		LinkedList<String> result = new LinkedList<String>();
		SearchTreeNode current = node;
		for(int i=node.getDepth(); i>=0; i--){
			if(visualize)
				result.addFirst(current.toString() + "\n");
			else
				result.addFirst(current.getOperatorApplied() + "\n");
			try{
				current = current.getParent();
			}catch(NullPointerException e){ }
		}
		return result;
	}

	public static int heuristic1(SearchTreeNode node){
		int cost = 0;
		
		if(isGoal(node))
			return 0;
		
		int tempX = node.getWorldState().x, tempY = node.getWorldState().y;
		if(!node.getState().localGoal){
			cost += Math.abs(tempX - node.getWorldState().stones.get(0)[0]) 
					+ Math.abs(tempY - node.getWorldState().stones.get(0)[1]);
			tempX = node.getWorldState().stones.get(0)[0];
			tempY = node.getWorldState().stones.get(0)[1];
		}
		
		for(int i=0; i<node.getWorldState().walkers.size(); i++){
			cost += Math.abs(tempX - node.getWorldState().walkers.get(i)[0]) 
					+ Math.abs(tempY - node.getWorldState().walkers.get(i)[1]) - 1;
			tempX = node.getWorldState().walkers.get(i)[0];
			tempY = node.getWorldState().walkers.get(i)[1];
		}
		return cost;
	}
	
	public static int heuristic2(SearchTreeNode node){
		int cost = 0;
		
		if(isGoal(node))
			return 0;
		
		if(!node.getState().localGoal)
			cost++;
		
		for(int i=0; i<node.getWorldState().walkers.size(); i++)
			cost++;
		
		return cost;
	}
	
	public abstract SearchTreeNode begin();

}
