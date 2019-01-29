package com.github.jakz.openmom.data.effect;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CompoundEffect extends Effect
{
  private final List<Effect> effects;

  protected CompoundEffect()
  {
    super(EffectType.compound);
    effects = new ArrayList<>();
  }
  
  public CompoundEffect(List<Effect> effects)
  {
    this();
    this.effects.addAll(effects);
  }
  
  public String toString() {
    return effects.stream()
      .map(Object::toString)
      .collect(Collectors.joining(", ", "( ", " )"));
  }
}
