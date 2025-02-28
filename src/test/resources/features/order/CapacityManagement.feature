Feature: Restaurant Capacity Management
  As a restaurant owner
  I want to manage the capacity of my restaurant
  So that I can accept orders within my capacity limits

  Background:
	Given these restaurants exist:
	  | name         | day     | opening_hours | closing_hours | staff_count |
	  | Gourmandise  | TUESDAY | 08:00:00      | 23:00:00      | 5           |
	  | Gouter       | TUESDAY | 10:00:00      | 22:00:00      | 3           |
	  | Mister Pizza | TUESDAY | 09:00:00      | 21:00:00      | 4           |
	And the items are available in the restaurant "Gourmandise":
	  | menu_item_name | price | preparation_time |
	  | Pizza          | 10.00 | 15               |
	  | Burger         | 8.00  | 10               |
	  | Salad          | 7.00  | 20               |

  Scenario: Show Available Restaurants
  	Given the targeted time is next Tuesday at "12:00:00"
	When the user requests the list of available restaurants
	Then the list should contain the following restaurants:
	  | name         | day     | opening_hours | closing_hours | staff_count |
	  | Gourmandise  | TUESDAY | 08:00:00      | 23:00:00      | 5           |
	  | Gouter       | TUESDAY | 10:00:00      | 22:00:00      | 3           |
	  | Mister Pizza | TUESDAY | 09:00:00      | 21:00:00      | 4           |

  Scenario: Select a restaurant and check available delivery times
	Given the user chooses the restaurant "Gourmandise"
	When the user requests the available delivery times for "Gourmandise" next Tuesday at "20:00:00"
	Then the list should contain the following delivery times:
	  | time     |
	  | 20:30:00 |
	  | 21:00:00 |

  Scenario: Reserve a delivery time and create an order
  	Given the user chooses the delivery time next Tuesday at "18:00:00" and the restaurant "Gourmandise"
	When the user reserves as delivery time next Tuesday at "18:00:00"
	Then a new order should be created with status "PENDING"
	And the order's delivery time should be next Tuesday at "18:00:00"
	And the remaining capacity for "Gourmandise" should be 9000

  Scenario: Display available menu items for the order
	Given the order is created with restaurant "Gourmandise"
	When the user requests the available menu items for "Gourmandise" next Tuesday at "18:00:00"
	Then the list of available menu items should include:
	  | menu_item_name  | price |
	  | Pizza           | 10.00 |
	  | Burger          | 8.00  |
	  | Salad           | 7.00  |

  Scenario: Add menu items to the order and place the order
	Given the user adds the following menu items to the order:
	  | menu_item_name | quantity |
	  | Pizza           | 1       |
	  | Burger          | 1       |
	When the user places the order
	Then the order status should be "CONFIRMED"
	And the remaining capacity for "Gourmandise" should be 9000

  Scenario: Pay for the order
	Given the order has been placed and the order status is "CONFIRMED"
	When the user pays for the order
	Then the payment should be successful
	And the remaining capacity for "Gourmandise" should be 9000
