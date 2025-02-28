Feature: Join Group Order
  As a Campus user
  I want to join an existing group order
  So that I can place my sub-order

  Scenario Outline: Successfully Joining a group order
    Given a group order with the status <groupOrderStatus>
    When the campus user joins the group order
    Then the groupOrder is associated with a pending sub-order
    And the group order status is <groupOrderStatus>
    Examples:
      | groupOrderStatus |
      | "initialized"    |
      | "completing"     |

  Scenario Outline: Placing an order within a group order
    Given a group order with the status <groupOrderStatus>
    And a pending sub-order associated with the group order
    When the campus user places the sub-order
    Then the sub-order is associated with the group order
    And the sub-order delivery details are the same as the group order's
    And the group order status is "completing"
    Examples:
      | groupOrderStatus |
      | "initialized"    |
      | "completing"     |

  Scenario Outline: Joining a group order with an invalid group order identifier
    Given a group order identifier <groupID>
    When the campus user joins the group order
    Then the order joining error message "Group order with id 00000000 not found" is displayed
    Examples:
      | groupID    |
      | "00000000" |

 # Scenario Outline: Joining a group order that is already closed
  #  Given a group order with the status <groupOrderStatus>
   # When the campus user joins the group order
    #Then the order joining error message "Group order already closed" is displayed
    #Examples:
     # | groupOrderStatus |
      #| "finalising"     |