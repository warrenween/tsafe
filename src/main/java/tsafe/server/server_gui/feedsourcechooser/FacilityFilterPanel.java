/*
 TSAFE Prototype: A decision support tool for air traffic controllers
 Copyright (C) 2003  Gregory D. Dennis

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package tsafe.server.server_gui.feedsourcechooser;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;

import tsafe.common_datastructures.TSAFEProperties;
import tsafe.server.server_gui.utils.LayoutUtils;
import tsafe.server.server_gui.utils.table.DefaultSortTableModel;
import tsafe.server.server_gui.utils.table.JSortTable;
import tsafe.server.server_gui.utils.table.TableUtils;
import fig.io.FIGFileContentFilter;

/**
 * Allows a user to filter messages based on the ETMS-assigned facility.
 */
class FacilityFilterPanel extends FilterPanel {


    //
    // CONSTANTS
    //

    /**
     * The facility table's column names.
     */
    private final static Vector FACILITY_TABLE_COLUMNS;
    static {
        FACILITY_TABLE_COLUMNS = new Vector();
        FACILITY_TABLE_COLUMNS.add("Facility Code");
        FACILITY_TABLE_COLUMNS.add("Description");
    }

    /**
     * The index of the "Facility" table column.
     */
    private final static int COLUMN_FACILITY = 0;

    /**
     * The width of the "Facility" table column.
     */
    private final static int COLUMN_WIDTH_FACILITY = 125;

    /**
     * The index of the "Description" table column.
     */
    private final static int COLUMN_DESCRIPTION = 1;

    /**
     * The width of the "Description" table column.
     */
    private final static int COLUMN_WIDTH_DESCRIPTION = 275;



    //
    // MEMBER VARIABLES
    //

    /**
     * The dialog that owns this panel.
     */
    private JDialog parentDialog;

    /**
     * The checkbox to include all facilities.  If this checkbox is selected,
     * the facilities table will be disabled.
     */
    private JCheckBox allFacilitiesCheckbox;

    /**
     * The table of facilities.
     */
    private JSortTable facilityTable;

    /**
     * The facility table's model.
     */
    private DefaultSortTableModel facilityTableModel;



    //
    // LAYOUT METHODS
    //

    //-------------------------------------------
    /**
     * Constructs a new panel to filter messages by facility.
     *
     * @param parentDialog      the dialog that owns this panel
     * @param commonButtonPanel the common buttons between all filtering panels; assumes
     *                          this argument is not null
     */
    FacilityFilterPanel(JDialog parentDialog, JPanel commonButtonPanel) {
        super();
        GridBagLayout gridbag = new GridBagLayout();
        setLayout(gridbag);
        setBorder(BorderFactory.createEmptyBorder(LayoutUtils.PANEL_BORDERSIZE,
                                                  LayoutUtils.PANEL_BORDERSIZE,
                                                  LayoutUtils.PANEL_BORDERSIZE,
                                                  LayoutUtils.PANEL_BORDERSIZE));       
        this.parentDialog = parentDialog;


        // Init this panel's contents.  Note that since we are laying out
        // components using GridBagLayout, changes to the layout must be
        // propogated to each of the methods below.  Use "gridy" to 
        // represent the first available vertical gridpoint.
        int gridy = 0;
        MyEventListener mel = new MyEventListener();
        gridy = initInstructions(gridbag, mel, gridy);
        gridy = initFacilitiesTable(gridbag, mel, gridy);
        gridy = addCommonButtonPanel(gridbag, gridy, commonButtonPanel);


        // Set initial values.
        setFacilityTableDataVector(TSAFEProperties.getFacilityDataVector());
    }


    //-------------------------------------------
    /**
     * Set up the filtering instructions.
     *
     * @param gridbag  the gridbag layout object
     * @param mel      the general event listener
     * @param gridy    the first available vertical position in the layout
     * @return  the first available vertical position in the layout after this method
     *          has finished laying out all of its components
     */
    private int initInstructions(GridBagLayout gridbag, MyEventListener mel, int gridy) {

        // Set the instructions.
        JTextArea instructions = new JTextArea("You can play back messages belonging to specific facilities.  " +
                                               "Select the facilities from the table below, or select the " +
                                               "checkbox to include all facilities.", 5, 40);
        instructions.setOpaque(false);
        instructions.setEditable(false);
        instructions.setLineWrap(true);
        instructions.setWrapStyleWord(true);
        GridBagConstraints c = LayoutUtils.makeGridBagConstraints(0, gridy,
                                                                  2, 1,
                                                                  LayoutUtils.TEXTAREA_WEIGHTX, 
                                                                  LayoutUtils.PANEL_B_WEIGHTY_TOPMOST,
                                                                  GridBagConstraints.BOTH, 
                                                                  GridBagConstraints.WEST,
                                                                  new Insets(LayoutUtils.PANEL_B_SPACE_ABOVE_TOPMOST, 
                                                                             0,0,0));
        gridbag.setConstraints(instructions, c);
        add(instructions);
        gridy++;

           
        return gridy;
    }


    //-------------------------------------------
    /**
     * Set up the facilities table.
     *
     * @param gridbag  the gridbag layout object
     * @param mel      the general event listener
     * @param gridy    the first available vertical position in the layout
     */
    private int initFacilitiesTable(GridBagLayout gridbag, MyEventListener mel, int gridy) {
            
        // Create the "All Facilities" checkbox.
        allFacilitiesCheckbox = new JCheckBox("All facilities");
        allFacilitiesCheckbox.setToolTipText("Accept messages of all facilities");
        allFacilitiesCheckbox.addItemListener(mel);
        GridBagConstraints c = LayoutUtils.makeGridBagConstraints(0, gridy,
                                                                  1, 1,
                                                                  LayoutUtils.CHECKBOX_WEIGHTX, 
                                                                  LayoutUtils.PANEL_B_WEIGHTY_ABOVE,
                                                                  GridBagConstraints.HORIZONTAL, 
                                                                  GridBagConstraints.SOUTHWEST,
                                                                  new Insets(LayoutUtils.PANEL_B_SPACE_BETWEEN, 
                                                                             0,0,0));
        gridbag.setConstraints(allFacilitiesCheckbox, c);
        add(allFacilitiesCheckbox);
        gridy++;


        // Create the table.
        facilityTableModel = new DefaultSortTableModel(new Vector(), FACILITY_TABLE_COLUMNS);
        facilityTableModel.setEditable(false);
        facilityTable = new JSortTable(facilityTableModel);
        facilityTable.setToolTipText("Double click a table cell for an expanded view");
        
        TableUtils.addExpandedCellViewer(facilityTable);
        facilityTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        facilityTable.getColumnModel().getColumn(COLUMN_FACILITY).setPreferredWidth(COLUMN_WIDTH_FACILITY);
        facilityTable.getColumnModel().getColumn(COLUMN_DESCRIPTION).setPreferredWidth(COLUMN_WIDTH_DESCRIPTION);
        facilityTable.setPreferredScrollableViewportSize(new Dimension(COLUMN_WIDTH_FACILITY + 
                                                                       COLUMN_WIDTH_DESCRIPTION, 
                                                                       10 * facilityTable.getRowHeight()));

        JScrollPane tableScrollPane = new JScrollPane(facilityTable);
        c = LayoutUtils.makeGridBagConstraints(0, gridy,
                                               2, 1,
                                               LayoutUtils.TABLE_WEIGHTX, 
                                               LayoutUtils.TABLE_WEIGHTY,
                                               GridBagConstraints.BOTH, 
                                               GridBagConstraints.CENTER,
                                               new Insets(LayoutUtils.PANEL_IB_SPACE_BETWEEN, 
                                                          0,0,0));
        gridbag.setConstraints(tableScrollPane, c);
        add(tableScrollPane);
        gridy++;
 

        return gridy;
    }


    //-------------------------------------------
    /**
     * Add the common buttons to this filtering panel.
     *
     * @param gridbag           the gridbag layout object
     * @param gridy             the first available vertical position in the layout
     * @param commonButtonPanel the common buttons between all filtering panels
     * @return  the first available vertical position in the layout after this method
     *          has finished laying out all of its components
     */
    private int addCommonButtonPanel(GridBagLayout gridbag, int gridy, JPanel commonButtonPanel) {
        
        // Create a visual separator.
        JSeparator separator = new JSeparator();
        GridBagConstraints c = 
            LayoutUtils.makeGridBagConstraints(0, gridy,
                                               2, 1,
                                               LayoutUtils.PANEL_SP_WEIGHTX, 
                                               LayoutUtils.PANEL_SP_WEIGHTY,
                                               GridBagConstraints.HORIZONTAL, 
                                               GridBagConstraints.SOUTH,
                                               new Insets(LayoutUtils.PANEL_SP_SPACE_ABOVE_FINAL_BUTTONP,
                                                          0,0,0));
        gridbag.setConstraints(separator, c);
        add(separator);
        gridy++;
        

        c = LayoutUtils.makeGridBagConstraints(0, gridy,
                                               2, 1,
                                               LayoutUtils.BUTTONP_WEIGHTX, 
                                               LayoutUtils.PANEL_B_WEIGHTY_FINAL_BUTTONP, 
                                               GridBagConstraints.NONE, 
                                               GridBagConstraints.NORTH,
                                               new Insets(LayoutUtils.BUTTONP_BORDERSIZE +
                                                          LayoutUtils.PANEL_SP_SPACE_BELOW_FINAL_BUTTONP,
                                                          LayoutUtils.BUTTONP_BORDERSIZE,
                                                          LayoutUtils.BUTTONP_BORDERSIZE +
                                                          LayoutUtils.PANEL_B_SPACE_BELOW_BOTTOMMOST,
                                                          LayoutUtils.BUTTONP_BORDERSIZE));
        gridbag.setConstraints(commonButtonPanel, c);
        add(commonButtonPanel);        
        gridy++;

           
        return gridy;
    }



	//
 	// FIELD MAINTENANCE METHODS
	//

    //-------------------------------------------
    /**
     * Set the information in the facility table's model.  If the specified data 
     * vector is null, the table model is set to the empty table model.
     *
     * @param newFacilities  data vector of facilities
     */
    private void setFacilityTableDataVector(Vector newFacilities) {
        if (newFacilities == null) {
            newFacilities = new Vector();
        }
                
        // Set the new model information while preserving the model settings.
        TableUtils.setDataVector(facilityTableModel, newFacilities);
        TSAFEProperties.setFacilityDataVector(newFacilities);
    }



	//
 	// FILTER_PANEL METHODS
	//

    //-------------------------------------------
    void setFilteringConditions(FIGFileContentFilter filter) {        
        super.setFilteringConditions(filter);


        // Set the selected facilities.

        String[] facilities = filter.getFacilities();
        facilityTable.clearSelection();

        // Case #1:  Allow all facilities.
        if (facilities.length == 0) {
            allFacilitiesCheckbox.setSelected(true);
        }

        // Case #2:  Allow specific facilities.
        else {
            allFacilitiesCheckbox.setSelected(false);

            // First, make sure that all selected facilities appear in our table of facilities.
            TableUtils.addMissingRows(facilityTableModel, facilities, COLUMN_FACILITY);

            // Next, select all the appropriate facilities in our table.
            TableUtils.selectRows(facilityTable, facilities, COLUMN_FACILITY);
        }
    }


    //-------------------------------------------
    void updateContentFilter(FIGFileContentFilter filter) {
        if (filter != null) {

            // If the user has chosen specific facilities, set the facilities in the content filter.
            if (allFacilitiesCheckbox.isSelected()) {
                filter.setFacilities(new String[0]);
            }
            else {
                String[] facilities = TableUtils.getSelectedValues(facilityTable, COLUMN_FACILITY);
                filter.setFacilities(facilities);
            }
        }        
    }




    //
    // INNER CLASSES
    //

    /**
     * Listens and responds to any events fired on behalf of the 
     * buttons or lists in this filter panel.
     */
    private class MyEventListener implements ActionListener, ItemListener {
        
        //
        // METHODS
        //
        
        //-------------------------------------------
        /**
         * Responds to changes in this filter panel's checkboxes.
         *
         * @param e  the ItemEvent
         */
        public void itemStateChanged(ItemEvent e) {
            Object source = e.getItemSelectable();
            

            // "All Facilities" checkbox

            if (source == allFacilitiesCheckbox) {
                boolean enable = false;

                // If the checkbox was deselected, enable the facilities table.
                if (e.getStateChange() == ItemEvent.DESELECTED) {
                    enable = true;
                }

                facilityTable.setEnabled(enable);
            } 


            // Default

            else { 
                System.out.println("Unrecognized source: " + source);
            }
        }


        //-------------------------------------------
        /**
         * Responds to any events thrown by the panel's buttons.
         *
         * @param event the ActionEvent
         */
        public void actionPerformed(ActionEvent event) {
            String command = event.getActionCommand();
            
            
            // Default

            System.out.println("Unrecognized command: " + command);
        }

    } // inner class MyEventListener


}
