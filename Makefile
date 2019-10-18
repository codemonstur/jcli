include .env
export

.PHONY: clean build check-versions release-notes deploy graal

DATE=`date +'%F'`
NAME=`xmllint --xpath "//project/artifactId/text()" pom.xml`
VERSION=`xmllint --xpath "//project/version/text()" pom.xml`

clean:
	@echo "[$(NAME)] Cleaning"
	@mvn -q clean

build:
	@echo "[$(NAME)] Building"
	@mvn -q -DskipTests=true clean package

check-versions:
	@mvn versions:display-dependency-updates
	@mvn versions:display-plugin-updates

release-notes:
	@echo "[$(NAME)] Writing release notes to src/docs/releases/release-$(VERSION).txt"
	@echo "$(VERSION)" > src/docs/releases/release-$(VERSION).txt
	@echo "" >> src/docs/releases/release-$(VERSION).txt
	@git log --pretty="%ci %an %s" master >> src/docs/releases/release-$(VERSION).txt

deploy: build
	@echo "[$(NAME)] Tagging and pushing to github"
	@git tag $(NAME)-$(VERSION)
	@git push && git push --tags
	@echo "[$(NAME)] Creating github release"
	@hub release create -d -a target/$(NAME)-$(VERSION).jar -a target/$(NAME)-$(VERSION)-javadoc.jar -a target/$(NAME)-$(VERSION)-sources.jar -F src/docs/releases/release-$(VERSION).txt $(NAME)-$(VERSION)

graal:
	docker run -it --mount type=bind,src=`pwd`,dst=/data -w /data oracle/graalvm-ce:1.0.0-rc14 native-image --static --no-server -jar target/jcli.jar target/jcli
