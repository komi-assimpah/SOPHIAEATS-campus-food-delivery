Feature: Manage restaurant schedule
  As a restaurant manager
  I want to manage my restaurant's working hours
  So that I can keep my schedule up to date for customers

  Background:
    Given a restaurant named "MacDo"


  Scenario: Adding a new schedule without any overlap
    Given the restaurant has no schedules
    When I add a schedule on "MONDAY" from "09:00" to "11:00" with 5 staff
    Then the restaurant should have the following schedule:
      | Day    | Start  | End    | Staff |
      | MONDAY | 09:00  | 11:00  | 5     |

  Scenario: Adding a schedule that partially overlaps with another one
    Given the restaurant has the following schedule:
      | Day    | Start  | End    | Staff |
      | MONDAY | 09:00  | 11:00  | 5     |
    When I add a schedule on "MONDAY" from "10:00" to "12:00" with 3 staff
    Then the restaurant should have the following schedules:
      | Day    | Start  | End    | Staff |
      | MONDAY | 09:00  | 10:00  | 5     |
      | MONDAY | 10:00  | 12:00  | 3     |

  Scenario: Adding a schedule that partially overlaps with another one
    Given the restaurant has the following schedule:
      | Day    | Start  | End    | Staff |
      | MONDAY | 09:00  | 11:00  | 5     |
      | MONDAY | 11:00  | 13:00  | 3     |
    When I add a schedule on "MONDAY" from "10:00" to "12:00" with 2 staff
    Then the restaurant should have the following schedules:
      | Day    | Start  | End    | Staff |
      | MONDAY | 09:00  | 10:00  | 5     |
      | MONDAY | 10:00  | 12:00  | 2     |
      | MONDAY | 12:00  | 13:00  | 3     |

  Scenario: Replacing a schedule fully with a new time range
    Given the restaurant has the following schedule:
      | Day    | Start  | End    | Staff |
      | MONDAY | 09:00  | 11:00  | 5     |
    When I replace the schedule on "MONDAY" from "09:00" to "11:00" with a new schedule from "09:00" to "10:00" with 5 staff
    Then the restaurant should have the following schedule:
      | Day    | Start  | End    | Staff |
      | MONDAY | 09:00  | 10:00  | 5     |

  Scenario: Adding a schedule that overlaps and has the same number of staff
    Given the restaurant has the following schedule:
      | Day    | Start  | End    | Staff |
      | MONDAY | 09:00  | 11:00  | 5     |
    When I add a schedule on "MONDAY" from "09:00" to "11:00" with 5 staff
    Then the restaurant should have the following schedule:
      | Day    | Start  | End    | Staff |
      | MONDAY | 09:00  | 11:00  | 5     |
    And the system should display an error message saying "Schedule already exist."

  Scenario: (Edge Case) Adding a schedule that overlaps and has the same number of staff
    Given the restaurant has the following schedule:
      | Day    | Start  | End    | Staff |
      | MONDAY | 09:00  | 11:00  | 5     |
    When I add a schedule on "MONDAY" from "10:00" to "12:00" with 5 staff
    Then the restaurant should have the following schedule:
      | Day    | Start  | End    | Staff |
      | MONDAY | 09:00  | 12:00  | 5     |

  Scenario: Merging adjacent schedules with the same number of staff
    Given the restaurant has the following schedule:
      | Day    | Start  | End    | Staff |
      | MONDAY | 09:00  | 10:00  | 4     |
    When I add a schedule on "MONDAY" from "10:00" to "12:00" with 4 staff
    Then the restaurant should have the following schedule:
      | Day    | Start  | End    | Staff |
      | MONDAY | 09:00  | 12:00  | 4     |

  Scenario: Adding a schedule without any overlap on a different day
    Given the restaurant has the following schedule:
      | Day    | Start  | End    | Staff |
      | MONDAY | 09:00  | 11:00  | 5     |
    When I add a schedule on "TUESDAY" from "14:00" to "16:00" with 3 staff
    Then the restaurant should have the following schedules:
      | Day    | Start  | End    | Staff |
      | MONDAY | 09:00  | 11:00  | 5     |
      | TUESDAY | 14:00 | 16:00  | 3     |

  Scenario: Removing a schedule
    Given the restaurant has the following schedule:
      | Day    | Start  | End    | Staff |
      | MONDAY | 09:00  | 11:00  | 5     |
    When I remove the schedule on "MONDAY" from "09:00" to "11:00" with 5 staff
    Then the restaurant should have the following schedules:
      | Day    | Start  | End    | Staff |