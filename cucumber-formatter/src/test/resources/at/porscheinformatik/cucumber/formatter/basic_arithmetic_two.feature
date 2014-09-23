@foo
Feature: Basic Arithmetic Two

  Background: A Calculator
    Given a calculator I just turned on

  Scenario: Addition Two
  # Try to change one of the values below to provoke a failure
    When I add 4 and 5
    Then the result is 9

  Scenario: Another Addition Two
  # Try to change one of the values below to provoke a failure
    When I add 4 and 7
    Then the result is 11

    @SILK_ID_4
  Scenario Outline: Many additions
    Given the previous entries:
      | first | second | operation |
      | 1     | 1      | +         |
      | 2     | 1      | +         |
    When I press +
    And I add <a> and <b>
    And I press +
    Then the result is <c>

  Examples: Double digits
    | a  | b  | c  |
    | 10 | 20 | 35 |
    | 20 | 30 | 2 |


    @SILK_ID_5
  Scenario: Another Addition Two
  # Try to change one of the values below to provoke a failure
    When I add 4 and 7
    And I have an unimplemented step
    Then the result is 11
