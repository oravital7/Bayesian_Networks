import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * This class represent variable in the given network
 * @author oravi
 *
 */
public class Var {

	private ArrayList<String> mParents, mChilds, mValues;
	private ArrayList<String[]> mCPT;
	private HashMap<String, Integer> mCachedIndex;
	private String mName;
	boolean mShadeFlag;

	/* ********* Constructors ********* */
	
	public Var(String name)
	{
		mName = name;
		mParents = new ArrayList<String>();
		mChilds = new ArrayList<String>();
		mValues = new ArrayList<String>();
		mCPT = new ArrayList<String[]>();
		mCachedIndex = new HashMap<String, Integer>();
		mShadeFlag = false;
	}

	public Var(Var var)
	{
		mName = var.mName;
		mParents = var.mParents;
		mChilds = var.mChilds;
		mValues = var.mValues;
		mCPT = new ArrayList<String[]>(var.mCPT);
		mCachedIndex = new HashMap<String, Integer>();
		mShadeFlag = var.mShadeFlag;
	}
	
	/* *********** Public methods *********** */

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

		ArrayList<String> completeValue = new ArrayList<String>(mValues); // Save result for complete row  (1 - completeValueProb)
		double completeValueProb = 0; // (1 - P)

		String valueArr[] = value.split(",");

		String tempEvidences[] = new String[mParents.size()];
		int col = 0, currentRow = mCPT.size() - 1;

		for (int i = 0; i < valueArr.length; i++)
		{
			if (i < mParents.size())
			{
				tempEvidences[i] = valueArr[i];
			}
			else if (valueArr[i].contains("="))
			{
				mCPT.add(new String[mParents.size() + 2]);
				currentRow++;
				for (String evidence : tempEvidences)
					mCPT.get(currentRow)[col++] = evidence;

				mCPT.get(currentRow)[col++] = valueArr[i].substring(1);
				completeValue.remove(valueArr[i].substring(1));
			}
			else
			{
				mCPT.get(currentRow)[col] = valueArr[i];
				completeValueProb += Double.parseDouble(valueArr[i]);
				col = 0;
			}
		}

		mCPT.add(new String[mParents.size() + 2]);
		currentRow++;

		for (String evidence : tempEvidences)
			mCPT.get(currentRow)[col++] = evidence;

		mCPT.get(currentRow)[col++] = completeValue.get(0);
		mCPT.get(currentRow)[col] = "" + (1 - completeValueProb);
	}

	/**
	 *
	 * @param var
	 * @return column index of specific var by name that exist in the CPT Array
	 */
	public int indexOf(String var)
	{
		Integer result = mCachedIndex.get(var);
		if (result != null)
			return mCachedIndex.get(var);

		if (var.equals(mName))
			result = mParents.size();
		else
			result = mParents.indexOf(var);

		mCachedIndex.put(var, result);
		return result;
	}

	public void fillRow(String value, int col)
	{
		mCPT.get(mCPT.size() - 1)[col] = value;
	}

	public void addEmptyRow()
	{
		mCPT.add(new String[mParents.size() + 2]);
	}

	public void addChild(String child) { mChilds.add(child); }

	/* ******************* Getters ******************* */

	public String getName() { return mName; }

	public ArrayList<String> getParents() { return mParents; }

	public ArrayList<String> getChilds() { return mChilds; }

	public ArrayList<String[]> getCPT() { return mCPT; }

	public int getNumberOfValues()	{ return mValues.size(); }

	@Override
	public String toString() 
	{
		String result = "Var [mName=" + mName + ", mParents=" + mParents + ", mChilds=" + mChilds + ", mValues=" + mValues +
				", mCPT= " + mCPT.size() + ", mShadeFlag:"  + mShadeFlag + "]\nCPT:";

		for (String s[] : mCPT)
			result += '\n' + Arrays.toString(s);

		return result;
	}
}
