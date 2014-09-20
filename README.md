MetroProgressIndicator
======================

JavaFx 8 Indetermine skin for ProgressIndicator control inspired by Windows 8's ProgressRing

How to apply this skin to your application:

Check whether you use Java 8 as target platform.</li>
Copy this file to your source folder.</li>
Copy the LICENSE file to your root folder.</li>
Add following lines as CSS property to your application:</li>

.progress-indicator {
	-fx-skin: "cuina.metro.skin.MetroProgressIndicatorSkin";
	-fx-indeterminate-segment-count: 5;
}

You can customize the angle per step, maximum step time, time until the next
segment appears and time between two spins.<br>
Simply add to the <i>.progress-indicator</i> CSS property:

	-fx-indeterminate-angle-per-step: 40;
	-fx-indeterminate-max-step-time: 300.0;
	-fx-indeterminate-next-segment-time: 200.0;
	-fx-indeterminate-next-spin-time: 500.0;
 
License: LGPL v3.0