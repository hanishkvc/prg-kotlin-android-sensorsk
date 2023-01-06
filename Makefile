
pull:
	#adb pull /storage/emulated/0/Android/data/universe.earth.india.hanishkvc.sensork/files/events.csv.txt .
	mkdir -p data/device.files
	adb pull /storage/emulated/0/Android/data/universe.earth.india.hanishkvc.sensork/files/ data/device.files/

pdf: readme.rst
	rst2pdf readme.rst

icon: icon.py
	python3 icon.py

clean:
	rm readme.pdf

