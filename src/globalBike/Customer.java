package globalBike;

import java.util.logging.Logger;

public class Customer extends Thread {

    Site startingSite;
    Site arrivalSite;

    int id;

    public Customer(int id, Site startingSite, Site arrivalSite) {
        this.id = id;
        this.startingSite = startingSite;
        this.arrivalSite = arrivalSite;
    }

    @Override
    public void run() {
        startingSite.borrow(id);

        try {
            Thread.sleep(startingSite.distanceBetween(arrivalSite));
        } catch (InterruptedException e) {
            Logger.getGlobal().warning("Sleep Interrupted!" + e);
            /* Clean up whatever needs to be handled before interrupting */
            Thread.currentThread().interrupt();
        }

        arrivalSite.returnBike(id);
        System.out.println("Customer " + id + ": Finished.");
    }
}
