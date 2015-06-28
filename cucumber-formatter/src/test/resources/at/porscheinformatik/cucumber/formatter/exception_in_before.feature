@ExceptionOnBefore
Feature: Check whether an exception in the before-hook doesn't mess up the report

  Scenario: Simple Addition
    Given a calculator I just turned on
    When I add 4 and 5
    Then the result is 9