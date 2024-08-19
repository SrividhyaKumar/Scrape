package com.hooks;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.testng.annotations.BeforeSuite;

import com.recipe.RecipeScrapper;
import com.recipe.RecipeScrapper_DBMode;
import com.recipe.database.DatabaseOperations;
import com.recipe.vos.FilterVo;
import com.utilities.ConfigReader;
import com.utilities.ExcelReader;

import log.LoggerLoad;


public class Hooks {

	public static Map<String,String> ExceptionIngredientMapping= new HashMap<String,String>();

	public static ConfigReader configreader;

	Properties prop;
	
	public static List<String> allergies=Arrays.asList("Milk","Soy","Egg","Sesame","Peanuts","Walnut","Almond","Hazelnut","Pecan","Cashew","Pistachio","Shell fish","Seafood");
	//providing allergy list to create its respective table

	@BeforeSuite(alwaysRun = true)
	public void beforeSuiteWork()
	{
		LoggerLoad.info("Inside beforeSuiteWork.....");

		configreader=new ConfigReader();

		try {
			prop=configreader.loadConfig();
		} catch (Throwable e) {

			e.printStackTrace();
		}

		//------------------------------------------------------------------------LFV filter----------------------------------------------------------------------

		String file="/Users/ashwini/git/team_4scrappinghackers/src/test/resources/data/IngredientsAndComorbidities-ScrapperHackathon_New_Updated.xlsx";
		String sheet="Final list for LFV Elimination ";
		String LCHFsheet="Final list for LCHFElimination ";


		Integer toAddCol=2;
		Integer avoidTermCol=3;

		FilterVo filterVo_LFV= ExcelReader.read(file, sheet,toAddCol,avoidTermCol);

		LoggerLoad.info("LFV Filter.....");
		System.out.println("LFV eliminate ingr -->"+filterVo_LFV.getLstEliminate());
		System.out.println("LFV add ingr -->" +filterVo_LFV.getLstAdd());
		System.out.println("LFV avoid receipe -->"+filterVo_LFV.getRecipeToAvoid());
		//System.out.println("LFV optional -->" +filterVo.getOptinalRecipes());

		filterVo_LFV.getLstAdd().add("tea");
		filterVo_LFV.getLstAdd().add("coffee");
		filterVo_LFV.getLstAdd().add("herbal drink");
		filterVo_LFV.getLstAdd().add("chai");

		filterVo_LFV.setFilterName("LFV");

		//--------------------------------------------------------------------------LCHFE filter---------------------------------------------------------------------

		toAddCol=null;
		avoidTermCol=2;

		FilterVo filterVo_LCHFE= ExcelReader.read(file,LCHFsheet,toAddCol,avoidTermCol);
		System.out.println("LCHFE Filter.....");
		System.out.println("LCHFE eliminate ingr -->"+filterVo_LCHFE.getLstEliminate());
		System.out.println("LCHFE add ingr -->" +filterVo_LCHFE.getLstAdd());
		System.out.println("LCHFE avoid receipe -->"+filterVo_LCHFE.getRecipeToAvoid());
		filterVo_LCHFE.setFilterName("LCHFE");

		//-------------------------------------------------------------decide which filter to apply LFV, or LCHFE or.... ------------------------------------------------------

		//to mention in Lower case
		ExceptionIngredientMapping.put("pea", "chick pea");
		ExceptionIngredientMapping.put("potato", "sweet potato");
		//ExceptionIngredientMapping.put("curd", "tofu bean curd soya paneer");
		ExceptionIngredientMapping.put("mango", "mango powder amchur");
		RecipeScrapper.LFV_FILTER=filterVo_LFV;
		RecipeScrapper.LCHFE_FILTER=filterVo_LCHFE;

		String[] foodProcesses = new String[] {"Raw", "Steamed", "Boiled", "Porched", "Sauted", "Airfryed", "Pan fried"};

		filterVo_LCHFE.setLstAddfoodprocess(Arrays.asList(foodProcesses));

		filterVo_LCHFE.getRecipeToAvoid().add("Processed");

		prepareDatabase(filterVo_LFV.getFilterName());
		prepareDatabase(filterVo_LCHFE.getFilterName());

		filterVo_LFV.setAlreadySaved( DatabaseOperations.getAlreadyCheckedRecipeIds(filterVo_LFV.getFilterName()));
		filterVo_LCHFE.setAlreadySaved( DatabaseOperations.getAlreadyCheckedRecipeIds(filterVo_LCHFE.getFilterName()));

		
		//comment below 2 lines when running from web mode === testng.xml
		//else leave uncommented and run thr testngDB.xml
		new RecipeScrapper_DBMode().srapRecipes(filterVo_LFV);
		new RecipeScrapper_DBMode().srapRecipes(filterVo_LCHFE);


		DatabaseOperations.printRowCounts(filterVo_LFV.getFilterName());
		DatabaseOperations.printRowCounts(filterVo_LCHFE.getFilterName());


	}

	private void prepareDatabase(String filterName) {


		String query="CREATE TABLE  IF NOT EXISTS public.<TABLE_NAME>"
				+ "("
				+ "  "
				+ "    \"Recipe_ID\" integer NOT NULL,"
				+ "    \"Recipe_Name\" text NOT NULL,"
				+ "    \"Recipe_Category\" text,"
				+ "    \"Food_Category\" text,"
				+ "    \"Ingredients\" text,"
				+ "    \"Preparation_Time\" text,"
				+ "    \"Cooking_Time\" text,"
				+ "    \"Tag\" text,"
				+ "    \"No_of_servings\" text,"
				+ "    \"Cuisine_category\" text,"
				+ "    \"Recipe_Description\" text,"
				+ "    \"Preparation_method\" text,"
				+ "    \"Nutrient_values\" text,"
				+ "    \"Recipe_URL\" text,"
				+ "    PRIMARY KEY (\"Recipe_ID\")"
				+ ");";
		
		
		String mastertableQuery="CREATE TABLE  IF NOT EXISTS public."+DatabaseOperations.MasterTableName
				+ "("
				+ "  "
				+ "    \"Recipe_ID\" integer NOT NULL,"
				+ "    \"Recipe_Name\" text NOT NULL,"
				+ "    \"Recipe_Category\" text,"
				+ "    \"Food_Category\" text,"
				+ "    \"Ingredients\" text,"
				+ "    \"Preparation_Time\" text,"
				+ "    \"Cooking_Time\" text,"
				+ "    \"Tag\" text,"
				+ "    \"No_of_servings\" text,"
				+ "    \"Cuisine_category\" text,"
				+ "    \"Recipe_Description\" text,"
				+ "    \"Preparation_method\" text,"
				+ "    \"Nutrient_values\" text,"
				+ "    \"Recipe_URL\" text,"
				+ "    \"PlainIngrList\" text,"
				+ "    PRIMARY KEY (\"Recipe_ID\")"
				+ ");";
		
		
		DatabaseOperations.createTable(mastertableQuery);


		DatabaseOperations.createTable("CREATE TABLE IF NOT EXISTS public.\"AlreadyCheckedRecipes_"+filterName+"\""
				+ "("
				+ "    \"Recipe_Id\" integer NOT NULL,"
				+ "    CONSTRAINT \"AlreadyCheckedRecipes_"+filterName+"_pkey\" PRIMARY KEY (\"Recipe_Id\")"
				+ ")"
				+ "");

		DatabaseOperations.createTable(query.replace("<TABLE_NAME>",DatabaseOperations.tablePrefix+ filterName+"_elimination"));

		if(filterName.equalsIgnoreCase("lfv"))
			DatabaseOperations.createTable(query.replace("<TABLE_NAME>",DatabaseOperations.tablePrefix+filterName+"_to_add"));


		for (String singleAllergyTerm : allergies) {

			DatabaseOperations.createTable(query.replace("<TABLE_NAME>", filterName+"_Allergy_"+singleAllergyTerm.replaceAll(" ", "_")));
		}
	}

	

}