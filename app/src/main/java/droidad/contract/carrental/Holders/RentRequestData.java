package droidad.contract.carrental.Holders;

import org.json.JSONObject;

public class RentRequestData {

    String _id;
    String rentStartDate;
    String rentEndDate;
    String ownerId;
    String customerId;
    int cost;
    String status;
    String isRequestAccepted;
    boolean isPaid;


    public RentRequestData(String _id, String rentStartDate, String rentEndDate, String ownerId, String customerId, int cost, String status, String isRequestAccepted, boolean isPaid) {
        this._id = _id;
        this.rentStartDate = rentStartDate;
        this.rentEndDate = rentEndDate;
        this.ownerId = ownerId;
        this.customerId = customerId;
        this.cost = cost;
        this.status = status;
        this.isRequestAccepted = isRequestAccepted;
        this.isPaid = isPaid;
    }

    public String get_id() {
        return _id;
    }

    public String getRentStartDate() {
        return rentStartDate;
    }

    public String getRentEndDate() {
        return rentEndDate;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public int getCost() {
        return cost;
    }

    public String getStatus() {
        return status;
    }

    public String getIsRequestAccepted() {
        return isRequestAccepted;
    }

    public boolean isPaid() {
        return isPaid;
    }
}
