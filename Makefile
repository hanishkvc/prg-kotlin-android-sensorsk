
pull:
	#adb pull /storage/emulated/0/Android/data/universe.earth.india.hanishkvc.sensork/files/events.csv.txt .
	mkdir device.files || /bin/true
	adb pull /storage/emulated/0/Android/data/universe.earth.india.hanishkvc.sensork/files/ device.files/