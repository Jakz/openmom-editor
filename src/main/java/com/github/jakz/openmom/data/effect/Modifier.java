package com.github.jakz.openmom.data.effect;

import java.util.Locale;

public class Modifier
{
  public static enum Type
  {
    ADDITIVE,
    ADDITIVE_LEVEL_BASED,
    FIXED,
  };
  
  String type;
  float fvalue;
  
  public Modifier(float value, String type)
  {
    this.fvalue = value;
    this.type = type;
  }
  
  public String toString() { return String.format(Locale.US, "%1.1f, %s", fvalue, type); } 
}
