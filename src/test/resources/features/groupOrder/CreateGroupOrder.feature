Feature: Create Group Order
  As a Campus user
  I want to create a group order
  So that my mates can join me for the meal

  Background:
    Given the list of delivery locations:
      | locationName  | street               | zipCode | city | country |
      | Templiers A   | 930 Route des Colles |  06410  | Biot | France  |
      | Templiers B   | 930 Route des Colles |  06410  | Biot | France  |

  Scenario Outline: Successfully create a group order without delivery time
    When a campus user selects <deliveryAddress> as the delivery location
    And the campus user creates a group order
    Then a group order is created with the status "initialized"
    And a unique group order identifier is generated
    And the group order does not have a delivery time set
    Examples:
      | deliveryAddress |
      | "Templiers A"   |
      | "Templiers B"   |

  Scenario Outline: Successfully create a group order with a delivery time
    When a campus user selects <deliveryAddress> as the delivery location
    And the customer sets a delivery time <deliveryDelay> minutes from now
    And the campus user creates a group order
    Then a group order is created with the status "initialized"
    And a unique group order identifier is generated
    And the group order has the delivery time set to 125 minutes from now
    Examples:
      | deliveryAddress | deliveryDelay |
      | "Templiers A"   | 125           |
      | "Templiers B"   | 125           |

  Scenario Outline: Create a group order with an invalid delivery time
    When a campus user selects <deliveryAddress> as the delivery location
    And the customer sets a delivery time <deliveryDelay> minutes from now
    And the campus user creates a group order
    Then the order opening error message "Invalid delivery time!!" is displayed
    Examples:
      | deliveryAddress | deliveryDelay  |
      | "Templiers A"   | -15            |
      | "Templiers B"   | 0              |
