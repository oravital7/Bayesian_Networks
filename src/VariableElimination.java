import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class VariableElimination {

	private HashMap<String, Var> mNetWork;
	private int mMultCount, mSumCount;

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
		return startAlgo(hidden, evidences, varQuery);
	}

	private String startAlgo(String[] hidden, HashMap<String, String> evidences, String[] varQuery) 
	{
		ArrayList<Var> factors = makeFactors(hidden, evidences, varQuery[0]);
		mSumCount = mMultCount = 0;

		for (String hiddenVar : hidden)
		{
			while (true)
			{
				int indexMinimalPair[] = findMinimalPair(hiddenVar, factors, evidences);
				if (indexMinimalPair == null)
				{
					break;
				}
				else if (indexMinimalPair.length == 1)
				{
					Var newVar = sumFactors(hiddenVar, factors.get(indexMinimalPair[0]));
					factors.remove(indexMinimalPair[0]);
					factors.add(newVar);
				}
				else
				{
					joinFactors(hiddenVar, indexMinimalPair, factors, evidences);
				}
			}


		}
		
		while (factors.size() > 1)
		{
			int indexs[] = {0, 1};
			joinFactors(varQuery[0], indexs, factors, evidences);
		}
		
		normalize(factors.get(0));
		System.out.println("factors:");
		System.out.println(factors);
		
		return calcFinalResult(factors.get(0), varQuery);
	}

	private String calcFinalResult(Var var, String[] varQuery) 
	{
		mSumCount += var.getCPT().size() - 1;
		int indexOfResult = var.indexOf(varQuery[0]);
		
		String result = "," + mSumCount + "," + mMultCount;
		for (String s[] : var.getCPT())
		{
			if (s[indexOfResult].equals(varQuery[1]))
				return result = String.format("%.5f", Double.parseDouble(s[s.length - 1])) + result;
		}
		
		return null;
	}

	private void normalize(Var var) 
	{
		double sumRows = 0;
		for (String row[] : var.getCPT())
			sumRows += Double.parseDouble(row[row.length - 1]);
		
		for (String row[] : var.getCPT())
			row[row.length - 1] = "" + (Double.parseDouble(row[row.length - 1]) / sumRows);
	}

	private void joinFactors(String hiddenVar, int[] indexMinimalPair, ArrayList<Var> factors, HashMap<String, String> evidences)
	{
		Var var1 = factors.get(indexMinimalPair[0]);
		Var var2 = factors.get(indexMinimalPair[1]);
		Var result = new Var(hiddenVar);

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
		mMultCount++;
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
			}});
		
		for (int i = 0; i < var.getCPT().size(); i++)
		{
			String currentRow[] = var.getCPT().get(i);
			if (Double.parseDouble(currentRow[currentRow.length - 1]) == 0)
				var.getCPT().remove(i--);
		}
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
		
		if (!evidences.containsKey(var.getName()) && !var.getName().equals(hiddenVar) && !var.getParents().contains(var.getName()))
			newVar.addParents(var.getName());
	}

	private Var sumFactors(String hiddenVar, Var var) 
	{
		//		System.out.println(var);
		int indexOfHidden = var.indexOf(hiddenVar);
		boolean isRowCalculated[] = new boolean[var.getCPT().size()];
		ArrayList<String[]> varCPT = var.getCPT();
		Var newVar = initNewVarSum(var, hiddenVar);
		//		System.out.println("new var: " + newVar);

		for (int i = 0; i < varCPT.size(); i++)
		{
			if (!isRowCalculated[i])
			{
				isRowCalculated[i] = true;
				String currentRow[] = varCPT.get(i);
				double sumRows = Double.parseDouble(currentRow[currentRow.length - 1]);
				for (int j = i + 1; j < varCPT.size(); j++)
				{
					if (!isRowCalculated[j])
					{
						String otherRow[] = varCPT.get(j);
						boolean isMatch = true;
						for (int k = 0; k < currentRow.length - 1 && isMatch; k++)
						{
							if (k != indexOfHidden)
							{
								if (!currentRow[k].equals(otherRow[k]))
									isMatch = false;
							}
						}

						if (isMatch)
						{
							isRowCalculated[j] = true;
							sumRows += Double.parseDouble(otherRow[otherRow.length - 1]);
							mSumCount++;
						}
					}
				}
				newVar.addEmptyRow();
				int col = 0;
				for (int l = 0; l < currentRow.length - 1; l++)
				{
					if (l != indexOfHidden)
						newVar.fillRow(currentRow[l], col++);
				}
				newVar.fillRow("" + sumRows, col);
			}
		}

		//		System.out.println(newVar);
		return newVar;
	}

	private Var initNewVarSum(Var var, String hiddenVar) 
	{
		ArrayList<String> tempVars = new ArrayList<String>();
		for (String parent : var.getParents())
		{
			if (!parent.equals(hiddenVar))
			{
				tempVars.add(parent);
			}
		}

		if (!var.getName().equals(hiddenVar))
			tempVars.add(var.getName());

		System.out.println("varNewSum " + var + " hidden: "  + hiddenVar);
		System.out.println(tempVars);
		Var newVar = new Var(tempVars.get(tempVars.size() - 1));
		for (int i = 0; i < tempVars.size() - 1; i++)
			newVar.addParents(tempVars.get(i));

		return newVar;
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
