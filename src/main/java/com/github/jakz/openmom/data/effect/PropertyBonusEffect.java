package com.github.jakz.openmom.data.effect;

public class PropertyBonusEffect extends Effect
{
  public String property;
  public Modifier modifier;
  
  public PropertyBonusEffect(EffectType type, String property, Modifier modifier)
  {
    super(type);
    this.property = property;
    this.modifier = modifier;
  }

  public String toString() {
    return String.format("(%s, %s, %s)", type, property, modifier.toString());
  }
}
