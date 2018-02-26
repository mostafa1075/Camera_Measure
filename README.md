# Camera_Measure
An Android app that measures the distance and height of an object using live camera
feed and sensors. Also, it can measure various objects in a captured image using Image
Processing techniques.
## Online Mode
In this mode the user uses the camera to adjust the phone at different angles. The app uses the accelerometer and gravity sensors to 
measure the angles required to measure the object. 
### How to use
- Keep the phone at eye level and enter the height of the phone above the ground (usually your height - 10cm).
<p align="center">
  <img width="460" height="300" src="https://github.com/mostafa1075/Camera_Measure/blob/master/Full%20Documentation/online1.jpg">
</p>

- Point the crosshair at the bottom of the object and tap it.
<p align="center">
  <img width="460" height="300" src="https://github.com/mostafa1075/Camera_Measure/blob/master/Full%20Documentation/online2.png">
</p>

- point crosshair at the top of the object and tap it.
<p align="center">
  <img width="460" height="300" src="https://github.com/mostafa1075/Camera_Measure/blob/master/Full%20Documentation/online3.png">
</p>

- It should show the height of the object and the distance from the object.
<p align="center">
  <img width="460" height="300" src="https://github.com/mostafa1075/Camera_Measure/blob/master/Full%20Documentation/online4.png">
</p>
Note: the object must be on the ground. If not check the full documentation for futher instructions.

## Offline Mode
In this mode the user can load an image from his phone, or capture one himself of the object to be measured. If a reference object
out of the ones supported (coin and A4 paper) exists in the same image, he can use it to measure the desired object. If not, he can 
enter the measurements himself of another object to act as a reference.

### How to use
#### Region Growing Mode
- Choose the reference object in the modes pop-up.
<p align="center">
  <img width="460" height="300" src="https://github.com/mostafa1075/Camera_Measure/blob/master/Full%20Documentation/offline1.png">
</p>

- Place the bullet on the reference object.
<p align="center">
  <img width="460" height="300" src="https://github.com/mostafa1075/Camera_Measure/blob/master/Full%20Documentation/offline2.png">
</p>

- Press on Take Reference Length button.
<p align="center">
  <img width="460" height="300" src="https://github.com/mostafa1075/Camera_Measure/blob/master/Full%20Documentation/offline3.png">
</p>

- The object will be colored to demonstrate that the region growing was successful. Sometimes, the object might not be correctly colored 
under different conditions (bad lighting, equal colored surroundings). You can adjust the threshold slider to fix it.
<p align="center">
  <img width="460" height="300" src="https://github.com/mostafa1075/Camera_Measure/blob/master/Full%20Documentation/offline4.png">
</p>

- Move the bullets around the object to measure it.
<p align="center">
  <img width="460" height="300" src="https://github.com/mostafa1075/Camera_Measure/blob/master/Full%20Documentation/offline5.png">
</p>

#### Custom Mode
Check the full documentation on how to use this
