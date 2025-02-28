Feature: Placing an Individual Order
  As a registered user
  I want to be able to place an individual order from  restaurant
  So that I can have my food delivered to a selected location at a chosen time

  Background:
    Given the following restaurants exists:
      | name    | day    | opening_hours | closing_hours | staff_count |
      | McDo    | Monday |08:00         | 23:00         | 5           |
    And the following menu items exist for the restaurant "McDo":
      | name        | price | preparation_time |
      | Big Mac     | 5.0   | 300              |
      | Fries       | 2.0   | 60               |
    And the user "John Doe" with email "john@doe.com" is logged in

  Scenario: Successfully placing an individual order
    Given the user selected the restaurant "McDo" with the following menu items:
      | item        | quantity |
      | Big Mac     | 1        |
      | Fries       | 2        |
    And the user selects the delivery location "Templiers A"
    And the user selects a delivery time for "Monday" at 12:00
    And the order is created with the status "pending"
    When the user proceeds to payment
    Then the payment is successfully processed
    And the order status is changed to "confirmed"
