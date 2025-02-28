Feature: Restaurant Menu Management
  In order to manage menu items
  As a Restaurant Manager
  I want to be able to add, remove, and view menu items for my restaurant

  Scenario: Adding a new menu item to an empty restaurant menu
    Given a restaurant named "Subway" exists with no menu items
    When the Restaurant Manager adds a menu item "Burger" with a price of 8.99 and preparation time of 300 seconds
    Then the restaurant menu should contain exactly 1 item:
      | Name   | Price | Preparation Time |
      | Burger | 8.99  | 300              |

  Scenario: Adding a new menu item to a restaurant with an existing menu
    Given a restaurant named "Subway" exists with the following menu:
      | Name   | Price | Preparation Time |
      | Pizza  | 10.99 | 600              |
    When the Restaurant Manager adds a menu item "Salad" with a price of 6.50 and preparation time of 200 seconds
    Then the restaurant menu should contain exactly 2 item:
      | Name   | Price | Preparation Time |
      | Pizza  | 10.99 | 600              |
      | Salad  | 6.50  | 200              |

  Scenario: Adding a duplicate menu item to a restaurant with an existing menu
    Given a restaurant named "Subway" exists with the following menu:
      | Name   | Price | Preparation Time |
      | Pizza  | 10.99 | 600              |
    When the Restaurant Manager adds a menu item "Pizza" with a price of 10.99 and preparation time of 600 seconds
    Then the system should return an error message "Menu item 'Pizza' already exists. Please update the existing menu item instead or use another name."

  # TODO: How to determine if a menu item is "duplicate"? By name, price, preparation time, or a combination of these? TBD
  Scenario: Preventing duplicate menu items from being added
    Given a restaurant named "Subway" exists with the following menu:
      | Name   | Price | Preparation Time |
      | Pizza  | 10.99 | 600              |
    When the Restaurant Manager adds a menu item "Pizza" with a price of 11.99 and preparation time of 700 seconds
    Then the system should return an error message "Menu item 'Pizza' already exists. Please update the existing menu item instead or use another name."

  Scenario: Removing a menu item from a restaurant's menu
    Given a restaurant named "Subway" exists with the following menu:
      | Name   | Price | Preparation Time |
      | Pizza  | 10.99 | 600              |
      | Salad  | 6.50  | 200              |
    When the Restaurant Manager removes the menu item "Pizza"
    Then the restaurant menu should contain exactly 1 item:
      | Name   | Price | Preparation Time |
      | Salad  | 6.50  | 200              |

  Scenario: Removing a non-existent menu item
    Given a restaurant named "Subway" exists with the following menu:
      | Name   | Price | Preparation Time |
      | Pizza  | 10.99 | 600              |
      | Salad  | 6.50  | 200              |
    When the Restaurant Manager attempts to remove a menu item "Burger"
    Then the system should return an error message "Menu item 'Burger' not found"

  Scenario: Viewing all menu items for a restaurant
    Given a restaurant named "Subway" exists with the following menu:
      | Name   | Price | Preparation Time |
      | Pizza  | 10.99 | 600              |
      | Salad  | 6.50  | 200              |
    When any Internet User views the restaurant menu
    Then the restaurant menu should contain the following items:
      | Name   | Price | Preparation Time |
      | Pizza  | 10.99 | 600              |
      | Salad  | 6.50  | 200              |


