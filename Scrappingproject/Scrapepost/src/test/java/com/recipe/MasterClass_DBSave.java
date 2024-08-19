package com.recipe;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.recipe.Library;
import com.recipe.database.DatabaseOperations;
import com.recipe.vos.RecipeVo;

public class MasterClass_DBSave {

	public static final String URL="https://www.tarladalal.com/RecipeAtoZ.aspx";
	public static List<Integer> alreadySaved=getAlreadySavedRecipeIds();

	@Test (dataProvider = "data-provider")
	public void myLFVTest (String currentSearchTerm) {

		//	WebDriver driver = new FirefoxDriver();

		FirefoxOptions firefoxOptions=new FirefoxOptions();
	//	firefoxOptions.addArguments("--headless");
		WebDriver driver= new FirefoxDriver(firefoxOptions);

		System.out.println("searchTerm : "+currentSearchTerm);

		driver.navigate().to(URL+"?beginswith="+currentSearchTerm+"&pageindex=1");
		//recipes from other tab  radio button
		//driver.findElement(By.xpath("//input[@id='ctl00_cntleftpanel_rbltdmem_1']")).click();
		
		List<WebElement> allLinks =driver.findElements(By.xpath("//div[contains(.,'Goto Page:')]//a[@class='respglink']"));

		int lastPageIndex = Integer.parseInt(allLinks.get(allLinks.size()-1).getText());

		System.out.println("lastPageIndex ---> "+lastPageIndex);

		String rcpId;

		for(int pageNumber=1;pageNumber<=lastPageIndex;pageNumber++)
		{
			try {
				driver.manage().timeouts().implicitlyWait(Duration.ofMinutes(2));

				System.out.println(" ========== Currently Searching -> "+currentSearchTerm +" Page : "+(pageNumber)+" ========== ");

				driver.navigate().to(URL+"?beginswith="+currentSearchTerm+"&pageindex="+pageNumber);
				//driver.findElement(By.xpath("//input[@id='ctl00_cntleftpanel_rbltdmem_1']")).click();

				List<WebElement> allRecipeCards =driver.findElements(By.xpath("//div[@class='rcc_recipecard']"));

				System.out.println("all Recipe cards count : "+allRecipeCards.size());

				int totalRecipes=allRecipeCards.size();

				for (int cardsCounter=0;cardsCounter<totalRecipes;cardsCounter++) 
				{
					allRecipeCards =driver.findElements(By.xpath("//div[@class='rcc_recipecard']"));

					WebElement singleCard =allRecipeCards.get(cardsCounter);

					WebElement nameEle=singleCard.findElement(By.xpath("//div[@id='"+singleCard.getAttribute("id")+"']//span[@class='rcc_recipename']"));

					rcpId=singleCard.getAttribute("id").replaceAll("rcp", "").trim();

					if(alreadySaved.contains(Integer.parseInt(rcpId)))
					{
						System.out.println("already saved "+rcpId);
						continue;
					}

					nameEle.click();

					System.out.println("\n_________________________Checking Recipe# "+rcpId+"_______________________________");

					RecipeVo sinleRecipeOutput;
					try {
						sinleRecipeOutput = Library.getRecipeDetails(driver);
					} catch (Exception e) {
						driver.navigate().to(URL+"?beginswith="+currentSearchTerm+"&pageindex="+pageNumber);
						System.out.println("skipping "+rcpId +"Error : "+e.getMessage());
						continue;
					}

					sinleRecipeOutput.setRecipe_ID(rcpId);

					insertRecipe(sinleRecipeOutput,DatabaseOperations.MasterTableName);

					driver.navigate().to(URL+"?beginswith="+currentSearchTerm+"&pageindex="+pageNumber);
					//driver.findElement(By.xpath("//input[@id='ctl00_cntleftpanel_rbltdmem_1']")).click();

				}
			} catch (Exception e) {

				System.out.println("Error. This can be ignored.");
				

			}
		}
	}




	private void insertRecipe(RecipeVo recipeVo, String tableName) {


		try {
			PreparedStatement ps = null;
			String sql = "INSERT INTO "+tableName+"("
					+ "	\"Recipe_ID\", \"Recipe_Name\", \"Recipe_Category\", \"Food_Category\", \"Ingredients\", \"Preparation_Time\", "
					+ " \"Cooking_Time\", \"Tag\", \"No_of_servings\", "
					+ "\"Cuisine_category\", \"Recipe_Description\", \"Preparation_method\", \"Nutrient_values\", \"Recipe_URL\",\"PlainIngrList\")"
					+ "	VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?);";

			ps = DatabaseOperations.getConn().prepareStatement(sql);

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
			ps.setString(columnCounter++,  StringUtils.join(recipeVo.getPlainIngredientsList()));


			ps.executeUpdate();


		} catch (Exception e) 
		{
			System.out.println("Error while inserting / duplicate !");
		}
	}

	private static List<Integer> getAlreadySavedRecipeIds() {

		List<Integer> savedIds=new ArrayList<Integer>();
		try 
		{
			Connection conn= DatabaseOperations.getConn();

			Statement statement = conn.createStatement();

			ResultSet resultSet = statement.executeQuery("SELECT \"Recipe_ID\" FROM "+DatabaseOperations.MasterTableName);

			while (resultSet.next())
			{
				savedIds.add(resultSet.getInt("Recipe_ID"));
			}

			System.out.println("got "+savedIds.size()+" already saved ids");

		} catch (Exception e) {
				
		}
		return savedIds;

	}


	@DataProvider (name = "data-provider", parallel = true)
	public Object[][] dpMethod(){
			//{"C"},{"D"},{"E"},{"F"},{"G"},
		String search[][]= {{"H"},{"I"},{"J"},{"K"},{"L"},{"M"},{"Q"},{"R"},{"S"},{"T"},{"U"},{"V"},{"W"},{"X"},{"Y"},{"Z"}};

		return search;
	}
}
