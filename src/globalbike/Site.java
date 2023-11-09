package globalbike;

public class Site {

    /* Constantes communes à tous les sites */

    static final int STOCK_INIT = 5;
    static final int STOCK_MAX = 10;
    static final int BORNE_SUP = 8;
    static final int BORNE_INF = 2;

    int totalNumberOfSites;

    int id;
    int currentStock;

    public Site(int id) {
        this.id = id;
        this.currentStock = STOCK_INIT;
        this.totalNumberOfSites = SystemeEmprunt.NB_SITES;
    }

    public int distanceBetween(Site arrivalSite) {
        return arrivalSite.id > this.id ? arrivalSite.id - this.id : totalNumberOfSites - this.id + 1 + arrivalSite.id;

        // if (arrivalSite.id > this.id) {
        // return arrivalSite.id - this.id;
        // } else {
        // // + 1 cause the first id is 0
        // return totalNumberOfSites - this.id + 1 + arrivalSite.id;
        // }
    }

    // NOTE: In this setup, customers must wait each other in a Site and same for
    // the Truck

    /** Used by Customers */
    public synchronized void borrow(int customerID) {
        while (currentStock <= 0) {
            System.out.println("Customer " + customerID + " waits a bike on site " + id);
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
        currentStock--;
        // notifyAll();
        afficher(customerID, "borrowed");
    }

    /** Used by Customers */
    public synchronized void returnBike(int customerID) {
        while (currentStock >= STOCK_MAX) {
            System.out.println("Customer " + customerID + " waits to return their bike on site " + id);
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
        currentStock++;
        // sometimes customers fell asleep, and even if the site is refilled
        // (by others customers) they won't be woke up unless we `notifyAll()`
        // after each bike returned.
        notifyAll();
        afficher(customerID, "returned");
    }

    /**
     * Used by Truck.
     * 
     * @return the new truck's stock.
     */
    public synchronized int adjustStock(int truckStock) {
        int newTruckStock = truckStock;

        if (currentStock < BORNE_INF) {
            int amountToRefill = STOCK_INIT - currentStock;
            int amountRefilled = truckStock >= amountToRefill ? amountToRefill : truckStock;

            if (amountRefilled > 0) {
                currentStock += amountRefilled;
                // Must wake all potential sleepers on the stock
                notifyAll();
                newTruckStock = truckStock - amountRefilled;
                System.out.println("Truck (" + truckStock + "->" + newTruckStock + ") loads " + amountRefilled
                        + " on site " + id + " (new=" + currentStock + ")");
            }
        } else if (currentStock > BORNE_SUP) {
            int amountUnloaded = currentStock - STOCK_INIT;
            newTruckStock = truckStock + amountUnloaded;
            System.out.println("Truck (" + truckStock + "->" + newTruckStock + ") unloads " + amountUnloaded
                    + " on  site " + id + " (new=" + currentStock + ")");

            currentStock = STOCK_INIT;
            // we must `notifyAll` to wake returning bike blocked at a site.
            notifyAll();
        }

        return newTruckStock;
    }

    /**
     * This function overrides `adjustStock()` !
     * Used by Truck, to retrieve at most `SupplyTruck.INITIAL_STOCK` whatever the
     * site's state.
     * 
     * @return the new truck's stock.
     */
    public synchronized int forceRefillTruck(int truckStock) {
        int newTruckStock = truckStock;

        if (currentStock > 0) {
            int amountUnloaded = currentStock >= SupplyTruck.INITIAL_STOCK ? SupplyTruck.INITIAL_STOCK
                    : currentStock;
            this.currentStock -= amountUnloaded;
            newTruckStock += amountUnloaded;
            System.out.println("Truck (" + truckStock + "->" + newTruckStock + ") force unloads " + amountUnloaded
                    + " on  site " + id + " (new=" + currentStock + ")");

            // we must `notifyAll` to wake returning bike blocked at a site.
            notifyAll();
        }

        return newTruckStock;
    }

    /**
     * Affiche l'etat de l'objet site
     */
    public void afficher(int customerID, String pastAction) {
        System.out.println("The site " + id + " contains " + currentStock + " bike(s), after that Customer "
                + customerID + " " + pastAction + ".");
    }
}
