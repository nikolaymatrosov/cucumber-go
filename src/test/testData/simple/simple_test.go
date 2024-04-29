package simple

import (
	"github.com/cucumber/godog"
	"testing"
)

func TestFeatures(t *testing.T) {
	suite := godog.TestSuite{
		ScenarioInitializer: InitializeScenario,
		Options: &godog.Options{
			Format:   "pretty",
			Paths:    []string{"."},
			TestingT: t, // Testing instance that will run subtests.
		},
	}

	if suite.Run() != 0 {
		t.Fatal("non-zero status returned, failed to run feature tests")
	}
}

func iRunEcho(arg1 string) error {
	return godog.ErrPending
}

func theOutputShouldContain(arg1 string) error {
	return godog.ErrPending
}

func theString(arg1 string) error {
	return godog.ErrPending
}

func InitializeScenario(ctx *godog.ScenarioContext) {
	ctx.Step(`^I run echo "([^"]*)"$`, iRunEcho)
	ctx.Step(`^the output should contain "([^"]*)"$`, theOutputShouldContain)
	ctx.Step(`^the string "([^"]*)"$`, theString)
}
