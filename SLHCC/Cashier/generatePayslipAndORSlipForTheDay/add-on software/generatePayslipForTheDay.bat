set mainDirectory=%~dp0
cd /d %mainDirectory%
java -cp .\software;.\software\org.json.jar;.\software\org.apache.httpclient.jar;.\software\org.apache.httpcore.jar;.\software\org.apache.commons-logging.jar UsbongHTTPConnect http://localhost/ input/*.txt
cd ..
%2
#PAUSE
exit