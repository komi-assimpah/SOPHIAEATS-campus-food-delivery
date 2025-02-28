Feature: Restaurant Application of Percentage Strategy on Order
  As a RestaurantManager
  I want to apply a percentage strategy for my customers
  So that discount is added to their balance

  Background:
    Given a restaurant already named "McDonald's" exists with the following menu:
      | name          | price | preparation_time_in_mins |
      | Wrap          | 5.0   | 5                        |
      | French Fries  | 2.0   | 1                        |
    And restaurant already has a menu with menuItems

    Scenario: Percentage discount is applied to order with 10+ items
      Given an order with 11 "Wrap" with the price of 5.0 each
      And the restaurant has applied a 10% discount for orders with 10+ items
      When the customer confirms the order
      Then a percentage discount is applied
      And a discount message is displayed: "A discount of 5.5 has been applied. Thanks to the restaurant manager."





