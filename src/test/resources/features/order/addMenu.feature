Feature: Add Menus to the Order
  As a registered user
  I want to add menu items to my order from a restaurant
  So that I can review the order contents and proceed to checkout

  Background:
	Given the following restaurants exist:
	  | name    | day    | opening_hours | closing_hours | staff_count |
	  | McDo    | Monday | 08:00         | 23:00         | 5           |
	  | Subway  | Monday | 10:00         | 22:00         | 3           |
	  | KFC     | Monday | 09:00         | 21:00         | 4           |
	And the following menu items exist in the restaurant "McDo":
	  | name        | price | preparation_time |
	  | Big Mac     | 5.0   | 300              |
	  | Fries       | 2.0   | 60               |
	And the user "Gherkin" with email "gherkin@doe.com" is logged in with the password "gherkin"
	And the user selects the delivery location "Templiers A"
	And the user selects as delivery time next Monday at "14:00:00"

  Scenario: Error - Menu item not available in the selected restaurant
	Given the user selects the restaurant "Subway"
	When the user tries to add "Big Mac" with a quantity of 1
	Then the order remains empty

  Scenario: Error - No restaurant selected for the order
	Given no restaurant is selected
	When the user tries to add "Big Mac" with a quantity of 1
	Then the order remains empty

  Scenario: Error - Invalid quantity
	Given the user selects the restaurant "McDo"
	When the user tries to add "Big Mac" with a quantity of 0
	Then the order remains empty

  Scenario: Error - Null menu item
	Given the user selects the restaurant "McDo"
	When the user tries to add a null item with a quantity of 1
	And the order remains empty

  Scenario: Error - Preparation time exceeds delivery time
	Given the user selects the restaurant "McDo"
	And the following menu items are available at "McDo":
	  | name        | price | preparation_time |
	  | Big Mac     | 5.0   | 300              |
	And the user selects as delivery time next Monday at "13:30:00"
	When the user tries to add "Big Mac" with a quantity of 1
	Then the order remains empty

  Scenario: Successfully add menu items to the order
	Given the user selects the restaurant "McDo"
	And the following menu items are available at "McDo":
	  | name        | price |preparation_time |
	  | Big Mac     | 5.0   |10               |
	  | Fries       | 2.0   |20			   |
	When the user adds the following items to the order:
	  | name        | quantity |
	  | Fries       | 2        |
	Then the total price of the order should be 4.0 €

