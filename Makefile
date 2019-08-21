
graal:
	docker run -it --mount type=bind,src=`pwd`,dst=/data -w /data oracle/graalvm-ce:1.0.0-rc14 native-image --static --no-server -jar target/simplehttp.jar target/http
