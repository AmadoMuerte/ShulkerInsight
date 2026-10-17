GRADLE := ./gradlew
MC_26_2 := -Pminecraft_version=26.2 -Pfabric_version=0.152.1+26.2

.DEFAULT_GOAL := help
.PHONY: help build build-26.2 check test run-client run-server clean

help: ## Show this help
	@grep -E '^[a-zA-Z0-9._-]+:.*?## ' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-12s %s\n", $$1, $$2}'

build: ## Build with default Minecraft 26.3
	$(GRADLE) build

build-26.2: ## Build for Minecraft 26.2
	$(GRADLE) build $(MC_26_2)

check: build build-26.2 ## CI parity: build both MC versions

test: ## Run unit tests
	$(GRADLE) test

run-client: ## Run dev client
	$(GRADLE) runClient

run-server: ## Run dev server
	$(GRADLE) runServer

clean: ## Clean build outputs
	$(GRADLE) clean
