# Makefile to manage version updates, compilation, and Docker build (cross-platform: macOS/Linux)

# Extract the version from gradle.properties
VERSION := $(shell grep -oE '^version=.*' snxp.properties | cut -d= -f2)

# Define Docker image name
IMAGE_NAME := snxp:$(VERSION)

# OS detection (macOS vs. Linux)
OS := $(shell uname)

# sed command adjustment for macOS compatibility
SED_I := $(if $(filter Darwin,$(OS)),-i '',-i)

# Default target
.PHONY: all
all: update-all compile docker-build
	@echo "All tasks completed."

# Update the version in the configuration file
.PHONY: update-conf
update-conf:
	@echo "Updating version in conf file..."
	@sed $(SED_I) "s/^snipsnap\.server\.version=.*/snipsnap.server.version=$(VERSION)/" conf/snipsnap.conf
	@echo "Updated conf file with version $(VERSION)."

# Update the version in the Dockerfile
.PHONY: update-dockerfile
update-dockerfile:
	@echo "Updating version in Dockerfile..."
	@sed $(SED_I) "s|ADD dist/snipsnap-.*\.tar\.gz /dist|ADD dist/snipsnap-$(VERSION).tar.gz /dist|" Dockerfile
	@sed $(SED_I) "s|RUN mv /dist/snipsnap-.* /data|RUN mv /dist/snipsnap-$(VERSION) /data|" Dockerfile
	@echo "Updated Dockerfile with version $(VERSION)."

# Update both configuration and Dockerfile
.PHONY: update-all
update-all: update-conf update-dockerfile
	@echo "All updates completed."

# Run the compile script
.PHONY: compile
compile:
	@echo "Running compile script..."
	@./compile ant dist
	@echo "Compilation completed."

# Build the Docker image
.PHONY: docker-build
docker-build:
	@echo "Building Docker image $(IMAGE_NAME)..."
	@docker build -t $(IMAGE_NAME) .
	@echo "Docker image $(IMAGE_NAME) built successfully."