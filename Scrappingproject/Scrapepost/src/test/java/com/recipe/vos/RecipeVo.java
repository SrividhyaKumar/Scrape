package com.recipe.vos;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class RecipeVo {


	@Override
	public String toString() {
		return "[Recipe_ID=" + Recipe_ID + ", \nRecipe_Name=" + Recipe_Name + ", \nRecipe_Category="
				+ Recipe_Category + ", \nFood_Category=" + Food_Category + ", \nIngredients=" + Ingredients
				+ ", \nPreparation_Time=" + Preparation_Time + ", \nCooking_Time=" + Cooking_Time + ", \nTags=" + Tags
				+ ", \nNo_of_servings=" + No_of_servings + ", \nCuisine_category=" + Cuisine_category
				+ ", \nRecipe_Description=" + Recipe_Description + ", \nPreparation_method=" + Preparation_method
				+ ", \nNutrient_values=" + Nutrient_values + ", \nRecipe_URL=" + Recipe_URL + "]" + "\nBreadscrums : "+breadcrumbs;

	}


	public List<String> getIngredients() {
		return Ingredients;
	}

	public void setIngredients(List<String> ingredients) {
		Ingredients = ingredients;
	}
	public List<String> getTags() {
		return Tags;
	}
	public void setTags(List<String> tags) {
		Tags = tags;

		for (String singleTagText : tags) {

			checkAndSetRecipeCat(singleTagText);

			checkAndSetFoodCat(singleTagText);
		}

		checkAndSetCuisineCat(tags);
	}

	private void checkAndSetCuisineCat(List<String> tags) {

		if(this.Cuisine_category==null || this.Cuisine_category.isEmpty())
		{
			for(String cusineCat : FilterVo.CuisineCategory)
			{
				for (String singleTagText : tags) {

					if(singleTagText.toLowerCase().contains(cusineCat.toLowerCase()))
					{
						this.Cuisine_category = cusineCat;
						return;
					}
				}
			}
		}
	}

	public List<String> getPreparation_method() {
		return Preparation_method;
	}
	public void setPreparation_method(List<String> preparation_method) {
		Preparation_method = preparation_method;
	}
	String Recipe_ID; 
	String Recipe_Name;

	List<String> Recipe_Category;
	String Food_Category;
	List<String> Ingredients;
	List<String> PlainIngredientsList;

	public List<String> getPlainIngredientsList() {
		
		return Ingredients;
		//return PlainIngredientsList;
	}
	public void setPlainIngredientsList(List<String> plainIngredientsList) {

		PlainIngredientsList = plainIngredientsList;
	}
	String Preparation_Time;
	String Cooking_Time;
	List<String> Tags;

	List<String> breadcrumbs;

	public List<String> getBreadcrumbs() {
		return breadcrumbs;
	}
	public void setBreadcrumbs(List<String> breadcrumbs) {
		this.breadcrumbs = breadcrumbs;

		for (String singleTagText : breadcrumbs) {

			checkAndSetRecipeCat(singleTagText);

			checkAndSetFoodCat(singleTagText);

		}
		
		checkAndSetCuisineCat(breadcrumbs);


	}
	private void checkAndSetRecipeCat(String singleTagText) {

		if(singleTagText.toLowerCase().contains("lunch"))
			this.addRecCat("Lunch");
		if(singleTagText.toLowerCase().contains("snack"))
			this.addRecCat("Snack");
		if(singleTagText.toLowerCase().contains("dinner"))
			this.addRecCat("Dinner");
		if(singleTagText.toLowerCase().contains("breakfast"))
			this.addRecCat("Breakfast");

	}
	private void checkAndSetFoodCat(String singleTagText) {

		if(this.Food_Category==null || this.Food_Category.isEmpty())
		{
			if(singleTagText.toLowerCase().contains("vegan"))
				this.setFood_Category("vegan");
			else if(singleTagText.toLowerCase().contains("jain")) 
				this.setFood_Category("Jain");
			else if(singleTagText.toLowerCase().contains("vegetarian"))
				this.setFood_Category("Vegetarian");
			else if(singleTagText.toLowerCase().contains("eggitarian"))
				this.setFood_Category("Eggitarian");
			else if(singleTagText.toLowerCase().contains("non-veg") ||
					singleTagText.toLowerCase().contains("non-vegetarian"))
				this.setFood_Category("Non-veg");
			else if(singleTagText.toLowerCase().contains("veg"))
				this.setFood_Category("Veg");
		}

	}
	String No_of_servings;
	String Cuisine_category;
	String Recipe_Description;
	List<String> Preparation_method;
	List<String> Nutrient_values;
	String Recipe_URL;
	public String getRecipe_ID() {
		return Recipe_ID;
	}
	public void setRecipe_ID(String recipe_ID) {
		Recipe_ID = recipe_ID;
	}
	public String getRecipe_Name() {
		return Recipe_Name;
	}
	public void setRecipe_Name(String recipe_Name) {
		Recipe_Name = recipe_Name;
	}

	public List<String> getRecipe_Category() {
		return Recipe_Category;
	}
	public void setRecipe_Category(List<String> recipe_Category) {
		Recipe_Category = recipe_Category;
	}
	public String getFood_Category() {
		return Food_Category;
	}
	public void setFood_Category(String food_Category) {
		Food_Category = food_Category;
	}

	public String getPreparation_Time() {
		return Preparation_Time;
	}
	public void setPreparation_Time(String preparation_Time) {
		Preparation_Time = preparation_Time;
	}
	public String getCooking_Time() {
		return Cooking_Time;
	}
	public void setCooking_Time(String cooking_Time) {
		Cooking_Time = cooking_Time;
	}

	public String getNo_of_servings() {
		return No_of_servings;
	}
	public void setNo_of_servings(String no_of_servings) {
		No_of_servings = no_of_servings;
	}
	public String getCuisine_category() {
		return Cuisine_category;
	}
	public void setCuisine_category(String cuisine_category) {
		Cuisine_category = cuisine_category;
	}
	public String getRecipe_Description() {
		return Recipe_Description;
	}
	public void setRecipe_Description(String recipe_Description) {
		Recipe_Description = recipe_Description;
	}

	public List<String> getNutrient_values() {
		return Nutrient_values;
	}
	public void setNutrient_values(List<String> nutrient_values) {
		Nutrient_values = nutrient_values;
	}
	public String getRecipe_URL() {
		return Recipe_URL;
	}
	public void setRecipe_URL(String recipe_URL) {
		Recipe_URL = recipe_URL;

		//this.Recipe_ID=recipe_URL.substring(recipe_URL.lastIndexOf("-")+1,recipe_URL.length()-1);


	}
	public void addIng(String text) {

		if(Ingredients==null)
			Ingredients=new ArrayList<String>();

		Ingredients.add(text);
	}

	public void addBc(String text) {

		if(breadcrumbs==null)
			breadcrumbs=new ArrayList<String>();

		if(!breadcrumbs.contains(text))
			breadcrumbs.add(text);
	}

	public void addTag(String text) {
		if(Tags==null)
			Tags=new ArrayList<String>();

		Tags.add(text);

	}

	public void addStep(String text) {
		if(Preparation_method==null)
			Preparation_method=new ArrayList<String>();

		Preparation_method.add(text);

	}
	public void addNut(WebElement webElement) {

		if(Nutrient_values==null)
			Nutrient_values=new ArrayList<String>();

		List<WebElement> tds=webElement.findElements(By.tagName("td"));

		Nutrient_values.add(tds.get(0).getText()+":"+tds.get(1).getText());
	}
	public void addRecCat(String string) {

		if(Recipe_Category==null)
			Recipe_Category=new ArrayList<String>();

		if(!Recipe_Category.contains(string))
			Recipe_Category.add(string);
	}

}
