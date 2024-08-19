package com.recipe.vos;

public class RecipeDetailsLocatorsVo {

	
	//Recipe Category(Breakfast/lunch/snack/dinner)
	//tag -> recipeCategory
	public static  String  Recipe_Category_loc;
	
	//Food Category(Veg/non-veg/vegan/Jain)
	public static  String  Food_Category_loc;
	
	public static  String  Cuisine_category_loc;
	//tag-> recipeCuisine
	
	public static  String  Ingredients_loc="//div[@id='rcpinglist']//span[@itemprop='recipeIngredient']";
	public static  String  Ingredients_Plain_loc="//div[@id='rcpinglist']//span[@itemprop='recipeIngredient']//a";

	public static  String  Preparation_Time_loc="//time[@itemprop='prepTime']";
	public static  String  Cooking_Time_loc="//time[@itemprop='cookTime']";
	public static  String  Tags_loc="//div[@id='recipe_tags']//a";
	public static  String  No_of_servings_loc="//span[contains(@id,'lblServes')]";
	public static  String  Recipe_Description_loc="//p[@id='recipe_description']";
	public static  String  Preparation_method_loc="//div[contains(@id,'pnlRcpMethod')]//li";
	public static  String  Nutrient_values_loc="//table[@id='rcpnutrients']//tr";
	
	public static  String  Receipe_Name="//div[@id='recipehead']//span[contains(@id,'RecipeName')]";
	
}
