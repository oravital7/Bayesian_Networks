import java.util.ArrayList;
import java.util.HashMap;


public class BayesBall {

	private HashMap<String, Var> mNetWork;

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
		String evidence[] = temp[1].split(",");
		for (String val : evidence)
		{
			evidenceNodes.add(val.substring(0, val.indexOf('=')));
		}

		shadesOrResetNet(evidenceNodes, true);

		return null;
	}

	private void shadesOrResetNet(ArrayList<String> evidenceNodes, boolean shadeReset) {
		for (String node : evidenceNodes)
			mNetWork.get(node).mShadeFlag = shadeReset;
	}

}
