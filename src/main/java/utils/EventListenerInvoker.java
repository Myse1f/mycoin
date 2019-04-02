/**
 * Created By Yufan Wu
 * 2019/4/2
 */
package utils;

import java.util.List;

/**
 * A util class that makes it easier to invoker a list of listener method
 */
public abstract class EventListenerInvoker<E> {
    public abstract void invoke(E listener);

    public static <E> void invoke(List<E> listeners, EventListenerInvoker<E> invoker) {
        synchronized (listeners) {
            for (int i=0; i<listeners.size(); i++) {
                E listener = listeners.get(i);
                synchronized (listener) {
                    invoker.invoke(listener);
                }
                if (listeners.get(i) != listener) {
                    i --;
                }
            }
        }
    }
}
