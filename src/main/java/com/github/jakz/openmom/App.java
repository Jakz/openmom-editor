package com.github.jakz.openmom;

import java.awt.Dimension;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.esotericsoftware.yamlbeans.YamlReader;
import com.github.jakz.openmom.data.HouseType;
import com.github.jakz.openmom.data.Race;
import com.github.jakz.openmom.data.Ranged;
import com.github.jakz.openmom.data.Skill;
import com.github.jakz.openmom.data.SkillType;
import com.github.jakz.openmom.data.SpriteInfo;
import com.github.jakz.openmom.data.SpriteInfoLBX;
import com.github.jakz.openmom.data.Unit;
import com.github.jakz.openmom.data.effect.AbilityEffect;
import com.github.jakz.openmom.data.effect.CompoundEffect;
import com.github.jakz.openmom.data.effect.Effect;
import com.github.jakz.openmom.data.effect.EffectType;
import com.github.jakz.openmom.data.effect.Modifier;
import com.github.jakz.openmom.data.effect.MovementEffect;
import com.github.jakz.openmom.data.effect.ParametricAbilityEffect;
import com.github.jakz.openmom.data.effect.PropertyBonusEffect;
import com.github.jakz.openmom.data.effect.SpecialAttackEffect;
import com.github.jakz.openmom.lbx.LBX;
import com.github.jakz.openmom.lbx.SpriteSheet;
import com.github.jakz.openmom.ui.MainPanel;
import com.github.jakz.openmom.ui.TablePanel;
import com.github.jakz.openmom.ui.UnitTable;
import com.pixbits.lib.lang.Pair;
import com.pixbits.lib.ui.UIUtils;
import com.pixbits.lib.ui.WrapperFrame;
import com.pixbits.lib.ui.color.Color;
import com.pixbits.lib.ui.color.Palette;
import com.pixbits.lib.ui.table.DataSource;
import com.pixbits.lib.ui.table.ModifiableDataSource;
import com.pixbits.lib.yaml.YamlNode;
import com.pixbits.lib.yaml.YamlParser;
import com.pixbits.lib.yaml.YamlUnserializer;
import com.pixbits.lib.yaml.unserializer.ReflectiveUnserializer;
import com.pixbits.lib.yaml.unserializer.EnumUnserializer;
import com.pixbits.lib.yaml.unserializer.ListUnserializer;

public class App 
{
  static
  {
    try
    {
      System.loadLibrary("lbx");
    }
    catch (UnsatisfiedLinkError e)
    {
      e.printStackTrace();
      System.exit(1);
    }
  }
  
  static final Function<String, String> fieldNameFromYaml = n -> {
    StringBuilder b = new StringBuilder();
    
    for (int i = 0; i < n.length(); ++i)
    {
      if (n.charAt(i) == '_' && i < n.length()-1)
        b.append(Character.toUpperCase(n.charAt(i+1)));
      else
        b.append(n.charAt(i));
    }
    
    return b.toString();
  };
  
  static final Function<String, String> fieldNameToYaml = n -> {
    StringBuilder b = new StringBuilder();
    
    for (int i = 0; i < n.length(); ++i)
    {
      char c = n.charAt(i);
      if (Character.isUpperCase(c))
        b.append('_').append(Character.toLowerCase(c));
      else
        b.append(c);
    }
    
    return b.toString();
  };
  
  
  public static void main( String[] args )
  {
    try
    {      
      final Path base = Paths.get("../../data/yaml");
      final Data data = new Data();
            
      YamlParser parser = new YamlParser();
      
      parser.setReflectiveUnserializeFieldRemapper(fieldNameToYaml);
      
      parser.registerUnserializer(SpriteInfo.class, y -> {
        if (y.get(0).asString().equals("lbx"))
        {
          String lbx = y.get(1).asString() + ".lbx";
          int index = y.get(2).asInt();
          return new SpriteInfoLBX(lbx, index);
        }
        
        return null;
      });
      
      parser.registerUnserializer(Ranged.class, y -> {
        if (y.size() != 3)
          return new Ranged(Ranged.Type.none);
        else
        {
          int strength = y.get(0).asInt();
          YamlUnserializer<Ranged.Type> unserializer = y.environment().findUnserializer(Ranged.Type.class);
          Ranged.Type type = unserializer.unserialize(y.get(1));
          int ammo = y.get(2).asInt();
          return new Ranged(strength, type, ammo);
        }
      });
      
      parser.registerUnserializer(Modifier.class, y -> {
        if (!y.isSequence())
        {
          if (y.isInteger())
            return new Modifier(y.asInt(), "additive");
          else
            return new Modifier(y.asFloat(), "additive-level-based");

        }
        else
          return new Modifier(y.get(0).asFloat(), y.get(1).asString());
      });
      
      parser.registerUnserializer(Effect.class, y -> {
        String stype = y.get("type").asString();
        
        YamlUnserializer<Modifier> muns = y.environment().findUnserializer(Modifier.class);
        
        switch (stype)
        {
          case "movement": return new MovementEffect(y.get("kind").asString());
          case "ability": return new AbilityEffect(y.get("kind").asString());
          case "unit_bonus": return new PropertyBonusEffect(EffectType.unit_bonus, y.get("property").asString(), muns.unserialize(y.get("modifier")));
          case "army_bonus": return new PropertyBonusEffect(EffectType.army_bonus, y.get("property").asString(), muns.unserialize(y.get("modifier")));
          case "wizard_bonus": return new PropertyBonusEffect(EffectType.wizard_bonus, y.get("property").asString(), muns.unserialize(y.get("modifier")));
          case "parametric_ability": return new ParametricAbilityEffect(y.get("kind").asString(), y.get("value").asInt());
          case "special_attack": return new SpecialAttackEffect(y.get("kind").asString(), y.get("value").asInt());
          
          case "compound":
          {
            ListUnserializer<Effect> unserializer = new ListUnserializer<>(Effect.class);
            List<Effect> effects = unserializer.unserialize(y.get("elements"));
            return new CompoundEffect(effects);
          }
        }
        
        return Effect.unknown();
      });
      
      parser.registerUnserializer(HouseType.class, new EnumUnserializer<HouseType>(HouseType.class));
      parser.registerUnserializer(SkillType.class, new EnumUnserializer<SkillType>(SkillType.class, s -> s.equals("native") ? "_native" : s));
      parser.registerUnserializer(EffectType.class, new EnumUnserializer<EffectType>(EffectType.class, s -> s, true));     

      /* races.yaml */
      {
        Path path = base.resolve("races.yaml");
        
        YamlNode root = parser.parse(path).get("races");
        YamlUnserializer<List<Race>> unserializer = new ListUnserializer<>(Race.class);
        List<Race> races = unserializer.unserialize(root);
        data.races = ModifiableDataSource.of(races);
      }
      
      
      /* units.yaml */
      {
        Path path = base.resolve("units.yaml");
     

        
        YamlNode root = parser.parse(path).get("units");
        
        ReflectiveUnserializer<Unit> uns = new ReflectiveUnserializer<>(Unit.class);
        
        List<Unit> units = new ArrayList<>();
        
        for (YamlNode node : root)
        {
          Unit unit = uns.unserialize(node);
          units.add(unit);
          //System.out.println(unit.identifier+", "+unit.type+", "+unit.upkeep+", "+unit.visuals.i18n/*+", "+String.join(", ", unit.skills)*/);
        }
        
        data.units = ModifiableDataSource.of(units);
      }
      
      /* skills.yaml */
      {
        Path path = base.resolve("skills.yaml");
        YamlNode root = parser.parse(path);
        
        List<Skill> skills = new ArrayList<>();
        
        ReflectiveUnserializer<Skill> suns = new ReflectiveUnserializer<>(Skill.class);
        
        for (YamlNode node : root.get("skills"))
        {
          Skill skill = suns.unserialize(node);
          skills.add(skill);
        }
        
        data.skills = ModifiableDataSource.of(skills);
      }

      UIUtils.setNimbusLNF();

      MainPanel mainPanel = new MainPanel(data);
      
      WrapperFrame<?> frame = UIUtils.buildFrame(mainPanel, "OpenMoM Editor v0.1");
      frame.exitOnClose();
      frame.setVisible(true);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}
