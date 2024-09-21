all: nextdns-update time-since orange-analyze-report

clean:
	scala-cli clean ./nextdns-update.scala
	scala-cli clean ./orange-analyze-report.scala
	scala-cli clean ./time-since.scala
	rm -rf build/

clean-ide:
	rm -rf .idea/ .bsp .metals .scala-build

edit-nextdns-update: clean-ide
	scala-cli setup-ide \
		--suppress-directives-in-multiple-files-warning \
		./nextdns-update.scala
	code . --goto ./nextdns-update.scala

nextdns-update: nextdns-update.scala
	scala-cli --power package --native-image \
		--graalvm-java-version 21 \
		--graalvm-version 21 \
		./nextdns-update.scala \
		-f -o build/nextdns-update
	[ -L ../nextdns-update ] || ln -s scala/build/nextdns-update ../nextdns-update

edit-time-since: clean-ide
	scala-cli setup-ide \
		--suppress-directives-in-multiple-files-warning \
		./time-since.scala
	code . --goto ./time-since.scala

time-since: ./time-since.scala
	scala-cli --power package --native-image \
		--graalvm-java-version 21 \
		--graalvm-version 21 \
		./time-since.scala \
		-f -o build/time-since
	[ -L ../time-since ] || ln -s scala/build/time-since ../time-since

edit-orange-analyze-report: clean-ide
	scala-cli setup-ide \
		--suppress-directives-in-multiple-files-warning \
		./orange-analyze-report.scala
	code . --goto ./orange-analyze-report.scala

orange-analyze-report: ./orange-analyze-report.scala
	scala-cli --power package --native-image \
		--graalvm-java-version 21 \
		--graalvm-version 21 \
		./orange-analyze-report.scala \
		-f -o build/orange-analyze-report
	[ -L ../orange-analyze-report ] || ln -s scala/build/orange-analyze-report ../orange-analyze-report
