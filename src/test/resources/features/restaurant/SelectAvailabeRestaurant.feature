Feature: Select an available restaurant
  As a campus user,
  I want to search a restaurant available from campus restaurants list,
  where I can place order right now.

  Background:
    Given the following restaurants exist in our system:
      | name  |        street         | zipCode | city | country   | day     | opening_hours | closing_hours | staff_count |
      | McDo  | 930 Route des Colles  |  06410  | Biot | France    | Tuesday |  12:00        |    12:30      |    2        |
      | KFC   | 930 Route des Colles  |  06410  | Biot | France    | Tuesday |  12:00        |    14:00      |    3        |


  Scenario: View a List of Restaurants available
    Given the user is connected to our application on "Tuesday" at 12:10
    When the user browses the list of available restaurants
    Then  the user should see all the available restaurants on MONDAY at 12:10

