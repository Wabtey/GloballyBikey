package globalbike;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SupplyTruck extends Thread {

    static final int INITIAL_STOCK = 5;

    int stock;

    // /**
    // * ## Role Play
    // *
    // * This is an API used to request sites' infos from administrators.
    // */
    // Site[] sites;
    int currentSite;

    /**
     * ## Role Play
     * 
     * The truck's local map, used to monitor stocks at all sites
     * and decide which one to unload.
     * 
     * ## Note
     * 
     * kinda a duplicate of sites (perhaps just use a hashmap with key = Integer
     * value = Site)
     */
    HashMap<Integer, Site> siteMap = new HashMap<>();

    Optional<Integer> starvingSite = Optional.empty();
    Optional<Integer> closestLargestSite = Optional.empty();

    public SupplyTruck(Site[] sites) {
        for (Site site : sites)
            siteMap.put(site.id, site);
        // if sites.len() == 0...
        this.currentSite = 0;
        this.stock = INITIAL_STOCK;
    }

    /**
     * Set the starvingSite with the site having stock issue
     * 
     * OPTIMIZE: Select the closest if two sites have the same stock
     */
    private void updateStarvation() {
        // reset
        this.starvingSite = Optional.empty();
        for (Site site : siteMap.values()) {
            if (site.currentStock <= Site.BORNE_INF &&
                    (!this.starvingSite.isPresent()
                            || site.currentStock <= siteMap.get(this.starvingSite.get()).currentStock))
                this.starvingSite = Optional.of(site.id);
        }
    }

    /**
     * ## Note
     * 
     * - If `this.starvingSite` is the `(currentSite % NB_SITE) + 1`
     * `this.closestLargestSite` is set to `None` (`Optional.empty()`).
     * In other words, `this.starvingSite` won't be the `this.closestLargestSite`.
     * 
     * - `this.closestLargestSite` can still be a site with low or 0 bike left,
     * in this case: `-----C--nextButLow--S-----`
     * 
     * @param starvingSite
     */
    private void updateClosestLargestSite(int starvingSite) {
        // First case: starvingSite < currentSite
        // ---largest-----S---------C-------
        // Second case: currentSite < starvingSite
        // -------C-----largest-----S-------

        // DOC: Draw in the markdown this loop range
        // 0 .. starvingSite = 25 .. currentSite = 100 .. 500
        // 0 .. currentSite = 50 .. 70 .. starvingSite = 150 .. 500

        // potentialClosestLargestSite
        Optional<Site> pCLS = siteMap
                .values()
                .stream()
                .filter(site -> (starvingSite < currentSite && ((0 <= site.id && site.id < starvingSite)
                        || (currentSite <= site.id && site.id < SystemeEmprunt.NB_SITES)))
                        || (currentSite < starvingSite && (site.id <= currentSite && site.id < starvingSite)))
                // no big deal if we don't have the closest to the current (need one more
                // calculus otherwise)
                .reduce((site1, site2) -> site1.currentStock >= site2.currentStock ? site1
                        : site2);

        this.closestLargestSite = pCLS.isPresent() ? Optional.of(pCLS.get().id)
                : Optional.empty();

        // --------------------------------------------------------------------
        StringBuilder log = new StringBuilder();
        if (pCLS.isPresent()) {
            log.append("Site Choosen: " + pCLS.get().id + " - "
                    + pCLS.get().currentStock + "/" + Site.STOCK_MAX);
        } else {
            log.append("There is no closest largest site");
        }

        // DEBUG: Print all largest Sites
        log.append("\nCurrent Site: " + this.currentSite + "\n" +
                "Starving Site: " + this.starvingSite.get() + " - "
                + siteMap.get(this.starvingSite.get()).currentStock
                + "/" + Site.STOCK_MAX + "\n" +
                "Largest Sites are:");

        // DEBUG: to visuliaze which site was choosen if there were more than one max
        // BUG: `collect()` seems to call a Comparator for Site. (and there is none so
        // crash)
        // int max = pCLS.isPresent() ? pCLS.get().currentStock : 0;
        // List<Site> largestSites = siteMap.values().stream()
        // .filter(site -> site.currentStock == max)
        // .sorted() // is this mandatory ? (see `values()`)
        // .collect(Collectors.toList());

        // for (Site site : largestSites)
        // log.append("\n - Site " + site.id + ": " + site.currentStock + "/" +
        // Site.STOCK_MAX);
        System.out.println(log.toString());
    }

    @Override
    public void run() {
        while (true) {
            System.out.println("Truck is in " + currentSite + " with " + stock + " bike(s)");

            if (this.stock == 0 && !this.starvingSite.isPresent()) {
                updateStarvation();
                if (this.starvingSite.isPresent()) {
                    updateClosestLargestSite(this.starvingSite.get());
                }
            }

            /* --------------------------- Manage Site's Stock -------------------------- */

            // If we are in the `nearest largest site`, unload even if in boundaries.
            // and even if our truck has recharged by the time it arrives.
            if (closestLargestSite.isPresent() && closestLargestSite.get() == currentSite) {
                // NOTE: there is no way that the `closestLargestSite` is the `starvingSite`.
                this.stock = siteMap.get(currentSite).forceRefillTruck(this.stock);
                this.closestLargestSite = Optional.empty();
            } else {
                this.stock = siteMap.get(currentSite).adjustStock(this.stock);
                if (this.starvingSite.isPresent() && this.starvingSite.get() == currentSite) {
                    this.starvingSite = Optional.empty();
                }
            }

            /* ------------------------ Prepare next destination ------------------------ */

            int nextSite = currentSite < SystemeEmprunt.NB_SITES - 1 ? currentSite + 1 : 0;

            /* --------------------------------- Travel --------------------------------- */

            try {
                Thread.sleep(siteMap.get(currentSite).distanceBetween(siteMap.get(nextSite)));
            } catch (InterruptedException e) {
                Logger.getGlobal().warning("Truck Sleep Interrupted!");
                /* Clean up whatever needs to be handled before interrupting */
                Thread.currentThread().interrupt();
            }

            currentSite = nextSite;
        }
    }
}
