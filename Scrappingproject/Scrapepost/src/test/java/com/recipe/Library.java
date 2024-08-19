package com.recipe;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.hooks.Hooks;
import com.recipe.database.DatabaseOperations;
import com.recipe.vos.FilterVo;
import com.recipe.vos.RecipeDetailsLocatorsVo;
import com.recipe.vos.RecipeVo;

public class Library {

	public static void coreLogic(RecipeVo sinleRecipeOutput, FilterVo filterVo)
	{
		System.out.println("\napplying core logic ... for "+filterVo.getFilterName());

		List<String> filtercompare;

		//Step 1 - check if recipe is NOT having eliminated ingredients
		if(! Library.isIngPresent(sinleRecipeOutput.getPlainIngredientsList(), filterVo.getLstEliminate(),filterVo.getFilterName()+" elimination"))
		{
			//Step 2 - check if recipe IS having 'add' ingredients
			if(Library.isIngPresent(sinleRecipeOutput.getPlainIngredientsList(), filterVo.getLstAdd(),filterVo.getFilterName()+" add"))
			{

				if(filterVo.getFilterName().equalsIgnoreCase("LFV"))
				{
					filtercompare=sinleRecipeOutput.getTags();	
				}
				else
				{
					filtercompare=sinleRecipeOutput.getPlainIngredientsList();
				}

				//Step 3 - check if recipe is NOT having avoiding terms
				if(! Library.isNeedToAvoidThisRecipe(filtercompare, filterVo.getRecipeToAvoid()))
				{		
					commonFlow(sinleRecipeOutput , filterVo);

					//					if(filterVo.getFilterName().equalsIgnoreCase("LFV") || 
					//							(filterVo.getFilterName().equalsIgnoreCase("LCHFE")
					//									&& Library.isIngPresent(sinleRecipeOutput.getTags(), filterVo.getLstAddfoodprocess(), filterVo.getFilterName()+" food process")))
					//					{
					//						//commonFlow===inserts into main elm table, checks and inserts into respective allergy table
					//						commonFlow(sinleRecipeOutput , filterVo);
					//					}
					//					else
					//					{
					//						System.out.println("[#4 ignore for "+filterVo.getFilterName()+" recipe] This recipe is Not having Food processing terms !" );	
					//					}
				}                   
				else
				{
					System.out.println("[#3 ignore for "+filterVo.getFilterName()+" recipe] This recipe is having avoiding terms !");
				}
			}
			else
			{
				if(filterVo.getFilterName().equalsIgnoreCase("LCHFE")
						&& Library.isIngPresent(sinleRecipeOutput.getTags(), filterVo.getLstAddfoodprocess(), filterVo.getFilterName()+" food process"))
				{
					commonFlow(sinleRecipeOutput , filterVo);
				}
				else
				{
					System.out.println("[#2 ignore for"+filterVo.getFilterName()+" recipe] This recipe is NOT having any required(add) igredients !");
				}
			}
		}
		else
		{
			System.out.println("[#1 ignore "+filterVo.getFilterName()+" recipe] This recipe is having eliminated igredients !");

			if(!filterVo.getTo_Add_If_notFullyVgean().isEmpty())
			{
				// Step 8
				//prepare new elimination criteria ~ regular elimination minus "to add"
				List<String> toAddEliminationNew= new ArrayList<String>();
				toAddEliminationNew.addAll(filterVo.getLstEliminate());  //e.g. butter can be there
				toAddEliminationNew.removeAll(filterVo.getTo_Add_If_notFullyVgean()); // removed e.g. butter

				// recipe is NOT having new elimination criteria
				if(! Library.isIngPresent(sinleRecipeOutput.getPlainIngredientsList(), toAddEliminationNew, " new elimination(toadd)"))
				{
					// recipe is having "to_add" ingredients
					if( Library.isIngPresent(sinleRecipeOutput.getPlainIngredientsList(), filterVo.getTo_Add_If_notFullyVgean()," to-add") &&
							! Library.isNeedToAvoidThisRecipe(sinleRecipeOutput.getTags(), filterVo.getRecipeToAvoid())	)
					{
						// Step 9 - if to_add ingredients present, insert into *_to_add table
						DatabaseOperations.insertRecipe(sinleRecipeOutput,DatabaseOperations.tablePrefix+ filterVo.getFilterName()+"_to_add");
					}
				}
			}
		}

	}

	private static void commonFlow(RecipeVo sinleRecipeOutput, FilterVo filterVo) {

		System.out.println("# Got required recipe/inserting in db  for "+filterVo.getFilterName());
		//Step 5 - insert required recipe in Elimination table
		DatabaseOperations.insertRecipe(sinleRecipeOutput,DatabaseOperations.tablePrefix+filterVo.getFilterName()+"_Elimination");

		//Step 6 -  get list(set) of matching allergic ingredients from recipe ingredients
		Set<String> matchingAllergicIngredients=getMatchingAllergiIngredients(sinleRecipeOutput.getPlainIngredientsList());

		//Step 7 - 'for each' allergic ingredient found, insert into respective allergy table
		if(!matchingAllergicIngredients.isEmpty())
		{
			System.out.println("Got matching allergic ingredients.."+matchingAllergicIngredients);
			for (String singleAllergicIngredient : matchingAllergicIngredients) {

				System.out.println("Inserting into "+filterVo.getFilterName()+"_Allergy_"+singleAllergicIngredient.replaceAll(" ", "_"));
				DatabaseOperations.insertRecipe(sinleRecipeOutput,filterVo.getFilterName()+"_Allergy_"+singleAllergicIngredient.replaceAll(" ", "_"));
			}
		}
		else
		{
			System.out.println("this recipe is not having any allergic ingredients for "+filterVo.getFilterName());
		}	 

	}

	private  static Set<String> getMatchingAllergiIngredients(List<String> plainIngredientsList) {

		Set<String> allergicIngr=new HashSet<String>();
		for (String plainIngr : plainIngredientsList) {

			for (String algIng :Hooks. allergies) {


				if(algIng.trim().equalsIgnoreCase(plainIngr.trim())||
						(plainIngr.trim()).equalsIgnoreCase(algIng.trim()+"s")||(plainIngr.trim()).equalsIgnoreCase(algIng.trim()+"es")||
						Arrays.asList(plainIngr.trim().split(" ")).contains(algIng.toLowerCase().trim())||
						Arrays.asList(plainIngr.trim().split(" ")).contains((algIng+"s").toLowerCase().trim())||
						Arrays.asList(plainIngr.trim().split(" ")).contains((algIng+"es").toLowerCase().trim()))
				{
					allergicIngr.add(algIng);
				}
			}
		}

		return allergicIngr;

	}

	/*----------------------------- Single Recipe --------------------------------------------*/
	public static RecipeVo getRecipeDetails(WebDriver driver) {

		RecipeVo sinleRecipeOutput=new RecipeVo();

		sinleRecipeOutput.setRecipe_Name(driver.findElement(By.xpath(RecipeDetailsLocatorsVo.Receipe_Name)).getText());

		List<WebElement> allIngr= driver.findElements(By.xpath(RecipeDetailsLocatorsVo.Ingredients_loc));

		sinleRecipeOutput.setIngredients(allIngr.stream().map(webElement -> webElement.getText()).collect(Collectors.toList()));

		List<WebElement> allIngrPln= driver.findElements(By.xpath(RecipeDetailsLocatorsVo.Ingredients_Plain_loc));

		sinleRecipeOutput.setPlainIngredientsList(allIngrPln.stream().map(webElement -> webElement.getText()).collect(Collectors.toList()));


		sinleRecipeOutput.setPreparation_Time(driver.findElement(By.xpath(RecipeDetailsLocatorsVo.Preparation_Time_loc)).getText());
		sinleRecipeOutput.setCooking_Time(driver.findElement(By.xpath(RecipeDetailsLocatorsVo.Cooking_Time_loc)).getText());

		sinleRecipeOutput.setRecipe_Description(driver.findElement(By.xpath(RecipeDetailsLocatorsVo.Recipe_Description_loc)).getText());
		sinleRecipeOutput.setRecipe_URL(driver.getCurrentUrl());

		List<WebElement> alltags= driver.findElements(By.xpath(RecipeDetailsLocatorsVo.Tags_loc));

		sinleRecipeOutput.setTags(alltags.stream().map(webElement -> webElement.getText()).collect(Collectors.toList()));

		List<WebElement> allBCs=driver.findElements(By.xpath("//div[@id='show_breadcrumb']/div//span"));

		sinleRecipeOutput.setBreadcrumbs(allBCs.stream().map(webElement -> webElement.getText()).collect(Collectors.toList()));

		List<WebElement> allSteps= driver.findElements(By.xpath(RecipeDetailsLocatorsVo.Preparation_method_loc));
		sinleRecipeOutput.setPreparation_method(allSteps.stream().map(webElement -> webElement.getText()).collect(Collectors.toList()));

		sinleRecipeOutput.setNo_of_servings(driver.findElement(By.xpath(RecipeDetailsLocatorsVo.No_of_servings_loc)).getText());

		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));

		List<WebElement>  tableOfNutRows = driver.findElements(By.xpath(RecipeDetailsLocatorsVo.Nutrient_values_loc));
		sinleRecipeOutput.setNutrient_values(tableOfNutRows.stream().map(webElement -> webElement.getText()).collect(Collectors.toList()));

		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(30));

		return sinleRecipeOutput;
	}

	public static boolean isNeedToAvoidThisRecipe(List<String> allTags,List<String> lFV_Avoid) {


		for (String singleTag : allTags) {

			for (String avoidItem : lFV_Avoid) {

				if(singleTag.toLowerCase().contains(avoidItem.toLowerCase()))
				{
					System.out.println("need to avoid this receipe.... got avoiding term "+avoidItem +" in ingredient "+singleTag);
					return true;
				}
			}
		}

		return false;

	}

	public static boolean isIngPresent(List<String> inputRecIngr, List<String> criteriaList,String forLog) {

		for (String singleIng : inputRecIngr) {

			singleIng=singleIng.toLowerCase();
			singleIng= singleIng.replaceAll("[^a-zA-Z0-9 ]", " ").replaceAll(" +", " ");  

			for (String eleIng : criteriaList) {


				if(singleIng.trim().equalsIgnoreCase(eleIng.trim())||
						Arrays.asList(singleIng.trim().split(" ")).contains(eleIng.toLowerCase().trim())||
						Arrays.asList(singleIng.trim().split(" ")).contains((eleIng+"s").toLowerCase().trim())||
						Arrays.asList(singleIng.trim().split(" ")).contains((eleIng+"es").toLowerCase().trim()))
				{

					String toIgnore= Hooks.ExceptionIngredientMapping.get(eleIng);
					if(toIgnore!=null)
					{
						if(!singleIng.trim().toLowerCase().contains(toIgnore))
						{
							System.out.println("[match] found  "+eleIng+ "... in "+singleIng  +" for "+forLog);

							return true;
						}

					}
					else
					{
						System.out.println("[match] found  "+eleIng+ "... in "+singleIng  +" for "+forLog);
						return true;
					}
				}

				//olive oil - cri
				//extra virgin olive oil   = ing
				if(eleIng.contains(" ") && singleIng.contains(eleIng.toLowerCase()) )
				{
					System.out.println("[match] found  "+eleIng+ "... in "+singleIng  +" for "+forLog);


					String toIgnore= Hooks.ExceptionIngredientMapping.get(eleIng);
					if(toIgnore!=null)
					{
						if(!singleIng.trim().toLowerCase().contains(toIgnore))
						{
							System.out.println("[match] found  "+eleIng+ "... in "+singleIng  +" for "+forLog);
							return true;
						}
					}
					else
					{
						System.out.println("[match] found  "+eleIng+ "... in "+singleIng  +" for "+forLog);
						return true;
					}
				}
			}
		}

		return false;
	}

}
