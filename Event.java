/*
 * This is the event class.
 * Will have the various event types
 * Also contain room for event data.
 * The parameters are as follow for each Event Type
 *
 * GEN_FINISHED:
 * param1: PowerType
 * 
 * BLOCK_ARRIVED:
 * param1: int layer
 *
 * MACH_FINISHED
 * param1: int pathID
 * param2: int machineID
 * 
 */

import java.lang.Comparable;

public class Event implements Comparable<Event> {

    public EventType type;
    public Object param1;
    public Object param2;
    public double t;

    public Event(EventType e, double time) {
        type = e;
        t = time;
    }

    public Event(EventType e, double time, Object p1) {
        type = e;
        param1 = p1;
        t = time;
    }

    public Event(EventType e, double time, Object p1, Object p2) {
        type = e;
        param1 = p1;
        param2 = p2;
        t = time;
    }

    public int compareTo(Event e) {
        if (e.t > this.t) {
            return -1;
        } else if (e.t < this.t) {
            return 1;
        } else {
            return 0;
        }
    }

}
