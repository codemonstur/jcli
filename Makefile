.PHONY: help clean build validate release-notes deploy graal

DATE=`date +'%F'`
NAME=`xmllint --xpath "//project/artifactId/text()" pom.xml`
VERSION=`xmllint --xpath "//project/version/text()" pom.xml`
PREVIOUS_TAG=`git tag | sort -r | head -n 1`

help:
	@echo "Available targets for $(NAME):"
	@echo "\thelp\t\t\tThis help"
	@echo "\tclean\t\t\tDelete everything in ./target"
	@echo "\tbuild\t\t\tCleans the project and rebuilds the code"
	@echo "\tvalidate\t\tCheck dependency versions, cves, and licenses"
	@echo "\trelease-notes\t\tCreate release notes for the latest version"
	@echo "\tdeploy\t\t\tClean, build and deploy a version to Github and Maven Central"
	@echo "\tgraal\t\t\tBuild a native version of the tool"

clean:
	@echo "[$(NAME)] Cleaning"
	@mvn clean

build:
	@echo "[$(NAME)] Building"
	@mvn -DskipTests=true clean package

validate:
	@mvn versions:display-dependency-updates
	@mvn versions:display-plugin-updates
	@mvn validate -P security
	@mvn validate -P licenses

release-notes:
	@echo "[$(NAME)] Writing release notes to src/docs/releases/release-$(VERSION).txt"
	@echo "$(VERSION)" >> src/docs/releases/release-$(VERSION).txt
	@echo "" >> src/docs/releases/release-$(VERSION).txt
	@git log --pretty="%s" $(PREVIOUS_TAG)... master >> src/docs/releases/release-$(VERSION).txt

deploy: build
	@echo "[$(NAME)] Uploading to maven central"
	@mvn clean deploy
	@echo "[$(NAME)] Creating github release"
	@gh release create $(NAME)-$(VERSION) target/$(NAME)-$(VERSION).jar target/$(NAME)-$(VERSION)-javadoc.jar target/$(NAME)-$(VERSION)-sources.jar -F src/docs/releases/release-$(VERSION).txt
	@echo "[$(NAME)] Tagging and pushing to github"
	@git tag $(NAME)-$(VERSION)
	@git push && git push --tags

graal:
	@docker run -it --mount type=bind,src=`pwd`,dst=/data -w /data oracle/graalvm-ce:1.0.0-rc14 native-image --static --no-server -jar target/$(NAME).jar target/$(NAME)
