import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class ex1 {
	public static void main(String[] args) {
		new ex1().start();		
	}

	private HashMap<String, Var> mNetWork;
	private BayesBall mbaseBallAlg;

	public ex1() 
	{
		mNetWork = new HashMap<String, Var>();
		mbaseBallAlg = new BayesBall(mNetWork);
	}

	public void start() 
	{
		File input = new File("input.txt");

		try {
			Scanner sc = new Scanner(input);
			sc.nextLine();

			String lineInput = sc.nextLine();
			buildVariables(lineInput.substring(lineInput.indexOf(' ') + 1));
			buildVar(sc);
			String result = startAnswer(sc);
			sc.close();
		} catch (FileNotFoundException e) {
			System.out.println("Unable read 'input.txt', please make sure file exists!");
		}
		printNet();
	}

	private String startAnswer(Scanner sc) 
	{
		String result = "", line = "";
		while(sc.hasNextLine())
		{
			line = sc.nextLine();
			if (line.charAt(0) == 'P' || line.charAt(0) == 'p')
				System.out.println("line: " + line);
			else
				result += mbaseBallAlg.startCalculate(line);
			
			if (sc.hasNextLine())
				result += '\n';
		}
		System.out.println(result);
		return result;
	}

	private void buildVariables(String lineInput) 
	{
		for (String var : lineInput.split(","))
		{
			mNetWork.put(var, new Var(var));
		}
	}

	private void buildVar(Scanner sc) 
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
				initCPT(var);
				while (!inputLine.isEmpty())
				{
					inputLine = sc.nextLine();
					var.addCPT(inputLine);
				}
			}
		}
	}

	private void initCPT(Var var) 
	{
		int rows = 1;
		for (String varParent : var.getParents())
		{
			rows *= mNetWork.get(varParent).NumberOfValues(); 
		}
		var.initCPT(rows * (var.NumberOfValues() - 1));
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
	private String mName, mCPT[][];
	private int mCurrentRow;
	boolean mShadeFlag;

	public Var(String name)
	{
		mName = name;
		mParents = new ArrayList<String>();
		mChilds = new ArrayList<String>();
		mValues = new ArrayList<String>();
		mCurrentRow = 0;
		mShadeFlag = false;
	}

	public void initCPT(int rows) 
	{
		mCPT = new String[rows][mParents.size() + 2];
	}

	public void addValues(String values) 
	{
		for (String value : values.split(","))
		{
			mValues.add(value);
		}
	}

	public void addParents(String parent) 
	{
		mParents.add(parent);
	}

	//	public void addCPT(String value) 
	//	{
	//		if (value.length() == 0)
	//			return;
	//
	//		String valueArr[] = value.split(",");
	//		ArrayList<String> temp = new ArrayList<String>();
	//		int col = 0;
	//		boolean foundFirstEqual = false;
	//		String val = "";
	//		for (int i = 0; i < valueArr.length; i++)
	//		{
	//			if (!foundFirstEqual)
	//			{
	//				if (valueArr[i].contains("="))
	//				{
	//					foundFirstEqual = true;
	//					val = valueArr[i].substring(1);
	//				}
	//				else
	//				{
	//					val = valueArr[i];
	//					temp.add(val);
	//				}
	//
	//			}
	//			else if (valueArr[i].contains("="))
	//			{
	//				mCurrentRow++; col = 0;
	//				for (int j = 0; j < temp.size(); j++)
	//					mCPT[mCurrentRow][col++] = temp.get(j);
	//
	//				val = valueArr[i].substring(1);
	//			}
	//			else
	//			{
	//				val  = valueArr[i];
	//			}
	//
	//			mCPT[mCurrentRow][col++] = val;
	//		}
	//		mCurrentRow++;
	//	}

	public void addCPT(String value)
	{
		if (value.length() == 0)
			return;

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
				for (String evidence : tempEvidences)
					mCPT[mCurrentRow][col++] = evidence;

				mCPT[mCurrentRow][col++] = valueArr[i].substring(1);
			}
			else
			{
				mCPT[mCurrentRow++][col] = valueArr[i];
				col = 0;
			}
		}
	}

	public void addChild(String child) { mChilds.add(child); }

	public String getName() { return mName; }

	public ArrayList<String> getParents() { return mParents; }
	
	public ArrayList<String> getChilds() { return mChilds; }

	public int NumberOfValues()	{ return mValues.size(); }

	@Override
	public String toString() 
	{
		return "Var [mName=" + mName + ", mParents=" + mParents + ", mChilds=" + mChilds + ", mValues=" + mValues +
				", mCPT= rows: " + mCPT.length + " cols: " + mCPT[0].length + " mShadeFlag: " + mShadeFlag + "]";
	}

}
