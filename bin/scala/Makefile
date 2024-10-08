all: \
	cloudflare-ddns-update \
	my-launch-agents \
	nextdns-update \
	orange-analyze-report \
	time-since

clean:
	find . -depth 1 -type f -name "*.scala" | xargs -Ipath scala-cli clean "path"
	rm -rf build/

clean-ide:
	rm -rf .idea/ .bsp .metals .scala-build

# --------------------------------------------------
# nextdns-update

edit-nextdns-update: clean-ide
	scala-cli setup-ide \
		--suppress-directives-in-multiple-files-warning \
		./nextdns-update.scala
	code . --goto ./nextdns-update.scala

nextdns-update: nextdns-update.scala
	scala-cli --power package --native-image \
		--graalvm-java-version 23 \
		--graalvm-version 23 \
		./nextdns-update.scala \
		-f -o build/nextdns-update
	[ -L ../nextdns-update ] || ln -s scala/build/nextdns-update ../nextdns-update

# --------------------------------------------------
# time-since

edit-time-since: clean-ide
	scala-cli setup-ide \
		--suppress-directives-in-multiple-files-warning \
		./time-since.scala
	code . --goto ./time-since.scala

time-since: ./time-since.scala
	scala-cli --power package --native-image \
		--graalvm-java-version 23 \
		--graalvm-version 23 \
		./time-since.scala \
		-f -o build/time-since
	[ -L ../time-since ] || ln -s scala/build/time-since ../time-since

# --------------------------------------------------
# orange-analyze-report

edit-orange-analyze-report: clean-ide
	scala-cli setup-ide \
		--suppress-directives-in-multiple-files-warning \
		./orange-analyze-report.scala
	code . --goto ./orange-analyze-report.scala

orange-analyze-report: ./orange-analyze-report.scala
	scala-cli --power package --native-image \
		--graalvm-java-version 23 \
		--graalvm-version 23 \
		./orange-analyze-report.scala \
		-f -o build/orange-analyze-report
	[ -L ../orange-analyze-report ] || ln -s scala/build/orange-analyze-report ../orange-analyze-report

# --------------------------------------------------
# cloudflare-ddns-update

edit-cloudflare-ddns-update: clean-ide
	scala-cli setup-ide \
		--suppress-directives-in-multiple-files-warning \
		./cloudflare-ddns-update.scala
	code . --goto ./cloudflare-ddns-update.scala

cloudflare-ddns-update: ./cloudflare-ddns-update.scala
	scala-cli --power package --native-image \
		--graalvm-java-version 23 \
		--graalvm-version 23 \
		./cloudflare-ddns-update.scala \
		-f -o build/cloudflare-ddns-update
	[ -L ../cloudflare-ddns-update ] || ln -s scala/build/cloudflare-ddns-update ../cloudflare-ddns-update

# --------------------------------------------------
# my-launch-agents

edit-my-launch-agents: clean-ide
	scala-cli setup-ide \
		--suppress-directives-in-multiple-files-warning \
		./my-launch-agents.scala
	code . --goto ./my-launch-agents.scala

my-launch-agents: ./my-launch-agents.scala
	scala-cli --power package --native-image \
		--graalvm-java-version 23 \
		--graalvm-version 23 \
		./my-launch-agents.scala \
		-f -o build/my-launch-agents
	[ -L ../my-launch-agents ] || ln -s scala/build/my-launch-agents ../my-launch-agents
