import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

public class ex1 {
	public static void main(String[] args) {
		new ex1().start("input.txt");		
	}

	private HashMap<String, Var> mNetWork;

	public void start(String inputPath) 
	{
		mNetWork = new HashMap<String, Var>();
		BayesBall baseBallAlg = new BayesBall(mNetWork);
		VariableElimination variableEliminationAlg = new VariableElimination(mNetWork);
		
		File fileInput = new File(inputPath);
		Scanner sc;
		try {
			sc = new Scanner(fileInput);
			sc.nextLine();

			String lineInput = sc.nextLine();
			buildVars(lineInput.substring(lineInput.indexOf(' ') + 1));
			buildVarProperties(sc);
			String result = startAnswer(sc, baseBallAlg, variableEliminationAlg);

			sc.close();
		} catch (FileNotFoundException e) {
			System.out.println("Unable read '"+ inputPath + "' , please make sure file exists!");
		} catch (Exception e) {
			System.out.println("Something get wrong");
			e.printStackTrace();
		}
		printNet();
	}

	/* ***************************************************
	 ***************** Private Methods *****************
	 *************************************************** */

	private String startAnswer(Scanner sc, BayesBall baseBallAlg, VariableElimination variableEliminationAlg) 
	{
		String result = "", query = "";
		while(sc.hasNextLine())
		{
			query = sc.nextLine();
			if (query.charAt(0) == 'P' || query.charAt(0) == 'p')
				result += variableEliminationAlg.getQueryResult(query);
			else
				result += baseBallAlg.getQueryResult(query);

			if (sc.hasNextLine())
				result += '\n';
		}
		System.out.println(result);
		return result;
	}

	private void buildVars(String lineInput) 
	{
		for (String var : lineInput.split(","))
			mNetWork.put(var, new Var(var));
	}

	private void buildVarProperties(Scanner sc) 
	{
		String inputLine = "";

		while (!inputLine.contains("Queries"))
		{
			inputLine = sc.nextLine();
			if (inputLine.contains("Var"))
			{
				Var var = mNetWork.get(inputLine.split(" ")[1]);
				var.addValues(sc.nextLine().split(" ")[1]);
				addParents(sc.nextLine().split(" ")[1], var);
				sc.nextLine();

				while (!inputLine.isEmpty())
				{
					inputLine = sc.nextLine();
					var.addCPT(inputLine);
				}
			}
		}
	}

	private void addParents(String parents, Var var) 
	{
		if (!parents.equals("none"))
		{
			for (String parentVar : parents.split(","))
			{
				var.addParents(parentVar);
				mNetWork.get(parentVar).addChild(var.getName());
			}
		}
	}

	// For test! Delete it!
	private void printNet()
	{
		for(Var v : mNetWork.values())
		{
			System.out.println(v);
		}
	}
}

class Var {

	private ArrayList<String> mParents, mChilds, mValues;
	private ArrayList<String[]> mCPT;
	private String mName;
	private int mCurrentRow;
	boolean mShadeFlag;

	public Var(String name)
	{
		mName = name;
		mParents = new ArrayList<String>();
		mChilds = new ArrayList<String>();
		mValues = new ArrayList<String>();
		mCPT = new ArrayList<String[]>();
		mCurrentRow = 0;
		mShadeFlag = false;
	}
	
	public Var(Var var)
	{
		mName = var.mName;
		mParents = var.mParents;
		mChilds = var.mChilds;
		mValues = var.mValues;
		mCPT = new ArrayList<String[]>(var.mCPT);
		mCurrentRow = var.mCurrentRow;
		mShadeFlag = var.mShadeFlag;
	}

	public void addValues(String values) 
	{
		for (String value : values.split(","))
			mValues.add(value);
	}

	public void addParents(String parent) 
	{ 
		mParents.add(parent); 
	}

	public void addCPT(String value)
	{
		if (value.length() == 0)
			return;

		ArrayList<String> completeValue = new ArrayList<String>(mValues);
		double completeValueProb = 0;
		
		String valueArr[] = value.split(",");

		String tempEvidences[] = new String[mParents.size()];
		int col = 0;

		for (int i = 0; i < valueArr.length; i++)
		{
			if (i < mParents.size())
			{
				tempEvidences[i] = valueArr[i];
			}
			else if (valueArr[i].contains("="))
			{
				mCPT.add(new String[mParents.size() + 2]);
				for (String evidence : tempEvidences)
					mCPT.get(mCurrentRow)[col++] = evidence;

				mCPT.get(mCurrentRow)[col++] = valueArr[i].substring(1);
				completeValue.remove(valueArr[i].substring(1));
			}
			else
			{
				mCPT.get(mCurrentRow++)[col] = valueArr[i];
				completeValueProb += Double.parseDouble(valueArr[i]);
				col = 0;
			}
		}
		mCPT.add(new String[mParents.size() + 2]);
		for (String evidence : tempEvidences)
			mCPT.get(mCurrentRow)[col++] = evidence;
		
		mCPT.get(mCurrentRow)[col++] = completeValue.get(0);
		mCPT.get(mCurrentRow++)[col] = "" + (1 - completeValueProb);

	}

	public void addChild(String child) { mChilds.add(child); }

	public String getName() { return mName; }

	public ArrayList<String> getParents() { return mParents; }

	public ArrayList<String> getChilds() { return mChilds; }
	
	public ArrayList<String[]> getCPT() { return mCPT; }


	public int NumberOfValues()	{ return mValues.size(); }
	
	@Override
	public String toString() 
	{
		String result = "Var [mName=" + mName + ", mParents=" + mParents + ", mChilds=" + mChilds + ", mValues=" + mValues +
				", mCPT= " + mCPT.size() + ", mShadeFlag:"  + mShadeFlag + "]\nCPT: \n";
		
		for (String s[] : mCPT)
			result += Arrays.toString(s) + '\n';

		return result;
	}
}
