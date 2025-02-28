Feature: Application of Buy1Get1 Strategy on Order for McDonald's
  As a Restaurant Manager
  I want to apply a Buy1Get1 strategy for my customers
  So that they receive free items for every qualifying item they purchase

  Background:
    Given this restaurant already exists
    And buy1get1 strategy already added to restaurant strategies
    And restaurant already has a menu with menuItems

  Scenario: Customer receives free items based on Buy1Get1 promotion
    Given a restaurant manager
    When the restaurant manager sets the item "Fries" as a buy1get1 free item
    And the restaurant manager sets the item "Coke" as a buy1get1 free item
    And the customer places an order with 4 "Fries" and 3 "Caesar" and 2 "Coke"
    Then the customer receives 4 free "Fries"
    And the customer receives 2 free "Coke"
    And the customer receives a total of 8 "Fries" and 4 "Coke"
    And the total price reflects the discount, excluding the price of the free items
