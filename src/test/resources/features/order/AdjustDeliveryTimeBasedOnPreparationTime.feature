Feature: Dynamically adjusting delivery dates based on preparation times
  As a registered user
  The system to propose only menu items that can be prepared in time for the chosen delivery date
  So that my order is delivered on time

  Scenario: Adding items to an order with a pre-set delivery date
    Given the restaurant "McDonald's" has the following menu items:
      | name    | price | preparation_time_in_minutes |
      | Sushi   | 5.0   | 15                          |
      | Lasagne | 2.0   | 2                           |
    And the restaurant "McDonald's" is open on Monday from "12:00" to "14:00"
    And the customer places an order whose delivery time was set to next Monday at "12:15:00"
    When the customer adds a "Sushi" to the order
    Then the system should show the earliest delivery time as next Monday at "12:15:00"

  Scenario: Proposing available delivery dates based on preparation times
    Given the restaurant "Mister Pizza" has the following menu items:
      | name       | price | preparation_time_in_minutes |
      | Margherita | 7.0   | 180                         |
      | Pepperoni  | 8.0   | 240                         |
    And the restaurant "Mister Pizza" is open on Monday from "12:00" to "13:00"
    When the customer browses for restaurants
    Then the restaurant should not appear in the list of available restaurants

