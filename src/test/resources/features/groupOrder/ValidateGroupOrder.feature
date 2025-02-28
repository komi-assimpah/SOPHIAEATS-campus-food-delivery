Feature: Validate Group Order
  As a Campus user
  I want to validate a group order
  So that the order is prepared and delivered on time

  Background:
    Given a restaurant opening time "06:00:00"

  Scenario Outline: Successfully validating a complete group order
    Given a delivery time <deliveryDelay> minutes from opening time provided
    And a closing group order with the status "completing"
    And an order completed within the group
    When the campus user validates the group order
    Then the closing group order status is "finalising"
    Examples:
      | deliveryDelay |
      | 125           |

  Scenario: Successfully confirming a group order
    Given a closing group order with the status "finalising"
    When the campus user confirms the group order
    Then the confirmation moment is recorded
    And the closing group order status is "in_preparation"

  Scenario Outline: Validating a complete group order providing a delivery time
    Given a closing group order with the status "completing"
    And no delivery time provided in the group order
    And an order completed within the group
    And a minimum delay of <delay> minute(s) before restaurant availability for order contents
    When the campus user provides a delivery time <duration> minute(s) from opening time
    And the campus user validates the group order
    Then the closing group order status is "finalising"
    Examples:
      | duration  | delay |
      | 125       | 40    |

  Scenario Outline: Validating a group order with an invalid delivery time
    Given a closing group order with the status "completing"
    And no delivery time provided in the group order
    And an order completed within the group
    And a minimum delay of <delay> minute(s) before restaurant availability for order contents
    When the campus user provides a delivery time <duration> minute(s) from opening time
    And the campus user validates the group order
    Then a group validating error message with minimum delay is displayed
    And the closing group order status is "completing"
    Examples:
      | duration | delay |
      | 10       | 25    |

  Scenario Outline: Confirming a group order with a percentage discount
    Given a delivery time <deliveryDelay> minutes from opening time provided
    And a closing group order with the status "finalising"
    And in a restaurant with a <ratio>% on <content> percentage discount
    And a set of <content> menu items in the group order
    When the campus user confirms the group order
    Then the confirmation moment is recorded
    And a percentage discount is applied to each sub-order in the group order
    And the closing group order status is "in_preparation"
    Examples:
      | deliveryDelay | content | ratio |
      | 125           | 10      | 15    |

  Scenario: Validating a group order that is already validated
    Given a closing group order with the status "finalising"
    When the campus user validates the group order
    Then the group validating error message "Group order already validated" is displayed
    And the closing group order status is "finalising"

  Scenario: Confirming a group order that is already confirmed
    Given a closing group order with the status "in_preparation"
    When the campus user confirms the group order
    Then the group completing error message "Group order already confirmed" is displayed
    And the closing group order status is "in_preparation"
