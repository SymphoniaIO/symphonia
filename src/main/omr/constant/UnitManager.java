//-----------------------------------------------------------------------//
//                                                                       //
//                         U n i t M a n a g e r                         //
//                                                                       //
//  Copyright (C) Herve Bitteur 2000-2006. All rights reserved.          //
//  This software is released under the terms of the GNU General Public  //
//  License. Please contact the author at herve.bitteur@laposte.net      //
//  to report bugs & suggestions.                                        //
//-----------------------------------------------------------------------//

package omr.constant;

import omr.util.Logger;

import java.util.Map;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

/**
 * Class <code>UnitManager</code> implements the management of all units
 * (aka classes), for which we have either a ConstantSet, or a Logger, or
 * both.
 *
 * <p>It is a collection of static methods since only one manager is
 * intended.
 *
 * <p>To help {@link UnitTreeTable} display the whole tree of UnitNodes,
 * UnitManager pre-loads all the classes known to contain a ConstantSet or
 * a Logger. This list is kept up-to-date and stored as a property
 *
 * @author Herv&eacute; Bitteur
 * @version $Id$
 */
public class UnitManager
{
    //~ Static variables/initializers -------------------------------------

    private static final String UNIT = UnitManager.class.getName();

    // The single instance of this class
    private static UnitManager INSTANCE;

    // Separator used in property that concatenates all unit names
    private static final String SEPARATOR = ";";

    //~ Instance variables ------------------------------------------------

    // Map of PackageNodes and UnitNodes
    private final SortedMap<String, Node> mapOfNodes = new TreeMap<String, Node>();
    private final PackageNode root = new PackageNode("<root>", null);

    // Lists of all units known as containing a constantset
    // This is kept up-to-date and saved as a property.
    private Constant.String units;

    // Flag to avoid storing units being pre-loaded
    private volatile boolean storeIt = false;

    private static final Logger logger = Logger.getLogger(UnitManager.class);

    //~ Constructors ------------------------------------------------------

    //-------------//
    // UnitManager //
    //-------------//
    private UnitManager (String main)
    {
        mapOfNodes.put("<root>", root);

        // Pre-load all known units with a ConstantSet and/or Logger
        if (main != null) {
            preLoadUnits(main);
        }
    }

    //~ Methods -----------------------------------------------------------

    //-------------//
    // getInstance //
    //-------------//
    /**
     * Report the single instance of this package, after creating it with
     * default parameters if needed
     *
     * @return the single instance
     */
    public static UnitManager getInstance ()
    {
        //log ("getInstance");
        if (INSTANCE == null) {
            INSTANCE = new UnitManager(null);
        }
        return INSTANCE;
    }

    //-------------//
    // getInstance //
    //-------------//
    /**
     * Report the single instance of this package, after creating it with
     * default parameters if needed
     *
     * @return the single instance
     */
    public static UnitManager getInstance (String main)
    {
        //log ("getInstance main=" + main);
        if (INSTANCE == null) {
            INSTANCE = new UnitManager(main);
        } else {
            INSTANCE.preLoadUnits(main);
        }

        return INSTANCE;
    }

    //---------//
    // addUnit //
    //---------//
    /**
     * Include a Unit in the hierarchy
     *
     * @param unit the Unit to include
     */
    void addUnit (UnitNode unit)
    {
        //log ("addUnit unit=" + unit.getName());
        // Update the hierarchy. Include it in the map, as well as all needed
        // intermediate package nodes if any is needed.
        synchronized (mapOfNodes) {
            String name = unit.getName();
            Node node = getNode(name);
            if (node == null) {
                // Nothing existed before, add this node and its needed
                // parents
                //log ("addUnit new unit: " + name);
                mapOfNodes.put(name, unit);
                updateParents(unit);
                if (storeIt) {
                    // Make this unit name permanent
                    storeUnits();
                }
            } else if (node instanceof PackageNode) {
                logger.severe("Unit with same name as package "
                              + name);
            } else if (node instanceof UnitNode) {
                logger.severe("duplicate Unit " + name);
            }
        }
    }

    //--------//
    // addSet //
    //--------//
    /**
     * Add a ConstantSet, which means perhaps adding a UnitNode if not
     * already allocated and setting its ConstantSet reference to the
     * provided ConstantSet.
     *
     * @param set the ConstantSet to add to the hierarchy
     */
    void addSet (ConstantSet set)
    {
        //log ("addSet set=" + set.getName());
        retrieveUnit(set.getName()).setConstantSet(set);
    }

    //-----------//
    // addLogger //
    //-----------//
    /**
     * Add a Logger, which like addSet means perhaps adding a UnitNode if
     * not already allocated and setting its Logger reference to the
     * provided Logger
     *
     * @param logger the Logger ro add to the hierarchy
     */
    public void addLogger (Logger logger)
    {
        //log ("addLogger logger=" + logger.getName());
        retrieveUnit(logger.getName()).setLogger(logger);
    }

    //--------------//
    // retrieveUnit //
    //--------------//
    /**
     * Looks for the unit with given name. If the unit does not exist, it
     * is created and inserted in the hierarchy
     *
     * @param name the name of the desired unit
     *
     * @return the unit (found, or created)
     */
    private UnitNode retrieveUnit (String name)
    {
        Node node = getNode(name);

        if (node == null) {
            // Create a hosting unit node
            UnitNode unit = new UnitNode(name);
            addUnit(unit);
            return unit;
        } else if (node instanceof UnitNode) {
            return (UnitNode) node;
        } else if (node instanceof PackageNode) {
            logger.severe("Unit with same name as package "
                          + name);
        }
        return null;
    }

    //---------//
    // getNode //
    //---------//
    /**
     * Retrieves a node object, knowing its path name
     *
     * @param path fully qyalified node name
     *
     * @return the node object, or null if not found
     */
    Node getNode (String path)
    {
        return mapOfNodes.get(path);
    }

    //---------//
    // getRoot //
    //---------//
    /**
     * Return the PackageNode at the root of the node hierarchy
     *
     * @return the root PackageNode
     */
    PackageNode getRoot ()
    {
        return root;
    }

    //--------------//
    // dumpAllUnits //
    //--------------//
    /**
     * Dumps on the standard output the current value of all Constants of
     * all ConstantSets.
     */
    public void dumpAllUnits ()
    {
        System.out.println("\nUnitManager. All Units:");
        System.out.println("=======================");

        for (Map.Entry<String, Node> entry : mapOfNodes.entrySet()) {
            Node node = getNode(entry.getKey());
            if (node instanceof UnitNode) {
                UnitNode unit = (UnitNode) node;

                // Logger?
                Logger logger = unit.getLogger();
                if (logger != null) {
                    System.out.println("\n[" + unit.getName() + "] Logger -> " + logger.getEffectiveLevel());
                }

                // ConstantSet?
                ConstantSet set = unit.getConstantSet();
                if (set != null) {
                    set.dump();
                }
            }
        }
    }

    //---------------//
    // updateParents //
    //---------------//
    /**
     * Update the chain of parents of a Unit, by walking up all the package
     * names found in the fully qualified Unit name, creating PackageNodes
     * when needed, or adding a new child to an existing PackageNode.
     *
     * @param unit the Unit whose chain of parents is to be updated
     */
    void updateParents (UnitNode unit)
    {
        String name = unit.getName();
        int length = name.length();
        Node prev = unit;

        for (int i = name.lastIndexOf('.', length - 1); i >= 0;
             i = name.lastIndexOf('.', i - 1)) {
            String sub = name.substring(0, i);
            Node obj = mapOfNodes.get(sub);

            // Create a provision node for a future parent.
            if (obj == null) {
                //log("No parent " + sub + " found. Creating PackageNode.");
                PackageNode pn = new PackageNode(sub, prev);
                mapOfNodes.put(sub, pn);
                prev = pn;
            } else if (obj instanceof PackageNode) {
                //log("PackageNode  " + sub + " found. adding child");
                ((PackageNode) obj).addChild(prev);
                return;
            } else {
                Exception e = new IllegalStateException("unexpected node type " +
                                                        obj.getClass() + " in map.");
                e.printStackTrace();
                return;
            }
        }

        // No intermediate parent found, so hook it to the root itself
        getRoot().addChild(prev);
    }

    //--------------//
    // preLoadUnits //
    //--------------//
    /**
     * Allows to preload the names of the various nodes in the hierarchy,
     * by simply extracting names stored at previous runs.
     */
    private void preLoadUnits (String main)
    {
        //log("pre-loading units");

        String unitName;
        if (main != null) {
            unitName = main + ".Units";
        } else {
            unitName = "Units";
        }

        units = new Constant.String
                (UNIT,
                 unitName,
                 "",
                 "List of units known as containing a ConstantSet and/or a Logger");

        // Initialize units using the constant 'units'
        StringTokenizer st = new StringTokenizer(units.getValue(), SEPARATOR);

        storeIt = false;
        while (st.hasMoreTokens()) {
            String unit = st.nextToken();
            try {
                //log ("pre-loading '" + unit + "'...");
                Class.forName(unit); // This loads its ConstantSet and Logger
                //log ("unit '" + unit + "' pre-loaded");
            } catch (ClassNotFoundException ex) {
                System.err.println("*** Cannot load ConstantSet " + unit);
            }
        }
        storeIt = true;

        // Save the latest set of Units
        storeUnits();

        //log("all units have been pre-loaded from " + main);
    }

    //------------//
    // storeUnits //
    //------------//
    /**
     * Build a string by concatenating all node names and store it to disk
     * for subsequent runs.
     */
    private void storeUnits ()
    {
        //log("storing units");

        // Update the constant 'units' according to current units content
        StringBuffer buf = new StringBuffer(1024);

        for (String name : mapOfNodes.keySet()) {
            Node node = getNode(name);
            if (node instanceof UnitNode) {
                if (buf.length() > 0) {
                    buf.append(SEPARATOR);
                }

                buf.append(name);
            }
        }

        // Side-effect: all constants are stored to disk
        units.setValue(buf.toString());

        //log(units.getName() + "=" + units.getValue());
    }

    //-----//
    // log // To avoid premature use of a Logger TBD
    //-----//
    private static void log (String msg)
    {
        System.out.println("UnitManager:: " + msg);
    }
}
