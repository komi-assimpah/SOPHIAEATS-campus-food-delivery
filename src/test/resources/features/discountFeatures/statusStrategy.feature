Feature: Applying Status Discount Strategy on Order
  As a RestaurantManager
  I want to apply a status-based discount for my customers
  So that customers with a specific status benefit from a discount on their orders

  Background:
    Given this restaurant already exists
    And a status discount strategy for CAMPUS_STUDENT with a 15% discount is added to the restaurant strategies
    And restaurant already has a menu with menuItems already


  Scenario: Apply status discount to an order placed by a CAMPUS_STUDENT
    Given an order with 3 items placed by a customer with the CAMPUS_STUDENT status
    When the customer confirms the order as a student
    Then a 15% status discount is applied to the order
    And a discount message is displayed: "A 15% discount has been applied for CAMPUS_STUDENT." for CAMPUS_STUDENT
    And the customer's balance is updated with the discount amount

  Scenario: No discount applied for a customer with LOYAL_CUSTOMER status when the status discount is set for CAMPUS_STUDENT
    Given an order with 5 items placed by a customer with the LOYAL_CUSTOMER status
    When the customer confirms the order as a student
    Then no discount should be applied
    And the customer's balance remains the same
    And a message is displayed: "No discount available for your status."

  Scenario: Restaurant manager tries to add an invalid status discount
    Given this restaurant already exists
    When the restaurant manager tries to add a status discount strategy with a percentage greater than 100%
    Then an error message is displayed: "Discount percentage must be between 0 and 100."