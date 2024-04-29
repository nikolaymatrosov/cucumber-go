Feature: simple

  Scenario Outline: demo
    Given the string "<value>"
    When I run echo "<value>"
    Then the output should contain "<value>"
    Examples:
      | value |
      | hello |