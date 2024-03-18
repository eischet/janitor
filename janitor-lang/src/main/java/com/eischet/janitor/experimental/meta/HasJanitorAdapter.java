package com.eischet.janitor.experimental.meta;

/**
 * This interface marks on object as compatible with Janitor.
 *
 * The language runtime can use the provided adapter to find the objects properties and methods.
 *
 * Why "? super T"? I hope I got it right; the adapter is supposed to "consume" a T and do things with it.
 * https://stackoverflow.com/questions/2723397/what-is-pecs-producer-extends-consumer-super
 *
 * @param <T> the class that is to be adapted, itself
 */
public interface HasJanitorAdapter<T> {
    JanitorAdapter<? super T> getJanitorAdapter();
}
