import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class VariableElimination {

	private HashMap<String, Var> mNetWork;

	public VariableElimination(HashMap<String, Var> netWork) 
	{
		mNetWork = netWork;

	}

	public String getQueryResult(String query)
	{
		String varQuery[] = query.substring(query.indexOf('(') + 1, query.indexOf('|')).split("=");
		String subQueryHidden = query.substring(query.indexOf(')') + 2);
		String hidden[] = new String[0];
		HashMap<String, String> evidences = new HashMap<String, String>();

		if (!subQueryHidden.isEmpty())
			hidden = subQueryHidden.split("-");

		for (String tempEvidence : query.substring(query.indexOf('|') + 1, query.indexOf(')')).split(","))
			evidences.put(tempEvidence.substring(0, tempEvidence.indexOf('=')), tempEvidence.substring(tempEvidence.indexOf('=') + 1));
		System.out.println("newwwwwwwwwwwwwwwwww");
		startAlgo(hidden, evidences, varQuery);
		return null;
	}

	private void startAlgo(String[] hidden, HashMap<String, String> evidences, String[] varQuery) 
	{
		ArrayList<Var> factors = makeFactors(hidden, evidences, varQuery[0]);

		for (String hiddenVar : hidden)
		{
			while (true)
			{
				int indexMinimalPair[] = findMinimalPair(hiddenVar, factors, evidences);
				if (indexMinimalPair == null)
					break;
				else if (indexMinimalPair.length == 1)
					sumFactors(hiddenVar, indexMinimalPair[0], factors, evidences);
				else
				{
					joinFactors(hiddenVar, indexMinimalPair, factors, evidences);
				}
			}
		}
		//		System.out.println("START PRINITG FACTORS");
		//		for(Var v : factors)
		//		{
		//			System.out.println(v);
		//		}
		//		System.out.println("FINISH PRINITG FACTORS");

	}

	private void joinFactors(String hiddenVar, int[] indexMinimalPair, ArrayList<Var> factors, HashMap<String, String> evidences)
	{
		Var var1 = factors.get(indexMinimalPair[0]);
		Var var2 = factors.get(indexMinimalPair[1]);
		Var result = new Var(hiddenVar);

		System.out.println("choose: " + var1.getName() +", " + var2.getName() + " hiddenVar: " + hiddenVar);

		initNewVarParent(result, var1, evidences, hiddenVar);
		initNewVarParent(result, var2, evidences, hiddenVar);

		cleanCPTevidences(var1, evidences);
		cleanCPTevidences(var2, evidences);

		joinVars(var1, var2, result, evidences);

		factors.remove(var1);
		factors.remove(var2);

		factors.add(result);
	}

	private void joinVars(Var var1, Var var2, Var result, HashMap<String, String> evidences) 
	{
		HashMap<String, Integer> sharedVariable = makeSharedVariables(var1, var2, evidences);
		System.out.println(sharedVariable);

		for (int i = 0; i < var1.getCPT().size(); i++)
		{
			for (int j = 0; j < var2.getCPT().size(); j++)
			{
				boolean isMatchRow = true;

				for (Map.Entry<String, Integer> set : sharedVariable.entrySet())
				{
					if (set.getValue() == 2 && !var1.getCPT().get(i)[var1.indexOf(set.getKey())].
							equals(var2.getCPT().get(j)[var2.indexOf(set.getKey())]))
					{
						isMatchRow = false;
						break;
					}
				}

				if (isMatchRow)
				{
					result.addEmptyRow();
					fillRow(var1, i, result);
					fillRow(var2, j, result);
					multiplyCol(var1.getCPT().get(i), var2.getCPT().get(j), result);
				}
			}
		}
	}

	private void multiplyCol(String[] var1, String[] var2, Var result) 
	{
		double newValue = Double.parseDouble(var1[var1.length - 1]) * Double.parseDouble(var2[var2.length - 1]);
		result.fillRow("" + newValue, result.getParents().size() + 1);
	}

	private void fillRow(Var var, int indexRowVar, Var result)
	{
		for (int k = 0; k < var.getParents().size(); k++)
		{
			String currentName = var.getParents().get(k);
			int IndexOfCurrent = result.indexOf(currentName);
			if (IndexOfCurrent != -1)
			{
				String value = var.getCPT().get(indexRowVar)[k];
				result.fillRow(value, IndexOfCurrent);
			}
		}

		int indexOfCurrent = result.indexOf(var.getName());
		if (indexOfCurrent != -1)
		{
			String value = var.getCPT().get(indexRowVar)[var.getParents().size()];
			result.fillRow(value, indexOfCurrent);
		}
	}

	/**
	 * Clean unnecessary rows from the CPT, prepare to join
	 * @param var - var to clean
	 * @param evidences
	 */
	private void cleanCPTevidences(Var var, HashMap<String, String> evidences) 
	{
		evidences.forEach((key, value) -> {
			int keyCPTindex = var.indexOf(key);
			if (keyCPTindex != -1)
			{
				for (int i = 0; i < var.getCPT().size(); i++)
				{
					if (!var.getCPT().get(i)[keyCPTindex].equals(value))
						var.getCPT().remove(i--);
				}
			}
		});
	}

	/**
	 * Init parent of the new result by join
	 * @param newVar - the reuslt of the join
	 * @param var
	 * @param evidences
	 * @param hiddenVar
	 */
	private void initNewVarParent(Var newVar, Var var, HashMap<String, String> evidences, String hiddenVar)
	{
		for (String parent : var.getParents())
		{
			if (!evidences.containsKey(parent) && !parent.equals(hiddenVar) && !newVar.getParents().contains(parent))
				newVar.addParents(parent);
		}
	}

	private void sumFactors(String hiddenVar, int i, ArrayList<Var> factors, HashMap<String, String> evidences) {
		System.out.println("hello sum factors " + factors);

	}

	private ArrayList<Var> makeFactors(String[] hidden, HashMap<String, String> evidences, String varQuery) 
	{
		ArrayList<Var> factors = new ArrayList<Var>();
		for (String hiddenVar : hidden)
		{
			if (dfsFindAncestor(hiddenVar, evidences, varQuery, new HashSet<String>(Arrays.asList(hiddenVar))))
				factors.add(new Var(mNetWork.get(hiddenVar)));
		}

		factors.add(new Var(mNetWork.get(varQuery)));

		for (String evidence : evidences.keySet())
			factors.add(new Var(mNetWork.get(evidence)));

		removeInstantiatedOneValued(factors, evidences);

		return factors;
	}

	private void removeInstantiatedOneValued(ArrayList<Var> factors, HashMap<String, String> evidences) 
	{
		for (int i = 0; i < factors.size(); i++)
		{
			boolean isOneValued = true;
			ArrayList<String> parents = factors.get(i).getParents();

			if (!evidences.containsKey(factors.get(i).getName()))
				isOneValued = false;

			for (int j = 0; j < parents.size() && isOneValued; j++)
			{
				if (!evidences.containsKey(parents.get(j)))
					isOneValued = false;
			}

			if (isOneValued)
			{
				factors.remove(i--);
			}
		}
	}

	private boolean dfsFindAncestor(String currentVar, HashMap<String, String> evidences, String varQuery, HashSet<String> visited) 
	{
		if (conatinInQueryEvidence(currentVar, evidences, varQuery))
			return true;

		boolean result = false;
		for (String varChild : mNetWork.get(currentVar).getChilds())
		{
			if (!visited.contains(varChild))
			{
				visited.add(varChild);
				result = result || dfsFindAncestor(varChild, evidences, varQuery, visited);
				if (result) return true;
			}
		}

		return result;
	}

	private boolean conatinInQueryEvidence(String currentVar, HashMap<String, String> evidences, String varQuery) 
	{
		if (currentVar.equals(varQuery))
			return true;

		for (String evidence : evidences.keySet())
		{
			if (evidence.equals(currentVar))
				return true;
		}

		return false;
	}

	private int[] findMinimalPair(String hiddenVar, ArrayList<Var> factors, HashMap<String, String> evidences) 
	{
		int minRows = Integer.MAX_VALUE;
		ArrayList<Integer> containsHidden = containsHidden(hiddenVar, factors);
		if (containsHidden.size() == 0)
			return null;
		if (containsHidden.size() == 1)
		{
			int result[] = {containsHidden.get(0)};
			return result;
		}

		int result[] = {containsHidden.get(0), containsHidden.get(1)};
		for (int i = 0; i < containsHidden.size() - 1 ; i++)
		{
			for (int j = i + 1; j < containsHidden.size(); j++)
			{
				int tempRows = calcRows(factors.get(containsHidden.get(i)), factors.get(containsHidden.get(j)), evidences);
				if (tempRows < minRows)
				{
					minRows = tempRows;
					result[0] = containsHidden.get(i);
					result[1] = containsHidden.get(j);
				}
			}
		}

		return result;
	}

	private int calcRows(Var var1, Var var2, HashMap<String, String> evidences) 
	{
		HashMap<String, Integer> sharedVars = makeSharedVariables(var1, var2, evidences);

		int result = 1;
		for (String var : sharedVars.keySet())
			result *= mNetWork.get(var).NumberOfValues();

		return result;
	}

	HashMap<String, Integer> makeSharedVariables(Var var1, Var var2, HashMap<String, String> evidences)
	{
		HashMap<String, Integer> sharedVars = new HashMap<String, Integer>();

		if (!evidences.containsKey(var1.getName()))
			addToMap(var1.getName(), sharedVars);

		if (!evidences.containsKey(var2.getName()))
			addToMap(var2.getName(), sharedVars);

		for (String parent : var1.getParents())
		{
			if (!evidences.containsKey(parent))
				addToMap(parent, sharedVars);
		}

		for (String parent : var2.getParents())
		{
			if (!evidences.containsKey(parent))
				addToMap(parent, sharedVars);
		}

		return sharedVars;
	}

	private void addToMap(String name, HashMap<String, Integer> sharedVars) 
	{
		if (sharedVars.containsKey(name))
			sharedVars.put(name, sharedVars.get(name) + 1);
		else
			sharedVars.put(name, 1);
	}

	private ArrayList<Integer> containsHidden(String hiddenVar, ArrayList<Var> factors) 
	{
		ArrayList<Integer> result = new ArrayList<Integer>();

		for (int i = 0; i < factors.size(); i++)
		{
			if (factors.get(i).getName().equals(hiddenVar))
			{
				result.add(i);
			}
			else 
			{
				for (String parent : factors.get(i).getParents())
				{
					if (parent.equals(hiddenVar))
					{
						result.add(i);
						break;
					}
				}
			}
		}

		return result;
	}
}
