SHELL := /usr/bin/env bash -o errexit -o pipefail -o nounset

install: ## Install console
	pnpm install

build-packages: install ## Build packages of console
	pnpm build:packages

build: build-packages ## Build console
	pnpm build

lint: build-packages ## Lint console
	pnpm lint
	pnpm typecheck

test: build-packages ## Test console
	pnpm test:unit

check: lint test ## Check console

dev: build-packages ## Run console with development environment
	pnpm dev

api-client-gen: install ## Generate API client
	pnpm api-client:gen

help: ## print this help
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z0-9_-]+:.*?## / {gsub("\\\\n",sprintf("\n%22c",""), $$2);printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)
