SHELL := /usr/bin/env bash -o errexit -o pipefail -o nounset

.PHONY: all
all: lint test ## lint and test code

.PHONY: install
install: ## install dependencies
	pnpm install

.PHONY: build
build: install ## build console
	pnpm build:packages
	pnpm build

.PHONY: lint
lint: install ## lint code
	pnpm lint
	pnpm typecheck

.PHONY: test
test: install ## run tests
	pnpm test:unit

.PHONY: help
help: ## print this help
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z0-9_-]+:.*?## / {gsub("\\\\n",sprintf("\n%22c",""), $$2);printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)
