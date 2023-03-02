SHELL := /usr/bin/env bash -o errexit -o pipefail -o nounset

.PHONY: cinstall
cinstall: ## Install console
	./gradlew pnpm_install

.PHONY: cbuild-packages
cbuild-packages: cinstall ## Build packages of console
	./gradlew pnpm_build-packages

.PHONY: cbuild
cbuild: cbuild-packages ## Build console
	./gradlew pnpm_build

.PHONY: clint
clint: ## Lint console
	./gradlew pnpm_lint
	./gradlew pnpm_typecheck

.PHONY: help
help: ## print this help
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z0-9_-]+:.*?## / {gsub("\\\\n",sprintf("\n%22c",""), $$2);printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)
