build :
	gradle  build 

upload : build
	adb un com.dilawar.hipposhare || echo "Failed to uninstall"
	adb install -r ./app/build/outputs/apk/debug/app-debug.apk
