package com.papco.sundar.cylinderinventory.screens.operations.allotment;

public class AllotmentListItem {

    private String cylinderTypeName;
    private int requiredQuantity;
    private int approvedQuantity;
    private boolean checked=true;

    public String getCylinderTypeName() {
        return cylinderTypeName;
    }

    public void setCylinderTypeName(String cylinderTypeName) {
        this.cylinderTypeName = cylinderTypeName;
    }

    public int getRequiredQuantity() {
        return requiredQuantity;
    }

    public void setRequiredQuantity(int requiredQuantity) {
        this.requiredQuantity = requiredQuantity;
    }

    public int getApprovedQuantity() {
        return approvedQuantity;
    }

    public void setApprovedQuantity(int approvedQuantity) {
        this.approvedQuantity = approvedQuantity;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
