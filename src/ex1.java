import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Scanner;

public class Ex1 {
	public static void main(String[] args) {
		new Ex1().start("input.txt");
	}

	/* ****************** Member class ******************** */

	private HashMap<String, Var> mNetWork;

	/* ***************************************************
	 ***************** Privates Methods *****************
	 *************************************************** */

	public void start(String inputPath) 
	{
		mNetWork = new HashMap<String, Var>();
		BayesBall baseBallAlg = new BayesBall(mNetWork);
		VariableElimination variableEliminationAlg = new VariableElimination(mNetWork);

		File fileInput = new File(inputPath);
		try {
			Scanner sc = new Scanner(fileInput);

			buildVars(sc);
			buildVarProperties(sc);
			String result = startAnswer(sc, baseBallAlg, variableEliminationAlg);
			exportResultToFile(result);

			sc.close();
		} catch (FileNotFoundException e) {
			System.out.println("Unable read '"+ inputPath + "' , please make sure file exists!");
		} catch (IOException e) {
			System.out.println("Unable write output file!");
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Something got wrong");
			e.printStackTrace();
		}
	}

	/* ***************************************************
	 ***************** Private Methods *****************
	 *************************************************** */

	private void exportResultToFile(String result) throws IOException 
	{
		Files.write(Paths.get("output.txt"), result.getBytes());
	}

	private String startAnswer(Scanner sc, BayesBall baseBallAlg, VariableElimination variableEliminationAlg) 
	{
		String result = "", query;
		while(sc.hasNextLine())
		{
			query = sc.nextLine();
			if (query.toLowerCase().charAt(0) == 'p' && query.charAt(1) == '(')
				result += variableEliminationAlg.getQueryResult(query);
			else
				result += baseBallAlg.getQueryResult(query);

			if (sc.hasNextLine())
				result += '\n';
		}

		return result;
	}

	private void buildVars(Scanner sc) 
	{
		String lineInput = "";
		while (!lineInput.toLowerCase().contains("variables"))
			lineInput = sc.nextLine();

		lineInput = lineInput.replaceAll(" ", "").substring(lineInput.indexOf(':') + 1);

		for (String var : lineInput.split(","))
			mNetWork.put(var, new Var(var));
	}

	private void buildVarProperties(Scanner sc) 
	{
		String inputLine = "";

		while (!inputLine.toLowerCase().contains("queries"))
		{
			inputLine = sc.nextLine();
			if (inputLine.contains("Var"))
			{
				Var var = mNetWork.get(inputLine.split(" ")[1]);
				var.addValues(sc.nextLine().split(" ")[1]);
				addParents(sc.nextLine().split(" ")[1], var);

				while (!inputLine.toLowerCase().contains("cpt"))
					inputLine = sc.nextLine();

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
}
