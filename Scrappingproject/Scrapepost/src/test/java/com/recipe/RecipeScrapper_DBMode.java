package com.recipe;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import com.recipe.database.DatabaseOperations;
import com.recipe.vos.FilterVo;
import com.recipe.vos.RecipeVo;

public class RecipeScrapper_DBMode {

	public void srapRecipes ( FilterVo filterVo)  
	{

		try {
			List<Integer> alreadySaved = filterVo.getAlreadySaved();

			Connection conn= DatabaseOperations.getConn();

			Statement statement =conn.createStatement();

			ResultSet resultSet = statement.executeQuery("SELECT *	FROM public."+DatabaseOperations.MasterTableName+";");

			while (resultSet.next())
			{
				try {
					RecipeVo sinleRecipeOutput = getRecipeObj(resultSet);

					if(alreadySaved.contains(Integer.parseInt(sinleRecipeOutput.getRecipe_ID())))
					{
						//System.out.println("Already processed : "+sinleRecipeOutput.getRecipe_ID());
						continue;
					}
					else
					{
						DatabaseOperations.insertCheckedRecipeId(Integer.parseInt(sinleRecipeOutput.getRecipe_ID()),filterVo.getFilterName());
					}

					System.out.println("\n_________________________Checking Recipe# "+sinleRecipeOutput.getRecipe_ID()+"_______________________________");

					Library.coreLogic(sinleRecipeOutput, filterVo);


				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private RecipeVo getRecipeObj(ResultSet resultSet) {


		try {
			RecipeVo out=new RecipeVo();


			out.setCooking_Time(resultSet.getString("Cooking_Time"));

			out.setCuisine_category(resultSet.getString("Cuisine_category"));

			out.setRecipe_ID(resultSet.getString("Recipe_ID"));

			out.setRecipe_Name(resultSet.getString("Recipe_Name"));


			out.setNo_of_servings(resultSet.getString("No_of_servings"));

			out.setFood_Category(resultSet.getString("Food_Category"));

			out.setPreparation_Time(resultSet.getString("Preparation_Time"));

			out.setCooking_Time(resultSet.getString("Cooking_Time"));

			out.setRecipe_URL(resultSet.getString("Recipe_URL"));


			if(resultSet.getString("Nutrient_values")!=null)
				out.setNutrient_values(Arrays.asList(resultSet.getString("Nutrient_values").replaceAll("\\[", "").replaceAll("\\]", "").split(",")));

			//			if(resultSet.getString("Recipe_Category")!=null)
			//				out.setRecipe_Category(Arrays.asList(resultSet.getString("Recipe_Category").split(",")));

			if(resultSet.getString("Preparation_method")!=null)
				out.setPreparation_method(Arrays.asList(resultSet.getString("Preparation_method").replaceAll("\\[", "").replaceAll("\\]", "").split(",")));

			if(resultSet.getString("Tag")!=null && !resultSet.getString("Tag").isEmpty())
			{
				try {
					String tag=resultSet.getString("Tag");
					tag=tag.replaceAll("\\[", "").replaceAll("\\]", "");

					out.setTags(Arrays.asList(tag.split(",")));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			out.setRecipe_Description(resultSet.getString("Recipe_Description"));

			if(resultSet.getString("Ingredients")!=null)
				out.setIngredients(Arrays.asList(resultSet.getString("Ingredients").replaceAll("\\[", "").replaceAll("\\]", "").split(",")));


			if(resultSet.getString("PlainIngrList")!=null)
				out.setPlainIngredientsList(Arrays.asList(resultSet.getString("PlainIngrList").replaceAll("\\[", "").replaceAll("\\]", "").split(",")));


			return out;
		} catch (Exception e) {

			e.printStackTrace();

			return null;

		}
	}
}
