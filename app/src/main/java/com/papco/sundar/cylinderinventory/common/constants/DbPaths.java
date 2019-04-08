package com.papco.sundar.cylinderinventory.common.constants;

public class DbPaths {

    public static enum AggregationType{
        FULL,EMPTY,DAMAGED,CLIENT,REFILL_STATIONS,REPAIR_STATIONS,APPROVED
    }

    //collections
    public static final String COLLECTION_CYLINDERS="cylinders";
    public static final String COLLECTION_DESTINATIONS="destinations";
    public static final String COLLECTION_GRAVEYARD="graveyard";
    public static final String COLLECTION_ALLOTMENT="allotments";
    public static final String COLLECTION_BATCHES="batches";
    public static final String COLLECTION_CYLINDER_TYPES="cylinderTypes";


    //Cylinder aggregation
    public static final String COUNT_CYLINDERS_TOTAL="/aggregation/cylinders_total";
    public static final String COUNT_CYLINDERS_FULL="/aggregation/cylinders_full";
    public static final String COUNT_CYLINDERS_EMPTY="/aggregation/cylinders_empty";
    public static final String COUNT_CYLINDERS_GRAVEYARD="/aggregation/cylinders_graveyard";
    public static final String COUNT_CYLINDERS_DAMAGED="/aggregation/cylinders_damaged";
    public static final String COUNT_CYLINDERS_CLIENT="/aggregation/cylinders_clients";
    public static final String COUNT_CYLINDERS_REFILL_STATION="/aggregation/cylinders_refill_stations";
    public static final String COUNT_CYLINDERS_REPAIR_STATION="/aggregation/cylinders_repair_stations";
    public static final String COUNT_DESTINATION="/aggregation/destinations";
    public static final String COUNT_ALLOTMENT="/aggregation/allotments";
    public static final String COUNT_BATCHES_INVOICE="/aggregation/batches_invoice";
    public static final String COUNT_BATCHES_ECR="/aggregation/batches_ecr";
    public static final String COUNT_BATCHES_REFILL="/aggregation/batches_refill";
    public static final String COUNT_BATCHES_FCI ="/aggregation/batches_fci";
    public static final String COUNT_BATCHES_REPAIR="/aggregation/batches_repair";
    public static final String COUNT_BATCHES_RCI="/aggregation/batches_rci";

    //get aggregation path for cylinderTypes
    public static String getAggregationForType(String cylinderType,AggregationType aggregationType){

        String aggString="";

        switch (aggregationType){

            case FULL:
                aggString="cylinders_full";
                break;

            case EMPTY:
                aggString="cylinders_empty";
                break;

            case CLIENT:
                aggString="cylinders_clients";
                break;

            case DAMAGED:
                aggString="cylinders_damaged";
                break;

            case REFILL_STATIONS:
                aggString="cylinders_refill_stations";
                break;

            case REPAIR_STATIONS:
                aggString="cylinders_repair_stations";
                break;

            case APPROVED:
                aggString="cylinders_approved";
                break;

        }

        return "/"+COLLECTION_CYLINDER_TYPES+"/"+cylinderType+"/aggregation/"+aggString;

    }

}
