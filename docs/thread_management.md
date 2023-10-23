# Thread Management

## NotifyAll

fix Truck not waking up customers after unloading from a site

Example:
If Customer 1 waits at Site 0, to return their bike. Site 0 has 9 bike already, so Customer 1 waits. Truck arrives and unloads 1 bike from Site 0 to match the `BORNE_SUP` (8), if truck doesn't notify Customer 1 will be stuck.
A `notifyAll` is mandatory, because if truck unloads 2 bike and there is two customers waiting to return their bike. A `notify` is not enough
