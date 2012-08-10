RadioButton protIr1, protIr2, protIr3, protIr4;
Textfield protItxt1, protItxt2, protItxt3, protItxt4;

RadioButton protOr1, protOr2, protOr3, protOr4;
Textfield protOtxt1, protOtxt2, protOtxt3, protOtxt4;

String InputCard="", OutputCard="";
ArrayList InputControls = new ArrayList(), OutputControls=new ArrayList(); //in case we need to kill them mid-run

void ClearInput()
{
  for(int i=0;i<InputControls.size();i++)
  {
    if(InputControls.get(i).getClass().equals(controlP5.RadioButton.class))
    {
      ((ControllerGroup)InputControls.get(i)).remove();
    }
    else ((Controller)InputControls.get(i)).remove();
  }  
    InputCard = "";
}
void ClearOutput()
{
  for(int i=0;i<OutputControls.size();i++)
  {
    if(OutputControls.get(i).getClass().equals(controlP5.RadioButton.class))
    {
      ((ControllerGroup)OutputControls.get(i)).remove();
    }
    else ((Controller)OutputControls.get(i)).remove();
  }
    OutputCard = "";
}

void CreateUI(String cardID, String tab, int top)
{
  if(cardID.equals("IID1") || cardID.equals("IID2"))
  {

    ClearInput();
    InputCard = cardID;
    InputControls.clear();


    controlP5.addTextlabel("spec0","Specify which input to use: ", configLeft,top);

    r2 = controlP5.addRadioButton("radioButton2",configLeft,top+22);
    r2.setColorForeground(color(120));
    r2.setColorActive(color(255));
    r2.setColorLabel(color(255));
    r2.setItemsPerRow(1);
    r2.setSpacingColumn(75);
    addToRadioButton(r2,"Thermocouple",1);
    addToRadioButton(r2,"Thermistor",2);
    r2.getItem(0).setState(true);

    controlP5.addTextlabel("spec1","Thermistor Coefficients: ", configLeft,top+70);
    controlP5.addTextlabel("T","T    =", configLeft+5,top+90);
    controlP5.addTextlabel("00","0", configLeft+10,top+95);
    controlP5.addTextlabel("R","R    =", configLeft+5,top+115);
    controlP5.addTextlabel("01","0", configLeft+12,top+120);
    controlP5.addTextlabel("Beta","Beta =", configLeft+5,top+140);
    T0Field= controlP5.addTextfield("",configLeft+45,top+84,60,20);         //   Buttons, Labels, and
    R0Field = controlP5.addTextfield(" ",configLeft+45,top+109,60,20);           //   Text Fields we'll be
    BetaField = controlP5.addTextfield("  ",configLeft+45,top+136,60,20);         //   using
    controlP5.addButton("Send_Input_Config",0.0,configLeft,top+180,160,20);

    String[] names = {
      "spec1", "T", "00", "R", "01", "Beta", "spec0", "Send_Input_Config"                              };
    for(int i=0;i<names.length;i++)
    {
      controlP5.controller(names[i]).moveTo(tab);
      InputControls.add(controlP5.controller(names[i])); 
    }

    r2.moveTo(tab); 
    T0Field.moveTo(tab); 
    R0Field.moveTo(tab); 
    BetaField.moveTo(tab);  
    InputControls.add(r2); 
    InputControls.add(T0Field); 
    InputControls.add(R0Field); 
    InputControls.add(BetaField); 

  }
  else if(cardID.equals("OID1"))
  {
 
    ClearOutput();
    OutputCard = "OID1";   
    controlP5.addTextlabel("spec3","Specify which output to use: ", configLeft,top);                  

    r3 = controlP5.addRadioButton("radioButton3",configLeft,top+22);
    r3.setColorForeground(color(120));
    r3.setColorActive(color(255));
    r3.setColorLabel(color(255));
    r3.setItemsPerRow(1);
    r3.setSpacingColumn(75);

    addToRadioButton(r3,"Onboard Relay",1);
    addToRadioButton(r3,"Digital Output",2);
    r3.getItem(0).setState(true);

    controlP5.addTextlabel("spec2","Relay Output Window: ", configLeft,top+56);  
    controlP5.addTextlabel("sec","Seconds = ", configLeft+5,top+76);  
    oSecField = controlP5.addTextfield("   ",configLeft+55,top+70,50,20);
    controlP5.addButton("Send_Output_Config",0.0,configLeft,top+180,160,20);         //    
    String[] names = {
      "spec2","spec3","sec","   ", "Send_Output_Config"                              };
    for(int i=0;i<names.length;i++)
    {
      controlP5.controller(names[i]).moveTo(tab);
      OutputControls.add(controlP5.controller(names[i]));
    }
    r3.moveTo(tab);  
    OutputControls.add(r3);
  }
  else if(cardID.equals("IID0"))
  {

    ClearInput();
    InputCard = "IID0";
    InputControls.clear();

    protIr1 = controlP5.addRadioButton("protIr1",configLeft,top);
    protIr1.setColorForeground(color(120));
    protIr1.setColorActive(color(255));
    protIr1.setColorLabel(color(255));
    protIr1.setItemsPerRow(2);
    protIr1.setSpacingColumn(75);
    addToRadioButton(protIr1,"On 1",1);
    addToRadioButton(protIr1,"Off 1",2);
    protIr1.getItem(0).setState(true);
       protIr1.getItem(0).captionLabel().style().backgroundWidth = 45;
       protIr1.getItem(1).captionLabel().style().backgroundWidth = 45;
       
    protIr2 = controlP5.addRadioButton("protIr2",configLeft,top+18);
    protIr2.setColorForeground(color(120));
    protIr2.setColorActive(color(255));
    protIr2.setColorLabel(color(255));
    protIr2.setItemsPerRow(2);
    protIr2.setSpacingColumn(75);
    addToRadioButton(protIr2,"On 2",1);
    addToRadioButton(protIr2,"Off 2",2);
    protIr2.getItem(0).setState(true);
       protIr2.getItem(0).captionLabel().style().backgroundWidth = 45;
       protIr2.getItem(1).captionLabel().style().backgroundWidth = 45;

    
    protIr3 = controlP5.addRadioButton("protIr3",configLeft,top+36);
    protIr3.setColorForeground(color(120));
    protIr3.setColorActive(color(255));
    protIr3.setColorLabel(color(255));
    protIr3.setItemsPerRow(2);
    protIr3.setSpacingColumn(75);
    addToRadioButton(protIr3,"On 3",1);
    addToRadioButton(protIr3,"Off 3",2);
    protIr3.getItem(0).setState(true);
       protIr3.getItem(0).captionLabel().style().backgroundWidth = 45;
       protIr3.getItem(1).captionLabel().style().backgroundWidth = 45;
       
    protIr4 = controlP5.addRadioButton("protIr4",configLeft,top+54);
    protIr4.setColorForeground(color(120));
    protIr4.setColorActive(color(255));
    protIr4.setColorLabel(color(255));
    protIr4.setItemsPerRow(2);
    protIr4.setSpacingColumn(75);
    addToRadioButton(protIr4,"On 4",1);
    addToRadioButton(protIr4,"Off 4",2);
    protIr4.getItem(0).setState(true);
           protIr4.getItem(0).captionLabel().style().backgroundWidth = 45;
       protIr4.getItem(1).captionLabel().style().backgroundWidth = 45;
       
       controlP5.addButton("Send_Input_Config",0.0,configLeft,top+180,160,20);
   
   protItxt1 = controlP5.addTextfield("      ",configLeft+85,top+74,60,20);
   protItxt2 = controlP5.addTextfield("       ",configLeft+85,top+99,60,20);
   protItxt3 = controlP5.addTextfield("        ",configLeft+85,top+124,60,20);
   protItxt4 = controlP5.addTextfield("         ",configLeft+85,top+149,60,20);
       
    controlP5.addTextlabel("protIlbl1", "Float 1", configLeft, top+79);
    controlP5.addTextlabel("protIlbl2", "Float 2", configLeft, top+104);
    controlP5.addTextlabel("protIlbl3", "Float 3", configLeft, top+129);
    controlP5.addTextlabel("protIlbl4", "Float 4", configLeft, top+154);    

    String[] names = {"Send_Input_Config", "protIlbl1"  
, "protIlbl2", "protIlbl3", "protIlbl4"      };
    for(int i=0;i<names.length;i++)
    {
      controlP5.controller(names[i]).moveTo(tab);
      InputControls.add(controlP5.controller(names[i])); 
    }

    protIr1.moveTo(tab); 
    protIr2.moveTo(tab); 
    protIr3.moveTo(tab); 
    protIr4.moveTo(tab);  
    protItxt1.moveTo(tab);
    protItxt2.moveTo(tab);
    protItxt3.moveTo(tab);
    protItxt4.moveTo(tab);
    InputControls.add(protIr1); 
    InputControls.add(protIr2); 
    InputControls.add(protIr3); 
    InputControls.add(protIr4); 
    InputControls.add(protItxt1); 
    InputControls.add(protItxt2);
    InputControls.add(protItxt3);
    InputControls.add(protItxt4);

  }
else if(cardID.equals("OID0"))
  {

    ClearOutput();
    OutputCard = "OID0";
    OutputControls.clear();

    protOr1 = controlP5.addRadioButton("protOr1",configLeft,top);
    protOr1.setColorForeground(color(120));
    protOr1.setColorActive(color(255));
    protOr1.setColorLabel(color(255));
    protOr1.setItemsPerRow(2);
    protOr1.setSpacingColumn(75);
    addToRadioButton(protOr1,"On 1",1);
    addToRadioButton(protOr1,"Off 1",2);
    protOr1.getItem(0).setState(true);
       protOr1.getItem(0).captionLabel().style().backgroundWidth = 45;
       protOr1.getItem(1).captionLabel().style().backgroundWidth = 45;
       
    protOr2 = controlP5.addRadioButton("protOr2",configLeft,top+18);
    protOr2.setColorForeground(color(120));
    protOr2.setColorActive(color(255));
    protOr2.setColorLabel(color(255));
    protOr2.setItemsPerRow(2);
    protOr2.setSpacingColumn(75);
    addToRadioButton(protOr2,"On 2",1);
    addToRadioButton(protOr2,"Off 2",2);
    protOr2.getItem(0).setState(true);
       protOr2.getItem(0).captionLabel().style().backgroundWidth = 45;
       protOr2.getItem(1).captionLabel().style().backgroundWidth = 45;

    
    protOr3 = controlP5.addRadioButton("protOr3",configLeft,top+36);
    protOr3.setColorForeground(color(120));
    protOr3.setColorActive(color(255));
    protOr3.setColorLabel(color(255));
    protOr3.setItemsPerRow(2);
    protOr3.setSpacingColumn(75);
    addToRadioButton(protOr3,"On 3",1);
    addToRadioButton(protOr3,"Off 3",2);
    protOr3.getItem(0).setState(true);
       protOr3.getItem(0).captionLabel().style().backgroundWidth = 45;
       protOr3.getItem(1).captionLabel().style().backgroundWidth = 45;
       
    protOr4 = controlP5.addRadioButton("protOr4",configLeft,top+54);
    protOr4.setColorForeground(color(120));
    protOr4.setColorActive(color(255));
    protOr4.setColorLabel(color(255));
    protOr4.setItemsPerRow(2);
    protOr4.setSpacingColumn(75);
    addToRadioButton(protOr4,"On 4",1);
    addToRadioButton(protOr4,"Off 4",2);
    protOr4.getItem(0).setState(true);
           protOr4.getItem(0).captionLabel().style().backgroundWidth = 45;
       protOr4.getItem(1).captionLabel().style().backgroundWidth = 45;
       
       controlP5.addButton("Send_Output_Config",0.0,configLeft,top+180,160,20);
   
   protOtxt1 = controlP5.addTextfield("          ",configLeft+85,top+74,60,20);
   protOtxt2 = controlP5.addTextfield("           ",configLeft+85,top+99,60,20);
   protOtxt3 = controlP5.addTextfield("            ",configLeft+85,top+124,60,20);
   protOtxt4 = controlP5.addTextfield("             ",configLeft+85,top+149,60,20);
       
    controlP5.addTextlabel("protOlbl1", "Float 1", configLeft, top+79);
    controlP5.addTextlabel("protOlbl2", "Float 2", configLeft, top+104);
    controlP5.addTextlabel("protOlbl3", "Float 3", configLeft, top+129);
    controlP5.addTextlabel("protOlbl4", "Float 4", configLeft, top+154);    

    String[] names = {"Send_Output_Config", "protOlbl1"  
, "protOlbl2", "protOlbl3", "protOlbl4"      };
    for(int i=0;i<names.length;i++)
    {
      controlP5.controller(names[i]).moveTo(tab);
      OutputControls.add(controlP5.controller(names[i])); 
    }

    protOr1.moveTo(tab); 
    protOr2.moveTo(tab); 
    protOr3.moveTo(tab); 
    protOr4.moveTo(tab);  
    protOtxt1.moveTo(tab);
    protOtxt2.moveTo(tab);
    protOtxt3.moveTo(tab);
    protOtxt4.moveTo(tab);
    OutputControls.add(protOr1); 
    OutputControls.add(protOr2); 
    OutputControls.add(protOr3); 
    OutputControls.add(protOr4); 
    OutputControls.add(protOtxt1); 
    OutputControls.add(protOtxt2);
    OutputControls.add(protOtxt3);
    OutputControls.add(protOtxt4);
                
  }
}

void PopulateCardFields(String cardName, String[] fields)
{
  if(cardName.equals("IID1") || cardName.equals("IID2"))
  {
    int v = int(fields[1]); 
    if(v==0) r2.getItem(0).setState(true);
    else if(v==1) r2.getItem(1).setState(true);

    R0Field.setText(fields[2]);    //   the arduino,  take the
    BetaField.setText(fields[3]);    //   current values and put
    T0Field.setText(fields[4]);

  }
  else if(cardName.equals("IID0"))
  {
    int v1 = int(fields[1]); 
    int v2 = int(fields[2]); 
    int v3 = int(fields[3]); 
    int v4 = int(fields[4]); 
    protIr1.getItem(v1==1?0:1).setState(true);
    protIr2.getItem(v2==1?0:1).setState(true);
    protIr3.getItem(v3==1?0:1).setState(true);
    protIr4.getItem(v4==1?0:1).setState(true);
  
    protItxt1.setText(fields[5]);
    protItxt2.setText(fields[6]);
    protItxt3.setText(fields[7]);
    protItxt4.setText(fields[8]);    

  }
  else if(cardName.equals("OID1"))
  {
    int v = int(fields[1]);
    if(v==0) r3.getItem(0).setState(true);
    else r3.getItem(1).setState(true);
    oSecField.setText(fields[2]);
  }
  else if(cardName.equals("OID0"))
  {
    int v1 = int(fields[1]); 
    int v2 = int(fields[2]); 
    int v3 = int(fields[3]); 
    int v4 = int(fields[4]); 
    protOr1.getItem(v1==1?0:1).setState(true);
    protOr2.getItem(v2==1?0:1).setState(true);
    protOr3.getItem(v3==1?0:1).setState(true);
    protOr4.getItem(v4==1?0:1).setState(true);
  
    protOtxt1.setText(fields[5]);
    protOtxt2.setText(fields[6]);
    protOtxt3.setText(fields[7]);
    protOtxt4.setText(fields[8]);    

  }
}


void Send_Input_Config()
{  //build the send string for the appropriate input card

  if(InputCard.equals("IID1") || InputCard.equals("IID2"))
  {
    myPort.write(byte(5));
    Byte a =0;
    if(r2.getState(1)==true)a=1;
    myPort.write(a);

    myPort.write(floatArrayToByteArray(new float[]{
      float(R0Field.getText()),
      float(BetaField.getText()), 
      float(T0Field.getText()),
      float(R0Field.getText()),//hidden reference resistance
    }
    ));
  }
  else if(InputCard.equals("IID0"))
  {
    myPort.write(byte(5));
    Byte a =0;
    myPort.write( protIr1.getState(0)==true? (byte) 0 : (byte)1);
    myPort.write( protIr2.getState(0)==true? (byte) 0 : (byte)1);
    myPort.write( protIr3.getState(0)==true? (byte) 0 : (byte)1);
    myPort.write( protIr4.getState(0)==true? (byte) 0 : (byte)1);    
    
    myPort.write(floatArrayToByteArray(new float[]{
      float(protItxt1.getText()),
      float(protItxt2.getText()), 
      float(protItxt3.getText()),
      float(protItxt4.getText()),
    }
    ));
  }
}

void Send_Output_Config()
{
  byte[] toSend;
  if(OutputCard.equals("OID1"))
  {
    myPort.write(byte(6));
    byte o = r3.getState(0)==true ? (byte)0 : (byte)1;

    myPort.write(o);
    myPort.write(floatArrayToByteArray(new float[]{
      float(oSecField.getText())                }
    ));
  }
  else if(OutputCard.equals("OID0"))
  {
    myPort.write(byte(6));

    myPort.write( protOr1.getState(0)==true? (byte) 0 : (byte)1);
    myPort.write( protOr2.getState(0)==true? (byte) 0 : (byte)1);
    myPort.write( protOr3.getState(0)==true? (byte) 0 : (byte)1);
    myPort.write( protOr4.getState(0)==true? (byte) 0 : (byte)1);    
    
    myPort.write(floatArrayToByteArray(new float[]{
      float(protOtxt1.getText()),
      float(protOtxt2.getText()), 
      float(protOtxt3.getText()),
      float(protOtxt4.getText()),
    }
    ));
  }
}








