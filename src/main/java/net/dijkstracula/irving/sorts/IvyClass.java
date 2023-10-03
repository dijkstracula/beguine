package net.dijkstracula.irving.sorts;

import com.microsoft.z3.DatatypeSort;
import net.dijkstracula.melina.runtime.MelinaContext;

/** All Ivy classes must implement this interface, where C is the extracted class to produce. */
// TODO: What does the type parameter R in DatatypeSort encode?
public abstract class IvyClass<C> extends IvySort<C, DatatypeSort<?>> {

    public IvyClass(String name, MelinaContext ctx) {
        super(name, ctx);
    }

}
