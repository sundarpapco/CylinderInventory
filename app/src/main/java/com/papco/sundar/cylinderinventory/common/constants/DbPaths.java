package com.papco.sundar.cylinderinventory.common.constants;

public class DbPaths {

    public static final String CYLINDERS="cylinders";

    //collections
    public static final String COLLECTION_CYLINDERS="cylinders";
    public static final String COLLECTION_DESTINATIONS="destinations";


    //Cylinder aggregation
    public static final String COUNT_CYLINDERS_TOTAL="/aggregation/cylinders_total";
    public static final String COUNT_CYLINDERS_FULL="/aggregation/cylinders_full";
    public static final String COUNT_CYLINDERS_EMPTY="/aggregation/cylinders_empty";
    public static final String COUNT_CYLINDERS_CLIENTS="aggregation/cylinders_clients";
    public static final String COUNT_CYLINDERS_REFILL_STATIONS="aggregation/cylinders_refill_stations";
    public static final String COUNT_CYLINDERS_REPAIR_STATIONS="aggregation/cylinders_repair_stations";
    public static final String COUNT_CYLINDERS_GRAVEYARD="aggregation/cylinders_graveyard";
    public static final String COUNT_CYLINDERS_DAMAGED="aggregation/cylinders_damaged";

    //Destinations
    public static final String COUNT_DESTINATION="/aggregation/destinations";

}
