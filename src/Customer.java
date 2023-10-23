import java.util.logging.Logger;

public class Customer extends Thread {

    Site startingSite;
    Site arrivalSite;

    public Customer(Site startingSite, Site arrivalSite) {
        this.startingSite = startingSite;
        this.arrivalSite = arrivalSite;
    }

    @Override
    public void run() {

        startingSite.borrow();

        try {
            Thread.sleep(startingSite.distanceBetween(arrivalSite));
        } catch (InterruptedException e) {
            Logger.getGlobal().warning("Sleep Interrupted!" + e);
            /* Clean up whatever needs to be handled before interrupting */
            Thread.currentThread().interrupt();
        }

        arrivalSite.returnBike();
    }
}
