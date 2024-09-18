all: nextdns-vpn-update time-since orange-analyze-report

clean:
	scala clean src/nextdns-vpn-update.scala
	scala clean src/time-since.sc
	rm -rf build/

nextdns-vpn-update: src/nextdns-vpn-update.scala
	scala --power package --native-image \
		--graalvm-java-version 21 \
		--graalvm-version 21 \
		./src/nextdns-vpn-update.scala \
		-f -o build/nextdns-vpn-update

time-since: src/time-since.sc
	scala --power package --native-image \
		--graalvm-java-version 21 \
		--graalvm-version 21 \
		./src/time-since.sc \
		-f -o build/time-since

orange-analyze-report: src/orange-analyze-report.scala
	scala --power package --native-image \
		--graalvm-java-version 21 \
		--graalvm-version 21 \
		./src/orange-analyze-report.scala \
		-f -o build/orange-analyze-report

