void createTabs()
{
  // in case you want to receive a controlEvent when
  // a  tab is clicked, use activeEvent(true)
  controlP5.addTab("Tab1")
    .activateEvent(true)
    .setId(2)
    .setLabel("Tune")
    ;

  // in case you want to receive a controlEvent when
  // a  tab is clicked, use activeEvent(true)
  controlP5.addTab("Tab2")
    .activateEvent(true)
    .setId(3)
    .setLabel("Config")
    ;


  // in case you want to receive a controlEvent when
  // a  tab is clicked, use activeEvent(true)
  controlP5.addTab("Tab3")
    .activateEvent(true)
    .setId(4)
    .setLabel("Prefs")
    ;

  // in case you want to receive a controlEvent when
  // a  tab is clicked, use activeEvent(true)
  controlP5.addTab("Tab4")
    .activateEvent(true)
    .setId(5)
    .setLabel("Profile")
    ;


  // to rename the label of a tab, use setLabe("..."),
  // the name of the tab will remain as given when initialized.
  runTab = controlP5.getTab("default")
    .activateEvent(true)
    .setLabel("Run")
    .setId(1)
    ;
}

void populateDashTab()
{
  ConnectButton = controlP5.addButton("Connect")
    .setValue(0.0)
    .setPosition(commLeft, commTop)
    .setSize(60, 20)
    ;

  DisconnectButton = controlP5.addButton("Disconnect")
    .setValue(0.0)
    .setPosition(commLeft, commTop)
    .setSize(60, 20)
    ;

  Connecting = controlP5.addTextlabel("Connecting", "Connecting...", commLeft, commTop+3);

  // RadioButtons for available CommPorts
  r1 =controlP5.addDropdownList("select_comm", commLeft, commTop+25, 160, 80);

  CommPorts = Serial.list();
  for (int i=0; i<CommPorts.length; i++)
  {
    r1.addItem(CommPorts[i], i);
  }

  commH = 100;// 27+12*CommPorts.length;
  dashTop = commTop+commH+20;

  DisconnectButton.setVisible(false);
  Connecting.setVisible(false);

  // Dasboard
  AMButton = controlP5.addButton("Toggle_AM")
    .setValue(0.0)
    .setPosition(dashLeft, dashTop)
    .setSize(60, 20)
    ;  
  AMLabel = controlP5.addTextlabel("AM", "Manual", dashLeft+2, dashTop+22);            //
  SPField= controlP5.addTextfield("Setpoint", dashLeft, dashTop+40, 60, 20);         //   Buttons, Labels, and
  InField = controlP5.addTextfield("Input", dashLeft, dashTop+80, 60, 20);           //   Text Fields we'll be
  OutField = controlP5.addTextfield("Output", dashLeft, dashTop+120, 60, 20);         //   using

  AMCurrent = controlP5.addTextlabel("AMCurrent", "Manual", dashLeft+70, dashTop+15);   //
  SPLabel=controlP5.addTextlabel("SP", "3", dashLeft+70, dashTop+43);                  //
  InLabel=controlP5.addTextlabel("In", "1", dashLeft+70, dashTop+83);                  //
  OutLabel=controlP5.addTextlabel("Out", "2", dashLeft+70, dashTop+123);                // 

  controlP5.addButton("Send_Dash")
    .setValue(0.0)
    .setPosition(dashLeft, dashTop + 160)
    .setSize(160, 20)
    ;    
  int dashStatTop = configTop+490;
  for (int i=0; i<6; i++)
  { 
    controlP5.addTextlabel("dashstat"+i, "", configLeft, dashStatTop+12*i+5);
  }
  controlP5.addTextlabel("dashstatus", "Status", configLeft+9, dashStatTop-8);
}

void populateTuneTab()
{
  // Tunings
  PField = controlP5.addTextfield("Kp (Proportional)", tuneLeft, tuneTop, 60, 20);          //
  IField = controlP5.addTextfield("Ki (Integral)", tuneLeft, tuneTop+40, 60, 20);          //
  DField = controlP5.addTextfield("Kd (Derivative)", tuneLeft, tuneTop+80, 60, 20);          //

  DRButton = controlP5.addButton("Toggle_DR")
    .setValue(0.0)
    .setPosition(tuneLeft, tuneTop + 120)
    .setSize(60, 20)
    ;  
  DRLabel = controlP5.addTextlabel("DR", "Direct", tuneLeft+2, tuneTop+144);            //

  PLabel=controlP5.addTextlabel("P", "4", tuneLeft+70, tuneTop+3);                    //
  ILabel=controlP5.addTextlabel("I", "5", tuneLeft+70, tuneTop+43);                    //
  DLabel=controlP5.addTextlabel("D", "6", tuneLeft+70, tuneTop+83);                    //
  DRCurrent = controlP5.addTextlabel("DRCurrent", "Direct", tuneLeft+70, tuneTop+123);   //

  controlP5.addButton("Send_Tunings")
    .setValue(0.0)
    .setPosition(tuneLeft, tuneTop + 160)
    .setSize(160, 20)
    ; 

  PField.moveTo("Tab1"); 
  IField.moveTo("Tab1"); 
  DField.moveTo("Tab1");
  DRButton.moveTo("Tab1");  
  DRLabel.moveTo("Tab1"); 
  PLabel.moveTo("Tab1");
  ILabel.moveTo("Tab1"); 
  DLabel.moveTo("Tab1"); 
  DRCurrent.moveTo("Tab1");
  controlP5.getController("Send_Tunings").moveTo("Tab1");

  // Autotune
  oSField = controlP5.addTextfield("Output Step", ATLeft, ATTop, 60, 20);          //
  nField = controlP5.addTextfield("Noise Band", ATLeft, ATTop+40, 60, 20);          //
  lbField = controlP5.addTextfield("Look Back", ATLeft, ATTop+80, 60, 20);          //

  ATButton = controlP5.addButton("ATune_CMD")
    .setValue(0.0)
    .setPosition(ATLeft, ATTop + 120)
    .setSize(60, 20)
    ;   

  ATLabel = controlP5.addTextlabel("ATune", "OFF", ATLeft+2, ATTop+142);            //

  oSLabel=controlP5.addTextlabel("oStep", "4", ATLeft+70, ATTop+3);                    //
  nLabel=controlP5.addTextlabel("noise", "5", ATLeft+70, ATTop+43); 
  lbLabel=controlP5.addTextlabel("lback", "5", ATLeft+70, ATTop+83);   //
  ATCurrent = controlP5.addTextlabel("ATuneCurrent", "Start", ATLeft+70, ATTop+123);   //

  controlP5.addButton("Send_Auto_Tune")
    .setValue(0.0)
    .setPosition(ATLeft, ATTop + 160)
    .setSize(160, 20)
    ; 

  oSField.moveTo("Tab1"); 
  nField.moveTo("Tab1"); 
  lbField.moveTo("Tab1");
  ATButton.moveTo("Tab1");
  ATLabel.moveTo("Tab1");  
  oSLabel.moveTo("Tab1"); 
  nLabel.moveTo("Tab1");
  lbLabel.moveTo("Tab1");
  ATCurrent.moveTo("Tab1"); 
  controlP5.getController("Send_Auto_Tune").moveTo("Tab1");
}

void populateConfigTab()
{
  controlP5.addButton("Reset_Factory_Defaults")
    .setValue(0.0)
    .setPosition(RsLeft, RsTop + 160)
    .setSize(160, 20)
    ;   
  controlP5.getController("Reset_Factory_Defaults").moveTo("Tab2");

  commconfigLabel1 = controlP5.addTextlabel("spec6", "This area will populate when", configLeft, configTop); 
  commconfigLabel2 = controlP5.addTextlabel("spec7", "connection is established.", configLeft, configTop+15); 
  commconfigLabel1.moveTo("Tab2");
  commconfigLabel2.moveTo("Tab2");
}

void populatePrefTab()
{
  // Preferences
  for (int i=0; i<prefs.length; i++)
  {
    controlP5.addTextfield(prefs[i], 10, 30+40*i, 60, 20);    
    controlP5.getController(prefs[i]).moveTo("Tab3");
  }

  controlP5.addButton("Save_Preferences")
    .setValue(0.0)
    .setPosition(10, 30 + 40*prefs.length)
    .setSize(160, 20)
    ;   
  controlP5.getController("Save_Preferences").moveTo("Tab3");

  PopulatePrefVals();
}

void populateProfileTab()
{
  LBPref = controlP5.addListBox("Available Profiles")
    .setPosition(configLeft, configTop+5)
    .setSize(160, 15+15*profs.length);
  controlP5.addTextlabel("spec4", "Currently Displaying: ", configLeft+5, configTop+15+15*profs.length);

  int profStatTop = configTop+490;

  ProfCmd = controlP5.addButton("Run_Profile")
    .setValue(0.0)
    .setPosition(configLeft, profStatTop - 40)
    .setSize(160, 20)
    ;   

  ProfCmdStop = controlP5.addButton("Stop_Profile")
    .setValue(0.0)
    .setPosition(configLeft, profStatTop - 40)
    .setSize(160, 20)
    .setVisible(false)
    ; 

  for (int i=0; i<6; i++)
  { 
    controlP5.addTextlabel("profstat"+i, "", configLeft, profStatTop+12*i+5);
    controlP5.getController("profstat"+i).moveTo("Tab4");
  }

  controlP5.addTextlabel("profstatus", "Status", configLeft+9, profStatTop-8);
  controlP5.getController("profstatus").moveTo("Tab4");

  for (int i=0; i<profs.length; i++) LBPref.addItem(profs[i].Name, i);
  profSelLabel  = controlP5.addTextlabel("spec5", profs.length==0? "N/A" : profs[0].Name, configLeft+100, configTop+15+15*profs.length); 
  if (profs.length>0) LBPref.setValue(0.0);

  ProfButton = controlP5.addButton("Send_Profile")
    .setValue(0.0)
    .setPosition(configLeft, configTop +50+ 15 * profs.length)
    .setSize(160, 20)
    ; 

  LBPref.moveTo("Tab4");
  profSelLabel.moveTo("Tab4");
  ProfButton.moveTo("Tab4");
  ProfCmd.moveTo("Tab4");
  ProfCmdStop.moveTo("Tab4");
  controlP5.getController("spec4").moveTo("Tab4");
}

void addToRadioButton(RadioButton theRadioButton, String theName, int theValue ) 
{
  RadioButton t = theRadioButton.addItem(theName, theValue);
  t.getCaptionLabel().setColorBackground(color(80));
  t.getCaptionLabel().getStyle().movePadding(2, 0, -1, 2);
  t.getCaptionLabel().getStyle().moveMargin(-2, 0, 0, -3);
  t.getCaptionLabel().getStyle().backgroundWidth = 100;
}