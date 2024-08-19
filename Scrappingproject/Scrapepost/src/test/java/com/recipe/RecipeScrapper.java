package com.recipe;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.hooks.Hooks;
import com.recipe.database.DatabaseOperations;
import com.recipe.vos.FilterVo;
import com.recipe.vos.RecipeVo;

public class RecipeScrapper {


	public static FilterVo LFV_FILTER;
	public static FilterVo LCHFE_FILTER;


	public static final String URL="https://www.tarladalal.com/RecipeAtoZ.aspx";

	@DataProvider (name = "data-provider", parallel = true)
	public Object[][] dpMethod(){

		String search[][]= {{"A"},{"B"},{"C"},{"D"},{"E"},{"F"},{"G"},{"H"},{"I"},{"J"},{"K"},{"L"},{"M"},{"N"},{"O"},{"P"},{"Q"},{"R"},{"S"},{"T"},{"U"},{"V"},{"W"},{"X"},{"Y"},{"Z"},{"Misc"}};

		return search;
	}

	@Test (dataProvider = "data-provider")
	public void srapRecipes (String currentSearchTerm) 
	{
		WebDriver driver=null;

		try
		{
			FirefoxOptions firefoxOptions=new FirefoxOptions();
			if(!Hooks.configreader.getBrowserMode().isEmpty())
			{
				//firefoxOptions.addArguments(Hooks.configreader.getBrowserMode());
			}

			driver= new FirefoxDriver(firefoxOptions);
			driver.manage().timeouts().implicitlyWait(Duration.ofMinutes(2));

			System.out.println("Launching browser--> searchTerm --> "+currentSearchTerm);

			//default page = 1 for any search term
			driver.navigate().to(URL+"?beginswith="+currentSearchTerm+"&pageindex=1");

			//for each search term get last page number
			List<WebElement> allLinks =driver.findElements(By.xpath("//div[contains(.,'Goto Page:')]//a[@class='respglink']"));

			int lastPageIndex=0;
			try {
				lastPageIndex = Integer.parseInt(allLinks.get(allLinks.size()-1).getText());
			} catch (Exception e) {
				System.out.println("lastPageIndex is Not available");
			}

			System.out.println("searchTerm : "+currentSearchTerm+ ", lastPageIndex ---> "+lastPageIndex);

			String recipeId;

			for(int pageNumber=1;pageNumber<=lastPageIndex;pageNumber++)
			{
				try {

					driver.navigate().to(URL+"?beginswith="+currentSearchTerm+"&pageindex="+pageNumber);
					driver.manage().timeouts().implicitlyWait(Duration.ofMinutes(2));

					List<WebElement> allRecipeCards =driver.findElements(By.xpath("//div[@class='rcc_recipecard']"));

					System.out.println(" ========== Currently Searching -> "+currentSearchTerm +" Page : "+(pageNumber)+ " Cards/Recipes Count : "+allRecipeCards.size() +"  ========== ");

					for (int cardsCounter=0;cardsCounter<allRecipeCards.size();cardsCounter++) 
					{
						allRecipeCards = driver.findElements(By.xpath("//div[@class='rcc_recipecard']"));

						WebElement singleCard = allRecipeCards.get(cardsCounter);

						//first we extract recipe id from current card
						WebElement recipeNameElement=singleCard.findElement(By.xpath("//div[@id='"+singleCard.getAttribute("id")+"']//span[@class='rcc_recipename']"));

						recipeId=singleCard.getAttribute("id").replaceAll("rcp", "").trim();
						System.out.println("\n_________________________Checking Recipe# "+recipeId+"_______________________________");

						if(LFV_FILTER.getAlreadySaved().contains(Integer.parseInt(recipeId)) && LCHFE_FILTER.getAlreadySaved().contains(Integer.parseInt(recipeId)))
						{
							System.out.println("Recipe "+recipeId+" Already in db for both lfv and lchfe ..");
							continue;
						}

						//open card/ open recipe
						recipeNameElement.click();
						RecipeVo sinleRecipeOutput = Library.getRecipeDetails(driver);
						sinleRecipeOutput.setRecipe_ID(recipeId);

						System.out.println("recipe opened !");

						if( !LFV_FILTER.getAlreadySaved().contains(Integer.parseInt(recipeId)) )
						{
							Library.coreLogic(sinleRecipeOutput, LFV_FILTER);
							DatabaseOperations.insertCheckedRecipeId(Integer.parseInt(recipeId),LFV_FILTER.getFilterName());

						}

						if( !LCHFE_FILTER.getAlreadySaved().contains(Integer.parseInt(recipeId)) )
						{
							Library.coreLogic(sinleRecipeOutput, LCHFE_FILTER);
							DatabaseOperations.insertCheckedRecipeId(Integer.parseInt(recipeId),LCHFE_FILTER.getFilterName());
						}

						driver.navigate().to(URL+"?beginswith="+currentSearchTerm+"&pageindex="+pageNumber);
					}

				} catch (Exception e) {

					e.printStackTrace();

					System.out.println(" ========== Error, skipping that recipe Currently Searching -> "+currentSearchTerm +" Page : "+(pageNumber)+" ========== ");
				}
			}
		}
		finally
		{
			if(driver!=null)
				driver.quit();
		}
	}



}
