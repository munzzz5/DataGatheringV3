# DataGatheringV3
MultipleWorker for gathering android sensor data

Android app to gather sensor data in background.

Procedure to add more workers:
1. create new periodic Request in foreground service
2. add to queue with unique ID of worker
3. create worker class implementing sensorListener
4. gather data in onSensorChanged and use the saveData in utils to create new textfile internally to store
5. call onstopped after execution time (long variable currently set at 5 secs) reached


