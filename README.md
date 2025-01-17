

# MAVGAnalysis

## In-Flight/PX4Log/ULog Analyzer for PX4 

[![Build Status](https://travis-ci.org/ecmnet/MAVGCL.svg?branch=master)](https://travis-ci.org/ecmnet/MAVGCL) [![Build status](https://ci.appveyor.com/api/projects/status/jqo0dnkcksaj6b3s?svg=true)](https://ci.appveyor.com/project/ecmnet/mavgcl) ![alt tag](https://img.shields.io/github/release/ecmnet/MAVGCL.svg)



This JavaFx based tool enables PX4 Users to record and analyse data published via UDP during flight or offline based on PX4Logs or ULogs. It is not intended to replace the QGC. It is not tested with ardupilot.

Any feedback, comments and contributions are very welcome.

**Development Status:** Last updated 	11/08/22

- Auto loading of custom keyfigure definition files from a dedicated directory at startup
- Autosave into result directory requests and saves DMESG output
- Multiple videostreams
- Videostream replay (if video found in result directory)

Note: 3D map data can be transferred to MAVGCL using the custom MAVLink message [msg_msp_micro_grid](https://github.com/ecmnet/mavcom/blob/af3a826866d977b898170547d15e1ad334899682/mavcom/mavlink/lquac.xml#L231) - for encoding refer to [MAP3DSpacialInfo.java](https://github.com/ecmnet/mavmap/blob/aa739520e2de797cad3ba71da01f041c87445557/mavmap/src/main/java/com/comino/mavmap/map/map3D/Map3DSpacialInfo.java#L84)

See MAVGCL some action: 
- Video replay: <https://www.youtube.com/watch?v=liEKiAzEZ5Y> (2022)
- XY View: <https://youtu.be/jOWNSIwIA9k> (2018)


**Features:**

- Realtime data acquisition (20ms sampling, 100ms rolling display) based on MAVLink messages or ULOG data over MAVLink
- Timechart annotated by messages (MAVLink and ULog) and parameter changes (MAVLink only)
- XY Analysis for selected key-figures
- FFT for selected keyfigures
- 3D View (vehicle and observer perspective)
- MAVLink inspector (reporting raw MAVlink messages)
- Zooming (Click&Drag), pause / continue rolling update while continue recording in the background
- MAVLinkShell provides NutShell over MAVLink (Console)
- Map viewer of global/local position and raw gps data with option to record path (cached)
- Offline-mode: Import of key-figures from ULog (file or last log from device via WiFi)
- Save and load of collected data 
- Key figure conversion based on expressions
- FrSky Taranis USB supported in SITL
- PS4Controller support in OSX
- Low latency MJPEG based video stream display based on [uv4l](http://www.linux-projects.org) or any other source
- Video stream recorded as H264/MP4 file while collecting data
- Virtual (calculated) key figures added (Example here: [default definition file](https://github.com/ecmnet/MAVGCL/blob/master/MAVGCL/src/com/comino/flight/model/AnalysisDataModelMetaData.xml#L1000))
- RTCM3 base supported with automatic survey-in for UBlox devices (OS X only)

**Note** that some features (MSP) are only available if you run a companion on your vehicle using MAVComm .


**Requirements:**

- requires minimum  **Java 11** JRE 
- A companion running a serial-to-udp-proxy (either MAVComm or MAVROS, not required for PIXRacer)
- Video streaming from the vehicle requires an rtps mjpeg streaming service running on companion 

Note: maven building currently requires local mavcom, mavmap, mavbase and mavutils in local repository

**Binaries:**

Available binaries can be found [here](https://github.com/ecmnet/MAVGCL/releases).

Screenshots (Light Theme):

![alt tag](https://raw.github.com/ecmnet/MAVGCL/master/MAVGCL/image0.png)

![alt tag](https://raw.github.com/ecmnet/MAVGCL/master/MAVGCL/image1.png) 

![alt tag](https://raw.github.com/ecmnet/MAVGCL/master/MAVGCL/image2.png)

![alt tag](https://raw.github.com/ecmnet/MAVGCL/master/MAVGCL/image3.png) 

Screenshot (Dark Theme):

![alt tag](https://raw.github.com/ecmnet/MAVGCL/master/MAVGCL/image4.png) 

**How to define custom key-figure metadata files:**

- Refer to [example file](https://github.com/ecmnet/MAVGCL/blob/master/MAVGCL/ExampleKeyfigureMetaData.xml) or [default definition file](https://github.com/ecmnet/MAVGCL/blob/master/MAVGCL/src/com/comino/flight/model/AnalysisDataModelMetaData.xml#L515)
- Conversion based on expressions ( [exp4j](http://www.objecthunter.net/exp4j/#Built-in_functions) ).
  Example: `<Converter class="ExpressionConverter" expression="1.5 * sin(val)"/>`

**Limitations:**

- Limited to one device (MAVLink-ID '1')
- Default PX4Log/ULog keyfigure mapping still not [complete](https://github.com/ecmnet/MAVGCL/blob/master/MAVGCL/src/com/comino/flight/model/AnalysisDataModelMetaData.xml), but you can add your own definition file


**Note for developers:**

MAVGAnalysis depends on https://github.com/ecmnet/mavcom for MAVLink parsing.


Please note the [License terms](https://github.com/ecmnet/MAVGCL/blob/master/MAVGCL/LICENSE.md).

