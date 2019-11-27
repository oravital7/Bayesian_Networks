import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import javafx.scene.Parent;

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
			exportResultToFile(result);

			sc.close();
		} catch (FileNotFoundException e) {
			System.out.println("Unable read '"+ inputPath + "' , please make sure file exists!");
		} catch (IOException e) {
			System.out.println("Unable write output file!");
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Something get wrong");
			e.printStackTrace();
		}
	}

	/* ***************************************************
	 ***************** Private Methods *****************
	 *************************************************** */
	
	private void exportResultToFile(String result) throws IOException 
	{
		Files.write( Paths.get("output.txt"), result.getBytes());
	}
	
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
}
