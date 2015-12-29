/********************************************************
 * os PID Tuning Front-End,  Version 0.7
 * by Brett Beauregard
 * License: GPLv3
 * December 2015
 *
 * This application is written in processing and is
 * designed to interface with the osPID.  From this 
 * Control Panel you can observe & adjust PID 
 * performance in  real time
 *
 * The ControlP5 library (v2.2.5) is required to run this sketch.
 * files and install instructions can be found at
 * http://www.sojamo.de/libraries/controlP5/
 * 
 ********************************************************/

Updates for version 0.7
- Support for Processing v3.0.1
- Support for ControlP5 v2.2.5 
- Tested on Java version 1.7.0_91 (Linux) & 1.8.0_66 (Windows)
- Removed export application on Mac (please help to export!)
- Known issue: Upon successful connection to the osPID Kit, clicking on any of 
  the other tabs is required before the frontend responds to any other operation  
 
Updates for version 0.6
- Fixed "\\" bug that was making the software fail on MAC
- Added support for Temperature_Input_V1.2 card ("IID2")
 
Updates for version 0.5
- Support for different types of IO cards
- Reflow profile support
- Minor graph bug fixes
