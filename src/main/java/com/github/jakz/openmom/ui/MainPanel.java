package com.github.jakz.openmom.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.github.jakz.openmom.Data;
import com.github.jakz.openmom.data.SpriteInfo;
import com.github.jakz.openmom.data.SpriteInfoLBX;
import com.github.jakz.openmom.lbx.LBX;
import com.pixbits.lib.ui.table.DataSource;

public class MainPanel extends JPanel
{
  private JTabbedPane tabs;
  
  private UnitTable unitTable;
  private RaceTable raceTable;
  private SkillTable skillTable;

  private LBXTable lbxTable;
  
  public MainPanel(Data data)
  {
    tabs = new JTabbedPane(JTabbedPane.LEFT);

    raceTable = new RaceTable(data.races);
    TablePanel raceTablePanel = new TablePanel(raceTable, new Dimension(1024, 600));
    tabs.addTab("Races", raceTablePanel);
    
    unitTable = new UnitTable(data.units);
    TablePanel unitTablePanel = new TablePanel(unitTable, new Dimension(1024,600));
    tabs.addTab("Units", unitTablePanel);  
    
    skillTable = new SkillTable(data.skills);
    TablePanel skillTablePanel = new TablePanel(skillTable, new Dimension(1024,600));
    tabs.addTab("Skills", skillTablePanel);  

    tabs.addTab("LBX", new LBXMultiTable());  

    this.setLayout(new BorderLayout());
    this.add(tabs, BorderLayout.CENTER);
  }
}
