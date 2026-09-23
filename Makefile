COMPOSE ?= docker compose
GRADLEW ?= ./gradlew

.PHONY: local
local:
	$(COMPOSE) up -d
	@echo "Waiting for docker compose services to be ready..."
	@until $(COMPOSE) ps --status running --status healthy --services | grep -q .; do \
		sleep 2; \
	done
	$(GRADLEW) bootRunLocal

local-down:
	$(COMPOSE) down
