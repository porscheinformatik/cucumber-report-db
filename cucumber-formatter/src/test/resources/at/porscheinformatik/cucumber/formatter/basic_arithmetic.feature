@foo
Feature: Basic Arithmetic

    Background: A Calculator
        Given a calculator I just turned on

    @SILK_ID_1
    Scenario: Addition
    # Try to change one of the values below to provoke a failure
        When I add 4 and 5
        Then the result is 9

    @SILK_ID_2
    Scenario: Another Addition
  # Try to change one of the values below to provoke a failure
        When I add 4 and 7
        Then the result is 11

    @SILK_ID_3
    Scenario Outline: Many additions
        Given the previous entries:
            | first | second | operation |
            | 1     | 1      | +         |
            | 2     | 1      | +         |
        When I press +
        And I add <a> and <b>
        And I press +
        Then the result is <c>

    Examples: Single digits
        | a | b | c |
        | 1 | 2 | 1 |
        | 2 | 3 | 2 |

    Examples: Double digits
        | a  | b  | c  |
        | 10 | 20 | 35 |
        | 20 | 30 | 2  |


    Scenario: Another Addition
    # Try to change one of the values below to provoke a failure
        When I add 4 and 7
        And I have an unimplemented step
        Then the result is 11

    Scenario Outline: Negative additions
        Given the previous entries:
            | first   | second   | operation   |
            | <first> | <second> | <operation> |
        When I press +
        Then the result is <number>

    Examples:
        | first | second | number | operation |
        | 1     | -2     | 1      | +         |
        | -3    | -2     | 5      | +         |


    Scenario: Invalid values
        Given the previous entries:
            | first   | second   | operation |
            | <first> | <second> | +         |