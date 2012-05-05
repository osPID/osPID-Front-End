void createTabs()
{
  controlP5.tab("Tab1").activateEvent(true);
  controlP5.tab("Tab1").setId(2);
  controlP5.tab("Tab1").setLabel("Tune");

  // in case you want to receive a controlEvent when
  // a  tab is clicked, use activeEvent(true)
  controlP5.tab("Tab2").activateEvent(true);
  controlP5.tab("Tab2").setId(3);
  controlP5.tab("Tab2").setLabel("Config");

  // in case you want to receive a controlEvent when
  // a  tab is clicked, use activeEvent(true)
  controlP5.tab("Tab3").activateEvent(true);
  controlP5.tab("Tab3").setId(4);
  controlP5.tab("Tab3").setLabel("Prefs");
  
   // in case you want to receive a controlEvent when
  // a  tab is clicked, use activeEvent(true)
  controlP5.tab("Tab4").activateEvent(true);
  controlP5.tab("Tab4").setId(5);
  controlP5.tab("Tab4").setLabel("Profile");

  controlP5.tab("default").activateEvent(true);
  // to rename the label of a tab, use setLabe("..."),
  // the name of the tab will remain as given when initialized.
  controlP5.tab("default").setLabel("Run");
  controlP5.tab("default").setId(1);

}

void populateDashTab()
{

  ConnectButton = controlP5.addButton("Connect",0.0,commLeft,commTop,60,20);
  DisconnectButton = controlP5.addButton("Disconnect",0.0,commLeft,commTop,60, 20);
  Connecting = controlP5.addTextlabel("Connecting","Connecting...",commLeft,commTop+3);

  //RadioButtons for available CommPorts
  r1 = controlP5.addRadioButton("radioButton",commLeft,commTop+27);
  r1.setColorForeground(color(120));
  r1.setColorActive(color(255));
  r1.setColorLabel(color(255));
  r1.setItemsPerRow(1);
  r1.setSpacingColumn(75);

  CommPorts = Serial.list();
  for(int i=0;i<CommPorts.length;i++)
  {
    addToRadioButton(r1,CommPorts[i],i); 
  }
  if(CommPorts.length>0) r1.getItem(0).setState(true);
  commH = 27+12*CommPorts.length;
  dashTop = commTop+commH+20;

  DisconnectButton.setVisible(false);
  Connecting.setVisible(false);

  //dasboard

  AMButton = controlP5.addButton("Toggle_AM",0.0,dashLeft,dashTop,60,20);      //
  AMLabel = controlP5.addTextlabel("AM","Manual",dashLeft+2,dashTop+22);            //
  SPField= controlP5.addTextfield("Setpoint",dashLeft,dashTop+40,60,20);         //   Buttons, Labels, and
  InField = controlP5.addTextfield("Input",dashLeft,dashTop+80,60,20);           //   Text Fields we'll be
  OutField = controlP5.addTextfield("Output",dashLeft,dashTop+120,60,20);         //   using

  AMCurrent = controlP5.addTextlabel("AMCurrent","Manual",dashLeft+70,dashTop+15);   //
  SPLabel=controlP5.addTextlabel("SP","3",dashLeft+70,dashTop+43);                  //
  InLabel=controlP5.addTextlabel("In","1",dashLeft+70,dashTop+83);                  //
  OutLabel=controlP5.addTextlabel("Out","2",dashLeft+70,dashTop+123);                // 
  controlP5.addButton("Send_Dash",0.0,dashLeft,dashTop+160,160,20);         //
  int dashStatTop = configTop+490;
  for(int i=0;i<6;i++)
 { 
   controlP5.addTextlabel("dashstat"+i, "", configLeft, dashStatTop+12*i+5);
 }
  controlP5.addTextlabel("dashstatus", "Status", configLeft+9, dashStatTop-8);
}

void populateTuneTab()
{
  //tunings
  PField = controlP5.addTextfield("Kp (Proportional)",tuneLeft,tuneTop,60,20);          //
  IField = controlP5.addTextfield("Ki (Integral)",tuneLeft,tuneTop+40,60,20);          //
  DField = controlP5.addTextfield("Kd (Derivative)",tuneLeft,tuneTop+80,60,20);          //
  DRButton = controlP5.addButton("Toggle_DR",0.0,tuneLeft,tuneTop+120,60,20);      //
  DRLabel = controlP5.addTextlabel("DR","Direct",tuneLeft+2,tuneTop+144);            //

  PLabel=controlP5.addTextlabel("P","4",tuneLeft+70,tuneTop+3);                    //
  ILabel=controlP5.addTextlabel("I","5",tuneLeft+70,tuneTop+43);                    //
  DLabel=controlP5.addTextlabel("D","6",tuneLeft+70,tuneTop+83);                    //
  DRCurrent = controlP5.addTextlabel("DRCurrent","Direct",tuneLeft+70,tuneTop+123);   //
  controlP5.addButton("Send_Tunings",0.0,tuneLeft,tuneTop+160,160,20);         //  

  PField.moveTo("Tab1"); 
  IField.moveTo("Tab1"); 
  DField.moveTo("Tab1");
  DRButton.moveTo("Tab1");  
  DRLabel.moveTo("Tab1"); 
  PLabel.moveTo("Tab1");
  ILabel.moveTo("Tab1"); 
  DLabel.moveTo("Tab1"); 
  DRCurrent.moveTo("Tab1");
  controlP5.controller("Send_Tunings").moveTo("Tab1");



  //Autotune
  oSField = controlP5.addTextfield("Output Step",ATLeft,ATTop,60,20);          //
  nField = controlP5.addTextfield("Noise Band",ATLeft,ATTop+40,60,20);          //
  lbField = controlP5.addTextfield("Look Back",ATLeft,ATTop+80,60,20);          //
  ATButton = controlP5.addButton("ATune_CMD",0.0,ATLeft,ATTop+120,60,20);      //
  ATLabel = controlP5.addTextlabel("ATune","OFF",ATLeft+2,ATTop+142);            //

  oSLabel=controlP5.addTextlabel("oStep","4",ATLeft+70,ATTop+3);                    //
  nLabel=controlP5.addTextlabel("noise","5",ATLeft+70,ATTop+43); 
  lbLabel=controlP5.addTextlabel("lback","5",ATLeft+70,ATTop+83);   //
  ATCurrent = controlP5.addTextlabel("ATuneCurrent","Start",ATLeft+70,ATTop+123);   //
  controlP5.addButton("Send_Auto_Tune",0.0,ATLeft,ATTop+160,160,20);         //  

  oSField.moveTo("Tab1"); 
  nField.moveTo("Tab1"); 
  lbField.moveTo("Tab1");
  ATButton.moveTo("Tab1");
  ATLabel.moveTo("Tab1");  
  oSLabel.moveTo("Tab1"); 
  nLabel.moveTo("Tab1");
    lbLabel.moveTo("Tab1");
  ATCurrent.moveTo("Tab1"); 
  controlP5.controller("Send_Auto_Tune").moveTo("Tab1"); 
}

void populateConfigTab()
{
  controlP5.addButton("Reset_Factory_Defaults",0.0,RsLeft,RsTop,160,20);         //
  controlP5.controller("Reset_Factory_Defaults").moveTo("Tab2");

commconfigLabel1 = controlP5.addTextlabel("spec6","This area will populate when", configLeft,configTop); 
commconfigLabel2 = controlP5.addTextlabel("spec7","connection is established.", configLeft,configTop+15); 
commconfigLabel1.moveTo("Tab2");
commconfigLabel2.moveTo("Tab2");
}

void populatePrefTab()
{
   //preferences
  for(int i=0;i<prefs.length;i++)
  {
    controlP5.addTextfield(prefs[i],10,30+40*i,60,20);    
    controlP5.controller(prefs[i]).moveTo("Tab3");
  }

  controlP5.addButton("Save_Preferences", 0.0, 10,30+40*prefs.length,160,20);
  controlP5.controller("Save_Preferences").moveTo("Tab3");

  PopulatePrefVals(); 
}

void populateProfileTab(){
 LBPref = controlP5.addListBox("Available Profiles",configLeft,configTop+5,160,120);
 controlP5.addTextlabel("spec4","Currently Displaying: ", configLeft+5,configTop+10+15*profs.length);
 ProfButton = controlP5.addButton("Send_Profile",0.0,configLeft,configTop+25+15*profs.length,160,20);


 int profStatTop = configTop+490;
  ProfCmd = controlP5.addButton("Run_Profile",0.0,configLeft,profStatTop-40,160,20);
  ProfCmdStop = controlP5.addButton("Stop_Profile",0.0,configLeft,profStatTop-40,160,20);
  ProfCmdStop.setVisible(false);
 for(int i=0;i<6;i++)
 { 
   controlP5.addTextlabel("profstat"+i,"", configLeft, profStatTop+12*i+5);
   controlP5.controller("profstat"+i).moveTo("Tab4");
 }
 controlP5.addTextlabel("profstatus", "Status", configLeft+9, profStatTop-8);
   controlP5.controller("profstatus").moveTo("Tab4");
 
 for(int i=0;i<profs.length;i++) LBPref.addItem(profs[i].Name, i);
 profSelLabel  = controlP5.addTextlabel("spec5",profs.length==0? "N/A" : profs[0].Name, configLeft+100,configTop+10+15*profs.length); 
 
 LBPref.moveTo("Tab4");
 profSelLabel.moveTo("Tab4");
  ProfButton.moveTo("Tab4");
  ProfCmd.moveTo("Tab4");
  ProfCmdStop.moveTo("Tab4");
 controlP5.controller("spec4").moveTo("Tab4");
}

void addToRadioButton(RadioButton theRadioButton, String theName, int theValue ) {
  Toggle t = theRadioButton.addItem(theName,theValue);
  t.captionLabel().setColorBackground(color(80));
  t.captionLabel().style().movePadding(2,0,-1,2);
  t.captionLabel().style().moveMargin(-2,0,0,-3);
  t.captionLabel().style().backgroundWidth = 100;
}

