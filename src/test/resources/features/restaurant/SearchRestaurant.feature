Feature: Search a specific restaurant
  As a campus user,
  I want to search for a specific restaurant in the campus restaurants list
  so that I can quickly check if it is available for orders.

  Scenario: Search a specific restaurant another time
    Given the available restaurants list is exhaustive, I want to do a searching to get my restaurant quicker on MONDAY at 15:30
    When I am app home page again
    And I type "Régal" in the search bar
    Then I should get the restaurant "Régal" not being selectable because it's closed
    When I type "Subwaya" in the search bar
    Then I should restaurant "Subway" being selectable because it's still opened
