import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class BayesBall {

	private HashMap<String, Var> mNetWork;
	private final String independent = "yes", dependent = "no";

	public BayesBall(HashMap<String, Var> NetWork)
	{
		mNetWork = NetWork;
	}

	public String startCalculate(String query)
	{
		String temp[] = query.split("\\|");

		String source = temp[0].substring(0, temp[0].indexOf("-"));
		String target = temp[0].substring(temp[0].indexOf("-") + 1);

		ArrayList<String> evidenceNodes = new ArrayList<String>();

		if (temp.length > 1)
		{
			String evidence[] = temp[1].split(",");

			for (String val : evidence)
			{
				evidenceNodes.add(val.substring(0, val.indexOf('=')));
			}
			shadesOrResetNet(evidenceNodes, true);
		}

		String result = acitvePaths(makePaths(source, target));
		shadesOrResetNet(evidenceNodes, false);

		return result;
	}

	private String acitvePaths(ArrayList<Node[]> paths) 
	{
		for (Node path[] : paths)
		{
			System.out.println(Arrays.toString(path));

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
					isActive = dfsSearchShadeNode(currentNode);
				}
			}

			if (isActive) return dependent;
		}
		return independent;
	}

	private boolean dfsSearchShadeNode(Var currentNode) 
	{
		HashSet<String> visited = new HashSet<String>();
		return dfsSearchShadeNode(currentNode, visited);
	}

	private boolean dfsSearchShadeNode(Var currentNode, HashSet<String> visited) {
		if (currentNode.mShadeFlag)
			return true;

		for (String var : currentNode.getChilds())
		{
			if (!visited.contains(var))
			{
				visited.add(var);
				return dfsSearchShadeNode(mNetWork.get(var), visited);
			}
		}

		return false;
	}

	private ArrayList<Node[]> makePaths(String source, String target) 
	{
		ArrayList<Node[]> result = new ArrayList<Node[]>();
		Node temp[] = { new Node(source, false) };
		dfs(result, temp, target);

		return result;
	}

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

	private void findeNextNode(ArrayList<String> nextNodes, ArrayList<Node[]> result, Node[] currentPath, String target,
			boolean isParent) 
	{
		for (String nextNode : nextNodes)
		{
			if (!contatin(currentPath, nextNode))
			{
				dfs(result, cloneAndAdd(currentPath, nextNode, isParent), target);
			}
		}
	}

	private boolean contatin(Node[] currentPath, String nextNode) 
	{
		for (Node node : currentPath)
		{
			if (nextNode.equals(node.getName()))
				return true;
		}
		return false;
	}

	private Node[] cloneAndAdd(Node currentPath[], String currentNode, boolean isParent)
	{
		Node newCurrentPath[] = new Node[currentPath.length + 1];
		for (int i = 0; i < currentPath.length; i++)
			newCurrentPath[i] = new Node(currentPath[i]);

		newCurrentPath[currentPath.length] = new Node(currentNode, isParent);

		return newCurrentPath;
	}

	private void shadesOrResetNet(ArrayList<String> evidenceNodes, boolean shadeReset) {
		for (String node : evidenceNodes)
			mNetWork.get(node).mShadeFlag = shadeReset;
	}
}

class Node {

	private boolean mPrevIsParent;
	private String mNodeName;

	public Node(String node, boolean nextIsParent)
	{
		mPrevIsParent = nextIsParent;
		mNodeName = node;
	}

	public boolean prevIsParent() {
		return mPrevIsParent;
	}

	public Node(Node edge)
	{
		mPrevIsParent = edge.mPrevIsParent;
		mNodeName = edge.mNodeName;
	}

	public String getName()
	{
		return mNodeName;
	}

	@Override
	public String toString() 
	{
		return "Node [mPrevIsParent=" + mPrevIsParent + ", mNodeName=" + mNodeName + "]";
	}
	
}
