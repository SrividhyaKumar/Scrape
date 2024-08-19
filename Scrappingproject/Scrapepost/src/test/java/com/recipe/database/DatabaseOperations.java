package com.recipe.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.hooks.Hooks;
import com.recipe.vos.RecipeVo;

public class DatabaseOperations {

	static Connection connection; 

	public static final String tablePrefix="FinalList_For_";
	public static final String MasterTableName="MasterTable1";


	public static void main(String[] args) {

		dropAlltableContent("LFV");
		dropAlltableContent("LCHFE");
	}

	private static void dropAlltableContent(String filterName)
	{

		String query="DROP TABLE IF EXISTS public.<TABLE_NAME>";

		DatabaseOperations.dropTable(query.replace("<TABLE_NAME>","\"AlreadyCheckedRecipes_"+filterName+"\""));
		DatabaseOperations.dropTable(query.replace("<TABLE_NAME>",tablePrefix+filterName+"_elimination"));
		try {
			DatabaseOperations.dropTable(query.replace("<TABLE_NAME>",tablePrefix+filterName+"_to_add"));
		} catch (Exception e) {
		}

		for (String singleAllergyTerm : Hooks.allergies) {

			DatabaseOperations.dropTable(query.replace("<TABLE_NAME>", filterName+"_Allergy_"+singleAllergyTerm.replaceAll(" ", "_")));
		}
	}

	public static void dropTable(String query)
	{
		try {
			Statement statement = getConn().createStatement();
			statement.execute(query);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static void createTable(String query)
	{
		try {
			Statement statement = getConn().createStatement();
			statement.execute(query);
		} catch (Exception e) {
			e.printStackTrace();
		}


	}
	//filter == LFV, LCHFE ...

	public static List<Integer> getAlreadyCheckedRecipeIds(String filter) 
	{
		List<Integer> savedIds=new ArrayList<Integer>();
		try 
		{
			Statement statement = getConn().createStatement();

			ResultSet resultSet = statement.executeQuery("SELECT \"Recipe_Id\" "
					+ "	FROM public.\"AlreadyCheckedRecipes_"+filter+"\";");

			while (resultSet.next())
			{
				savedIds.add(resultSet.getInt("Recipe_Id"));
			}

			System.out.println("got "+savedIds.size()+" already checked ids");

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("got already checked recipes count for "+filter+ ": "+savedIds.size());
		return savedIds;

	}


	public static void insertCheckedRecipeId(int recId,String filter) {

		try {
			PreparedStatement ps = null;
			String	sql = "INSERT INTO public.\"AlreadyCheckedRecipes_"+filter+"\"("
					+ "	\"Recipe_Id\")"
					+ "	VALUES (?);";

			ps = getConn().prepareStatement(sql);

			ps.setInt(1, recId);

			ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	
	public static void insertRecipe(RecipeVo recipeVo,String tableName) {


		try {
			PreparedStatement ps = null;
			String sql = "INSERT INTO "+tableName+"("
					+ "	\"Recipe_ID\", \"Recipe_Name\", \"Recipe_Category\", \"Food_Category\", \"Ingredients\", \"Preparation_Time\", "
					+ " \"Cooking_Time\", \"Tag\", \"No_of_servings\", "
					+ "\"Cuisine_category\", \"Recipe_Description\", \"Preparation_method\", \"Nutrient_values\", \"Recipe_URL\")\n"
					+ "	VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

			ps = getConn().prepareStatement(sql);

			int recId=Integer.parseInt(recipeVo.getRecipe_ID().replaceAll("Recipe#", "").replaceAll("Recipe #", "").trim());

			int columnCounter=1;

			ps.setInt(columnCounter++, recId);
			ps.setString(columnCounter++, recipeVo.getRecipe_Name());
			ps.setString(columnCounter++, StringUtils.join( recipeVo.getRecipe_Category()));
			ps.setString(columnCounter++, recipeVo.getFood_Category());
			ps.setString(columnCounter++, StringUtils.join( recipeVo.getIngredients()));

			ps.setString(columnCounter++, recipeVo.getPreparation_Time());
			ps.setString(columnCounter++, recipeVo.getCooking_Time());
			ps.setString(columnCounter++, StringUtils.join(recipeVo.getTags()));
			ps.setString(columnCounter++, recipeVo.getNo_of_servings());
			ps.setString(columnCounter++, recipeVo.getCuisine_category());

			ps.setString(columnCounter++, recipeVo.getRecipe_Description());
			ps.setString(columnCounter++, StringUtils.join(recipeVo.getPreparation_method()));
			ps.setString(columnCounter++, StringUtils.join(recipeVo.getNutrient_values()));
			ps.setString(columnCounter++, recipeVo.getRecipe_URL());

			ps.executeUpdate();


		} catch (Exception e) 
		{
			System.out.println("Error while inserting / duplicate !");
		}
	}


	public static Connection getConn() throws Exception {

		if(connection==null)
		{
			String jdbcUrl = "jdbc:postgresql://localhost:5433/team4_hackathon";
			String username = "postgres";
			String password = "AshwiniDBTesting";

			Class.forName("org.postgresql.Driver");

			connection = DriverManager.getConnection(jdbcUrl, username, password);
		}

		return connection;
	}

	public static void printRowCounts(String filter) {

		String qry=null;

		System.out.println("\nPrinting row counts for filter "+filter);

		try 
		{
			Statement statement = getConn().createStatement();


			qry="select count(*) as ct from <TABLE_NAME>";
			ResultSet resultSet = statement.executeQuery(qry.replace("<TABLE_NAME>", tablePrefix+filter+"_elimination"));

			while (resultSet.next())
			{
				System.out.println(tablePrefix+filter+"elimination count : "+resultSet.getInt("ct"));
			}

			if(filter.equalsIgnoreCase("lfv"))
			{
				resultSet = statement.executeQuery(qry.replace("<TABLE_NAME>", tablePrefix+filter+"_to_add"));

				while (resultSet.next())
				{
					System.out.println("lfv_to_add count : "+resultSet.getInt("ct"));
				}
			}

			for (String singleAllergyTerm : Hooks.allergies) 
			{

				resultSet = statement.executeQuery(qry.replace("<TABLE_NAME>",  filter+"_Allergy_"+singleAllergyTerm.replaceAll(" ", "_")));

				while (resultSet.next())
				{
					System.out.println(filter+"_Allergy_"+singleAllergyTerm.replaceAll(" ", "_")+" count : "+resultSet.getInt("ct"));
				}
			}


		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
