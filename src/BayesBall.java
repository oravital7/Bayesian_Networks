import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class BayesBall {

    /* ****************** Member class ******************** */
	
	private HashMap<String, Var> mNetWork;
	private final String independent = "yes", dependent = "no";

	
	/* ***************************************************
	 ***************** Public Methods *****************
	 *************************************************** */
	
	public BayesBall(HashMap<String, Var> netWork)
	{
		mNetWork = netWork;
	}

	public String getQueryResult(String query)
	{
		String temp[] = query.split("\\|");

		String source = temp[0].substring(0, temp[0].indexOf("-"));
		String target = temp[0].substring(temp[0].indexOf("-") + 1);

		ArrayList<String> evidenceNodes = new ArrayList<String>();

		if (temp.length > 1)
		{
			for (String evidence : temp[1].split(","))
				evidenceNodes.add(evidence.split("=")[0]);

			shadesOrResetNet(evidenceNodes, true);
		}

		final String result = acitvePaths(makePaths(source, target));  // find paths that legal or illegal
		shadesOrResetNet(evidenceNodes, false); // return back all shades node to false before we leaving the function

		return result;
	}

	/* ***************************************************
	 ***************** Private Methods *****************
	 *************************************************** */

	/**
	 * By the rules of BayesBall alg we use dfs to find active paths otherwise is independent
	 * @param paths - represent all possibles paths that exist between the nodes in the given query
	 * @return
	 */
	private String acitvePaths(ArrayList<Node[]> paths) 
	{
		for (Node path[] : paths)
		{
			boolean isActive = true;
			for (int i = 1; i < path.length - 1 && isActive; i++)
			{
				Var currentNode = mNetWork.get(path[i].getName());
				if (currentNode.mShadeFlag && path[i + 1].prevIsParent())
				{
					isActive = false;
				}
				else if (!currentNode.mShadeFlag && path[i].prevIsParent() && !path[i + 1].prevIsParent())
				{
					isActive = dfsSearchShadeNode(currentNode, new HashSet<String>()); // Use DFS and find shades node that descendant of currentNode
				}
			}

			if (isActive) return dependent;
		}

		return independent;
	}

	/**
	 * Scan from currentNode and only with one direction by his childs
	 * @param currentNode
	 * @param visited
	 * @return true - found shadeNode
	 */
	private boolean dfsSearchShadeNode(Var currentNode, HashSet<String> visited) {
		if (currentNode.mShadeFlag)
			return true;

		boolean result = false;
		for (String var : currentNode.getChilds())
		{
			if (!visited.contains(var))
			{
				visited.add(var);
				result = result || dfsSearchShadeNode(mNetWork.get(var), visited);
				if (result) return true;
			}
		}

		return result;
	}

	/**
	 * given 2 nodes return all possibles paths between them
	 * @param source
	 * @param target
	 * @return ArrayList of all possibles paths
	 */
	private ArrayList<Node[]> makePaths(String source, String target) 
	{
		ArrayList<Node[]> result = new ArrayList<Node[]>();
		Node temp[] = { new Node(source, false) };
		dfs(result, temp, target);

		return result;
	}

	/**
	 * Simple DFS, adding path to array when we reach target
	 * @param result
	 * @param currentPath
	 * @param target
	 */
	private void dfs(ArrayList<Node[]> result, Node currentPath[], String target) {
		String currentNode = currentPath[currentPath.length - 1].getName();
		if (currentNode.equals(target))
		{
			result.add(currentPath);
			return;
		}

		Var currentNodeVar = mNetWork.get(currentNode);

		findeNextNode(currentNodeVar.getChilds(), result, currentPath, target, true);
		findeNextNode(currentNodeVar.getParents(), result, currentPath, target, false);
	}

	/**
	 * Find next node that not already exist in the path (prevent circular) and continue from there
	 * @param nextNodes
	 * @param result
	 * @param currentPath
	 * @param target
	 * @param isParent
	 */
	private void findeNextNode(ArrayList<String> nextNodes, ArrayList<Node[]> result, Node[] currentPath, String target,
			boolean isParent)
	{
		for (String nextNode : nextNodes)
		{
			if (!contatin(currentPath, nextNode))
			{
				dfs(result, cloneAndAdd(currentPath, nextNode, isParent), target); // Important! clone path
			}
		}
	}

	/**
	 * Method to check in simple array if variable exist on the path
	 * @param currentPath
	 * @param nextNode
	 * @return true - exist
	 */
	private boolean contatin(Node[] currentPath, String nextNode) 
	{
		for (Node node : currentPath)
		{
			if (nextNode.equals(node.getName()))
				return true;
		}
		return false;
	}
	
	/**
	 * Deep copy of currentPath and add new Node to the path
	 * @param currentPath
	 * @param currentNode
	 * @param isParent
	 * @return the new path
	 */
	private Node[] cloneAndAdd(Node currentPath[], String currentNode, boolean isParent)
	{
		Node newCurrentPath[] = new Node[currentPath.length + 1];
		for (int i = 0; i < currentPath.length; i++)
			newCurrentPath[i] = new Node(currentPath[i]);

		newCurrentPath[currentPath.length] = new Node(currentNode, isParent);

		return newCurrentPath;
	}

	/**
	 * Mark Shade (evidences) vars
	 * @param evidenceNodes
	 * @param shadeReset
	 */
	private void shadesOrResetNet(ArrayList<String> evidenceNodes, boolean shadeReset)
	{
		for (String node : evidenceNodes)
			mNetWork.get(node).mShadeFlag = shadeReset;
	}
}

/**
 * Represent Node that belong to chain of nodes (Kind of one direction linked list)
 * @author oravi
 *
 */
class Node {

	private boolean mPrevIsParent;  //  Edge created from son or parent
	private String mNodeName;

	public Node(String node, boolean nextIsParent)
	{
		mPrevIsParent = nextIsParent;
		mNodeName = node;
	}

	public Node(Node edge)
	{
		mPrevIsParent = edge.mPrevIsParent;
		mNodeName = edge.mNodeName;
	}

	public boolean prevIsParent() { return mPrevIsParent; }

	public String getName()	{ return mNodeName; }

	@Override
	public String toString() 
	{
		return "Node [mPrevIsParent=" + mPrevIsParent + ", mNodeName=" + mNodeName + "]";
	}

}
